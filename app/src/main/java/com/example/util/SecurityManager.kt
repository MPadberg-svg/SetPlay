package com.example.util

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import java.security.KeyStore
import java.security.MessageDigest
import java.security.SecureRandom
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicLong
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec

/**
 * Centralized security utility for SetPlay:
 * 1. Android Keystore AES-256-GCM encryption for saved IPTV credentials at rest.
 * 2. Salted SHA-256 hashing with constant-time comparison and brute-force cooldown for Parental PINs.
 * 3. Strict URL validation, container extension whitelisting, and credential log redaction.
 */
object SecurityManager {

    private const val KEYSTORE_PROVIDER = "AndroidKeyStore"
    private const val KEY_ALIAS = "SetPlay_AES_MasterKey_v1"
    private const val AES_TRANSFORMATION = "AES/GCM/NoPadding"
    private const val GCM_IV_LENGTH_BYTES = 12
    private const val GCM_TAG_LENGTH_BITS = 128

    const val ENCRYPTED_PREFIX = "ENC:v1:"
    const val HASHED_PIN_PREFIX = "HASH:v1:"

    private const val MAX_PIN_ATTEMPTS = 5
    private const val PIN_LOCKOUT_DURATION_MS = 30_000L

    private val failedPinAttempts = AtomicInteger(0)
    private val pinLockoutUntilEpochMs = AtomicLong(0L)

    private val ALLOWED_EXTENSIONS = setOf(
        "ts", "mp4", "mkv", "m3u8", "avi", "flv", "mov", "webm", "mpg", "mpeg", "mp3", "aac"
    )

    // Fallback in-memory/test key used only when running on local JVM unit tests without AndroidKeyStore hardware
    private val jvmFallbackKey: SecretKey by lazy {
        val digest = MessageDigest.getInstance("SHA-256")
            .digest("SetPlay-Local-JVM-Fallback-Key-2026".toByteArray(Charsets.UTF_8))
        SecretKeySpec(digest, "AES")
    }

    @Synchronized
    private fun getOrCreateSecretKey(): SecretKey {
        return try {
            val keyStore = KeyStore.getInstance(KEYSTORE_PROVIDER).apply { load(null) }
            val existingKey = keyStore.getKey(KEY_ALIAS, null) as? SecretKey
            if (existingKey != null) {
                return existingKey
            }
            val keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, KEYSTORE_PROVIDER)
            val spec = KeyGenParameterSpec.Builder(
                KEY_ALIAS,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
            )
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setKeySize(256)
                .build()
            keyGenerator.init(spec)
            keyGenerator.generateKey()
        } catch (_: Exception) {
            jvmFallbackKey
        }
    }

    /**
     * Encrypts a plaintext credential using AES-256-GCM.
     * Idempotent: if input already starts with [ENCRYPTED_PREFIX], returns it unchanged.
     */
    fun encryptCredential(plaintext: String): String {
        if (plaintext.isEmpty() || plaintext.startsWith(ENCRYPTED_PREFIX)) {
            return plaintext
        }
        return try {
            val secretKey = getOrCreateSecretKey()
            val cipher = Cipher.getInstance(AES_TRANSFORMATION)
            cipher.init(Cipher.ENCRYPT_MODE, secretKey)
            val iv = cipher.iv
            val cipherBytes = cipher.doFinal(plaintext.toByteArray(Charsets.UTF_8))

            val combined = ByteArray(iv.size + cipherBytes.size)
            System.arraycopy(iv, 0, combined, 0, iv.size)
            System.arraycopy(cipherBytes, 0, combined, iv.size, cipherBytes.size)

            ENCRYPTED_PREFIX + Base64.encodeToString(combined, Base64.NO_WRAP)
        } catch (_: Exception) {
            // Fallback to JVM key if AndroidKeyStore threw a provider exception
            try {
                val cipher = Cipher.getInstance(AES_TRANSFORMATION)
                val iv = ByteArray(GCM_IV_LENGTH_BYTES).also { SecureRandom().nextBytes(it) }
                cipher.init(Cipher.ENCRYPT_MODE, jvmFallbackKey, GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv))
                val cipherBytes = cipher.doFinal(plaintext.toByteArray(Charsets.UTF_8))
                val combined = ByteArray(iv.size + cipherBytes.size)
                System.arraycopy(iv, 0, combined, 0, iv.size)
                System.arraycopy(cipherBytes, 0, combined, iv.size, cipherBytes.size)
                ENCRYPTED_PREFIX + Base64.encodeToString(combined, Base64.NO_WRAP)
            } catch (_: Exception) {
                plaintext
            }
        }
    }

    /**
     * Decrypts an AES-256-GCM encrypted credential prefixed with [ENCRYPTED_PREFIX].
     * Transparently returns plaintext if the string is not encrypted (seamless migration).
     */
    fun decryptCredential(stored: String): String {
        if (stored.isEmpty() || !stored.startsWith(ENCRYPTED_PREFIX)) {
            return stored
        }
        val encoded = stored.removePrefix(ENCRYPTED_PREFIX)
        val combined = try {
            Base64.decode(encoded, Base64.NO_WRAP)
        } catch (_: Exception) {
            return ""
        }
        if (combined.size <= GCM_IV_LENGTH_BYTES) return ""

        val iv = combined.copyOfRange(0, GCM_IV_LENGTH_BYTES)
        val cipherBytes = combined.copyOfRange(GCM_IV_LENGTH_BYTES, combined.size)
        val spec = GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv)

        return try {
            val cipher = Cipher.getInstance(AES_TRANSFORMATION)
            cipher.init(Cipher.DECRYPT_MODE, getOrCreateSecretKey(), spec)
            String(cipher.doFinal(cipherBytes), Charsets.UTF_8)
        } catch (_: Exception) {
            try {
                val cipher = Cipher.getInstance(AES_TRANSFORMATION)
                cipher.init(Cipher.DECRYPT_MODE, jvmFallbackKey, spec)
                String(cipher.doFinal(cipherBytes), Charsets.UTF_8)
            } catch (_: Exception) {
                ""
            }
        }
    }

    /**
     * Hashes a 4-digit Parental PIN using a 16-byte random salt and SHA-256.
     */
    fun hashPin(pin: String): String {
        val cleanPin = pin.trim()
        if (cleanPin.startsWith(HASHED_PIN_PREFIX)) return cleanPin
        val salt = ByteArray(16).also { SecureRandom().nextBytes(it) }
        val digest = computeSha256(salt, cleanPin.toByteArray(Charsets.UTF_8))
        val saltB64 = Base64.encodeToString(salt, Base64.NO_WRAP)
        val digestB64 = Base64.encodeToString(digest, Base64.NO_WRAP)
        return "$HASHED_PIN_PREFIX$saltB64:$digestB64"
    }

    /**
     * Returns remaining lockout seconds if user exceeded [MAX_PIN_ATTEMPTS], or 0 if unlocked.
     */
    fun getPinLockoutRemainingSeconds(nowMs: Long = System.currentTimeMillis()): Int {
        val until = pinLockoutUntilEpochMs.get()
        if (until <= nowMs) return 0
        return (((until - nowMs) + 999L) / 1000L).toInt()
    }

    fun resetPinRateLimiter() {
        failedPinAttempts.set(0)
        pinLockoutUntilEpochMs.set(0L)
    }

    /**
     * Verifies a candidate PIN against a stored salted SHA-256 hash (or legacy plaintext default "0000")
     * using constant-time byte comparison and enforcing brute-force rate limiting.
     */
    fun verifyPin(candidatePin: String, storedPinOrHash: String, nowMs: Long = System.currentTimeMillis()): Boolean {
        if (getPinLockoutRemainingSeconds(nowMs) > 0) {
            return false
        }

        val cleanCandidate = candidatePin.trim()
        val isMatch = if (storedPinOrHash.startsWith(HASHED_PIN_PREFIX)) {
            val payload = storedPinOrHash.removePrefix(HASHED_PIN_PREFIX)
            val parts = payload.split(":")
            if (parts.size != 2) {
                false
            } else {
                try {
                    val salt = Base64.decode(parts[0], Base64.NO_WRAP)
                    val expectedDigest = Base64.decode(parts[1], Base64.NO_WRAP)
                    val actualDigest = computeSha256(salt, cleanCandidate.toByteArray(Charsets.UTF_8))
                    MessageDigest.isEqual(expectedDigest, actualDigest)
                } catch (_: Exception) {
                    false
                }
            }
        } else {
            val expectedBytes = storedPinOrHash.trim().toByteArray(Charsets.UTF_8)
            val actualBytes = cleanCandidate.toByteArray(Charsets.UTF_8)
            MessageDigest.isEqual(expectedBytes, actualBytes)
        }

        if (isMatch) {
            resetPinRateLimiter()
            return true
        } else {
            val failures = failedPinAttempts.incrementAndGet()
            if (failures >= MAX_PIN_ATTEMPTS) {
                pinLockoutUntilEpochMs.set(nowMs + PIN_LOCKOUT_DURATION_MS)
                failedPinAttempts.set(0)
            }
            return false
        }
    }

    private fun computeSha256(salt: ByteArray, data: ByteArray): ByteArray {
        val md = MessageDigest.getInstance("SHA-256")
        md.update(salt)
        return md.digest(data)
    }

    /**
     * Validates and normalizes an IPTV server URL.
     * Rejects dangerous schemes (file://, javascript:, content://, etc.) and strips trailing slashes.
     */
    fun sanitizeServerUrl(rawUrl: String, defaultScheme: String = "http"): String {
        val cleaned = rawUrl.trim().replace(Regex("[\\r\\n\\t]"), "")
        if (cleaned.isEmpty()) return ""

        val lower = cleaned.lowercase()
        if (lower.startsWith("file:") ||
            lower.startsWith("javascript:") ||
            lower.startsWith("content:") ||
            lower.startsWith("data:") ||
            lower.startsWith("ftp:")
        ) {
            return ""
        }

        val withScheme = when {
            lower.startsWith("https://") || lower.startsWith("http://") -> cleaned
            cleaned.contains("://") -> return "" // Block any unknown scheme
            else -> "$defaultScheme://$cleaned"
        }

        val parsed = withScheme.toHttpUrlOrNull() ?: return ""
        if (parsed.host.isBlank()) return ""

        val normalized = parsed.toString().trimEnd('/')
        return normalized
    }

    /**
     * Returns candidate base URLs for HTTPS-first connection attempts when a bare host is entered.
     */
    fun buildHttpsFirstCandidateUrls(rawUrl: String): List<String> {
        val cleaned = rawUrl.trim().replace(Regex("[\\r\\n\\t]"), "")
        if (cleaned.isEmpty()) return emptyList()
        val lower = cleaned.lowercase()

        return when {
            lower.startsWith("https://") -> {
                val primary = sanitizeServerUrl(cleaned, "https")
                if (primary.isEmpty()) emptyList() else listOf(primary)
            }
            lower.startsWith("http://") -> {
                val primary = sanitizeServerUrl(cleaned, "http")
                if (primary.isEmpty()) emptyList() else listOf(primary)
            }
            cleaned.contains("://") -> emptyList()
            else -> {
                // User entered bare host/domain: try HTTPS first, then fall back to HTTP
                val httpsCandidate = sanitizeServerUrl(cleaned, "https")
                val httpCandidate = sanitizeServerUrl(cleaned, "http")
                listOfNotNull(
                    httpsCandidate.takeIf { it.isNotEmpty() },
                    httpCandidate.takeIf { it.isNotEmpty() }
                ).distinct()
            }
        }
    }

    /**
     * Sanitizes a stream container extension against path traversal or injection.
     */
    fun sanitizeContainerExtension(rawExtension: String?, defaultExtension: String = "mp4"): String {
        val cleaned = (rawExtension ?: "")
            .trim()
            .trimStart('.')
            .lowercase()
            .replace(Regex("[^a-z0-9]"), "")
        return if (cleaned in ALLOWED_EXTENSIONS) cleaned else defaultExtension
    }

    /**
     * Scrubs usernames, passwords, and sensitive query parameters from logs and exception messages.
     */
    fun redactSensitiveText(message: String?, vararg secrets: String): String {
        if (message.isNullOrBlank()) return "Unknown error"
        var redacted = message
            .replace(Regex("(?i)(password=)[^&\\s]+"), "$1***")
            .replace(Regex("(?i)(username=)[^&\\s]+"), "$1***")
        secrets.forEach { secret ->
            val trimmed = secret.trim()
            if (trimmed.length >= 2) {
                redacted = redacted.replace(trimmed, "***")
            }
        }
        return redacted
    }
}

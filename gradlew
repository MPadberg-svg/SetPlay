#!/usr/bin/env bash
# ==============================================================================
# SetPlay Self-Bootstrapping Gradle Wrapper (gradlew)
# ==============================================================================
# Works seamlessly across:
#   1. Google AI Studio Build container (delegates to pre-installed `gradle`)
#   2. GitHub Actions CI runners (`ubuntu-latest`)
#   3. GitHub Copilot Agent sandboxes & local developer machines
# ==============================================================================
set -e

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$ROOT_DIR"

# 1. If running inside the AI Studio container where internal Gradle 9.3.1 cache exists, use system gradle directly
if command -v gradle >/dev/null 2>&1 && [ -d "/root/.gradle" ] && grep -q 'agp = "9.1.1"' gradle/libs.versions.toml 2>/dev/null; then
  exec gradle "$@"
fi

# 2. Running on GitHub Actions, GitHub Copilot Agent, or external Linux/macOS environment:
#    Prepare debug.keystore, .env, and normalize AI Studio internal versions to Public Maven Central / Google Maven
if [ -f "debug.keystore.base64" ] && [ ! -f "debug.keystore" ]; then
  base64 -d debug.keystore.base64 > debug.keystore 2>/dev/null || base64 -D debug.keystore.base64 > debug.keystore
elif [ ! -f "debug.keystore" ]; then
  keytool -genkey -v -keystore debug.keystore -storepass android -alias androiddebugkey \
    -keypass android -keyalg RSA -keysize 2048 -validity 10000 \
    -dname "CN=Android Debug,O=Android,C=US"
fi

if [ ! -f ".env" ] && [ -f ".env.example" ]; then
  cp .env.example .env
fi

# Ensure every required BuildConfig key exists in .env with a non-empty value
# (secrets-gradle-plugin 2.0.1 emits `public static final String KEY = ;` if a key has an empty value)
sed -i 's/^[[:space:]]*//g' .env 2>/dev/null || true

RESOLVED_SPORT_KEY="${SPORTAPI_AI_KEY_SECRET:-$SPORT_API_KEY_SECRET}"
if [ -n "$RESOLVED_SPORT_KEY" ]; then
  if grep -q "^SPORT_API_KEY=" .env 2>/dev/null; then
    sed -i "s|^SPORT_API_KEY=.*|SPORT_API_KEY=${RESOLVED_SPORT_KEY}|g" .env 2>/dev/null || true
  else
    echo "SPORT_API_KEY=${RESOLVED_SPORT_KEY}" >> .env
  fi
  if grep -q "^SPORTAPI_AI_KEY=" .env 2>/dev/null; then
    sed -i "s|^SPORTAPI_AI_KEY=.*|SPORTAPI_AI_KEY=${RESOLVED_SPORT_KEY}|g" .env 2>/dev/null || true
  else
    echo "SPORTAPI_AI_KEY=${RESOLVED_SPORT_KEY}" >> .env
  fi
fi

if [ -n "$WYZIE_API_KEY_SECRET" ]; then
  if grep -q "^WYZIE_API_KEY=" .env 2>/dev/null; then
    sed -i "s|^WYZIE_API_KEY=.*|WYZIE_API_KEY=${WYZIE_API_KEY_SECRET}|g" .env 2>/dev/null || true
  else
    echo "WYZIE_API_KEY=${WYZIE_API_KEY_SECRET}" >> .env
  fi
fi

if [ -n "$API_FOOTBALL_KEY_SECRET" ]; then
  if grep -q "^API_FOOTBALL_KEY=" .env 2>/dev/null; then
    sed -i "s|^API_FOOTBALL_KEY=.*|API_FOOTBALL_KEY=${API_FOOTBALL_KEY_SECRET}|g" .env 2>/dev/null || true
  else
    echo "API_FOOTBALL_KEY=${API_FOOTBALL_KEY_SECRET}" >> .env
  fi
fi

if [ -n "$FOOTBALL_DATA_KEY_SECRET" ]; then
  if grep -q "^FOOTBALL_DATA_KEY=" .env 2>/dev/null; then
    sed -i "s|^FOOTBALL_DATA_KEY=.*|FOOTBALL_DATA_KEY=${FOOTBALL_DATA_KEY_SECRET}|g" .env 2>/dev/null || true
  else
    echo "FOOTBALL_DATA_KEY=${FOOTBALL_DATA_KEY_SECRET}" >> .env
  fi
fi

if [ -n "$TMDB_API_KEY_SECRET" ]; then
  if grep -q "^TMDB_API_KEY=" .env 2>/dev/null; then
    sed -i "s|^TMDB_API_KEY=.*|TMDB_API_KEY=${TMDB_API_KEY_SECRET}|g" .env 2>/dev/null || true
  else
    echo "TMDB_API_KEY=${TMDB_API_KEY_SECRET}" >> .env
  fi
fi

if [ -n "$SUBDL_API_KEY_SECRET" ]; then
  if grep -q "^SUBDL_API_KEY=" .env 2>/dev/null; then
    sed -i "s|^SUBDL_API_KEY=.*|SUBDL_API_KEY=${SUBDL_API_KEY_SECRET}|g" .env 2>/dev/null || true
  else
    echo "SUBDL_API_KEY=${SUBDL_API_KEY_SECRET}" >> .env
  fi
fi

grep -q "^API_FOOTBALL_KEY=" .env 2>/dev/null || echo "API_FOOTBALL_KEY=YOUR_API_FOOTBALL_KEY" >> .env
grep -q "^FOOTBALL_DATA_KEY=" .env 2>/dev/null || echo "FOOTBALL_DATA_KEY=YOUR_FOOTBALL_DATA_KEY" >> .env
grep -q "^SPORT_API_KEY=" .env 2>/dev/null || echo "SPORT_API_KEY=YOUR_SPORT_API_KEY" >> .env
grep -q "^SPORTAPI_AI_KEY=" .env 2>/dev/null || echo "SPORTAPI_AI_KEY=YOUR_SPORTAPI_AI_KEY" >> .env
grep -q "^TMDB_API_KEY=" .env 2>/dev/null || echo "TMDB_API_KEY=YOUR_TMDB_API_KEY" >> .env
grep -q "^WYZIE_API_KEY=" .env 2>/dev/null || echo "WYZIE_API_KEY=YOUR_WYZIE_API_KEY" >> .env
grep -q "^SUBDL_API_KEY=" .env 2>/dev/null || echo "SUBDL_API_KEY=YOUR_SUBDL_API_KEY" >> .env

sed -i 's|^API_FOOTBALL_KEY=[[:space:]]*$|API_FOOTBALL_KEY=YOUR_API_FOOTBALL_KEY|g' .env 2>/dev/null || true
sed -i 's|^FOOTBALL_DATA_KEY=[[:space:]]*$|FOOTBALL_DATA_KEY=YOUR_FOOTBALL_DATA_KEY|g' .env 2>/dev/null || true
sed -i 's|^SPORT_API_KEY=[[:space:]]*$|SPORT_API_KEY=YOUR_SPORT_API_KEY|g' .env 2>/dev/null || true
sed -i 's|^SPORTAPI_AI_KEY=[[:space:]]*$|SPORTAPI_AI_KEY=YOUR_SPORTAPI_AI_KEY|g' .env 2>/dev/null || true
sed -i 's|^TMDB_API_KEY=[[:space:]]*$|TMDB_API_KEY=YOUR_TMDB_API_KEY|g' .env 2>/dev/null || true
sed -i 's|^WYZIE_API_KEY=[[:space:]]*$|WYZIE_API_KEY=YOUR_WYZIE_API_KEY|g' .env 2>/dev/null || true
sed -i 's|^SUBDL_API_KEY=[[:space:]]*$|SUBDL_API_KEY=YOUR_SUBDL_API_KEY|g' .env 2>/dev/null || true

# Accept Android SDK licenses if ANDROID_HOME is available
if [ -n "$ANDROID_HOME" ] && [ -f "$ANDROID_HOME/cmdline-tools/latest/bin/sdkmanager" ]; then
  yes 2>/dev/null | "$ANDROID_HOME/cmdline-tools/latest/bin/sdkmanager" --licenses > /dev/null 2>&1 || true
fi

# Ensure android.useAndroidX=true is always present in gradle.properties for AGP 8.x
if ! grep -q "^android.useAndroidX=true" gradle.properties 2>/dev/null; then
  echo "" >> gradle.properties
  echo "android.useAndroidX=true" >> gradle.properties
fi

# Normalize settings.gradle.kts, gradle/libs.versions.toml, build.gradle.kts, app/build.gradle.kts,
# and gradle.properties surgically in place if they still reference AI Studio container versions (agp = "9.1.1")
if grep -q 'agp = "9.1.1"' gradle/libs.versions.toml 2>/dev/null; then
  echo ">>> [SetPlay Gradle Wrapper] Running surgical CI normalizer (scripts/normalize_ci_gradle.py)..."
  python3 scripts/normalize_ci_gradle.py --root "$ROOT_DIR"
fi

# 3. Ensure Gradle 8.11.1 binary is available (cached in ~/.gradle/wrapper/dists for actions/setup-java@v4)
GRADLE_VERSION="8.11.1"
GRADLE_CACHE_ROOT="${HOME:-/tmp}/.gradle/wrapper/dists/setplay-gradle-${GRADLE_VERSION}"
GRADLE_DIR="${GRADLE_CACHE_ROOT}/gradle-${GRADLE_VERSION}"
GRADLE_ZIP="${GRADLE_CACHE_ROOT}/gradle-${GRADLE_VERSION}-bin.zip"

if [ ! -x "${GRADLE_DIR}/bin/gradle" ]; then
  mkdir -p "${GRADLE_CACHE_ROOT}"
  DOWNLOAD_OK=0
  for MIRROR_URL in \
    "https://services.gradle.org/distributions/gradle-${GRADLE_VERSION}-bin.zip" \
    "https://downloads.gradle.org/distributions/gradle-${GRADLE_VERSION}-bin.zip"; do
    echo ">>> [SetPlay Gradle Wrapper] Downloading Gradle ${GRADLE_VERSION} from ${MIRROR_URL}..."
    rm -f "${GRADLE_ZIP}"
    if curl --fail --location --retry 5 --retry-delay 3 --retry-all-errors -sS "${MIRROR_URL}" -o "${GRADLE_ZIP}"; then
      if unzip -tq "${GRADLE_ZIP}" >/dev/null 2>&1; then
        DOWNLOAD_OK=1
        break
      else
        echo ">>> [SetPlay Gradle Wrapper] Archive validation failed for ${MIRROR_URL}, trying next mirror..."
        rm -f "${GRADLE_ZIP}"
      fi
    fi
  done

  if [ "$DOWNLOAD_OK" -eq 1 ]; then
    unzip -q -o "${GRADLE_ZIP}" -d "${GRADLE_CACHE_ROOT}"
    chmod +x "${GRADLE_DIR}/bin/gradle"
    rm -f "${GRADLE_ZIP}"
  elif command -v gradle >/dev/null 2>&1; then
    echo ">>> [SetPlay Gradle Wrapper] Falling back to system gradle..."
    exec gradle "$@"
  else
    echo ">>> [SetPlay Gradle Wrapper] ERROR: Failed to download valid Gradle ${GRADLE_VERSION} archive and no system gradle found." >&2
    exit 1
  fi
fi

exec "${GRADLE_DIR}/bin/gradle" "$@"

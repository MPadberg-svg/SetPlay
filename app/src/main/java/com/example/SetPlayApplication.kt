package com.example

import android.app.ActivityManager
import android.app.Application
import android.content.Context
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.disk.DiskCache
import coil.memory.MemoryCache
import com.example.util.EditionResolver
import com.example.util.SetPlayEdition

class SetPlayApplication : Application(), ImageLoaderFactory {

    override fun newImageLoader(): ImageLoader {
        val activityManager = getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager
        val isTvOrLowRam = EditionResolver.resolveEdition(this) == SetPlayEdition.TV ||
            activityManager?.isLowRamDevice == true

        return ImageLoader.Builder(this)
            .memoryCache {
                MemoryCache.Builder(this)
                    .maxSizePercent(if (isTvOrLowRam) 0.10 else 0.18)
                    .build()
            }
            .diskCache {
                DiskCache.Builder()
                    .directory(cacheDir.resolve("setplay_image_cache"))
                    .maxSizeBytes(50L * 1024L * 1024L)
                    .build()
            }
            .allowRgb565(true)
            .crossfade(if (isTvOrLowRam) 0 else 150)
            .build()
    }
}

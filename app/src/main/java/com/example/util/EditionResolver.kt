package com.example.util

import android.app.UiModeManager
import android.content.Context
import android.content.res.Configuration
import com.example.BuildConfig

enum class SetPlayEdition {
    PHONE,
    TV
}

object EditionResolver {
    @Volatile
    var runtimeEditionOverride: SetPlayEdition? = null

    fun resolveEdition(context: Context): SetPlayEdition {
        runtimeEditionOverride?.let { return it }

        if (BuildConfig.DEVICE_EDITION.equals("TV", ignoreCase = true)) {
            return SetPlayEdition.TV
        }

        val uiModeManager = runCatching {
            context.getSystemService(Context.UI_MODE_SERVICE) as? UiModeManager
        }.getOrNull()
        if (uiModeManager?.currentModeType == Configuration.UI_MODE_TYPE_TELEVISION) {
            return SetPlayEdition.TV
        }

        return SetPlayEdition.PHONE
    }
}

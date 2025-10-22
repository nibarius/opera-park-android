package se.barsk.park.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

/**
 * A composable utility function that returns the dynamic color scheme on Android 12+
 * and falls back to the app's MaterialTheme.colorScheme on older versions.
 *
 * This is useful for components that should adopt system colors for a more integrated
 * feel, like contextual action bars or selected item states.
 */
@Composable
fun dynamicColorScheme(): ColorScheme {
    val context = LocalContext.current
    val isDarkTheme = isSystemInDarkTheme()

    return when {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            if (isDarkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        else -> MaterialTheme.colorScheme
    }
}
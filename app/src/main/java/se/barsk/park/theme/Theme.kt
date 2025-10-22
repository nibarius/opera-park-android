package se.barsk.park.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

// Define the light color scheme using your custom colors
private val LightColors = lightColorScheme(
    primary = Green600,
)

// Define the dark color scheme
private val DarkColors = darkColorScheme(
    primary = Green200,
)

@Composable
fun ParkTheme(
    useDarkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    //val context = LocalContext.current
    val colors = when {
        useDarkTheme -> DarkColors
        else -> LightColors
    }

    MaterialTheme(
        colorScheme = colors,
        content = content
    )
}
package se.barsk.park

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import se.barsk.park.theme.DarkAppBar

/**
 * A custom top app bar that uses a dark background with primary colored text
 * in dark mode, mimicking the old XML theme behavior.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ParkTopAppBar(
    title: @Composable () -> Unit,
    navigationIcon: @Composable () -> Unit = {},
    actions: @Composable () -> Unit = {},
    scrollBehavior: TopAppBarScrollBehavior? = null
) {
    // Determine which colors to use based on the theme
    val appBarColors = if (isSystemInDarkTheme()) {
        // In Dark Mode: Use a dark background and primary color for the title.
        TopAppBarDefaults.topAppBarColors(
            containerColor = DarkAppBar,
            scrolledContainerColor = DarkAppBar, // Keep color consistent when scrolling
            titleContentColor = MaterialTheme.colorScheme.primary,
            actionIconContentColor = MaterialTheme.colorScheme.onSurface,
            navigationIconContentColor = MaterialTheme.colorScheme.onSurface
        )
    } else {
        TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primary,
            scrolledContainerColor = MaterialTheme.colorScheme.primary,
            titleContentColor = MaterialTheme.colorScheme.onPrimary,
            actionIconContentColor = MaterialTheme.colorScheme.onPrimary,
            navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
        )
    }

    TopAppBar(
        title = title,
        navigationIcon = navigationIcon,
        actions = { actions() },
        scrollBehavior = scrollBehavior,
        colors = appBarColors // Apply our custom colors
    )
}
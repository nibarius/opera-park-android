package se.barsk.park.mainui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import se.barsk.park.R
import se.barsk.park.theme.*

/**
 * Top app bar for ParkActivity that displays the parking status and provides access to
 * the overflow menu.
 *
 * The app bar dynamically changes color based on the parking state:
 * - Red for FULL
 * - Orange for ALMOST_FULL
 * - Green for FREE_SPACE and other states
 *
 * In dark mode, the app bar uses a dark background with colored title text.
 * In light mode, it uses a colored background with white text.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ParkActivityAppBar(
    parkingState: ParkActivity.ParkingState,
    freeSpotsCount: Int,
    onManageCarsClick: () -> Unit,
    onSettingsClick: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }
    val isDarkTheme = isSystemInDarkTheme()

    // Determine title based on parking state
    val title = when (parkingState) {
        ParkActivity.ParkingState.FULL -> stringResource(R.string.park_status_full)
        ParkActivity.ParkingState.ALMOST_FULL -> stringResource(R.string.park_status_one_free)
        ParkActivity.ParkingState.FREE_SPACE -> stringResource(R.string.park_status_many_free, freeSpotsCount)
        else -> stringResource(R.string.app_name) // NO_SERVER, WAITING_ON_RESPONSE, REQUEST_FAILED, EMPTY
    }

    // Determine colors based on parking state and theme
    val appBarColors = when {
        isDarkTheme -> {
            // Dark mode: Dark background with colored title text
            val titleColor = when (parkingState) {
                ParkActivity.ParkingState.FULL -> Red200
                ParkActivity.ParkingState.ALMOST_FULL -> Orange200
                else -> Green200 // FREE_SPACE and placeholder states
            }
            TopAppBarDefaults.topAppBarColors(
                containerColor = DarkAppBar,
                scrolledContainerColor = DarkAppBar,
                titleContentColor = titleColor,
                actionIconContentColor = MaterialTheme.colorScheme.onSurface,
                navigationIconContentColor = MaterialTheme.colorScheme.onSurface
            )
        }
        else -> {
            // Light mode: Colored background with white text
            val containerColor = when (parkingState) {
                ParkActivity.ParkingState.FULL -> Red600
                ParkActivity.ParkingState.ALMOST_FULL -> Orange600
                else -> Green600 // FREE_SPACE and placeholder states
            }
            TopAppBarDefaults.topAppBarColors(
                containerColor = containerColor,
                scrolledContainerColor = containerColor,
                titleContentColor = Color.White,
                actionIconContentColor = Color.White,
                navigationIconContentColor = Color.White
            )
        }
    }

    TopAppBar(
        title = { Text(text = title) },
        actions = {
            IconButton(onClick = { showMenu = true }) {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = stringResource(R.string.manage_cars_more_options)
                )
            }
            DropdownMenu(
                expanded = showMenu,
                onDismissRequest = { showMenu = false }
            ) {
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.menu_manage_cars_entry)) },
                    onClick = {
                        showMenu = false
                        onManageCarsClick()
                    }
                )
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.menu_settings_entry)) },
                    onClick = {
                        showMenu = false
                        onSettingsClick()
                    }
                )
            }
        },
        colors = appBarColors
    )
}

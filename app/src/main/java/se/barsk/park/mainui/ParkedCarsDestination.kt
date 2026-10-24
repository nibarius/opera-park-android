package se.barsk.park.mainui

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import se.barsk.park.ParkApp
import se.barsk.park.R
import se.barsk.park.ServerUrlDialog
import se.barsk.park.datatypes.Garage

/**
 * The main screen of the app showing the parked cars and the user's own cars.
 */
@Composable
fun ParkedCarsDestination(
    viewModel: ParkViewModel,
    onManageCarsClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onAddCarClick: () -> Unit
) {
    val parkedCars by viewModel.garage.parkedCarsFlow.collectAsStateWithLifecycle()
    val ownCars by ParkApp.carCollection.ownCarsFlow.collectAsStateWithLifecycle()
    val currentParkingState by viewModel.parkingStateFlow.collectAsStateWithLifecycle()
    val isManualRefreshInProgress by viewModel.isRefreshingFlow.collectAsStateWithLifecycle()
    val unableToConnectText = stringResource(
        R.string.unable_to_connect_placeholder_text,
        ParkApp.storageManager.getServer()
    )

    // When there are no parked cars a loading placeholder is shown instead of the refresh
    // indicator.
    val isRefreshing by remember(parkedCars, isManualRefreshInProgress) {
        derivedStateOf { isManualRefreshInProgress && parkedCars.isNotEmpty() }
    }

    val freeSpotsCount = Garage.CAPACITY - parkedCars.size

    var showServerDialog by remember { mutableStateOf(false) }
    val showPrivacyDialog by viewModel.showPrivacyDialogFlow.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(Unit) {
        viewModel.errorEventsFlow.collect { message ->
            snackbarHostState.showSnackbar(message, duration = SnackbarDuration.Short)
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            ParkActivityAppBar(
                parkingState = currentParkingState,
                freeSpotsCount = freeSpotsCount,
                onManageCarsClick = onManageCarsClick,
                onSettingsClick = onSettingsClick
            )
        }
    ) { innerPadding ->
        ParkedCarsScreen(
            modifier = Modifier.padding(innerPadding),
            isRefreshing = isRefreshing,
            parkedCars = parkedCars,
            parkingState = currentParkingState,
            unableToConnectText = unableToConnectText,
            setServer = { showServerDialog = true },
            retry = { viewModel.retry() },
            onRefresh = { viewModel.refresh() },
            ownCars = ownCars,
            onCarClick = { car -> viewModel.onOwnCarClicked(car) },
            onAddCarClick = onAddCarClick
        )
    }

    if (showServerDialog) {
        ServerUrlDialog(
            currentUrl = ParkApp.storageManager.getServer(),
            title = stringResource(R.string.specify_server_dialog_title),
            onDismiss = { showServerDialog = false },
            onSave = { url ->
                showServerDialog = false
                viewModel.setServer(url)
            }
        )
    }

    if (showPrivacyDialog) {
        PrivacyOnboardingDialog(
            onAccept = { viewModel.acceptPrivacy() },
            onDecline = { viewModel.declinePrivacy() }
        )
    }
}

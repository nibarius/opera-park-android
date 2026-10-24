package se.barsk.park.mainui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import se.barsk.park.ParkApp
import se.barsk.park.RepeatableTask
import se.barsk.park.datatypes.Car
import se.barsk.park.datatypes.Garage
import se.barsk.park.datatypes.GarageStatusChangedListener
import se.barsk.park.datatypes.OwnCar
import se.barsk.park.storage.SettingsChangeListener
import se.barsk.park.utils.TimeUtils

/**
 * State and logic for the parked cars screen. The screen must call [onScreenResumed] and
 * [onScreenPaused] when it becomes visible / hidden so that the garage is refreshed and the
 * automatic updates only run while the screen is shown.
 */
class ParkViewModel(application: Application) : AndroidViewModel(application),
    GarageStatusChangedListener {

    val garage: Garage = Garage()
    private val networkState = NetworkState()

    private val _parkingState = MutableStateFlow(ParkingState.NO_SERVER)
    val parkingStateFlow = _parkingState.asStateFlow()

    val updateInProgressFlow = networkState.updateInProgressFlow

    private val _showPrivacyDialog = MutableStateFlow(false)
    val showPrivacyDialogFlow = _showPrivacyDialog.asStateFlow()

    // One-shot error messages surfaced from the (non-Compose) garage update callback to the
    // Compose SnackbarHost. A conflated channel is used so each error is shown once and is not
    // re-shown on recomposition; bursts of errors (e.g. tapping retry repeatedly) collapse to
    // at most one queued message (the newest) instead of a long backlog shown one after another.
    private val errorEvents = Channel<String>(Channel.CONFLATED)
    val errorEventsFlow = errorEvents.receiveAsFlow()

    private var serverBeforePause: String? = null
    private var lastGarageUpdateTime = TimeUtils.now()

    private var automaticUpdateTask: RepeatableTask? = null

    private val settingsChangeListener = object : SettingsChangeListener {
        override fun onSettingsChanged(which: SettingsChangeListener.Setting) {
            if (which == SettingsChangeListener.Setting.SERVER) {
                parkServerChanged()
            }
        }
    }

    init {
        garage.addListener(this)
        ParkApp.storageManager.settingsChangeListener = settingsChangeListener
    }

    override fun onCleared() {
        automaticUpdateTask?.stop()
        if (ParkApp.storageManager.settingsChangeListener === settingsChangeListener) {
            ParkApp.storageManager.settingsChangeListener = null
        }
    }

    // called when the garage status change (happens just after the update is ready in the success case)
    override fun onGarageStatusChange() {
        updateGarageStatus()
    }

    // called when the network request is done
    override fun onGarageUpdateReady(success: Boolean, errorMessage: String?) {
        networkState.requestFinished(success)

        lastGarageUpdateTime = TimeUtils.now()
        updateParkingState()
        if (errorMessage != null) {
            errorEvents.trySend(errorMessage)
        }
    }

    fun onScreenResumed() {
        if (TimeUtils.isBeforeReset(lastGarageUpdateTime) && TimeUtils.isAfterReset(TimeUtils.now())) {
            // The server have automatically reset the parked cars since last update so assume
            // the garage is empty and that we haven't talked to the server yet.
            networkState.resetState()
            garage.clear()
        }

        if (serverBeforePause != null && serverBeforePause != ParkApp.storageManager.getServer()) {
            // Server has changed since last time the screen was shown
            parkServerChanged()
        } else {
            updateGarageFromServer()
        }
        // when coming back to the screen the garage status must be updated to be in the correct
        // state before we've gotten the first response from the server.
        onGarageStatusChange()
        serverBeforePause = null
        automaticUpdateTask?.stop()
        automaticUpdateTask = RepeatableTask(
            { automaticUpdate() },
            ParkApp.storageManager.getAutomaticUpdateInterval()
        ).also { it.start() }
        if (!ParkApp.storageManager.hasSeenPrivacyOnBoarding()) {
            _showPrivacyDialog.value = true
        }
    }

    fun onScreenPaused() {
        serverBeforePause = ParkApp.storageManager.getServer()
        automaticUpdateTask?.stop()
    }

    fun retry() {
        networkState.resetState()
        updateGarageFromServer()
        updateParkingState()
    }

    fun setServer(url: String) {
        ParkApp.storageManager.setServer(url)
        parkServerChanged()
    }

    fun acceptPrivacy() {
        ParkApp.storageManager.setCrashReportingEnabled(true)
        ParkApp.crashlytics.enableIfAllowed()
        ParkApp.storageManager.setPrivacyOnBoardingSeen()
        _showPrivacyDialog.value = false
    }

    fun declinePrivacy() {
        ParkApp.storageManager.setCrashReportingEnabled(false)
        ParkApp.storageManager.setPrivacyOnBoardingSeen()
        _showPrivacyDialog.value = false
    }

    fun onOwnCarClicked(car: Car) {
        car as OwnCar
        when {
            garage.isParked(car) -> garage.unparkCar(getApplication(), car)
            !garage.isFull() -> garage.parkCar(getApplication(), car)
            else -> Unit // Do nothing when the garage is full
        }
    }

    private fun parkServerChanged() {
        networkState.resetState()
        garage.clear()
        updateGarageFromServer()
    }

    private fun automaticUpdate() {
        // Only try to update if we can communicate with the server and there is no update in progress
        if (parkingStateFlow.value.communicatesWithServer() && !networkState.updateInProgressFlow.value) {
            updateGarageFromServer()
        }
    }

    private fun updateGarageFromServer() {
        networkState.requestStarted()
        garage.updateStatusFromServer(getApplication())
    }

    private fun updateParkingState() {
        _parkingState.value = when {
            !ParkApp.storageManager.hasServer() -> ParkingState.NO_SERVER
            networkState.hasMadeFailedRequestsOnly() -> ParkingState.REQUEST_FAILED
            networkState.isWaitingForFirstResponse() -> ParkingState.WAITING_ON_RESPONSE
            garage.isEmpty() -> ParkingState.EMPTY
            garage.spotsFree > 1 -> ParkingState.FREE_SPACE
            garage.spotsFree == 1 -> ParkingState.ALMOST_FULL
            garage.isFull() -> ParkingState.FULL
            else -> throw RuntimeException("Unknown parking state")
        }
    }

    private fun updateGarageStatus() {
        updateParkingState()
        ParkApp.carCollection.updateParkStatus(garage)
    }
}

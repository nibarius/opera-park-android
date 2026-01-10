package se.barsk.park.mainui

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.core.view.WindowCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import se.barsk.park.INTENT_EXTRA_ADD_CAR
import se.barsk.park.ParkApp
import se.barsk.park.R
import se.barsk.park.RepeatableTask
import se.barsk.park.datatypes.Car
import se.barsk.park.datatypes.Garage
import se.barsk.park.datatypes.GarageStatusChangedListener
import se.barsk.park.datatypes.OwnCar
import se.barsk.park.error.ErrorHandler
import se.barsk.park.managecars.ManageCarsActivity
import se.barsk.park.settings.SettingsActivity
import se.barsk.park.storage.SettingsChangeListener
import se.barsk.park.theme.ParkTheme
import se.barsk.park.utils.TimeUtils


class ParkActivity : AppCompatActivity(), GarageStatusChangedListener,
    SpecifyServerDialog.SpecifyServerDialogListener {

    override fun parkServerChanged() {
        networkState.resetState()
        garage.clear()
        updateGarageFromServer()
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
            ErrorHandler.showMessage(window.decorView.rootView, errorMessage)
        }
    }


    enum class ParkingState {
        NO_SERVER,
        WAITING_ON_RESPONSE,
        REQUEST_FAILED,
        EMPTY,
        FREE_SPACE,
        ALMOST_FULL,
        FULL;

        fun showsPlaceholder(): Boolean = ordinal <= EMPTY.ordinal
        fun communicatesWithServer(): Boolean = ordinal >= EMPTY.ordinal
    }

    private val garage: Garage = Garage()
    private val networkState = NetworkState()

    private val _parkingState = MutableStateFlow(ParkingState.NO_SERVER)
    val parkingStateFlow = _parkingState.asStateFlow()

    private var serverBeforePause: String? = null
    private var lastGarageUpdateTime = TimeUtils.now()

    private lateinit var automaticUpdateTask: RepeatableTask

    override fun onCreate(savedInstanceState: Bundle?) {
        ParkApp.init(this)
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            val parkedCars by garage.parkedCarsFlow.collectAsStateWithLifecycle()
            val ownCars by ParkApp.carCollection.ownCarsFlow.collectAsStateWithLifecycle()
            val currentParkingState by parkingStateFlow.collectAsStateWithLifecycle()
            val updateInProgress by networkState.updateInProgressFlow.collectAsStateWithLifecycle()
            val unableToConnectText = stringResource(
                R.string.unable_to_connect_placeholder_text,
                ParkApp.storageManager.getServer()
            )

            val isRefreshing by remember(parkedCars, updateInProgress) {
                derivedStateOf { updateInProgress && parkedCars.isNotEmpty() }
            }

            val freeSpotsCount = Garage.CAPACITY - parkedCars.size

            ParkTheme {
                Scaffold(
                    topBar = {
                        ParkActivityAppBar(
                            parkingState = currentParkingState,
                            freeSpotsCount = freeSpotsCount,
                            onManageCarsClick = { navigateToManageCars() },
                            onSettingsClick = { navigateToSettings() }
                        )
                    }
                ) { innerPadding ->
                    ParkedCarsScreen(
                        modifier = Modifier.padding(innerPadding),
                        isRefreshing = isRefreshing,
                        parkedCars = parkedCars,
                        parkingState = currentParkingState,
                        unableToConnectText = unableToConnectText,
                        setServer = { showServerDialog() },
                        retry = {
                            networkState.resetState()
                            updateGarageFromServer()
                            updateParkingState()
                        },
                        ownCars = ownCars,
                        onCarClick = { car -> onOwnCarClicked(car) },
                        onAddCarClick = { navigateToManageCarsAndAddCar() }
                    )
                }
            }
        }

        garage.addListener(this)
        ParkApp.storageManager.settingsChangeListener = ServerChangeFromSettingsListener()
    }

    override fun onResume() {
        super.onResume()

        if (TimeUtils.isBeforeReset(lastGarageUpdateTime) && TimeUtils.isAfterReset(TimeUtils.now())) {
            // The server have automatically reset the parked cars since last update so assume
            // the garage is empty and that we haven't talked to the server yet.
            networkState.resetState()
            garage.clear()
        }

        if (serverBeforePause != null && serverBeforePause != ParkApp.storageManager.getServer()) {
            // Server has changed since last time the activity was open
            parkServerChanged()
        } else {
            updateGarageFromServer()
        }
        // when coming back to the activity the garage status must be updated to be in the correct
        // state before we've gotten the first response from the server.
        onGarageStatusChange()
        serverBeforePause = null
        automaticUpdateTask = RepeatableTask(
            { automaticUpdate() },
            ParkApp.storageManager.getAutomaticUpdateInterval()
        )
        automaticUpdateTask.start()
        showPrivacyDialogIfNeeded()
    }

    override fun onPause() {
        super.onPause()
        serverBeforePause = ParkApp.storageManager.getServer()
        automaticUpdateTask.stop()
    }

    override fun onDestroy() {
        ParkApp.storageManager.settingsChangeListener = null
        super.onDestroy()
    }

    private fun automaticUpdate() {
        // Only try to update if we can communicate with the server and there is no update in progress
        if (parkingStateFlow.value.communicatesWithServer() && networkState.updateInProgressFlow.value) {
            updateGarageFromServer()
        }
    }

    private fun updateGarageFromServer() {
        networkState.requestStarted()
        garage.updateStatusFromServer(applicationContext)
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

    private fun navigateToSettings() {
        val intent = Intent(this, SettingsActivity::class.java)
        startActivity(intent)
    }

    private fun navigateToManageCarsAndAddCar() {
        val intent = Intent(this, ManageCarsActivity::class.java)
        intent.putExtra(INTENT_EXTRA_ADD_CAR, true)
        startActivity(intent)
    }

    private fun navigateToManageCars() {
        val intent = Intent(this, ManageCarsActivity::class.java)
        startActivity(intent)
    }

    private fun onOwnCarClicked(car: Car) {
        car as OwnCar
        when {
            garage.isParked(car) -> garage.unparkCar(applicationContext, car)
            !garage.isFull() -> garage.parkCar(applicationContext, car)
            else -> Unit // Do nothing when the garage is full
        }
    }

    private fun updateGarageStatus() {
        updateParkingState()
        ParkApp.carCollection.updateParkStatus(garage)
    }

    private fun showServerDialog() =
        SpecifyServerDialog.newInstance().show(supportFragmentManager, "specifyServer")

    private fun showPrivacyDialogIfNeeded() {
        val tag = "privacyDialog"
        if (supportFragmentManager.findFragmentByTag(tag) == null &&
            !ParkApp.storageManager.hasSeenPrivacyOnBoarding()
        ) {
            PrivacyPolicyOnBoardingDialog.newInstance().show(supportFragmentManager, tag)
        }
    }

    inner class ServerChangeFromSettingsListener : SettingsChangeListener {
        override fun onSettingsChanged(which: SettingsChangeListener.Setting) {
            if (which == SettingsChangeListener.Setting.SERVER) {
                parkServerChanged()
            }
        }
    }
}

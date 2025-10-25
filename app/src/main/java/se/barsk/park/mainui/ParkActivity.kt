package se.barsk.park.mainui

import android.annotation.SuppressLint
import android.content.Intent
import android.content.res.Configuration.ORIENTATION_LANDSCAPE
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.Menu
import android.view.MenuItem
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.stringResource
import androidx.core.content.ContextCompat
import androidx.core.graphics.Insets
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import androidx.core.view.updatePadding
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.AppBarLayout
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import se.barsk.park.INTENT_EXTRA_ADD_CAR
import se.barsk.park.ParkApp
import se.barsk.park.R
import se.barsk.park.RepeatableTask
import se.barsk.park.consume
import se.barsk.park.databinding.ActivityParkBinding
import se.barsk.park.datatypes.Car
import se.barsk.park.datatypes.CarCollectionStatusChangedListener
import se.barsk.park.datatypes.Garage
import se.barsk.park.datatypes.GarageStatusChangedListener
import se.barsk.park.datatypes.OwnCar
import se.barsk.park.error.ErrorHandler
import se.barsk.park.managecars.ManageCarsActivity
import se.barsk.park.settings.SettingsActivity
import se.barsk.park.showPlaceholderIfNeeded
import se.barsk.park.storage.SettingsChangeListener
import se.barsk.park.theme.ParkTheme
import se.barsk.park.utils.TimeUtils


class ParkActivity : AppCompatActivity(), GarageStatusChangedListener,
    CarCollectionStatusChangedListener, SpecifyServerDialog.SpecifyServerDialogListener {

    override fun parkServerChanged() {
        networkState.resetState()
        garage.clear()
        updateGarageFromServer()
    }

    // called when the garage status change (happens just after the update is ready in the success case)
    override fun onGarageStatusChange() {
        updateGarageStatus()
        updateListOfOwnCars()
    }

    // called when the network request is done
    override fun onGarageUpdateReady(success: Boolean, errorMessage: String?) {
        networkState.requestFinished(success)

        binding.content.parkedCarsPullToRefresh.isRefreshing = false
        lastGarageUpdateTime = TimeUtils.now()
        updateParkingState()
        if (!success) {
            // On success the placeholder will be updated from the onGarageStatusChange() case
            // if the request was successful and there were no change there is no need to
            // update the placeholder.
            showParkedCarsPlaceholderIfNeeded()
        }
        if (errorMessage != null) {
            ErrorHandler.showMessage(binding.content.containerView, errorMessage)
        }
    }

    override fun onCarCollectionStatusChange() {
        updateListOfOwnCars()
        showOwnCarsPlaceholderIfNeeded()
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

    // Also used by tests
    lateinit var binding: ActivityParkBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        ParkApp.init(this)
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        binding = ActivityParkBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.content.composeRecyclerReplacement.setContent {
            val parkedCars by garage.parkedCarsFlow.collectAsStateWithLifecycle()
            val currentParkingState by parkingStateFlow.collectAsStateWithLifecycle()
            val unableToConnectText = stringResource(
                R.string.unable_to_connect_placeholder_text,
                ParkApp.storageManager.getServer()
            )
            ParkTheme {
                ParkedCarsScreen(
                    cars = parkedCars,
                    parkingState = currentParkingState,
                    unableToConnectText = unableToConnectText,
                    setServer = { showServerDialog() },
                    retry = {
                        networkState.resetState()
                        updateGarageFromServer()
                        updateParkingState()
                    }
                )
            }
        }

        setSupportActionBar(binding.toolbar)

        val ownCarsRecyclerView = binding.content.ownCarsRecyclerView
        ownCarsRecyclerView.layoutManager =
            if (resources.configuration.orientation == ORIENTATION_LANDSCAPE)
                LinearLayoutManager(this, RecyclerView.VERTICAL, false)
            else
                LinearLayoutManager(this, RecyclerView.HORIZONTAL, false)
        ownCarsRecyclerView.itemAnimator = DefaultItemAnimator()
        ownCarsRecyclerView.adapter =
            OwnCarsAdapter(ParkApp.carCollection.getCars(), this::onOwnCarClicked)

        binding.content.noOwnCarsPlaceholderButton.setOnClickListener { navigateToManageCarsAndAddCar() }

        binding.content.parkedCarsPullToRefresh.setOnRefreshListener { updateGarageFromServer() }

        garage.addListener(this)
        ParkApp.carCollection.addListener(this)
        ParkApp.storageManager.settingsChangeListener = ServerChangeFromSettingsListener()
        showOwnCarsPlaceholderIfNeeded()

        // Handle edge-to-edge so that parts of the UI doesn't get covered under system bars
        // Toolbar
        ViewCompat.setOnApplyWindowInsetsListener(binding.toolbar) { v, insets ->
            //val statusBarColor = ContextCompat.getColor(this, R.color.colorStatusBarAlmostFull)
            //v.setBackgroundColor(statusBarColor)
            val systemBars: Insets = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.updateLayoutParams<AppBarLayout.LayoutParams> {
                leftMargin = systemBars.left
                rightMargin = systemBars.right
                topMargin = systemBars.top
            }

            insets
        }
        // First set the insets generally for the whole content view when it comes to the right
        // and bottom part
        ViewCompat.setOnApplyWindowInsetsListener(binding.content.containerView) { v, insets ->
            val systemBars: Insets = insets.getInsets(
                WindowInsetsCompat.Type.systemBars() or
                        WindowInsetsCompat.Type.displayCutout()
            )
            v.updatePadding(
                right = systemBars.right,
                bottom = systemBars.bottom,
            )
            insets
        }
        // But leave the left side and set it only on the view switcher so only the content get a
        // padding and not the background
        ViewCompat.setOnApplyWindowInsetsListener(binding.content.parkedCarsPullToRefresh) { v, insets ->
            val systemBars: Insets = insets.getInsets(
                WindowInsetsCompat.Type.systemBars() or
                        WindowInsetsCompat.Type.displayCutout()
            )
            v.updatePadding(left = systemBars.left)
            WindowInsetsCompat.CONSUMED
        }
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

    private var optionsMenu: Menu? = null


    private fun automaticUpdate() {
        // Only try to update if we can communicate with the server and there is no update
        // in progress
        if (_parkingState.value.communicatesWithServer() && networkState.updateInProgress) {
            updateGarageFromServer()
        }
    }

    private fun updateGarageFromServer() {
        networkState.requestStarted()
        garage.updateStatusFromServer(applicationContext)
    }

    /**
     * Show list of own cars if there are any own cars, otherwise show the placeholder.
     */
    private fun showOwnCarsPlaceholderIfNeeded() {
        val viewSwitcher = binding.content.ownCarsViewSwitcher
        val ownCarsView = binding.content.ownCarsRecyclerView
        val empty = ParkApp.carCollection.getCars().isEmpty()
        showPlaceholderIfNeeded(viewSwitcher, ownCarsView, empty)
    }

    /**
     * Show the list of parked cars if there are any parked cars, otherwise show the appropriate
     * placeholder.
     */
    private fun showParkedCarsPlaceholderIfNeeded() {

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

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        optionsMenu = menu
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.menu_manage_cars -> consume { navigateToManageCars() }
        R.id.menu_settings -> consume { navigateToSettings() }
        else -> super.onOptionsItemSelected(item)
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
        updateToolbar(garage.spotsFree)
        showParkedCarsPlaceholderIfNeeded()
        ParkApp.carCollection.updateParkStatus(garage)
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun updateListOfOwnCars() {
        val adapter = binding.content.ownCarsRecyclerView.adapter as OwnCarsAdapter
        adapter.garageFull = garage.isFull()
        adapter.cars = ParkApp.carCollection.getCars()
        adapter.notifyDataSetChanged()
    }

    private fun updateToolbar(freeSpots: Int) {
        val textColor: Int
        val toolbarColor: Int
        val statusBarColor: Int
        val title: String
        when {
            _parkingState.value.showsPlaceholder() -> {
                title = getString(R.string.app_name)
                textColor = ContextCompat.getColor(this, R.color.colorToolbarTextFree)
                toolbarColor = ContextCompat.getColor(this, R.color.colorToolbarFree)
                statusBarColor = ContextCompat.getColor(this, R.color.colorStatusBarFree)
            }

            _parkingState.value == ParkingState.FULL -> {
                title = getString(R.string.park_status_full)
                textColor = ContextCompat.getColor(this, R.color.colorToolbarTextFull)
                toolbarColor = ContextCompat.getColor(this, R.color.colorToolbarFull)
                statusBarColor = ContextCompat.getColor(this, R.color.colorStatusBarFull)
            }

            _parkingState.value == ParkingState.ALMOST_FULL -> {
                title = resources.getString(R.string.park_status_one_free)
                textColor = ContextCompat.getColor(this, R.color.colorToolbarTextAlmostFull)
                toolbarColor = ContextCompat.getColor(this, R.color.colorToolbarAlmostFull)
                statusBarColor = ContextCompat.getColor(this, R.color.colorStatusBarAlmostFull)
            }

            else -> {
                title = resources.getString(R.string.park_status_many_free, freeSpots)
                textColor = ContextCompat.getColor(this, R.color.colorToolbarTextFree)
                toolbarColor = ContextCompat.getColor(this, R.color.colorToolbarFree)
                statusBarColor = ContextCompat.getColor(this, R.color.colorStatusBarFree)
            }
        }

        supportActionBar?.setBackgroundDrawable(ColorDrawable(toolbarColor))
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.statusBarColor = statusBarColor

        val span = SpannableString(title)
        span.setSpan(
            ForegroundColorSpan(textColor),
            0,
            title.length,
            Spannable.SPAN_EXCLUSIVE_INCLUSIVE
        )
        supportActionBar?.title = span
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

package se.barsk.park.mainui

import android.annotation.SuppressLint
import android.content.Intent
import android.content.res.Configuration.ORIENTATION_LANDSCAPE
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat
import androidx.core.graphics.Insets
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import androidx.core.view.updatePadding
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.AppBarLayout
import se.barsk.park.INTENT_EXTRA_ADD_CAR
import se.barsk.park.ParkApp
import se.barsk.park.R
import se.barsk.park.RepeatableTask
import se.barsk.park.SignInHandler
import se.barsk.park.consume
import se.barsk.park.databinding.ActivityParkBinding
import se.barsk.park.datatypes.Car
import se.barsk.park.datatypes.CarCollectionStatusChangedListener
import se.barsk.park.datatypes.Garage
import se.barsk.park.datatypes.GarageStatusChangedListener
import se.barsk.park.datatypes.OwnCar
import se.barsk.park.datatypes.User
import se.barsk.park.error.ErrorHandler
import se.barsk.park.fcm.NotificationsManager
import se.barsk.park.managecars.ManageCarsActivity
import se.barsk.park.settings.SettingsActivity
import se.barsk.park.showPlaceholderIfNeeded
import se.barsk.park.storage.SettingsChangeListener
import se.barsk.park.utils.TimeUtils


class ParkActivity : AppCompatActivity(), GarageStatusChangedListener,
    CarCollectionStatusChangedListener, SpecifyServerDialog.SpecifyServerDialogListener,
    MustSignInDialog.MustSignInDialogListener {

    override fun onSignInDialogPositiveClick() {
        user.signIn(this) { user.addToWaitList(this) }
    }

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

    private enum class ParkingState {
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
    private var parkingState: ParkingState = ParkingState.NO_SERVER
    private var serverBeforePause: String? = null
    private var lastGarageUpdateTime = TimeUtils.now()
    private val user: User by lazy { ParkApp.theUser }
    private val userListener = UserChangeListener()

    private lateinit var automaticUpdateTask: RepeatableTask

    // Also used by tests
    lateinit var binding: ActivityParkBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        ParkApp.init(this)
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        binding = ActivityParkBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        setSupportActionBar(binding.toolbar)

        val parkedCarsRecyclerView = binding.content.parkedCarsRecyclerView
        parkedCarsRecyclerView.layoutManager =
            LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        parkedCarsRecyclerView.itemAnimator = DefaultItemAnimator()
        parkedCarsRecyclerView.adapter = CarsAdapter(
            CarsAdapter.Type.PARKED_CARS,
            garage.parkedCars
        ) { /*listener that does nothing */ }

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
        NotificationsManager().createNotificationChannels(applicationContext)

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
        ViewCompat.setOnApplyWindowInsetsListener(binding.content.parkedCarsViewSwitcher) { v, insets ->
            val systemBars: Insets = insets.getInsets(
                WindowInsetsCompat.Type.systemBars() or
                        WindowInsetsCompat.Type.displayCutout()
            )
            v.updatePadding(left = systemBars.left)
            WindowInsetsCompat.CONSUMED
        }
    }

    override fun onStart() {
        super.onStart()
        user.addListener(userListener)
    }

    override fun onStop() {
        super.onStop()
        user.removeListener(userListener)
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
        // TODO: re-enable sign in feature when there is a backend for it.
        //user.silentSignIn(this)
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


    @Deprecated("Passing data should be done in a different way, but this is for an unused feature so I'm not fixing it yet")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // Result returned from launching the Intent from GoogleSignInClient.getSignInIntent(...);
        if (requestCode == SignInHandler.REQUEST_CODE_SIGN_IN) {
            user.onSignInResult(data)
        }
    }

    private var optionsMenu: Menu? = null
    private fun updateSignInText() {
        // TODO: re-enable when there's a backend available
        /*
        val title = if (user.isSignedIn) {
            getString(R.string.sign_out_menu_entry, user.accountName)
        } else {
            getString(R.string.sign_in_menu_entry)
        }
        optionsMenu?.findItem(R.id.menu_sign_in)?.title = title*/
    }


    private fun automaticUpdate() {
        // Only try to update if we can communicate with the server and there is no update
        // in progress
        if (parkingState.communicatesWithServer() && networkState.updateInProgress) {
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
        // This function is typically called just after the adapter have changed but before
        // the recyclerview have started animating the changes. Post a message on the message
        // queue to continue after the recycler view have started animations so we can detect
        // if they are still going
        Handler(Looper.getMainLooper()).post { showParkedCarsPlaceholderIfNeededAfterAnimations() }
    }

    /**
     * Switches to/from a placeholder when the RecyclerView have finished it's animations.
     * If the RecyclerView is replaced with a placeholder before the animation have finished
     * the animation will continue from the same state when the RecyclerView becomes visible
     * again.
     */
    private fun showParkedCarsPlaceholderIfNeededAfterAnimations() {
        if (binding.content.parkedCarsRecyclerView.isAnimating) {
            // If the recyclerview is animating, try again once current animation has finished
            // If it's animating there is an animator so it's safe to assume itemAnimator exists
            binding.content.parkedCarsRecyclerView.itemAnimator!!.isRunning { showParkedCarsPlaceholderIfNeeded() }
            return
        }
        if (parkingState.showsPlaceholder()) {
            setCorrectParkedCarsPlaceholder()
        }
        showPlaceholderIfNeeded(
            binding.content.parkedCarsViewSwitcher,
            binding.content.parkedCarsPullToRefresh,
            garage.isEmpty()
        )
    }

    /**
     * Sets the current placeholder view for the parked cars view depending on circumstances
     */
    private fun setCorrectParkedCarsPlaceholder() {
        val textView = binding.content.parkedCarsPlaceholderTextView
        val parkServerButton = binding.content.noParkServerPlaceholderButton
        val spinner = binding.content.loadingSpinner
        val text: String
        val top: Drawable?
        when (parkingState) {
            ParkingState.NO_SERVER -> {
                // no server set up
                spinner.visibility = View.GONE
                text = getString(R.string.no_server_placeholder_text)
                top = AppCompatResources.getDrawable(
                    applicationContext,
                    R.drawable.ic_cloud_off_black_72dp
                )
                parkServerButton.visibility = View.VISIBLE
                parkServerButton.text = getString(R.string.no_server_placeholder_button)
                parkServerButton.setOnClickListener { showServerDialog() }
            }

            ParkingState.REQUEST_FAILED -> {
                // failed to communicate with server
                spinner.visibility = View.GONE
                text = getString(
                    R.string.unable_to_connect_placeholder_text,
                    ParkApp.storageManager.getServer()
                )
                top = AppCompatResources.getDrawable(
                    applicationContext,
                    R.drawable.ic_cloud_off_black_72dp
                )
                parkServerButton.visibility = View.VISIBLE
                parkServerButton.text = getString(R.string.unable_to_connect_placeholder_button)
                parkServerButton.setOnClickListener {
                    networkState.resetState()
                    updateGarageFromServer()
                    updateParkingState()
                    setCorrectParkedCarsPlaceholder()
                }
            }

            ParkingState.WAITING_ON_RESPONSE -> {
                spinner.visibility = View.VISIBLE
                text = getString(R.string.updating_status_placeholder)
                top = null
                parkServerButton.visibility = View.GONE
            }

            ParkingState.EMPTY -> {
                // No parked cars
                spinner.visibility = View.GONE
                text = getString(R.string.parked_cars_placeholder)
                top =
                    AppCompatResources.getDrawable(applicationContext, R.drawable.empty_placeholder)
                parkServerButton.visibility = View.GONE
            }

            else -> {
                throw RuntimeException("No need for a placeholder")
            }
        }
        textView.text = text
        textView.setCompoundDrawablesRelativeWithIntrinsicBounds(null, top, null, null)
    }

    private fun updateParkingState() {
        parkingState = if (!ParkApp.storageManager.hasServer()) {
            ParkingState.NO_SERVER
        } else if (networkState.hasMadeFailedRequestsOnly()) {
            ParkingState.REQUEST_FAILED
        } else if (networkState.isWaitingForFirstResponse()) {
            ParkingState.WAITING_ON_RESPONSE
        } else if (garage.isEmpty()) {
            ParkingState.EMPTY
        } else if (garage.spotsFree > 1) {
            ParkingState.FREE_SPACE
        } else if (garage.spotsFree == 1) {
            ParkingState.ALMOST_FULL
        } else if (garage.isFull()) {
            ParkingState.FULL
        } else {
            throw RuntimeException("Unknown parking state")
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        optionsMenu = menu
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main_menu, menu)
        updateSignInText()
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.menu_manage_cars -> consume { navigateToManageCars() }
        R.id.menu_settings -> consume { navigateToSettings() }
        // R.id.menu_sign_in -> consume { signInOrOut() } // TODO: re-enable when backend is available
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

    private fun signInOrOut() {
        if (user.isSignedIn) {
            user.signOut(this)
        } else {
            user.signIn(this)
        }
    }

    private fun onOwnCarClicked(car: Car) {
        car as OwnCar
        when {
            garage.isParked(car) -> garage.unparkCar(applicationContext, car)
            !garage.isFull() -> garage.parkCar(applicationContext, car)
            user.isOnWaitList -> user.removeFromWaitList(this)
            user.isSignedIn -> user.addToWaitList(this)
            else -> {
                //TODO: re-enable when there is a backend available. For now do nothing when
                // clicking the car button on a full garage.

                // Garage is full, but the user is not signed in: show dialog for signing in.
                // MustSignInDialog.newInstance().show(supportFragmentManager, "signIn")
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun updateListOfParkedCars() {
        val adapter = binding.content.parkedCarsRecyclerView.adapter as CarsAdapter
        adapter.cars = garage.parkedCars
        adapter.notifyDataSetChanged()
    }

    private fun updateGarageStatus() {
        updateListOfParkedCars()
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
            parkingState.showsPlaceholder() -> {
                title = getString(R.string.app_name)
                textColor = ContextCompat.getColor(this, R.color.colorToolbarTextFree)
                toolbarColor = ContextCompat.getColor(this, R.color.colorToolbarFree)
                statusBarColor = ContextCompat.getColor(this, R.color.colorStatusBarFree)
            }

            parkingState == ParkingState.FULL -> {
                title = getString(R.string.park_status_full)
                textColor = ContextCompat.getColor(this, R.color.colorToolbarTextFull)
                toolbarColor = ContextCompat.getColor(this, R.color.colorToolbarFull)
                statusBarColor = ContextCompat.getColor(this, R.color.colorStatusBarFull)
            }

            parkingState == ParkingState.ALMOST_FULL -> {
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

    inner class UserChangeListener : User.ChangeListener {
        override fun onWaitListStatusChanged() = updateListOfOwnCars()
        override fun onWaitListFailed(message: String) =
            ErrorHandler.showMessage(binding.content.containerView, message)

        override fun onSignInStatusChanged() = updateSignInText()
        override fun onSignInFailed(statusCode: Int) {
            val message = SignInHandler.getMessageForStatusCode(applicationContext, statusCode)
            ErrorHandler.showMessage(
                binding.content.containerView,
                getString(R.string.sign_in_failed, message)
            )
            if (statusCode != com.google.android.gms.common.api.CommonStatusCodes.NETWORK_ERROR) {
                ErrorHandler.raiseException("Failed to sign in: $message")
            }
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

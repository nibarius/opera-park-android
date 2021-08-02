package se.barsk.park.mainui

import android.content.Intent
import android.content.res.Configuration.ORIENTATION_LANDSCAPE
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.ViewSwitcher
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks
import com.google.firebase.dynamiclinks.PendingDynamicLinkData
import se.barsk.park.*
import se.barsk.park.analytics.DynamicLinkFailedEvent
import se.barsk.park.analytics.ParkActionEvent
import se.barsk.park.datatypes.*
import se.barsk.park.error.ErrorHandler
import se.barsk.park.fcm.NotificationsManager
import se.barsk.park.managecars.ManageCarsActivity
import se.barsk.park.settings.SettingsActivity
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

        pullToRefreshView.isRefreshing = false
        lastGarageUpdateTime = TimeUtils.now()
        updateParkingState()
        if (!success) {
            // On success the placeholder will be updated from the onGarageStatusChange() case
            // if the request was successful and there were no change there is no need to
            // update the placeholder.
            showParkedCarsPlaceholderIfNeeded()
        }
        if (errorMessage != null) {
            ErrorHandler.showMessage(containerView, errorMessage)
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
    private val pullToRefreshView: SwipeRefreshLayout by lazy {
        findViewById<SwipeRefreshLayout>(R.id.parked_cars_pull_to_refresh)
    }
    private val parkedCarsRecyclerView: RecyclerView by lazy {
        findViewById<RecyclerView>(R.id.parked_cars_recycler_view)
    }

    private val ownCarsRecyclerView: RecyclerView by lazy {
        findViewById<RecyclerView>(R.id.own_cars_recycler_view)
    }

    private val containerView: View by lazy {
        findViewById<View>(R.id.container_view)
    }
    private lateinit var automaticUpdateTask: RepeatableTask

    override fun onCreate(savedInstanceState: Bundle?) {
        ParkApp.init(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_park)
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        parkedCarsRecyclerView.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        parkedCarsRecyclerView.itemAnimator = DefaultItemAnimator()
        parkedCarsRecyclerView.adapter = CarsAdapter(CarsAdapter.Type.PARKED_CARS,
                garage.parkedCars) { /*listener that does nothing */ }

        ownCarsRecyclerView.layoutManager =
                if (resources.configuration.orientation == ORIENTATION_LANDSCAPE)
                    LinearLayoutManager(this, RecyclerView.VERTICAL, false)
                else
                    LinearLayoutManager(this, RecyclerView.HORIZONTAL, false)
        ownCarsRecyclerView.itemAnimator = DefaultItemAnimator()
        ownCarsRecyclerView.adapter = OwnCarsAdapter(ParkApp.carCollection.getCars(), this::onOwnCarClicked)

        val addCarButton = findViewById<Button>(R.id.no_own_cars_placeholder_button)
        addCarButton.setOnClickListener { navigateToManageCarsAndAddCar() }

        pullToRefreshView.setOnRefreshListener { updateGarageFromServer() }

        garage.addListener(this)
        ParkApp.carCollection.addListener(this)
        ParkApp.storageManager.settingsChangeListener = ServerChangeFromSettingsListener()
        showOwnCarsPlaceholderIfNeeded()
        NotificationsManager().createNotificationChannels(applicationContext)
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
        automaticUpdateTask = RepeatableTask({ automaticUpdate() }, ParkApp.storageManager.getAutomaticUpdateInterval())
        automaticUpdateTask.start()
        getDynamicLink()
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
        val viewSwitcher = findViewById<ViewSwitcher>(R.id.own_cars_view_switcher)
        val ownCarsView = findViewById<View>(R.id.own_cars_recycler_view)
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
        Handler().post { showParkedCarsPlaceholderIfNeededAfterAnimations() }
    }

    /**
     * Switches to/from a placeholder when the RecyclerView have finished it's animations.
     * If the RecyclerView is replaced with a placeholder before the animation have finished
     * the animation will continue from the same state when the RecyclerView becomes visible
     * again.
     */
    private fun showParkedCarsPlaceholderIfNeededAfterAnimations() {
        if (parkedCarsRecyclerView.isAnimating) {
            // If the recyclerview is animating, try again once current animation has finished
            // If it's animating there is an animator so it's safe to assume itemAnimator exists
            parkedCarsRecyclerView.itemAnimator!!.isRunning { showParkedCarsPlaceholderIfNeeded() }
            return
        }
        if (parkingState.showsPlaceholder()) {
            setCorrectParkedCarsPlaceholder()
        }
        val viewSwitcher = findViewById<ViewSwitcher>(R.id.parked_cars_view_switcher)
        showPlaceholderIfNeeded(viewSwitcher, pullToRefreshView, garage.isEmpty())
    }

    /**
     * Sets the current placeholder view for the parked cars view depending on circumstances
     */
    private fun setCorrectParkedCarsPlaceholder() {
        val textView = findViewById<TextView>(R.id.parked_cars_placeholder_text_view)
        val parkServerButton = findViewById<Button>(R.id.no_park_server_placeholder_button)
        val spinner = findViewById<ProgressBar>(R.id.loading_spinner)
        val text: String
        val top: Drawable?
        when (parkingState) {
            ParkingState.NO_SERVER -> {
                // no server set up
                spinner.visibility = View.GONE
                text = getString(R.string.no_server_placeholder_text)
                top = getDrawable(R.drawable.ic_cloud_off_black_72dp)
                parkServerButton.visibility = View.VISIBLE
                parkServerButton.text = getString(R.string.no_server_placeholder_button)
                parkServerButton.setOnClickListener { showServerDialog() }
            }
            ParkingState.REQUEST_FAILED -> {
                // failed to communicate with server
                spinner.visibility = View.GONE
                text = getString(R.string.unable_to_connect_placeholder_text, ParkApp.storageManager.getServer())
                top = getDrawable(R.drawable.ic_cloud_off_black_72dp)
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
                top = getDrawable(R.drawable.empty_placeholder)
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
            garage.isParked(car) -> {
                garage.unparkCar(applicationContext, car)
                ParkApp.analytics.logEvent(ParkActionEvent(ParkActionEvent.Action.Unpark()))
            }
            !garage.isFull() -> {
                garage.parkCar(applicationContext, car)
                ParkApp.analytics.logEvent(ParkActionEvent(ParkActionEvent.Action.Park()))
            }
            user.isOnWaitList -> {
                user.removeFromWaitList(this)
                ParkApp.analytics.logEvent(ParkActionEvent(ParkActionEvent.Action.StopWaiting()))
            }
            user.isSignedIn -> {
                user.addToWaitList(this)
                ParkApp.analytics.logEvent(ParkActionEvent(ParkActionEvent.Action.Wait()))
            }
            else -> {
                //TODO: re-enable when there is a backend available. For now do nothing when
                // clicking the car button on a full garage.

                // Garage is full, but the user is not signed in: show dialog for signing in.
                // MustSignInDialog.newInstance().show(supportFragmentManager, "signIn")
            }
        }
    }

    private fun updateListOfParkedCars() {
        val adapter = parkedCarsRecyclerView.adapter as CarsAdapter
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

    private fun updateListOfOwnCars() {
        val adapter = ownCarsRecyclerView.adapter as OwnCarsAdapter
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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.statusBarColor = statusBarColor
        }
        val span = SpannableString(title)
        span.setSpan(ForegroundColorSpan(textColor),
                0,
                title.length,
                Spannable.SPAN_EXCLUSIVE_INCLUSIVE)
        supportActionBar?.title = span
    }

    private fun showServerDialog() =
            SpecifyServerDialog.newInstance().show(supportFragmentManager, "specifyServer")

    private fun showPrivacyDialogIfNeeded() {
        val tag = "privacyDialog"
        if (supportFragmentManager.findFragmentByTag(tag) == null &&
                !ParkApp.storageManager.hasSeenPrivacyOnBoarding()) {
            PrivacyPolicyOnBoardingDialog.newInstance().show(supportFragmentManager, tag)
        }
    }

    private fun getDynamicLink() {
        if (GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(this) !=
                com.google.android.gms.common.api.CommonStatusCodes.SUCCESS) {
            // If there is no Google play services on the device, then there is no
            // dynamic link either
            return
        }
        val listener = DynamicLinkListener()
        FirebaseDynamicLinks.getInstance()
                .getDynamicLink(intent)
                .addOnSuccessListener(this, listener)
                .addOnFailureListener(this, listener)
    }

    inner class UserChangeListener : User.ChangeListener {
        override fun onWaitListStatusChanged() = updateListOfOwnCars()
        override fun onWaitListFailed(message: String) = ErrorHandler.showMessage(containerView, message)
        override fun onSignInStatusChanged() = updateSignInText()
        override fun onSignInFailed(statusCode: Int) {
            val message = SignInHandler.getMessageForStatusCode(applicationContext, statusCode)
            ErrorHandler.showMessage(containerView, getString(R.string.sign_in_failed, message))
            if (statusCode != com.google.android.gms.common.api.CommonStatusCodes.NETWORK_ERROR) {
                ErrorHandler.raiseException("Failed to sign in: $message")
            }
        }
    }

    inner class DynamicLinkListener : OnSuccessListener<PendingDynamicLinkData>, OnFailureListener {
        override fun onFailure(exception: java.lang.Exception) =
                ParkApp.analytics.logEvent(DynamicLinkFailedEvent(exception.toString()))

        override fun onSuccess(pendingDynamicLinkData: PendingDynamicLinkData?) {
            val pendingLink = pendingDynamicLinkData?.link ?: return
            // Todo: fix problem with space in links getting converted to +
            // https://github.com/firebase/firebase-android-sdk/issues/959
            val deepLink = DeepLink(pendingLink)
            if (!deepLink.isValid) return

            if (!ParkApp.storageManager.hasServer()) {
                ParkApp.storageManager.setServer(deepLink.server)
                parkServerChanged()
            }
            ParkApp.carCollection.addCarsThatDoesNotExist(deepLink.cars)
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

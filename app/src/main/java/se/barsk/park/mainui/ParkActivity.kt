package se.barsk.park.mainui

import android.content.Intent
import android.content.res.Configuration.ORIENTATION_LANDSCAPE
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v4.content.ContextCompat
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.Toolbar
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.WindowManager
import android.widget.*
import com.crashlytics.android.Crashlytics
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks
import com.google.firebase.dynamiclinks.PendingDynamicLinkData
import io.fabric.sdk.android.Fabric
import se.barsk.park.*
import se.barsk.park.analytics.Analytics
import se.barsk.park.analytics.CrashReporting
import se.barsk.park.analytics.DynamicLinkFailedEvent
import se.barsk.park.datatypes.*
import se.barsk.park.managecars.ManageCarsActivity
import se.barsk.park.network.NetworkManager
import se.barsk.park.settings.SettingsActivity
import se.barsk.park.storage.StorageManager


class ParkActivity : AppCompatActivity(), GarageStatusChangedListener,
        CarCollectionStatusChangedListener, SpecifyServerDialog.SpecifyServerDialogListener {
    override fun parkServerChanged() {
        garage.clear()
        garage.updateStatus(applicationContext)
    }

    override fun onGarageStatusChange() {

        updateListOfParkedCars()
        updateGarageStatus()
    }

    override fun onGarageUpdateReady(success: Boolean, errorMessage: String?) {
        pullToRefreshView.isRefreshing = false
        updateParkingState()
        showParkedCarsPlaceholderIfNeeded()
        if (errorMessage != null) {
            val snackbar = Snackbar.make(containerView, errorMessage, Snackbar.LENGTH_LONG).setAction("Action", null)
            val textView = snackbar.view.findViewById<TextView>(android.support.design.R.id.snackbar_text)
            textView.maxLines = 5
            snackbar.show()
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

    private val garage: Garage = Garage.getInstance()
    private var parkingState: ParkingState = ParkingState.NO_SERVER
    private var parkedCarsRecyclerViewIsAnimating = false
    private var delayedGarageStatusChange = false
    private var serverBeforePause: String? = null
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
        super.onCreate(savedInstanceState)
        Fabric.with(this, Crashlytics())
        StorageManager.init(applicationContext)
        CrashReporting.init(applicationContext)
        Analytics.init(applicationContext)
        CarCollection.init()
        setContentView(R.layout.activity_park)
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        parkedCarsRecyclerView.layoutManager = LinearLayoutManager(this, LinearLayout.VERTICAL, false)
        parkedCarsRecyclerView.itemAnimator = object : DefaultItemAnimator() {
            override fun onAnimationFinished(viewHolder: RecyclerView.ViewHolder?) {
                parkedCarsRecyclerViewIsAnimating = false
                if (delayedGarageStatusChange) {
                    delayedGarageStatusChange = false
                    updateGarageStatus()
                }
            }
        }

        ownCarsRecyclerView.layoutManager =
                if (resources.configuration.orientation == ORIENTATION_LANDSCAPE)
                    LinearLayoutManager(this, LinearLayout.VERTICAL, false)
                else
                    LinearLayoutManager(this, LinearLayout.HORIZONTAL, false)
        ownCarsRecyclerView.itemAnimator = DefaultItemAnimator()
        ownCarsRecyclerView.adapter = CarsAdapter(CarsAdapter.Type.OWN_CARS,
                CarCollection.getCars(), this::onOwnCarClicked)

        val addCarButton = findViewById<Button>(R.id.no_own_cars_placeholder_button)
        addCarButton.setOnClickListener { _ -> navigateToManageCarsAndAddCar() }

        pullToRefreshView.setOnRefreshListener { garage.updateStatus(applicationContext) }

        garage.addListener(this)
        CarCollection.addListener(this)
        showOwnCarsPlaceholderIfNeeded()
    }

    override fun onResume() {
        super.onResume()
        if (serverBeforePause != null && serverBeforePause != StorageManager.getServer()) {
            // Server has changed since last time the activity was open
            parkServerChanged()
        } else {
            garage.updateStatus(applicationContext)
        }
        serverBeforePause = null
        automaticUpdateTask = RepeatableTask({ automaticUpdate() }, StorageManager.getAutomaticUpdateInterval())
        automaticUpdateTask.start()
        getDynamicLink()
    }

    override fun onPause() {
        super.onPause()
        serverBeforePause = StorageManager.getServer()
        automaticUpdateTask.stop()
    }

    private fun automaticUpdate() {
        // Only try to update if we can communicate with the server and there is no update
        // in progress
        if (parkingState.communicatesWithServer() &&
                NetworkManager.updateState != NetworkManager.UpdateState.UPDATE_IN_PROGRESS) {
            garage.updateStatus(applicationContext)
        }
    }


    /**
     * Show list of own cars if there are any own cars, otherwise show the placeholder.
     */
    private fun showOwnCarsPlaceholderIfNeeded() {
        val viewSwitcher = findViewById<ViewSwitcher>(R.id.own_cars_view_switcher)
        val ownCarsView = findViewById<View>(R.id.own_cars_recycler_view)
        val empty = CarCollection.getCars().isEmpty()
        showPlaceholderIfNeeded(viewSwitcher, ownCarsView, empty)
    }

    /**
     * Show the list of parked cars if there are any parked cars, otherwise show the appropriate
     * placeholder.
     */
    private fun showParkedCarsPlaceholderIfNeeded() {
        val viewSwitcher = findViewById<ViewSwitcher>(R.id.parked_cars_view_switcher)
        if (parkingState.showsPlaceholder()) {
            setCorrectParkedCarsPlaceholder()
        }
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
                parkServerButton.setOnClickListener { _ -> showServerDialog() }
            }
            ParkingState.REQUEST_FAILED -> {
                // failed to communicate with server
                spinner.visibility = View.GONE
                text = getString(R.string.unable_to_connect_placeholder_text, StorageManager.getServer())
                top = getDrawable(R.drawable.ic_cloud_off_black_72dp)
                parkServerButton.visibility = View.VISIBLE
                parkServerButton.text = getString(R.string.unable_to_connect_placeholder_button)
                parkServerButton.setOnClickListener { _ ->
                    garage.updateStatus(applicationContext)
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
        parkingState = if (!StorageManager.hasServer()) {
            ParkingState.NO_SERVER
        } else if (NetworkManager.state == NetworkManager.State.ONLY_FAILED_REQUESTS) {
            ParkingState.REQUEST_FAILED
        } else if (NetworkManager.state == NetworkManager.State.FIRST_RESPONSE_NOT_RECEIVED) {
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
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
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
        if (garage.isParked(car)) {
            garage.unparkCar(applicationContext, car)
        } else if (!garage.isFull()) {
            garage.parkCar(applicationContext, car)
        } else {
            return
        }
    }

    private fun updateListOfParkedCars() {
        parkedCarsRecyclerViewIsAnimating = !parkingState.showsPlaceholder()
        parkedCarsRecyclerView.swapAdapter(
                CarsAdapter(CarsAdapter.Type.PARKED_CARS, garage.parkedCars, { /*listener that does nothing */ }), false)
    }

    /**
     * Updates the garage status (except the list of parked cars) if the RecyclerView is
     * not currently animating. If it's animating it sets a flag that signals that the
     * RecyclerView animator should trigger an update once the animation is ready.
     *
     * If the RecyclerView is replaced with a placeholder before the animation have finished
     * the animation will continue from the same state when the RecyclerView becomes visible
     * again.
     */
    private fun updateGarageStatus() {
        // Don't update the garage status while the RecyclerView is animating. The recycler
        // view will call this function again once the animation is done.
        // If the RecyclerView is replaced with a placeholder before the animation have finished
        // the animation will continue from the same state when the RecyclerView becomes visible
        // again.
        if (parkedCarsRecyclerViewIsAnimating) {
            delayedGarageStatusChange = true
        } else {
            updateParkingState()
            updateToolbar(garage.spotsFree)
            showParkedCarsPlaceholderIfNeeded()
            CarCollection.updateParkStatus(garage)
        }
    }

    private fun updateListOfOwnCars() {
        ownCarsRecyclerView.swapAdapter(
                CarsAdapter(CarsAdapter.Type.OWN_CARS, CarCollection.getCars(), this::onOwnCarClicked), false)
    }

    private fun updateToolbar(freeSpots: Int) {
        val toolbarColor: Int
        val statusBarColor: Int
        val title: String
        when {
            parkingState.showsPlaceholder() -> {
                toolbarColor = ContextCompat.getColor(this, R.color.colorPrimary)
                statusBarColor = ContextCompat.getColor(this, R.color.colorPrimaryDark)
                title = getString(R.string.app_name)
            }
            parkingState == ParkingState.FULL -> {
                toolbarColor = ContextCompat.getColor(this, R.color.colorToolbarFull)
                statusBarColor = ContextCompat.getColor(this, R.color.colorStatusBarFull)
                title = getString(R.string.park_status_full)
            }
            parkingState == ParkingState.ALMOST_FULL -> {
                toolbarColor = ContextCompat.getColor(this, R.color.colorToolbarAlmostFull)
                statusBarColor = ContextCompat.getColor(this, R.color.colorStatusBarAlmostFull)
                title = getString(R.string.park_status_almost_full)
            }
            else -> {
                toolbarColor = ContextCompat.getColor(this, R.color.colorToolbarFree)
                statusBarColor = ContextCompat.getColor(this, R.color.colorStatusBarFree)
                title = getString(R.string.park_status_free, freeSpots)
            }
        }
        supportActionBar?.setBackgroundDrawable(ColorDrawable(toolbarColor))
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.statusBarColor = statusBarColor
        }
        supportActionBar?.title = title
    }

    private fun showServerDialog() {
        SpecifyServerDialog.newInstance().show(supportFragmentManager, "specifyServer")
    }

    private fun getDynamicLink() {
        val listener = DynamicLinkListener()
        FirebaseDynamicLinks.getInstance()
                .getDynamicLink(intent)
                .addOnSuccessListener(this, listener)
                .addOnFailureListener(this, listener)
    }

    inner class DynamicLinkListener : OnSuccessListener<PendingDynamicLinkData>, OnFailureListener {
        override fun onFailure(exception: java.lang.Exception) {
            Analytics.logEvent(DynamicLinkFailedEvent(exception.toString()))
        }

        override fun onSuccess(pendingDynamicLinkData: PendingDynamicLinkData?) {
            if (pendingDynamicLinkData == null) return
            val deepLink = DeepLink(pendingDynamicLinkData.link)
            if (!deepLink.isValid) return

            if (!StorageManager.hasServer()) {
                StorageManager.setServer(deepLink.server)
                parkServerChanged()
            }
            CarCollection.addCarsThatDoesNotExist(deepLink.cars)
        }
    }
}

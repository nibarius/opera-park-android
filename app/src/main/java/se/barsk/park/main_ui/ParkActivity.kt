package se.barsk.park.main_ui

import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v4.content.ContextCompat
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
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks
import com.google.firebase.dynamiclinks.PendingDynamicLinkData
import se.barsk.park.*
import se.barsk.park.analytics.Analytics
import se.barsk.park.analytics.DynamicLinkFailedEvent
import se.barsk.park.datatypes.*
import se.barsk.park.manage_cars.ManageCarsActivity
import se.barsk.park.network.NetworkManager
import se.barsk.park.settings.SettingsActivity
import se.barsk.park.storage.StorageManager


class ParkActivity : AppCompatActivity(), GarageStatusChangedListener,
        CarCollectionStatusChangedListener, SpecifyServerDialog.SpecifyServerDialogListener {
    override fun parkServerChanged() {
        operaGarage.clear()
        operaGarage.updateStatus()
    }

    override fun onGarageStatusChange() {
        updateParkingState()
        updateToolbar(operaGarage.spotsFree)
        updateListOfParkedCars()
        showParkedCarsPlaceholderIfNeeded()
        CarCollection.updateParkStatus(operaGarage)
    }

    override fun onGarageUpdateFail(errorMessage: String?) {
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
    }

    val operaGarage: Garage = Garage()
    private var parkingState: ParkingState = ParkingState.NO_SERVER
    private var serverBeforePause: String? = null
    private val parkedCarsRecyclerView: RecyclerView by lazy {
        findViewById<RecyclerView>(R.id.parked_cars_recycler_view)
    }
    private val layoutManager: RecyclerView.LayoutManager by lazy {
        LinearLayoutManager(this, LinearLayout.VERTICAL, false)
    }
    private val layoutManager2: RecyclerView.LayoutManager by lazy {
        LinearLayoutManager(this, LinearLayout.HORIZONTAL, false)
    }

    private val ownCarsRecyclerView: RecyclerView by lazy {
        findViewById<RecyclerView>(R.id.own_cars_recycler_view)
    }

    private val containerView: View by lazy {
        findViewById<View>(R.id.container_view)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Analytics.init(applicationContext)
        StorageManager.init(applicationContext)
        setContentView(R.layout.activity_park)
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        parkedCarsRecyclerView.layoutManager = layoutManager
        parkedCarsRecyclerView.itemAnimator = DefaultItemAnimator()

        ownCarsRecyclerView.layoutManager = layoutManager2
        ownCarsRecyclerView.itemAnimator = DefaultItemAnimator()
        ownCarsRecyclerView.adapter = CarsAdapter(CarsAdapter.Type.OWN_CARS,
                CarCollection.getCars(), this::onOwnCarClicked)

        val addCarButton = findViewById<Button>(R.id.no_own_cars_placeholder_button)
        addCarButton.setOnClickListener { _ -> navigateToManageCarsAndAddCar() }

        operaGarage.addListener(this)
        CarCollection.addListener(this)
        showOwnCarsPlaceholderIfNeeded()
    }

    override fun onResume() {
        super.onResume()
        if (serverBeforePause != null && serverBeforePause != StorageManager.getServer()) {
            // Server has changed since last time the activity was open
            parkServerChanged()
        } else {
            operaGarage.updateStatus()
        }
        serverBeforePause = null
        getDynamicLink()
    }

    override fun onPause() {
        super.onPause()
        serverBeforePause = StorageManager.getServer()
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
        val parkedCarsView = findViewById<View>(R.id.parked_cars_recycler_view)
        val empty = operaGarage.isEmpty()
        if (parkingState.showsPlaceholder()) {
            setCorrectParkedCarsPlaceholder()
        }
        showPlaceholderIfNeeded(viewSwitcher, parkedCarsView, empty)
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
                    operaGarage.updateStatus()
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
                top = getDrawable(R.drawable.empty_parking)
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
        } else if (operaGarage.isEmpty()) {
            ParkingState.EMPTY
        } else if (operaGarage.spotsFree > 1) {
            ParkingState.FREE_SPACE
        } else if (operaGarage.spotsFree == 1) {
            ParkingState.ALMOST_FULL
        } else if (operaGarage.isFull()) {
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
        if (operaGarage.isParked(car)) {
            operaGarage.unparkCar(car)
        } else if (!operaGarage.isFull()) {
            operaGarage.parkCar(car)
        } else {
            return
        }
    }

    private fun updateListOfParkedCars() {
        parkedCarsRecyclerView.swapAdapter(
                CarsAdapter(CarsAdapter.Type.PARKED_CARS, operaGarage.parkedCars, { /*listener that does nothing */ }), false)
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
                title = "Full"
            }
            parkingState == ParkingState.ALMOST_FULL -> {
                toolbarColor = ContextCompat.getColor(this, R.color.colorToolbarAlmostFull)
                statusBarColor = ContextCompat.getColor(this, R.color.colorStatusBarAlmostFull)
                title = "Last spot"
            }
            else -> {
                toolbarColor = ContextCompat.getColor(this, R.color.colorToolbarFree)
                statusBarColor = ContextCompat.getColor(this, R.color.colorStatusBarFree)
                title = "$freeSpots free spots"
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
                .addOnFailureListener(this, listener);
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

package se.barsk.park.main_ui

import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.support.design.widget.Snackbar
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.Toolbar
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.WindowManager
import android.widget.*
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks
import com.google.firebase.dynamiclinks.PendingDynamicLinkData
import de.psdev.licensesdialog.LicensesDialogFragment
import se.barsk.park.INTENT_EXTRA_ADD_CAR
import se.barsk.park.R
import se.barsk.park.consume
import se.barsk.park.datatypes.*
import se.barsk.park.manage_cars.ManageCarsActivity
import se.barsk.park.network.NetworkManager
import se.barsk.park.showPlaceholderIfNeeded
import se.barsk.park.storage.StorageManager


class ParkActivity : AppCompatActivity(), GarageStatusChangedListener,
        CarCollectionStatusChangedListener, SpecifyServerDialog.SpecifyServerDialogListener {
    override fun parkServerSpecified() {
        operaGarage.updateStatus()
    }

    override fun parkServerDialogCancelled() {
        showParkedCarsPlaceholderIfNeeded()
    }

    override fun onGarageStatusChange() {
        updateToolbar(operaGarage.spotsFree)
        updateListOfParkedCars()
        showParkedCarsPlaceholderIfNeeded()
        CarCollection.updateParkStatus(operaGarage)
    }

    override fun onGarageUpdateFail(errorMessage: String) {
        val snackbar = Snackbar.make(containerView, errorMessage, Snackbar.LENGTH_LONG).setAction("Action", null)
        val textView = snackbar.view.findViewById(android.support.design.R.id.snackbar_text) as TextView
        textView.maxLines = 5
        snackbar.show()
        showParkedCarsPlaceholderIfNeeded()
    }

    override fun onCarCollectionStatusChange() {
        updateListOfOwnCars()
        showOwnCarsPlaceholderIfNeeded()
    }

    val operaGarage: Garage = Garage()
    private lateinit var firebaseAnalytics: FirebaseAnalytics
    private val parkedCarsRecyclerView: RecyclerView by lazy {
        findViewById(R.id.parked_cars_recycler_view) as RecyclerView
    }
    private val layoutManager: RecyclerView.LayoutManager by lazy {
        LinearLayoutManager(this, LinearLayout.VERTICAL, false)
    }
    private val layoutManager2: RecyclerView.LayoutManager by lazy {
        LinearLayoutManager(this, LinearLayout.HORIZONTAL, false)
    }

    private val ownCarsRecyclerView: RecyclerView by lazy {
        findViewById(R.id.own_cars_recycler_view) as RecyclerView
    }

    private val containerView: View by lazy {
        findViewById(R.id.container_view) as View
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        StorageManager.init(applicationContext)
        setContentView(R.layout.activity_park)
        val toolbar = findViewById(R.id.toolbar) as Toolbar?
        setSupportActionBar(toolbar)

        parkedCarsRecyclerView.layoutManager = layoutManager
        parkedCarsRecyclerView.itemAnimator = DefaultItemAnimator()

        ownCarsRecyclerView.layoutManager = layoutManager2
        ownCarsRecyclerView.itemAnimator = DefaultItemAnimator()
        ownCarsRecyclerView.adapter = CarsAdapter(CarsAdapter.Type.OWN_CARS,
                CarCollection.getCars(), this::onOwnCarClicked)

        val addCarButton = findViewById(R.id.no_own_cars_placeholder_button) as Button
        addCarButton.setOnClickListener { _ -> navigateToManageCarsAndAddCar() }

        operaGarage.addListener(this)
        CarCollection.addListener(this)
        showOwnCarsPlaceholderIfNeeded()
        firebaseAnalytics = FirebaseAnalytics.getInstance(this)
        getDynamicLink()

        if (StorageManager.hasServer()) {
            // Wait a little bit with showing placeholders so the network request can
            // kick off and, maybe even finish if it's quick.
            Handler().postDelayed({ showParkedCarsPlaceholderIfNeeded() }, 500)
        } else {
            // Show missing server placeholder immediately
            showParkedCarsPlaceholderIfNeeded()
        }
    }

    override fun onResume() {
        super.onResume()
        operaGarage.updateStatus()
    }

    /**
     * Show list of own cars if there are any own cars, otherwise show the placeholder.
     */
    private fun showOwnCarsPlaceholderIfNeeded() {
        val viewSwitcher = findViewById(R.id.own_cars_view_switcher) as ViewSwitcher
        val ownCarsView = findViewById(R.id.own_cars_recycler_view)
        val empty = CarCollection.getCars().isEmpty()
        showPlaceholderIfNeeded(viewSwitcher, ownCarsView, empty)
    }

    /**
     * Show the list of parked cars if there are any parked cars, otherwise show the appropriate
     * placeholder.
     */
    private fun showParkedCarsPlaceholderIfNeeded() {
        val viewSwitcher = findViewById(R.id.parked_cars_view_switcher) as ViewSwitcher
        val parkedCarsView = findViewById(R.id.parked_cars_recycler_view)
        val empty = operaGarage.isEmpty()
        setCorrectParkedCarsPlaceholder()
        showPlaceholderIfNeeded(viewSwitcher, parkedCarsView, empty)
    }

    /**
     * Sets the current placeholder view for the parked cars view depending on circumstances
     */
    private fun setCorrectParkedCarsPlaceholder() {
        val textView = findViewById(R.id.parked_cars_placeholder_text_view) as TextView
        val parkServerButton = findViewById(R.id.no_park_server_placeholder_button) as Button
        val spinner = findViewById(R.id.loading_spinner) as ProgressBar
        val text: String
        val top: Drawable?
        if (!StorageManager.hasServer()) {
            // no server set up
            spinner.visibility = View.GONE
            text = getString(R.string.no_server_placeholder_text)
            top = getDrawable(R.drawable.ic_cloud_off_black_72dp)
            parkServerButton.visibility = View.VISIBLE
            parkServerButton.text = getString(R.string.no_server_placeholder_button)
            parkServerButton.setOnClickListener { _ -> showServerDialog() }
        } else if (NetworkManager.lastRequestFailed) {
            // failed to communicate with server
            spinner.visibility = View.GONE
            text = getString(R.string.unable_to_connect_placeholder_text, NetworkManager.serverUrl)
            top = getDrawable(R.drawable.ic_cloud_off_black_72dp)
            parkServerButton.visibility = View.VISIBLE
            parkServerButton.text = getString(R.string.unable_to_connect_placeholder_button)
            parkServerButton.setOnClickListener { _ ->
                operaGarage.updateStatus()
                setCorrectParkedCarsPlaceholder()
            }
        } else if (NetworkManager.waitingOnNetwork) {
            spinner.visibility = View.VISIBLE
            text = getString(R.string.updating_status_placeholder)
            top = null
            parkServerButton.visibility = View.GONE
        } else {
            // No parked cars
            spinner.visibility = View.GONE
            text = getString(R.string.parked_cars_placeholder)
            top = getDrawable(R.drawable.empty_parking)
            parkServerButton.visibility = View.GONE
        }
        textView.text = text
        textView.setCompoundDrawablesRelativeWithIntrinsicBounds(null, top, null, null)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.menu_manage_cars -> consume { navigateToManageCars() }
        R.id.menu_third_parties -> consume { showThirdPartyList() }
        R.id.server_dialog -> consume { showServerDialog() }
        R.id.remove_server -> consume { removeServer() }
        else -> super.onOptionsItemSelected(item)
    }

    private fun navigateToManageCars() {
        val intent = Intent(this, ManageCarsActivity::class.java)
        startActivity(intent)
    }

    private fun navigateToManageCarsAndAddCar() {
        val intent = Intent(this, ManageCarsActivity::class.java)
        intent.putExtra(INTENT_EXTRA_ADD_CAR, true)
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
            freeSpots <= 0 -> {
                toolbarColor = ContextCompat.getColor(this, R.color.colorToolbarFull)
                statusBarColor = ContextCompat.getColor(this, R.color.colorStatusBarFull)
                title = "Full"
            }
            freeSpots == 1 -> {
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

    private fun showThirdPartyList() {
        val fragment = LicensesDialogFragment.Builder(this)
                .setNotices(R.raw.notices)
                .setShowFullLicenseText(false)
                .setIncludeOwnLicense(true)
                .build()

        fragment.show(supportFragmentManager, null)
    }

    private fun showServerDialog() {
        SpecifyServerDialog.newInstance().show(supportFragmentManager, "specifyServer")
    }

    private fun removeServer() {
        StorageManager.setServer("")
        showParkedCarsPlaceholderIfNeeded()
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
            Log.w("barsk", "getDynamicLink:onFailure", exception);
        }

        override fun onSuccess(pendingDynamicLinkData: PendingDynamicLinkData?) {
            val deepLink: Uri? = pendingDynamicLinkData?.link
            val serverUrl = deepLink?.getQueryParameter("park_server")
            if (!StorageManager.hasServer() && serverUrl != null) {
                StorageManager.setServer(serverUrl)
                operaGarage.updateStatus()
                showParkedCarsPlaceholderIfNeeded()
            }
        }
    }
}

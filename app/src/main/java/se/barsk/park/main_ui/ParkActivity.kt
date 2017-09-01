package se.barsk.park.main_ui

import android.content.DialogInterface
import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v4.content.ContextCompat
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.Toolbar
import android.view.*
import android.widget.*
import de.psdev.licensesdialog.LicensesDialogFragment
import se.barsk.park.*
import se.barsk.park.datatypes.*
import se.barsk.park.manage_cars.ManageCarsActivity
import se.barsk.park.network.NetworkManager
import se.barsk.park.storage.StorageManager


class ParkActivity : AppCompatActivity(), GarageStatusChangedListener, CarCollectionStatusChangedListener {
    override fun onGarageStatusChange() {
        updateToolbar(operaGarage.spotsFree)
        updateListOfParkedCars()
        showParkedCarsPlaceholderIfNeeded()
        CarCollection.updateParkStatus(operaGarage)
    }

    override fun onGarageUpdateFail(errorMessage: String) {
        Snackbar.make(containerView, errorMessage, Snackbar.LENGTH_LONG).setAction("Action", null).show()
    }

    override fun onCarCollectionStatusChange() {
        updateListOfOwnCars()
        showOwnCarsPlaceholderIfNeeded()
    }

    val operaGarage: Garage = Garage()
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

    private val parkedCarsLabel: TextView by lazy {
        findViewById(R.id.parked_cars_label) as TextView
    }

    private val freeSpotsLabel: TextView by lazy {
        findViewById(R.id.status_label) as TextView
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
    }

    override fun onResume() {
        super.onResume()
        val server_url = StorageManager.readStringSetting(StorageManager.SETTINGS_SERVER_URL_KEY)
        if (server_url.isEmpty()) {
            showServerDialog()
        } else {
            operaGarage.updateStatus()
        }
    }

    private fun showOwnCarsPlaceholderIfNeeded() {
        val viewSwitcher = findViewById(R.id.own_cars_view_switcher) as ViewSwitcher
        val ownCarsView = findViewById(R.id.own_cars_recycler_view)
        val empty = CarCollection.getCars().isEmpty()
        showPlaceholderIfNeeded(viewSwitcher, ownCarsView, empty)
    }

    private fun showParkedCarsPlaceholderIfNeeded() {
        val viewSwitcher = findViewById(R.id.parked_cars_view_switcher) as ViewSwitcher
        val parkedCarsView = findViewById(R.id.parked_cars_recycler_view)
        val empty = operaGarage.isEmpty()
        showPlaceholderIfNeeded(viewSwitcher, parkedCarsView, empty)
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
        parkedCarsLabel.text = "Parked cars"
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
        val viewInflated = LayoutInflater.from(this).inflate(R.layout.specify_server_dialog, null);
        val input = viewInflated.findViewById(R.id.server_url_input) as EditText
        val dialog = AlertDialog.Builder(this)
                .setTitle("Specify park server")
                .setView(viewInflated)
                .setNegativeButton(R.string.cancel, { _, _ -> })
                .setPositiveButton("OK", { _, _: Int ->
                    val server = Utils.fixUrl(input.text.toString())
                    StorageManager.putSetting(StorageManager.SETTINGS_SERVER_URL_KEY, server)
                    NetworkManager.setServer(server)
                    operaGarage.updateStatus()
                })
                .create()
        input.setOnEditorActionListener({ _, _, _ ->
            dialog.getButton(DialogInterface.BUTTON_POSITIVE).performClick()
            false
        })
        input.setText(StorageManager.readStringSetting(StorageManager.SETTINGS_SERVER_URL_KEY))
        dialog.window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM)
        dialog.window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE)
        dialog.show()
    }
}

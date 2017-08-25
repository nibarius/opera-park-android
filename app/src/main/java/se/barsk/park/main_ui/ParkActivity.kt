package se.barsk.park.main_ui

import android.content.Intent
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.*
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.ViewSwitcher
import se.barsk.park.INTENT_EXTRA_ADD_CAR
import se.barsk.park.R
import se.barsk.park.consume
import se.barsk.park.datatypes.*
import se.barsk.park.manage_cars.ManageCarsActivity
import se.barsk.park.showPlaceholderIfNeeded
import se.barsk.park.storage.StorageManager


class ParkActivity : AppCompatActivity(), GarageStatusChangedListener, CarCollectionStatusChangedListener {
    override fun onGarageStatusChange() {
        updateStatusLabel(operaGarage.spotsFree)
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
        val decorator = AlternatingColorItemDecoration(R.color.colorOddRow, R.color.colorEvenRow)
        parkedCarsRecyclerView.addItemDecoration(decorator)

        ownCarsRecyclerView.layoutManager = layoutManager2
        ownCarsRecyclerView.itemAnimator = DefaultItemAnimator()
        ownCarsRecyclerView.adapter = CarsAdapter(CarsAdapter.Type.OWN_CARS,
                CarCollection.getCars(), this::onOwnCarClicked)
        ownCarsRecyclerView.addItemDecoration(DividerItemDecoration(this, LinearLayoutManager.HORIZONTAL))

        val addCarButton = findViewById(R.id.no_own_cars_placeholder_button) as Button
        addCarButton.setOnClickListener { _ -> navigateToManageCarsAndAddCar() }

        operaGarage.addListener(this)
        CarCollection.addListener(this)
        showOwnCarsPlaceholderIfNeeded()
    }

    override fun onResume() {
        super.onResume()
        operaGarage.updateStatus()
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

    private fun updateStatusLabel(freeSpots: Int) {
        if (freeSpots <= 0) {
            freeSpotsLabel.text = "Full"
        } else if (freeSpots == 1) {
            freeSpotsLabel.text = "Last spot"
        } else {
            freeSpotsLabel.text = "$freeSpots free spots"
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

}

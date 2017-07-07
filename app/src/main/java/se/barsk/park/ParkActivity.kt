package se.barsk.park

import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.*
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView


class ParkActivity : AppCompatActivity(), GarageStatusChangedListener {
    override fun onGarageStatusChange() {
        updateStatusLabel(operaGarage.spotsFree)
        updateListOfParkedCars()
        val parkStatusChanged = carCollection.updateParkStatus(operaGarage)
        if (parkStatusChanged) {
            updateListOfOwnCars()
        }
    }

    override fun onGarageUpdateFail(errorMessage: String) {
        Snackbar.make(containerView, errorMessage, Snackbar.LENGTH_LONG).setAction("Action", null).show()
    }

    val carCollection: CarCollection = CarCollection()
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
        setContentView(R.layout.activity_park)
        val toolbar = findViewById(R.id.toolbar) as Toolbar?
        setSupportActionBar(toolbar)

        parkedCarsRecyclerView.layoutManager = layoutManager
        parkedCarsRecyclerView.itemAnimator = DefaultItemAnimator()
        val decorator = AlternatingColorItemDecoration(R.color.colorOddRow, R.color.colorEvenRow)
        parkedCarsRecyclerView.addItemDecoration(decorator)

        carCollection.ownCars.add(OwnCar("AAA 111", "car1", "volvo"))
        carCollection.ownCars.add(OwnCar("BBB 222", "car2", "tesla"))
        carCollection.ownCars.add(OwnCar("BBB 223", "car3"))
        carCollection.ownCars.add(OwnCar("BBB 224", "car4"))
        carCollection.ownCars.add(OwnCar("BBB 225", "car5"))
        carCollection.ownCars.add(OwnCar("BBB 226", "car6"))
        ownCarsRecyclerView.layoutManager = layoutManager2
        ownCarsRecyclerView.itemAnimator = DefaultItemAnimator()
        ownCarsRecyclerView.adapter = CarsAdapter(carCollection.ownCars.toList(), this::onOwnCarClicked)
        ownCarsRecyclerView.addItemDecoration(DividerItemDecoration(this, LinearLayoutManager.HORIZONTAL))

        operaGarage.addListener(this)
    }

    override fun onResume() {
        super.onResume()
        operaGarage.updateStatus()
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
                CarsAdapter(operaGarage.parkedCars, { /*listener that does nothing */ }), false)
    }

    private fun updateListOfOwnCars() {
        ownCarsRecyclerView.swapAdapter(
                CarsAdapter(carCollection.ownCars, this::onOwnCarClicked), false)
    }

}

package se.barsk.park

import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.Toolbar
import android.widget.LinearLayout


class ParkActivity : AppCompatActivity() {
    val ownCars: MutableList<OwnCar> = mutableListOf()
    private var layoutManager: RecyclerView.LayoutManager? = null
    private var layoutManager2: RecyclerView.LayoutManager? = null
    private var parkedCarsRecyclerView: RecyclerView? = null
    private var ownCarsRecyclerView: RecyclerView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_park)
        val toolbar = findViewById(R.id.toolbar) as Toolbar?
        setSupportActionBar(toolbar)

        val fab = findViewById(R.id.fab) as FloatingActionButton?
        fab!!.setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show()
        }

        parkedCarsRecyclerView = findViewById(R.id.parked_cars_recycler_view) as RecyclerView?
        layoutManager = LinearLayoutManager(this, LinearLayout.VERTICAL, false)
        parkedCarsRecyclerView?.layoutManager = layoutManager
        parkedCarsRecyclerView?.itemAnimator = DefaultItemAnimator()
        //parkedCarsRecyclerView.addItemDecoration(DividerItemDecoration(this, LinearLayoutManager.VERTICAL))

        ownCars.add(OwnCar("AAA111", "car1", "volvo"))
        ownCars.add(OwnCar("BBB222", "car2", "tesla"))
        ownCarsRecyclerView = findViewById(R.id.own_cars_recycler_view) as RecyclerView?
        layoutManager2 = LinearLayoutManager(this, LinearLayout.HORIZONTAL, false)
        ownCarsRecyclerView?.layoutManager = layoutManager2
        ownCarsRecyclerView?.itemAnimator = DefaultItemAnimator()
        ownCarsRecyclerView?.adapter = CarsAdapter(ownCars.toList())
        //ownCarsRecyclerView.addItemDecoration(DividerItemDecoration(this, LinearLayoutManager.VERTICAL))
    }

    override fun onResume() {
        super.onResume()
        val parkedCars = NetworkManager.checkStatus()
        updateList(parkedCars.second)
    }

    private fun updateList(parkedCars: List<ParkedCar>) {
        parkedCarsRecyclerView?.swapAdapter(CarsAdapter(parkedCars), false)
    }

}

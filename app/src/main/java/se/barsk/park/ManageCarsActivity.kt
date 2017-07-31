package se.barsk.park

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.view.ActionMode
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.LinearLayout

class ManageCarsActivity : AppCompatActivity() {

    private val carCollection = CarCollection(mutableListOf(
            OwnCar("AAA 111", "car1", "volvo"),
            OwnCar("BBB 222", "car2", "tesla"),
            OwnCar("BBB 223", "car3"),
            OwnCar("BBB 224", "car4"),
            OwnCar("BBB 225", "car5"),
            OwnCar("BBB 226", "car6")
    ))

    private var actionMode: ActionMode? = null
    private val adapter = SelectableCarsAdapter(carCollection.ownCars.toList(), {})

    private val manageCarsRecyclerView: RecyclerView by lazy {
        findViewById(R.id.manage_cars_recyclerview) as RecyclerView
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_manage_cars)

        manageCarsRecyclerView.layoutManager = LinearLayoutManager(this, LinearLayout.VERTICAL, false)
        manageCarsRecyclerView.itemAnimator = DefaultItemAnimator()
        manageCarsRecyclerView.adapter = adapter
        manageCarsRecyclerView.addItemDecoration(adapter.selectionDecorator)
        manageCarsRecyclerView.addItemDecoration(DividerItemDecoration(this, LinearLayoutManager.VERTICAL))
        val touchListener = RecyclerTouchListener(this, manageCarsRecyclerView, object : RecyclerTouchListener.ClickListener {
            override fun onClick(view: View, position: Int) {
                // Select item if already in selection mode
                if (actionMode != null) {
                    onListItemSelect(position)
                }
            }

            override fun onLongClick(view: View, position: Int) {
                // Always select item on longpress
                onListItemSelect(position)
            }

        })
        manageCarsRecyclerView.addOnItemTouchListener(touchListener)
    }

    //List item select method
    private fun onListItemSelect(position: Int) {
        adapter.toggleSelection(position)
        if (adapter.hasSelectedItems() && actionMode == null) {
            // there are some selected items but no action mode, start the actionMode
            actionMode = (this as AppCompatActivity).startSupportActionMode(object: ActionMode.Callback {
                override fun onActionItemClicked(mode: ActionMode?, item: MenuItem?): Boolean {
                    return true
                }

                override fun onCreateActionMode(mode: ActionMode?, menu: Menu?): Boolean {
                    return true
                }

                override fun onPrepareActionMode(mode: ActionMode?, menu: Menu?): Boolean {
                    return true
                }

                override fun onDestroyActionMode(mode: ActionMode?) {
                    adapter.clearSelection()
                    actionMode = null
                }
            })
        }
        else if (!adapter.hasSelectedItems()) {
            // there no selected items, finish the actionMode
            actionMode?.finish()
            actionMode = null
        }

        actionMode?.title = "Selected: ${adapter.numSelectedItems()}"
    }
}

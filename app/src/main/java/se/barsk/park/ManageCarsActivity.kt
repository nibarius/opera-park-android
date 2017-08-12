package se.barsk.park

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.view.ActionMode
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.LinearLayout


class ManageCarsActivity : AppCompatActivity(), EditCarDialog.EditCarDialogListener {

    // Called when the user clicks Save in the add/edit car dialog
    override fun onDialogPositiveClick(newCar: OwnCar) {
        if (adapter.hasSelectedItems()) {
            // User edits a car
            carCollection.ownCars[adapter.selectedItemsIds.keyAt(0)] = newCar
            finishActionMode()
        } else {
            // User adds a new car
            carCollection.ownCars.add(newCar)
        }
        adapter.cars = carCollection.ownCars.toList()
        adapter.notifyDataSetChanged()
    }

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
                if (actionMode != null) {
                    // Only react to normal clicks in action mode
                    onListItemSelect(position)
                }
            }

            override fun onLongClick(view: View, position: Int) = onListItemSelect(position)
        })
        manageCarsRecyclerView.addOnItemTouchListener(touchListener)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.manage_cars_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.manage_cars_menu_add -> consume { showAddDialog() }
        else -> super.onOptionsItemSelected(item)
    }

    // Called when an item is selected in the list of cars
    private fun onListItemSelect(position: Int) {
        adapter.toggleSelection(position)
        if (adapter.hasSelectedItems() && actionMode == null) {
            // there are some selected items but no action mode, start the actionMode
            actionMode = (this as AppCompatActivity).startSupportActionMode(ActionModeCallback())
        } else if (!adapter.hasSelectedItems()) {
            // there no selected items, finish the actionMode
            finishActionMode()
        } else {
            // there are selected items and already in action mode, update the menu
            actionMode?.invalidate()
        }

        actionMode?.title = getString(R.string.cab_selected_items) + adapter.numSelectedItems()
    }

    private fun deleteSelectedItems() {
        val selected = adapter.selectedItemsIds
        for (i in selected.size() - 1 downTo 0) {
            val itemToDelete = selected.keyAt(i)
            carCollection.ownCars.removeAt(itemToDelete)
        }
        finishActionMode()
        adapter.cars = carCollection.ownCars.toList()
        adapter.notifyDataSetChanged()
    }

    private fun finishActionMode() {
        actionMode?.finish()
        actionMode = null
        adapter.clearSelection()
    }

    private fun showEditDialog(ownCar: OwnCar) = EditCarDialog(ownCar).show(supportFragmentManager, "editCar")
    private fun showAddDialog() = AddCarDialog().show(supportFragmentManager, "addCar")

    /**
     * Consume function for the menu that consumes the item selected event by
     * running the given function and returning true
     */
    inline fun consume(f: () -> Unit): Boolean {
        f()
        return true
    }

    /**
     * Listener for events related to the CAB.
     */
    inner class ActionModeCallback : ActionMode.Callback {
        override fun onActionItemClicked(mode: ActionMode?, item: MenuItem): Boolean = when (item.itemId) {
            R.id.item_delete -> consume { deleteSelectedItems() }
            R.id.item_edit -> consume {
                showEditDialog(carCollection.ownCars[adapter.selectedItemsIds.keyAt(0)])
            }
            R.id.item_share -> consume { print(3) }//share item
            else -> true
        }

        override fun onCreateActionMode(mode: ActionMode?, menu: Menu?): Boolean {
            menuInflater.inflate(R.menu.manage_cars_context_menu, menu)
            return true
        }

        override fun onPrepareActionMode(mode: ActionMode?, menu: Menu?): Boolean {
            val item = menu?.findItem(R.id.item_edit)
            if (item != null) {
                item.isEnabled = adapter.numSelectedItems() == 1
                item.isVisible = adapter.numSelectedItems() == 1
            }
            return true
        }

        override fun onDestroyActionMode(mode: ActionMode?) {
            adapter.clearSelection()
            actionMode = null
        }
    }
}

package se.barsk.park.manage_cars

import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.v7.app.AppCompatActivity
import android.support.v7.view.ActionMode
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.LinearLayout
import se.barsk.park.R
import se.barsk.park.consume
import se.barsk.park.datatypes.OwnCar
import se.barsk.park.storage.StorageManager


class ManageCarsActivity : AppCompatActivity(), ManageCarDialog.ManageCarDialogListener {

    // Called when the user clicks Save in the add/edit car dialog
    override fun onDialogPositiveClick(newCar: OwnCar, dialogType: ManageCarDialog.DialogType) {
        when (dialogType) {
            ManageCarDialog.DialogType.EDIT -> {
                carCollection.updateCar(newCar)
                finishActionMode()
            }
            ManageCarDialog.DialogType.ADD -> {
                carCollection.addCar(newCar)
            }
        }
        adapter.cars = carCollection.ownCars.toList()
        adapter.notifyDataSetChanged()
    }

    private val carCollection = StorageManager.fetchCarCollection()
    private var actionMode: ActionMode? = null
    private val adapter = SelectableCarsAdapter(carCollection.ownCars.toList(), {})
    private val manageCarsRecyclerView: RecyclerView by lazy {
        findViewById(R.id.manage_cars_recyclerview) as RecyclerView
    }
    private val fab: FloatingActionButton by lazy {
        findViewById(R.id.manage_cards_fab) as FloatingActionButton
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_manage_cars)

        manageCarsRecyclerView.layoutManager = LinearLayoutManager(this, LinearLayout.VERTICAL, false)
        manageCarsRecyclerView.itemAnimator = DefaultItemAnimator()
        manageCarsRecyclerView.adapter = adapter
        val touchListener = RecyclerTouchListener(this, manageCarsRecyclerView, object : RecyclerTouchListener.ClickListener {
            override fun onClick(view: View, position: Int) {
                if (actionMode != null) {
                    // Select item in action mode
                    onListItemSelect(position)
                } else {
                    // Edit item when not in action mode
                    showEditDialog(carCollection.ownCars[position])
                }
            }

            override fun onLongClick(view: View, position: Int) = onListItemSelect(position)
        })
        manageCarsRecyclerView.addOnItemTouchListener(touchListener)

        fab.setOnClickListener { _ -> showAddDialog() }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.manage_cars_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.manage_cars_menu_manage_mode -> consume { startActionModeWithoutSelection() }
        R.id.manage_cars_menu_select_all -> consume { selectAllItems() }
        else -> super.onOptionsItemSelected(item)
    }

    // Called when an item is selected in the list of cars
    private fun onListItemSelect(position: Int) {
        adapter.toggleSelection(position)
        if (adapter.hasSelectedItems() && actionMode == null) {
            // there are some selected items but no action mode, start the actionMode
            startActionMode()
        } else if (!adapter.hasSelectedItems()) {
            // there no selected items, finish the actionMode
            finishActionMode()
        } else {
            // there are selected items and already in action mode, update the menu
            actionMode?.invalidate()
        }

        actionMode?.title = adapter.numSelectedItems().toString()
    }

    private fun selectAllItems() {
        adapter.selectAll()
        startActionMode()
        actionMode?.title = adapter.numSelectedItems().toString()
    }

    private fun startActionModeWithoutSelection() {
        startActionMode()
        actionMode?.title = getString(R.string.select_cars)
    }

    private fun deleteSelectedItems() {
        val selected = adapter.selectedItemsIds
        for (i in selected.size() - 1 downTo 0) {
            val itemToDelete = selected.keyAt(i)
            carCollection.removeCarAt(itemToDelete)
        }
        adapter.cars = carCollection.ownCars.toList()
        adapter.notifyDataSetChanged()
        finishActionMode()
    }

    private fun startActionMode() {
        actionMode = (this as AppCompatActivity).startSupportActionMode(ActionModeCallback())
    }

    private fun finishActionMode() {
        actionMode?.finish()
        actionMode = null
    }

    private fun showEditDialog(ownCar: OwnCar) = EditCarDialog(ownCar).show(supportFragmentManager, "editCar")
    private fun showAddDialog() = AddCarDialog().show(supportFragmentManager, "addCar")

    /**
     * Listener for events related to the CAB.
     */
    inner class ActionModeCallback : ActionMode.Callback {
        override fun onActionItemClicked(mode: ActionMode?, item: MenuItem): Boolean = when (item.itemId) {
            R.id.item_delete -> consume { deleteSelectedItems() }
            R.id.item_share -> consume { TODO("Implement share feature") }//share item
            else -> true
        }

        override fun onCreateActionMode(mode: ActionMode?, menu: Menu?): Boolean {
            menuInflater.inflate(R.menu.manage_cars_context_menu, menu)
            fab.hide()
            return true
        }

        override fun onPrepareActionMode(mode: ActionMode?, menu: Menu?): Boolean {
            val delete = menu?.findItem(R.id.item_delete)
            if (delete != null) {
                delete.isEnabled = adapter.numSelectedItems() > 0
                delete.isVisible = adapter.numSelectedItems() > 0
            }
            return true
        }

        override fun onDestroyActionMode(mode: ActionMode?) {
            adapter.clearSelection()
            actionMode = null
            fab.show()
        }

    }
}

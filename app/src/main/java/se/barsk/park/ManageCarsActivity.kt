package se.barsk.park

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v4.app.DialogFragment
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

    override fun onDialogPositiveClick(newCar: OwnCar) {
        carCollection.ownCars[adapter.selectedItemsIds.keyAt(0)] = newCar
        actionMode?.finish() //todo: make a finish action mode method
        actionMode = null
        adapter.clearSelection() //todo: move the updating of te adapter into the adapter (updateData() or something similar)
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

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.manage_cars_menu, menu)
        return true
    }

    //List item select method
    private fun onListItemSelect(position: Int) {
        adapter.toggleSelection(position)
        if (adapter.hasSelectedItems() && actionMode == null) {
            // there are some selected items but no action mode, start the actionMode
            actionMode = (this as AppCompatActivity).startSupportActionMode(object: ActionMode.Callback {
                override fun onActionItemClicked(mode: ActionMode?, item: MenuItem): Boolean {
                    when (item.itemId) {
                        R.id.item_delete -> {
                            deleteSelectedItems()
                        }
                        R.id.item_edit -> {
                            if (adapter.hasSelectedItems()) {
                                showEditDialog(carCollection.ownCars[adapter.selectedItemsIds.keyAt(0)])
                            }
                        }
                        R.id.item_share -> print(3)//share item
                    }
                    return true
                }

                override fun onCreateActionMode(mode: ActionMode?, menu: Menu?): Boolean {
                    return true
                }

                override fun onPrepareActionMode(mode: ActionMode?, menu: Menu?): Boolean {
                    menuInflater.inflate(R.menu.manage_cars_context_menu, menu)
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

    private fun deleteSelectedItems() {
        val selected = adapter.selectedItemsIds
        for (i in selected.size() - 1 downTo 0) {
            val itemToDelete = selected.keyAt(i)
            carCollection.ownCars.removeAt(itemToDelete)
        }
        actionMode?.finish() //todo: make a finish action mode method
        actionMode = null
        adapter.clearSelection() //todo: move the updating of te adapter into the adapter (updateData() or something similar)
        adapter.cars = carCollection.ownCars.toList()
        adapter.notifyDataSetChanged()
    }

    private fun showEditDialog(ownCar: OwnCar) {
        val dialogFragment = EditCarDialog(ownCar)
        dialogFragment.show(supportFragmentManager, "editCar")
    }
}

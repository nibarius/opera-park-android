package se.barsk.park.managecars

import android.content.Intent
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
import android.widget.ViewSwitcher
import se.barsk.park.*
import se.barsk.park.analytics.ShareCarEvent
import se.barsk.park.datatypes.CarCollectionStatusChangedListener
import se.barsk.park.datatypes.OwnCar


class ManageCarsActivity : AppCompatActivity(), ManageCarDialog.ManageCarDialogListener, CarCollectionStatusChangedListener {
    override fun onCarCollectionStatusChange() = if (recyclerViewIsAnimating) {
        delayedPlaceholderSwitch = true
    } else {
        showCarsPlaceholderIfNeeded()
    }

    // Called when the user clicks Save in the add/edit car dialog
    override fun onDialogPositiveClick(newCar: OwnCar, dialogType: ManageCarDialog.DialogType) {
        recyclerViewIsAnimating = !showsPlaceholder()
        when (dialogType) {
            ManageCarDialog.DialogType.EDIT -> {
                ParkApp.carCollection.updateCar(newCar)
                finishActionMode()
            }
            ManageCarDialog.DialogType.ADD -> {
                ParkApp.carCollection.addCar(newCar)
            }
        }
        adapter.cars = ParkApp.carCollection.getCars()
        adapter.notifyDataSetChanged()
    }

    private var actionMode: ActionMode? = null
    private var recyclerViewIsAnimating = false
    private var delayedPlaceholderSwitch = true
    private val adapter = SelectableCarsAdapter(ParkApp.carCollection.getCars(), {})
    private val manageCarsRecyclerView: RecyclerView by lazy {
        findViewById<RecyclerView>(R.id.manage_cars_recyclerview)
    }
    private val fab: FloatingActionButton by lazy {
        findViewById<FloatingActionButton>(R.id.manage_cards_fab)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ParkApp.init(this)
        setContentView(R.layout.activity_manage_cars)

        manageCarsRecyclerView.layoutManager = LinearLayoutManager(this, LinearLayout.VERTICAL, false)
        manageCarsRecyclerView.itemAnimator = object : DefaultItemAnimator() {
            override fun onAnimationFinished(viewHolder: RecyclerView.ViewHolder?) {
                recyclerViewIsAnimating = false
                if (delayedPlaceholderSwitch) {
                    delayedPlaceholderSwitch = false
                    showCarsPlaceholderIfNeeded()
                }
            }
        }
        manageCarsRecyclerView.adapter = adapter
        val touchListener = RecyclerTouchListener(this, manageCarsRecyclerView, object : RecyclerTouchListener.ClickListener {
            override fun onClick(view: View, position: Int) = if (actionMode != null) {
                // Select item in action mode
                onListItemSelect(position)
            } else {
                // Edit item when not in action mode
                showEditDialog(ParkApp.carCollection.getCarId(position))
            }

            override fun onLongClick(view: View, position: Int) = onListItemSelect(position)
        })
        manageCarsRecyclerView.addOnItemTouchListener(touchListener)

        fab.setOnClickListener { _ -> showAddDialog() }

        if (intent.getBooleanExtra(INTENT_EXTRA_ADD_CAR, false)) {
            showAddDialog()
        }

        ParkApp.carCollection.addListener(this)
        showCarsPlaceholderIfNeeded()
    }

    override fun onDestroy() {
        super.onDestroy()
        ParkApp.carCollection.removeListener(this)
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
        actionMode?.title = getString(R.string.action_mode_title)
    }

    private fun deleteSelectedItems() {
        recyclerViewIsAnimating = true
        val selected = adapter.selectedItemsIds
        (selected.size() - 1 downTo 0)
                .map { selected.keyAt(it) }
                .forEach { ParkApp.carCollection.removeCarAt(it) }
        adapter.cars = ParkApp.carCollection.getCars()
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

    private fun showEditDialog(carId: String) =
            EditCarDialog.newInstance(carId).show(supportFragmentManager, "editCar")

    private fun showAddDialog() = AddCarDialog.newInstance().show(supportFragmentManager, "addCar")

    private fun showCarsPlaceholderIfNeeded() {
        val viewSwitcher = findViewById<ViewSwitcher>(R.id.manage_cars_view_switcher)
        val parkedCarsView = findViewById<View>(R.id.manage_cars_recyclerview)
        val empty = ParkApp.carCollection.getCars().isEmpty()
        showPlaceholderIfNeeded(viewSwitcher, parkedCarsView, empty)
    }

    private fun showsPlaceholder() = ParkApp.carCollection.getCars().isEmpty()

    private fun shareSelectedItems() {
        val selected = adapter.selectedItemsIds
        val cars: MutableList<OwnCar> = mutableListOf()
        (0 until selected.size())
                .map { selected.keyAt(it) }
                .mapTo(cars) { ParkApp.carCollection.getCarAtPosition(it) }
        val linkToShare = DeepLink.getDynamicLinkFor(cars)
        val shareTitle = resources.getQuantityString(R.plurals.share_car_title, selected.size())
        startActivity(Intent.createChooser(createShareIntent(linkToShare), shareTitle))
        ParkApp.analytics.logEvent(ShareCarEvent(selected.size()))
    }

    private fun createShareIntent(url: String): Intent {
        val shareIntent = Intent(Intent.ACTION_SEND)
        shareIntent.type = "text/plain"
        shareIntent.putExtra(Intent.EXTRA_TEXT, url)
        return shareIntent
    }

    /**
     * Listener for events related to the CAB.
     */
    inner class ActionModeCallback : ActionMode.Callback {
        override fun onActionItemClicked(mode: ActionMode?, item: MenuItem): Boolean = when (item.itemId) {
            R.id.item_delete -> consume { deleteSelectedItems() }
            R.id.item_share -> consume { shareSelectedItems() }
            else -> true
        }

        override fun onCreateActionMode(mode: ActionMode?, menu: Menu?): Boolean {
            menuInflater.inflate(R.menu.manage_cars_context_menu, menu)
            fab.hide()
            return true
        }

        override fun onPrepareActionMode(mode: ActionMode?, menu: Menu?): Boolean {
            val enabled = adapter.numSelectedItems() > 0
            val delete = menu?.findItem(R.id.item_delete)
            if (delete != null) {
                delete.isEnabled = enabled
                delete.isVisible = enabled
            }
            val share = menu?.findItem(R.id.item_share)
            if (share != null) {
                share.isEnabled = enabled
                share.isVisible = enabled
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

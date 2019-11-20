package se.barsk.park.managecars

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ViewSwitcher
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.ActionMode
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import se.barsk.park.*
import se.barsk.park.analytics.ShareCarEvent
import se.barsk.park.datatypes.Car
import se.barsk.park.datatypes.CarCollectionStatusChangedListener
import se.barsk.park.datatypes.OwnCar


class ManageCarsActivity : AppCompatActivity(), ManageCarDialog.ManageCarDialogListener, CarCollectionStatusChangedListener {
    override fun onCarCollectionStatusChange() {
        adapter.ownCars = ParkApp.carCollection.getCars()
        adapter.notifyDataSetChanged()
        showCarsPlaceholderIfNeeded()
        updateOptionsMenuItems()
    }

    // Called when the user clicks Save in the add/edit car dialog
    override fun onDialogPositiveClick(newCar: OwnCar, dialogType: ManageCarDialog.DialogType) {
        when (dialogType) {
            ManageCarDialog.DialogType.EDIT -> {
                ParkApp.carCollection.updateCar(newCar)
                finishActionMode()
            }
            ManageCarDialog.DialogType.ADD -> {
                ParkApp.carCollection.addCar(newCar)
            }
        }
    }

    private var actionMode: ActionMode? = null
    @Suppress("unused")  // Used to access actionMode in tests in the mock flavor
    fun getActionMode() = actionMode
    private val adapter: SelectableCarsAdapter by lazy {
        SelectableCarsAdapter(ParkApp.carCollection.getCars()) {}
    }
    private val manageCarsRecyclerView: RecyclerView by lazy {
        findViewById<RecyclerView>(R.id.manage_cars_recyclerview)
    }
    private val fab: FloatingActionButton by lazy {
        findViewById<FloatingActionButton>(R.id.manage_cards_fab)
    }
    private lateinit var optionsMenu: Menu

    // Exposing the onClick and onLongClick functions to be able to use them in unit tests.
    fun recyclerOnLongClick(position: Int) = onListItemSelect(position)

    fun recyclerOnClick(position: Int) = if (actionMode != null) {
        // Select item in action mode
        onListItemSelect(position)
    } else {
        // Edit item when not in action mode
        showEditDialog(adapter.ownCars[position].id)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        ParkApp.init(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_manage_cars)

        manageCarsRecyclerView.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        manageCarsRecyclerView.itemAnimator = DefaultItemAnimator()
        manageCarsRecyclerView.adapter = adapter
        val touchListener = RecyclerTouchListener(this, manageCarsRecyclerView,
                object : RecyclerTouchListener.ClickListener {
                    override fun onClick(view: View, position: Int) = recyclerOnClick(position)
                    override fun onLongClick(view: View, position: Int) = recyclerOnLongClick(position)
                })
        manageCarsRecyclerView.addOnItemTouchListener(touchListener)

        fab.setOnClickListener { showAddDialog() }

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
        optionsMenu = menu
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.manage_cars_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        updateOptionsMenuItems()
        return super.onPrepareOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.manage_cars_menu_manage_mode -> consume { startActionModeWithoutSelection() }
        R.id.manage_cars_menu_select_all -> consume { selectAllItems() }
        else -> super.onOptionsItemSelected(item)
    }

    private fun updateOptionsMenuItems() {
        val enabled = adapter.cars.isNotEmpty()
        val selectAll = optionsMenu.findItem(R.id.manage_cars_menu_select_all)
        selectAll.isVisible = enabled
        selectAll.isEnabled = enabled
        val manage = optionsMenu.findItem(R.id.manage_cars_menu_manage_mode)
        manage.isVisible = enabled
        manage.isEnabled = enabled
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

        actionMode?.title = getString(R.string.manage_cars_action_mode_title, adapter.numSelectedItems())
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
        val toDelete = adapter.selectedItemsIds.clone()
        // Make sure to clear the selection before deleting the items to prevent that
        // the view has a visible check view later on if the same view gets re-used in
        // the recycler view. This would lead to a crash when showItem() is called for
        // the new item being added to the recycler view when it tries to animate the
        // check view.
        adapter.clearSelection()
        ParkApp.carCollection.removeCars(toDelete)
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
        // This function is typically called just after the adapter have changed but before
        // the recyclerview have started animating the changes. Post a message on the message
        // queue to continue after the recycler view have started animations so we can detect
        // if they are still going
        Handler().post { showCarsPlaceholderIfNeededAfterAnimation() }
    }

    private fun showCarsPlaceholderIfNeededAfterAnimation() {
        if (manageCarsRecyclerView.isAnimating) {
            // If the recyclerview is animating, try again a bit later
            // If it's animating there is an animator so it's safe to assume itemAnimator exists
            manageCarsRecyclerView.itemAnimator!!.isRunning { showCarsPlaceholderIfNeeded() }
            return
        }
        val viewSwitcher = findViewById<ViewSwitcher>(R.id.manage_cars_view_switcher)
        val parkedCarsView = findViewById<View>(R.id.manage_cars_recyclerview)
        val empty = adapter.cars.isEmpty()
        showPlaceholderIfNeeded(viewSwitcher, parkedCarsView, empty)
    }

    private fun shareSelectedItems() {
        val selected = adapter.selectedItemsIds
        val cars: MutableList<Car> = mutableListOf()
        (0 until selected.size())
                .map { selected.keyAt(it) }
                .mapTo(cars) { adapter.cars[it] }
        val linkToShare = DeepLink.getDynamicLinkFor(cars, ParkApp.storageManager.getServer())
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

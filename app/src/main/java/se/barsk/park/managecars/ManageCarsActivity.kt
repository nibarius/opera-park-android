package se.barsk.park.managecars

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.ActionMode
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import androidx.core.view.updatePadding
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import se.barsk.park.INTENT_EXTRA_ADD_CAR
import se.barsk.park.ParkApp
import se.barsk.park.R
import se.barsk.park.consume
import se.barsk.park.databinding.ActivityManageCarsBinding
import se.barsk.park.datatypes.CarCollectionStatusChangedListener
import se.barsk.park.datatypes.OwnCar
import se.barsk.park.showPlaceholderIfNeeded


class ManageCarsActivity : AppCompatActivity(), ManageCarDialog.ManageCarDialogListener,
    CarCollectionStatusChangedListener {
    @SuppressLint("NotifyDataSetChanged")
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

    lateinit var binding: ActivityManageCarsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        ParkApp.init(this)
        super.onCreate(savedInstanceState)
        binding = ActivityManageCarsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Enable edge-to-edge
        // It's good practice to call this early in onCreate
        WindowCompat.setDecorFitsSystemWindows(window, false)

        binding.manageCarsRecyclerview.layoutManager =
            LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        binding.manageCarsRecyclerview.itemAnimator = DefaultItemAnimator()
        binding.manageCarsRecyclerview.adapter = adapter
        val touchListener = RecyclerTouchListener(
            this, binding.manageCarsRecyclerview,
            object : RecyclerTouchListener.ClickListener {
                override fun onClick(view: View, position: Int) = recyclerOnClick(position)
                override fun onLongClick(view: View, position: Int) = recyclerOnLongClick(position)
            })
        binding.manageCarsRecyclerview.addOnItemTouchListener(touchListener)

        binding.manageCardsFab.setOnClickListener { showAddDialog() }

        if (intent.getBooleanExtra(INTENT_EXTRA_ADD_CAR, false)) {
            showAddDialog()
        }

        ParkApp.carCollection.addListener(this)
        showCarsPlaceholderIfNeeded()

        // Handle edge-to-edge so that parts of the UI doesn't get covered under system bars

        // List of cars
        ViewCompat.setOnApplyWindowInsetsListener(binding.manageCarsRecyclerview) { v, insets ->
            val systemBars = insets.getInsets(
                WindowInsetsCompat.Type.systemBars() or
                        WindowInsetsCompat.Type.displayCutout() or
                        WindowInsetsCompat.Type.ime()
            )
            v.updatePadding(
                top = systemBars.top,
                left = systemBars.left,
                right = systemBars.right,
                bottom = systemBars.bottom,
            )
            insets
        }
        // FAB
        ViewCompat.setOnApplyWindowInsetsListener(binding.manageCardsFab) { v, windowInsets ->
            val originalMargin = resources.getDimensionPixelSize(R.dimen.fab_margin)
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                leftMargin = originalMargin + insets.left
                bottomMargin = originalMargin + insets.bottom
                rightMargin = originalMargin + insets.right
            }
            WindowInsetsCompat.CONSUMED
        }
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

        actionMode?.title =
            getString(R.string.manage_cars_action_mode_title, adapter.numSelectedItems())
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
        Handler(Looper.getMainLooper()).post { showCarsPlaceholderIfNeededAfterAnimation() }
    }

    private fun showCarsPlaceholderIfNeededAfterAnimation() {
        if (binding.manageCarsRecyclerview.isAnimating) {
            // If the recyclerview is animating, try again a bit later
            // If it's animating there is an animator so it's safe to assume itemAnimator exists
            binding.manageCarsRecyclerview.itemAnimator!!.isRunning { showCarsPlaceholderIfNeeded() }
            return
        }
        val empty = adapter.cars.isEmpty()
        showPlaceholderIfNeeded(
            binding.manageCarsViewSwitcher,
            binding.manageCarsRecyclerview,
            empty
        )
    }


    /**
     * Listener for events related to the CAB.
     */
    inner class ActionModeCallback : ActionMode.Callback {
        override fun onActionItemClicked(mode: ActionMode?, item: MenuItem): Boolean =
            when (item.itemId) {
                R.id.item_delete -> consume { deleteSelectedItems() }
                else -> true
            }

        override fun onCreateActionMode(mode: ActionMode?, menu: Menu?): Boolean {
            menuInflater.inflate(R.menu.manage_cars_context_menu, menu)
            binding.manageCardsFab.hide()
            return true
        }

        override fun onPrepareActionMode(mode: ActionMode?, menu: Menu?): Boolean {
            val enabled = adapter.numSelectedItems() > 0
            val delete = menu?.findItem(R.id.item_delete)
            if (delete != null) {
                delete.isEnabled = enabled
                delete.isVisible = enabled
            }
            return true
        }

        override fun onDestroyActionMode(mode: ActionMode?) {
            adapter.clearSelection()
            actionMode = null
            binding.manageCardsFab.show()
        }
    }
}

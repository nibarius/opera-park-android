package se.barsk.park

import android.annotation.SuppressLint
import android.content.Intent
import android.support.design.widget.FloatingActionButton
import android.support.v7.app.AlertDialog
import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.EditText
import android.widget.TextView
import org.amshove.kluent.*
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.robolectric.Robolectric
import org.robolectric.Shadows
import org.robolectric.android.controller.ActivityController
import se.barsk.park.carcollection.MockCarCollection
import se.barsk.park.managecars.AddCarDialog
import se.barsk.park.managecars.ManageCarsActivity
import se.barsk.park.managecars.SelectableCarsAdapter


class ManageCarsActivityTest : RobolectricTest() {
    private lateinit var controller: ActivityController<ManageCarsActivity>
    private lateinit var activity: ManageCarsActivity

    @Before
    fun setUp() {
        controller = Robolectric.buildActivity(ManageCarsActivity::class.java)
        activity = controller.create().start().resume().visible().get()
    }

    @After
    fun tearDown() {
        // Destroy activity after every test
        controller.pause().stop().destroy()
        ParkApp.carCollection = MockCarCollection()
    }

    @Test
    fun fabIsVisibleTest() {
        val fab = activity.findViewById<FloatingActionButton>(R.id.manage_cards_fab)
        fab.visibility shouldEqual View.VISIBLE
    }

    @Test
    fun carListShownWithCarsTest() {
        val manageCarsRecyclerView = activity.findViewById<RecyclerView>(R.id.manage_cars_recyclerview)
        val placeholderView = activity.findViewById<TextView>(R.id.manage_cars_placeholder)
        ParkApp.carCollection.getCars().shouldNotBeEmpty()
        manageCarsRecyclerView.visibility shouldEqual View.VISIBLE
        placeholderView.visibility shouldEqual View.GONE
    }

    @Test
    fun placeholderShownWithNoCarsTest() {
        (ParkApp.carCollection as MockCarCollection).replaceContent(mutableListOf())
        val manageCarsRecyclerView = activity.findViewById<RecyclerView>(R.id.manage_cars_recyclerview)
        val placeholderView = activity.findViewById<TextView>(R.id.manage_cars_placeholder)
        ParkApp.carCollection.getCars().shouldBeEmpty()
        manageCarsRecyclerView.visibility shouldEqual View.GONE
        placeholderView.visibility shouldEqual View.VISIBLE
    }

    @Test
    fun menuIsGoneWithNoCarsTest() {
        (ParkApp.carCollection as MockCarCollection).replaceContent(mutableListOf())
        val menu = Shadows.shadowOf(activity).optionsMenu
        val selectAll = menu.findItem(R.id.manage_cars_menu_select_all)
        val choose = menu.findItem(R.id.manage_cars_menu_manage_mode)
        selectAll.isVisible.shouldBeFalse()
        choose.isVisible.shouldBeFalse()
    }

    @Test
    fun hideFabWhenClickingItTest() {
        val fab = activity.findViewById<FloatingActionButton>(R.id.manage_cards_fab)
        fab.performClick()
        fab.visibility shouldEqual View.GONE
    }

    @Test
    fun openAddDialogWhenClickingFabTest() {
        val fab = activity.findViewById<FloatingActionButton>(R.id.manage_cards_fab)
        fab.performClick()
        val dialog = activity.supportFragmentManager.findFragmentByTag("addCar")
        dialog.shouldNotBeNull()
        dialog shouldBeInstanceOf AddCarDialog::class
    }

    @Test
    fun cancelAddDialogFabShouldBeVisibleTest() {
        val fab = activity.findViewById<FloatingActionButton>(R.id.manage_cards_fab)
        fab.performClick()
        val dialog = activity.supportFragmentManager.findFragmentByTag("addCar")
        dialog.shouldNotBeNull()
        dialog as AddCarDialog
        (dialog.dialog as AlertDialog).getButton(AlertDialog.BUTTON_NEGATIVE).performClick()
        fab.visibility shouldEqual View.VISIBLE
    }

    @SuppressLint("SetTextI18n")
    @Test
    fun disabledAddButtonInCreateDialogTest() {
        val fab = activity.findViewById<FloatingActionButton>(R.id.manage_cards_fab)
        fab.performClick()
        val dialog = activity.supportFragmentManager.findFragmentByTag("addCar")
        dialog as AddCarDialog
        val button = (dialog.dialog as AlertDialog).getButton(AlertDialog.BUTTON_POSITIVE)
        button.isEnabled.shouldBeFalse()
        val regnoView = dialog.dialog.findViewById<EditText>(R.id.regno)
        val userView = dialog.dialog.findViewById<EditText>(R.id.owner)
        regnoView.shouldNotBeNull()
        userView.shouldNotBeNull()
        regnoView.setText("regno")
        button.isEnabled.shouldBeFalse()
        regnoView.setText("")
        userView.setText("user")
        button.isEnabled.shouldBeFalse()
        regnoView.setText("regno")
        button.isEnabled.shouldBeTrue()
    }

    @Test
    fun chooseMenuEntryTest() {
        val shadowActivity = Shadows.shadowOf(activity)
        shadowActivity.clickMenuItem(R.id.manage_cars_menu_manage_mode)
        val actionMode = activity.getActionMode()
        actionMode.shouldNotBeNull()
        actionMode?.title shouldBe activity.getString(R.string.action_mode_title)

        val adapter = activity.findViewById<RecyclerView>(R.id.manage_cars_recyclerview)
                .adapter as SelectableCarsAdapter
        adapter.numSelectedItems() shouldEqual 0
    }

    @Test
    fun selectAllMenuEntryTest() {
        val shadowActivity = Shadows.shadowOf(activity)
        shadowActivity.clickMenuItem(R.id.manage_cars_menu_select_all)
        val actionMode = activity.getActionMode()
        actionMode.shouldNotBeNull()

        val adapter = activity.findViewById<RecyclerView>(R.id.manage_cars_recyclerview)
                .adapter as SelectableCarsAdapter
        actionMode?.title shouldEqual adapter.itemCount.toString()
        adapter.numSelectedItems() shouldEqual adapter.itemCount
    }

    @Test
    fun startWithIntentTest() {
        val intent = Intent(context(), ManageCarsActivity::class.java)
        intent.putExtra(INTENT_EXTRA_ADD_CAR, true)
        val intentController = Robolectric.buildActivity(ManageCarsActivity::class.java, intent)
        val intentActivity = intentController.create().start().resume().visible().get()

        val fab = intentActivity.findViewById<FloatingActionButton>(R.id.manage_cards_fab)
        fab.visibility shouldEqual View.GONE

        val dialog = intentActivity.supportFragmentManager.findFragmentByTag("addCar")
        dialog.shouldNotBeNull()
        dialog shouldBeInstanceOf AddCarDialog::class

        intentController.pause().stop().destroy()
    }

    /** Tests that would be nice to have:
     * 1. Clicking on recyclerview items with without selection mode
     * 2. Clicking on recyclerview items in selection mode
     * 3. FAB is hidden after entering selection mode (fab gets hidden after
     *    the action mode have finished. Not sure how to wait with asserting until it's gone.
     */

}
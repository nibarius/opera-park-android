package se.barsk.park

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Looper.getMainLooper
import android.view.View
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import org.amshove.kluent.*
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.robolectric.Robolectric
import org.robolectric.Shadows.shadowOf
import org.robolectric.android.controller.ActivityController
import org.robolectric.annotation.LooperMode
import se.barsk.park.datatypes.MockCarCollection
import se.barsk.park.managecars.AddCarDialog
import se.barsk.park.managecars.EditCarDialog
import se.barsk.park.managecars.ManageCarsActivity
import se.barsk.park.managecars.SelectableCarsAdapter

@LooperMode(LooperMode.Mode.PAUSED)
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
        fab.visibility shouldBeEqualTo View.VISIBLE
    }

    @Test
    fun carListShownWithCarsTest() {
        val manageCarsRecyclerView =
            activity.findViewById<RecyclerView>(R.id.manage_cars_recyclerview)
        val placeholderView = activity.findViewById<TextView>(R.id.manage_cars_placeholder)
        ParkApp.carCollection.getCars().shouldNotBeEmpty()
        manageCarsRecyclerView.visibility shouldBeEqualTo View.VISIBLE
        placeholderView.visibility shouldBeEqualTo View.GONE
    }

    @Test
    fun placeholderShownWithNoCarsTest() {
        (ParkApp.carCollection as MockCarCollection).replaceContent(mutableListOf())
        val manageCarsRecyclerView =
            activity.findViewById<RecyclerView>(R.id.manage_cars_recyclerview)
        val placeholderView = activity.findViewById<TextView>(R.id.manage_cars_placeholder)
        shadowOf(getMainLooper()).idle()
        ParkApp.carCollection.getCars().shouldBeEmpty()
        manageCarsRecyclerView.visibility shouldBeEqualTo View.GONE
        placeholderView.visibility shouldBeEqualTo View.VISIBLE
    }

    @Test
    fun menuIsGoneWithNoCarsTest() {
        (ParkApp.carCollection as MockCarCollection).replaceContent(mutableListOf())
        val menu = shadowOf(activity).optionsMenu
        val selectAll = menu.findItem(R.id.manage_cars_menu_select_all)
        val choose = menu.findItem(R.id.manage_cars_menu_manage_mode)
        selectAll.isVisible.shouldBeFalse()
        choose.isVisible.shouldBeFalse()
    }

    /*
     * Checking for fab visibility stopped working when upgrading from  support library 26.1.0
     * to 27.1.1 can't figure out why or how to make it work so disabling the test for now.
     * Relevant:
     * https://stackoverflow.com/questions/36793404/checking-android-fab-visibility-always-returns-visible-from-robolectric-test-cas
    @Test
    fun hideFabWhenClickingItTest() {
        val fab = activity.findViewById<FloatingActionButton>(R.id.manage_cards_fab)
        fab.performClick()
        fab.visibility shouldBeEqualTo View.GONE
    }*/

    @Test
    fun openAddDialogWhenClickingFabTest() {
        val fab = activity.findViewById<FloatingActionButton>(R.id.manage_cards_fab)
        fab.performClick()
        shadowOf(getMainLooper()).idle()
        val dialog = activity.supportFragmentManager.findFragmentByTag("addCar")
        dialog.shouldNotBeNull()
        dialog shouldBeInstanceOf AddCarDialog::class
    }

    @Test
    fun cancelAddDialogFabShouldBeVisibleTest() {
        val fab = activity.findViewById<FloatingActionButton>(R.id.manage_cards_fab)
        fab.performClick()
        shadowOf(getMainLooper()).idle()
        val dialog = activity.supportFragmentManager.findFragmentByTag("addCar")
        dialog.shouldNotBeNull()
        dialog as AddCarDialog
        (dialog.dialog as AlertDialog).getButton(AlertDialog.BUTTON_NEGATIVE).performClick()
        shadowOf(getMainLooper()).idle()
        fab.visibility shouldBeEqualTo View.VISIBLE
    }

    @SuppressLint("SetTextI18n")
    @Test
    fun disabledAddButtonInCreateDialogTest() {
        val fab = activity.findViewById<FloatingActionButton>(R.id.manage_cards_fab)
        fab.performClick()
        shadowOf(getMainLooper()).idle()
        val addCarDialog =
            activity.supportFragmentManager.findFragmentByTag("addCar") as AddCarDialog
        val dialog = addCarDialog.dialog as AlertDialog
        val button = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
        button.isEnabled.shouldBeFalse()
        val regnoView = dialog.findViewById<EditText>(R.id.regno)
        val userView = dialog.findViewById<EditText>(R.id.owner)
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
        val shadowActivity = shadowOf(activity)
        shadowActivity.clickMenuItem(R.id.manage_cars_menu_manage_mode)
        val actionMode = activity.getActionMode()
        actionMode.shouldNotBeNull()
        actionMode.title shouldBe activity.getString(R.string.action_mode_title)

        val adapter = activity.findViewById<RecyclerView>(R.id.manage_cars_recyclerview)
            .adapter as SelectableCarsAdapter
        adapter.numSelectedItems() shouldBeEqualTo 0
    }

    @Test
    fun selectAllMenuEntryTest() {
        val shadowActivity = shadowOf(activity)
        shadowActivity.clickMenuItem(R.id.manage_cars_menu_select_all)
        val actionMode = activity.getActionMode()
        actionMode.shouldNotBeNull()

        val adapter = activity.findViewById<RecyclerView>(R.id.manage_cars_recyclerview)
            .adapter as SelectableCarsAdapter
        actionMode.title shouldBeEqualTo adapter.itemCount.toString()
        adapter.numSelectedItems() shouldBeEqualTo adapter.itemCount
    }

    @Test
    fun startWithIntentTest() {
        val intent = Intent(context(), ManageCarsActivity::class.java)
        intent.putExtra(INTENT_EXTRA_ADD_CAR, true)
        val intentController = Robolectric.buildActivity(ManageCarsActivity::class.java, intent)
        val intentActivity = intentController.create().start().resume().visible().get()

        val fab = intentActivity.findViewById<FloatingActionButton>(R.id.manage_cards_fab)
        fab.visibility shouldBeEqualTo View.GONE

        val dialog = intentActivity.supportFragmentManager.findFragmentByTag("addCar")
        dialog.shouldNotBeNull()
        dialog shouldBeInstanceOf AddCarDialog::class

        intentController.pause().stop().destroy()
    }

    @Test
    fun clickOnListEntryTest() {
        val adapter = activity.findViewById<RecyclerView>(R.id.manage_cars_recyclerview).adapter
        adapter as SelectableCarsAdapter
        adapter.itemCount shouldBeGreaterThan 0
        activity.recyclerOnClick(0)
        shadowOf(getMainLooper()).idle()

        // Can't test fab visibility, see hideFabWhenClickingItTest()
        //val fab = activity.findViewById<FloatingActionButton>(R.id.manage_cards_fab)
        //fab.visibility shouldBeEqualTo View.GONE

        val editCarDialog = activity.supportFragmentManager.findFragmentByTag("editCar")
        editCarDialog.shouldNotBeNull()
        editCarDialog shouldBeInstanceOf EditCarDialog::class
        editCarDialog as EditCarDialog
        val dialog = editCarDialog.dialog as AlertDialog
        val regnoView = dialog.findViewById<EditText>(R.id.regno)
        val userView = dialog.findViewById<EditText>(R.id.owner)
        regnoView.shouldNotBeNull()
        userView.shouldNotBeNull()
        regnoView.text.toString() shouldBeEqualTo adapter.cars[0].regNo
        userView.text.toString() shouldBeEqualTo adapter.cars[0].owner
    }

    @Test
    fun longClickOnListEntryTest() {
        val adapter = activity.findViewById<RecyclerView>(R.id.manage_cars_recyclerview).adapter
        adapter as SelectableCarsAdapter
        adapter.itemCount shouldBeGreaterThan 0
        activity.recyclerOnLongClick(0)
        adapter.numSelectedItems() shouldBe 1
        adapter.isSelected(0).shouldBeTrue()
        activity.getActionMode().shouldNotBeNull()
    }

    @Test
    fun clickInActionModeTest() {
        val adapter = activity.findViewById<RecyclerView>(R.id.manage_cars_recyclerview).adapter
        adapter as SelectableCarsAdapter
        adapter.itemCount shouldBeGreaterThan 1
        activity.recyclerOnLongClick(0)
        activity.recyclerOnClick(1)
        adapter.isSelected(0).shouldBeTrue()
        adapter.isSelected(1).shouldBeTrue()
        activity.recyclerOnClick(0)
        adapter.isSelected(0).shouldBeFalse()
        activity.recyclerOnClick(1)
        activity.getActionMode().shouldBeNull()

        val fab = activity.findViewById<FloatingActionButton>(R.id.manage_cards_fab)
        fab.visibility shouldBeEqualTo View.VISIBLE
    }
}
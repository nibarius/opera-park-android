package se.barsk.park

import android.annotation.SuppressLint
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
import org.robolectric.android.controller.ActivityController
import se.barsk.park.carcollection.MockCarCollection
import se.barsk.park.managecars.AddCarDialog
import se.barsk.park.managecars.ManageCarsActivity


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
        fab.visibility shouldBe View.VISIBLE
    }

    @Test
    fun carListShownWithCarsTest() {
        val manageCarsRecyclerView = activity.findViewById<RecyclerView>(R.id.manage_cars_recyclerview)
        val placeholderView = activity.findViewById<TextView>(R.id.manage_cars_placeholder)
        ParkApp.carCollection.getCars().shouldNotBeEmpty()
        manageCarsRecyclerView.visibility shouldBe View.VISIBLE
        placeholderView.visibility shouldBe View.GONE
    }

    @Test
    fun placeholderShownWithNoCarsTest() {
        (ParkApp.carCollection as MockCarCollection).replaceContent(mutableListOf())
        val manageCarsRecyclerView = activity.findViewById<RecyclerView>(R.id.manage_cars_recyclerview)
        val placeholderView = activity.findViewById<TextView>(R.id.manage_cars_placeholder)
        ParkApp.carCollection.getCars().shouldBeEmpty()
        manageCarsRecyclerView.visibility shouldBe View.GONE
        placeholderView.visibility shouldBe View.VISIBLE
    }

    @Test
    fun hideFabWhenClickingItTest() {
        val fab = activity.findViewById<FloatingActionButton>(R.id.manage_cards_fab)
        fab.performClick()
        fab.visibility shouldBe View.GONE
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
        fab.visibility shouldBe View.VISIBLE
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
}
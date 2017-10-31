package se.barsk.park

import android.content.Intent
import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import com.google.firebase.FirebaseApp
import org.amshove.kluent.shouldBeEmpty
import org.amshove.kluent.shouldBeTrue
import org.amshove.kluent.shouldEqual
import org.amshove.kluent.shouldNotBeEmpty
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.robolectric.Robolectric
import org.robolectric.Shadows
import org.robolectric.android.controller.ActivityController
import org.robolectric.shadows.ShadowLooper
import se.barsk.park.carcollection.MockCarCollection
import se.barsk.park.mainui.ParkActivity
import se.barsk.park.managecars.ManageCarsActivity
import se.barsk.park.settings.SettingsActivity


class ParkActivityTest : RobolectricTest() {
    private lateinit var controller: ActivityController<ParkActivity>
    private lateinit var activity: ParkActivity

    @Before
    fun setUp() {
        FirebaseApp.initializeApp(context())
        controller = Robolectric.buildActivity(ParkActivity::class.java)
        activity = controller.create().start().resume().visible().get()
    }

    @After
    fun tearDown() {
        // Destroy activity after every test
        controller.pause().stop().destroy()
        ParkApp.carCollection = MockCarCollection()
    }

    @Test
    fun clickSettingsInMenuTest() {
        val shadowActivity = Shadows.shadowOf(activity)
        shadowActivity.clickMenuItem(R.id.menu_settings)

        val expectedIntent = Intent(activity, SettingsActivity::class.java)
        val actualIntent = shadowActivity.nextStartedActivity
        actualIntent.component shouldEqual expectedIntent.component
    }

    @Test
    fun clickManageCarsInMenuTest() {
        val shadowActivity = Shadows.shadowOf(activity)
        shadowActivity.clickMenuItem(R.id.menu_manage_cars)

        val expectedIntent = Intent(activity, ManageCarsActivity::class.java)
        val actualIntent = shadowActivity.nextStartedActivity
        actualIntent.component shouldEqual expectedIntent.component
    }

    @Test
    fun clickAddCarButtonTest() {
        val shadowActivity = Shadows.shadowOf(activity)
        val button = activity.findViewById<Button>(R.id.no_own_cars_placeholder_button)
        button.performClick()

        val expectedIntent = Intent(activity, ManageCarsActivity::class.java)
        val actualIntent = shadowActivity.nextStartedActivity
        actualIntent.component shouldEqual expectedIntent.component
        actualIntent.extras.getBoolean(INTENT_EXTRA_ADD_CAR).shouldBeTrue()
    }

    @Test
    @org.robolectric.annotation.Config(qualifiers = "land")
    fun ownCarsPlaceholderLandscapeTest() = ownCarsPlaceholderTest()

    @Test
    @org.robolectric.annotation.Config(qualifiers = "port")
    fun ownCarsPlaceholderPortraitTest() = ownCarsPlaceholderTest()

    private fun ownCarsPlaceholderTest() {
        val ownCarsRecyclerView = activity.findViewById<RecyclerView>(R.id.own_cars_recycler_view)
        val placeholderView = activity.findViewById<LinearLayout>(R.id.own_cars_placeholder)
        ParkApp.carCollection.getCars().shouldNotBeEmpty()
        ownCarsRecyclerView.visibility shouldEqual View.VISIBLE
        placeholderView.visibility shouldEqual View.GONE

        (ParkApp.carCollection as MockCarCollection).replaceContent(mutableListOf())
        ParkApp.carCollection.getCars().shouldBeEmpty()
        ownCarsRecyclerView.visibility shouldEqual View.GONE
        placeholderView.visibility shouldEqual View.VISIBLE
    }


    // Todo: running both landscape and portrait test fails, runnin just either is fine.
    /*@Test
    @org.robolectric.annotation.Config(qualifiers = "land")
    fun parkedCarsListWithServerAndEmptyGarageLandscapeTest() = parkedCarsListWithServerAndEmptyGarageTest()*/

    @Test
    @org.robolectric.annotation.Config(qualifiers = "port")
    fun parkedCarsListWithServerAndEmptyGaragePortraitTest() = parkedCarsListWithServerAndEmptyGarageTest()

    private fun parkedCarsListWithServerAndEmptyGarageTest() {
        // First there is a placeholder with an a progress spinner
        val placeholderView = activity.findViewById<LinearLayout>(R.id.parked_cars_placeholder)
        val loadingSpinner = activity.findViewById<ProgressBar>(R.id.loading_spinner)
        val textView = activity.findViewById<TextView>(R.id.parked_cars_placeholder_text_view)
        val setServerButton = activity.findViewById<Button>(R.id.no_park_server_placeholder_button)
        placeholderView.visibility shouldEqual View.VISIBLE
        loadingSpinner.visibility shouldEqual View.VISIBLE
        textView.text.toString() shouldEqual activity.getString(R.string.updating_status_placeholder)
        setServerButton.visibility shouldEqual View.GONE

        // wait until we've gotten a response from the "server"
        ShadowLooper.runUiThreadTasksIncludingDelayedTasks()

        // After loading data the garage is empty and the empty placeholder is shown
        placeholderView.visibility shouldEqual View.VISIBLE
        loadingSpinner.visibility shouldEqual View.GONE
        textView.text.toString() shouldEqual activity.getString(R.string.parked_cars_placeholder)
        setServerButton.visibility shouldEqual View.GONE
    }

    // Tests to add:
    //  - parkedCarsListWithServerAndNotEmptyGarageTest
    //  - parkedCarsListWithoutServerAndEmptyGarageTest
    //  - parkedCarsListWithoutServerAndNotEmptyGarageTest
    //  - unable to connect, retry button test
}
package se.barsk.park

import android.content.Intent
import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import com.google.firebase.FirebaseApp
import org.amshove.kluent.*
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
import se.barsk.park.network.MockNetworkManager
import se.barsk.park.settings.SettingsActivity
import java.util.concurrent.TimeUnit


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
        ParkApp.networkManager = MockNetworkManager()
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


    @Test
    @org.robolectric.annotation.Config(qualifiers = "land")
    fun parkedCarsListHasServerEmptyLandscapeTest() = parkedCarsListHasServerEmptyTest()

    @Test
    @org.robolectric.annotation.Config(qualifiers = "port")
    fun parkedCarsListHasServerEmptyPortraitTest() = parkedCarsListHasServerEmptyTest()

    private fun parkedCarsListHasServerEmptyTest() {
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

    @Test
    @org.robolectric.annotation.Config(qualifiers = "land")
    fun parkedCarsListHasServerNotEmptyLandscapeTest() = parkedCarsListHasServerNotEmptyTest()

    @Test
    @org.robolectric.annotation.Config(qualifiers = "port")
    fun parkedCarsListHasServerNotEmptyPortraitTest() = parkedCarsListHasServerNotEmptyTest()

    private fun parkedCarsListHasServerNotEmptyTest() {
        // Create a new activity just for this test with a special network manager
        ParkApp.networkManager = MockNetworkManager(3)
        val controller = Robolectric.buildActivity(ParkActivity::class.java)
        val activity = controller.create().start().resume().visible().get()

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

        // After loading data the garage have cars and the list of cars is shown
        placeholderView.visibility shouldEqual View.GONE
        val recyclerView = activity.findViewById<RecyclerView>(R.id.parked_cars_recycler_view)
        recyclerView.visibility shouldEqual View.VISIBLE
        activity.supportActionBar?.title shouldNotEqual activity.getString(R.string.app_name)

        controller.pause().stop().destroy()
    }

    @Test
    @org.robolectric.annotation.Config(qualifiers = "land")
    fun parkedCarsListNoConnectionEmptyLandscapeTest() = parkedCarsListNoConnectionEmptyTest()

    @Test
    @org.robolectric.annotation.Config(qualifiers = "port")
    fun parkedCarsListNoConnectionEmptyPortraitTest() = parkedCarsListNoConnectionEmptyTest()

    private fun parkedCarsListNoConnectionEmptyTest() {
        // First there is a placeholder with an a progress spinner
        val placeholderView = activity.findViewById<LinearLayout>(R.id.parked_cars_placeholder)
        val loadingSpinner = activity.findViewById<ProgressBar>(R.id.loading_spinner)
        val textView = activity.findViewById<TextView>(R.id.parked_cars_placeholder_text_view)
        val setServerButton = activity.findViewById<Button>(R.id.no_park_server_placeholder_button)
        placeholderView.visibility shouldEqual View.VISIBLE
        loadingSpinner.visibility shouldEqual View.VISIBLE
        textView.text.toString() shouldEqual activity.getString(R.string.updating_status_placeholder)
        setServerButton.visibility shouldEqual View.GONE

        (ParkApp.networkManager as MockNetworkManager).hasConnection = false

        // wait until we've gotten a response from the "server"
        ShadowLooper.runUiThreadTasksIncludingDelayedTasks()

        // Then there is a connection error placeholder
        placeholderView.visibility shouldEqual View.VISIBLE
        loadingSpinner.visibility shouldEqual View.GONE
        textView.text.toString() shouldEqual activity.getString(R.string.unable_to_connect_placeholder_text, ParkApp.storageManager.getServer())
        setServerButton.visibility shouldEqual View.VISIBLE
        setServerButton.text.toString() shouldEqual activity.getString(R.string.unable_to_connect_placeholder_button)

        (ParkApp.networkManager as MockNetworkManager).hasConnection = true
        setServerButton.performClick()

        ShadowLooper.pauseMainLooper();
        Robolectric.getForegroundThreadScheduler().advanceBy(100, TimeUnit.MILLISECONDS);
        ShadowLooper.unPauseMainLooper();

        // There should now be a placeholder with a spinner
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

    @Test
    @org.robolectric.annotation.Config(qualifiers = "land")
    fun parkedCarsListNoConnectionNotEmptyLandscapeTest() = parkedCarsListNoConnectionNotEmptyTest()

    @Test
    @org.robolectric.annotation.Config(qualifiers = "port")
    fun parkedCarsListNoConnectionNotEmptyPortraitTest() = parkedCarsListNoConnectionNotEmptyTest()

    private fun parkedCarsListNoConnectionNotEmptyTest() {
        // Create a new activity just for this test with a special network manager
        ParkApp.networkManager = MockNetworkManager(3)
        val controller = Robolectric.buildActivity(ParkActivity::class.java)
        val activity = controller.create().start().resume().visible().get()

        // First there is a placeholder with an a progress spinner
        val placeholderView = activity.findViewById<LinearLayout>(R.id.parked_cars_placeholder)
        val loadingSpinner = activity.findViewById<ProgressBar>(R.id.loading_spinner)
        val textView = activity.findViewById<TextView>(R.id.parked_cars_placeholder_text_view)
        val setServerButton = activity.findViewById<Button>(R.id.no_park_server_placeholder_button)
        placeholderView.visibility shouldEqual View.VISIBLE
        loadingSpinner.visibility shouldEqual View.VISIBLE
        textView.text.toString() shouldEqual activity.getString(R.string.updating_status_placeholder)
        setServerButton.visibility shouldEqual View.GONE

        (ParkApp.networkManager as MockNetworkManager).hasConnection = false

        // wait until we've gotten a response from the "server"
        ShadowLooper.runUiThreadTasksIncludingDelayedTasks()

        // Then there is a connection error placeholder
        placeholderView.visibility shouldEqual View.VISIBLE
        loadingSpinner.visibility shouldEqual View.GONE
        textView.text.toString() shouldEqual activity.getString(R.string.unable_to_connect_placeholder_text, ParkApp.storageManager.getServer())
        setServerButton.visibility shouldEqual View.VISIBLE
        setServerButton.text.toString() shouldEqual activity.getString(R.string.unable_to_connect_placeholder_button)

        (ParkApp.networkManager as MockNetworkManager).hasConnection = true
        setServerButton.performClick()

        ShadowLooper.pauseMainLooper();
        Robolectric.getForegroundThreadScheduler().advanceBy(100, TimeUnit.MILLISECONDS);
        ShadowLooper.unPauseMainLooper();

        // There should now be a placeholder with a spinner
        placeholderView.visibility shouldEqual View.VISIBLE
        loadingSpinner.visibility shouldEqual View.VISIBLE
        textView.text.toString() shouldEqual activity.getString(R.string.updating_status_placeholder)
        setServerButton.visibility shouldEqual View.GONE

        // wait until we've gotten a response from the "server"
        ShadowLooper.runUiThreadTasksIncludingDelayedTasks()

        // After loading data the garage have cars and the list of cars is shown
        placeholderView.visibility shouldEqual View.GONE
        val recyclerView = activity.findViewById<RecyclerView>(R.id.parked_cars_recycler_view)
        recyclerView.visibility shouldEqual View.VISIBLE
        activity.supportActionBar?.title shouldNotEqual activity.getString(R.string.app_name)

        controller.pause().stop().destroy()
    }
    // Tests to add:
    //  - parkedCarsListWithoutServerAndEmptyGarageTest
    //  - parkedCarsListWithoutServerAndNotEmptyGarageTest
}
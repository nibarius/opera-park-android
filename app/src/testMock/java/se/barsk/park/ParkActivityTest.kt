package se.barsk.park

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import com.google.firebase.FirebaseApp
import kotlinx.android.synthetic.main.content_park.*
import kotlinx.android.synthetic.main.own_car_entry.view.*
import kotlinx.android.synthetic.main.specify_server_dialog.*
import org.amshove.kluent.*
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.robolectric.Robolectric
import org.robolectric.Shadows
import org.robolectric.android.controller.ActivityController
import org.robolectric.shadows.ShadowLooper
import se.barsk.park.datatypes.MockCarCollection
import se.barsk.park.mainui.MustSignInDialog
import se.barsk.park.mainui.OwnCarListEntry
import se.barsk.park.mainui.ParkActivity
import se.barsk.park.mainui.SpecifyServerDialog
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
        activity.no_own_cars_placeholder_button.performClick()

        val expectedIntent = Intent(activity, ManageCarsActivity::class.java)
        val actualIntent = shadowActivity.nextStartedActivity
        actualIntent.component shouldEqual expectedIntent.component
        actualIntent.extras.shouldNotBeNull()
        val extras = actualIntent.extras as Bundle
        extras.getBoolean(INTENT_EXTRA_ADD_CAR).shouldBeTrue()
    }

    @Test
    @org.robolectric.annotation.Config(qualifiers = "land")
    fun ownCarsPlaceholderLandscapeTest() = ownCarsPlaceholderTest()

    @Test
    @org.robolectric.annotation.Config(qualifiers = "port")
    fun ownCarsPlaceholderPortraitTest() = ownCarsPlaceholderTest()

    private fun ownCarsPlaceholderTest() {
        ParkApp.carCollection.getCars().shouldNotBeEmpty()
        activity.own_cars_recycler_view.shouldBeVisible()
        activity.own_cars_placeholder.shouldBeGone()

        (ParkApp.carCollection as MockCarCollection).replaceContent(mutableListOf())
        ParkApp.carCollection.getCars().shouldBeEmpty()
        activity.own_cars_recycler_view.shouldBeGone()
        activity.own_cars_placeholder.shouldBeVisible()
    }


    @Test
    @org.robolectric.annotation.Config(qualifiers = "land")
    fun parkedCarsListHasServerEmptyLandscapeTest() = parkedCarsListHasServerEmptyTest()

    @Test
    @org.robolectric.annotation.Config(qualifiers = "port")
    fun parkedCarsListHasServerEmptyPortraitTest() = parkedCarsListHasServerEmptyTest()

    private fun parkedCarsListHasServerEmptyTest() {
        // First there is a placeholder with an a progress spinner
        loadingPlaceholderShown(activity)

        // wait until we've gotten a response from the "server"
        ShadowLooper.runUiThreadTasksIncludingDelayedTasks()

        // After loading data the garage is empty and the empty placeholder is shown
        emptyGaragePlaceholderShown(activity)
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
        loadingPlaceholderShown(activity)

        // wait until we've gotten a response from the "server"
        ShadowLooper.runUiThreadTasksIncludingDelayedTasks()

        // After loading data the garage have cars and the list of cars is shown
        listOfParkedCarsShown(activity)

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
        loadingPlaceholderShown(activity)

        (ParkApp.networkManager as MockNetworkManager).hasConnection = false

        // wait until we've gotten a response from the "server"
        ShadowLooper.runUiThreadTasksIncludingDelayedTasks()

        // Then there is a connection error placeholder
        connectionErrorPlaceholderShown(activity)

        (ParkApp.networkManager as MockNetworkManager).hasConnection = true
        activity.no_park_server_placeholder_button.performClick()

        ShadowLooper.pauseMainLooper()
        Robolectric.getForegroundThreadScheduler().advanceBy(100, TimeUnit.MILLISECONDS)
        ShadowLooper.unPauseMainLooper()

        // There should now be a placeholder with a spinner
        loadingPlaceholderShown(activity)

        // wait until we've gotten a response from the "server"
        ShadowLooper.runUiThreadTasksIncludingDelayedTasks()

        // After loading data the garage is empty and the empty placeholder is shown
        emptyGaragePlaceholderShown(activity)
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
        loadingPlaceholderShown(activity)

        (ParkApp.networkManager as MockNetworkManager).hasConnection = false

        // wait until we've gotten a response from the "server"
        ShadowLooper.runUiThreadTasksIncludingDelayedTasks()

        // Then there is a connection error placeholder
        connectionErrorPlaceholderShown(activity)

        (ParkApp.networkManager as MockNetworkManager).hasConnection = true
        activity.no_park_server_placeholder_button.performClick()

        ShadowLooper.pauseMainLooper()
        Robolectric.getForegroundThreadScheduler().advanceBy(100, TimeUnit.MILLISECONDS)
        ShadowLooper.unPauseMainLooper()

        // There should now be a placeholder with a spinner
        loadingPlaceholderShown(activity)

        // wait until we've gotten a response from the "server"
        ShadowLooper.runUiThreadTasksIncludingDelayedTasks()

        // After loading data the garage have cars and the list of cars is shown
        listOfParkedCarsShown(activity)

        controller.pause().stop().destroy()
    }

    @Test
    @org.robolectric.annotation.Config(qualifiers = "land")
    fun parkedCarsListResumeNotEmptyLandscapeTest() = parkedCarsListResumeNotEmptyTest()

    @Test
    @org.robolectric.annotation.Config(qualifiers = "port")
    fun parkedCarsListResumeNotEmptyPortraitTest() = parkedCarsListResumeNotEmptyTest()

    private fun parkedCarsListResumeNotEmptyTest() {
        // Create a new activity just for this test with a special network manager
        ParkApp.networkManager = MockNetworkManager(3)
        val controller = Robolectric.buildActivity(ParkActivity::class.java)
        val activity = controller.create().start().resume().visible().get()

        // First there is a placeholder with an a progress spinner
        loadingPlaceholderShown(activity)

        // wait until we've gotten a response from the "server"
        ShadowLooper.runUiThreadTasksIncludingDelayedTasks()

        // After loading data the garage have cars and the list of cars is shown
        listOfParkedCarsShown(activity)

        controller.pause().resume().visible()

        listOfParkedCarsShown(activity)

        controller.pause().stop().destroy()
    }

    @Test
    @org.robolectric.annotation.Config(qualifiers = "land")
    fun parkedCarsListActivityDestroyResumeNotEmptyLandscape() = parkedCarsListActivityDestroyResumeNotEmptyTest()

    @Test
    @org.robolectric.annotation.Config(qualifiers = "port")
    fun parkedCarsListActivityDestroyResumeNotEmptyPortrait() = parkedCarsListActivityDestroyResumeNotEmptyTest()

    // Test that the pause, stop, destroy, create, start, resume lifecycle works as it should
    // This happens for example when minimizing the app using hardware back
    private fun parkedCarsListActivityDestroyResumeNotEmptyTest() {
        // Create a new activity just for this test with a special network manager
        ParkApp.networkManager = MockNetworkManager(3)
        val controller = Robolectric.buildActivity(ParkActivity::class.java)
        val activity = controller.create().start().resume().visible().get()

        // First there is a placeholder with an a progress spinner
        loadingPlaceholderShown(activity)

        // wait until we've gotten a response from the "server"
        ShadowLooper.runUiThreadTasksIncludingDelayedTasks()

        // After loading data the garage have cars and the list of cars is shown
        listOfParkedCarsShown(activity)

        // Press HW back to close the app, then open the app again
        controller.pause().stop().destroy()
        val controller2 = Robolectric.buildActivity(ParkActivity::class.java)
        val newActivity = controller2.create().start().resume().visible().get()

        // After coming back to the app is a placeholder with an a progress spinner
        loadingPlaceholderShown(newActivity)

        // wait until we've gotten a response from the "server"
        ShadowLooper.runUiThreadTasksIncludingDelayedTasks()

        // After loading data the garage have cars and the list of cars is shown
        listOfParkedCarsShown(newActivity)

        controller2.pause().stop().destroy()
    }

    @Test
    @org.robolectric.annotation.Config(qualifiers = "land")
    fun parkedCarsListNoServerEmptyLandscapeTest() = parkedCarsListNoServerEmptyTest()

    @Test
    @org.robolectric.annotation.Config(qualifiers = "port")
    fun parkedCarsListNoServerEmptyPortraitTest() = parkedCarsListNoServerEmptyTest()

    private fun parkedCarsListNoServerEmptyTest() {
        ParkApp.storageManager.setServer("")
        val controller = Robolectric.buildActivity(ParkActivity::class.java)
        val activity = controller.create().start().resume().visible().get()

        // First there is a placeholder with an a progress spinner
        noServerPlaceholderShown(activity)

        activity.no_park_server_placeholder_button.performClick()

        val specifyServerDialog = activity.supportFragmentManager.findFragmentByTag("specifyServer")
        specifyServerDialog.shouldNotBeNull()
        specifyServerDialog shouldBeInstanceOf SpecifyServerDialog::class
        specifyServerDialog as SpecifyServerDialog
        val dialog = specifyServerDialog.dialog as AlertDialog
        dialog.server_url_input.setText("http://park.example.com/")
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).performClick()


        ShadowLooper.pauseMainLooper()
        Robolectric.getForegroundThreadScheduler().advanceBy(100, TimeUnit.MILLISECONDS)
        ShadowLooper.unPauseMainLooper()

        // There should now be a placeholder with a spinner
        loadingPlaceholderShown(activity)

        // wait until we've gotten a response from the "server"
        ShadowLooper.runUiThreadTasksIncludingDelayedTasks()

        // After loading data the garage have cars and the list of cars is shown
        emptyGaragePlaceholderShown(activity)
        controller.pause().stop().destroy()

    }

    @Test
    @org.robolectric.annotation.Config(qualifiers = "land")
    fun parkedCarsListNoServerNotEmptyLandscapeTest() = parkedCarsListNoServerNotEmptyTest()

    @Test
    @org.robolectric.annotation.Config(qualifiers = "port")
    fun parkedCarsListNoServerNotEmptyPortraitTest() = parkedCarsListNoServerNotEmptyTest()

    private fun parkedCarsListNoServerNotEmptyTest() {
        ParkApp.storageManager.setServer("")
        ParkApp.networkManager = MockNetworkManager(3)
        val controller = Robolectric.buildActivity(ParkActivity::class.java)
        val activity = controller.create().start().resume().visible().get()

        // First there is a placeholder with an a progress spinner
        noServerPlaceholderShown(activity)

        activity.no_park_server_placeholder_button.performClick()

        val specifyServerDialog = activity.supportFragmentManager.findFragmentByTag("specifyServer")
        specifyServerDialog.shouldNotBeNull()
        specifyServerDialog shouldBeInstanceOf SpecifyServerDialog::class
        specifyServerDialog as SpecifyServerDialog
        val dialog = specifyServerDialog.dialog as AlertDialog
        dialog.shouldNotBeNull()
        dialog.server_url_input.setText("http://park.example.com/")
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).performClick()


        ShadowLooper.pauseMainLooper()
        Robolectric.getForegroundThreadScheduler().advanceBy(100, TimeUnit.MILLISECONDS)
        ShadowLooper.unPauseMainLooper()

        // There should now be a placeholder with a spinner
        loadingPlaceholderShown(activity)

        // wait until we've gotten a response from the "server"
        ShadowLooper.runUiThreadTasksIncludingDelayedTasks()

        // After loading data the garage have cars and the list of cars is shown
        listOfParkedCarsShown(activity)
        controller.pause().stop().destroy()

    }


    @Test
    @org.robolectric.annotation.Config(qualifiers = "land")
    fun parkCarsLandscapeTest() = parkCarsTest()

    @Test
    @org.robolectric.annotation.Config(qualifiers = "port")
    fun parkCarsPortraitTest() = parkCarsTest()

    private fun parkCarsTest() {
        // Create a new activity just for this test with a special network manager
        ParkApp.networkManager = MockNetworkManager(5)
        val controller = Robolectric.buildActivity(ParkActivity::class.java)
        val activity = controller.create().start().resume().visible().get()

        // wait until we've gotten a response from the "server"
        ShadowLooper.runUiThreadTasksIncludingDelayedTasks()
        // After loading data the garage have cars and the list of cars is shown
        listOfParkedCarsShown(activity)

        val car1 = activity.own_cars_recycler_view.findViewHolderForAdapterPosition(0)?.itemView
        val car2 = activity.own_cars_recycler_view.findViewHolderForAdapterPosition(1)?.itemView
        car1.shouldNotBeNull()
        car2.shouldNotBeNull()
        car1 as OwnCarListEntry
        car2 as OwnCarListEntry

        carIsNotParked(car1)
        carIsNotParked(car2)
        activity.supportActionBar?.title shouldEqual activity.resources.getQuantityString(R.plurals.park_status_free, 1)

        // Park a car
        car1.performClick()
        ShadowLooper.runUiThreadTasksIncludingDelayedTasks()
        carIsParked(car1)
        carCanBePutOnWaitList(car2)
        activity.supportActionBar?.title shouldEqual activity.getString(R.string.park_status_full)

        // Unpark it again
        car1.performClick()
        ShadowLooper.runUiThreadTasksIncludingDelayedTasks()
        carIsNotParked(car1)
        carIsNotParked(car2)
        activity.supportActionBar?.title shouldEqual activity.resources.getQuantityString(R.plurals.park_status_free, 1)

        controller.pause().stop().destroy()
    }

    @Test
    @org.robolectric.annotation.Config(qualifiers = "land")
    fun waitListNotLoggedInLandscapeTest() = waitListNotLoggedInTest()

    @Test
    @org.robolectric.annotation.Config(qualifiers = "port")
    fun waitListNotLoggedInPortraitTest() = waitListNotLoggedInTest()

    private fun waitListNotLoggedInTest() {
        // Create a new activity just for this test with a special network manager
        ParkApp.networkManager = MockNetworkManager(6)
        val controller = Robolectric.buildActivity(ParkActivity::class.java)
        val activity = controller.create().start().resume().visible().get()
        ParkApp.theUser.signOut(activity)

        // wait until we've gotten a response from the "server"
        ShadowLooper.runUiThreadTasksIncludingDelayedTasks()
        // After loading data the garage have cars and the list of cars is shown
        listOfParkedCarsShown(activity)

        val car = activity.own_cars_recycler_view.findViewHolderForAdapterPosition(1)?.itemView
        car.shouldNotBeNull()
        car as OwnCarListEntry

        activity.supportActionBar?.title shouldEqual activity.getString(R.string.park_status_full)

        // Register on the wait list while not logged in should trigger the sign in dialog
        car.performClick()
        ShadowLooper.runUiThreadTasksIncludingDelayedTasks()

        val dialog = activity.supportFragmentManager.findFragmentByTag("signIn")
        dialog.shouldNotBeNull()
        dialog shouldBeInstanceOf MustSignInDialog::class

        controller.pause().stop().destroy()
    }

    @Test
    @org.robolectric.annotation.Config(qualifiers = "land")
    fun waitListLandscapeTest() = waitListTest()

    @Test
    @org.robolectric.annotation.Config(qualifiers = "port")
    fun waitListPortraitTest() = waitListTest()

    private fun waitListTest() {
        // Create a new activity just for this test with a special network manager
        ParkApp.networkManager = MockNetworkManager(6)
        val controller = Robolectric.buildActivity(ParkActivity::class.java)
        val activity = controller.create().start().resume().visible().get()

        // wait until we've gotten a response from the "server"
        ShadowLooper.runUiThreadTasksIncludingDelayedTasks()
        // After loading data the garage have cars and the list of cars is shown
        listOfParkedCarsShown(activity)

        val car = activity.own_cars_recycler_view.findViewHolderForAdapterPosition(1)?.itemView
        car.shouldNotBeNull()
        car as OwnCarListEntry
        carCanBePutOnWaitList(car)
        activity.supportActionBar?.title shouldEqual activity.getString(R.string.park_status_full)

        // Register on the wait list
        car.performClick()
        ShadowLooper.runUiThreadTasksIncludingDelayedTasks()
        carIsOnWaitList(car)

        //Remove from the wait list
        car.performClick()
        ShadowLooper.runUiThreadTasksIncludingDelayedTasks()
        carCanBePutOnWaitList(car)

        controller.pause().stop().destroy()
    }

    @Test
    @org.robolectric.annotation.Config(qualifiers = "land")
    fun parkCarsEmptyLandscapeTest() = parkCarsEmptyTest()

    @Test
    @org.robolectric.annotation.Config(qualifiers = "port")
    fun parkCarsEmptyPortraitTest() = parkCarsEmptyTest()

    private fun parkCarsEmptyTest() {
        // wait until we've gotten a response from the "server"
        ShadowLooper.runUiThreadTasksIncludingDelayedTasks()
        // After loading data the garage have cars and the list of cars is shown
        emptyGaragePlaceholderShown(activity)

        val car1 = activity.own_cars_recycler_view.findViewHolderForAdapterPosition(0)?.itemView
        val car2 = activity.own_cars_recycler_view.findViewHolderForAdapterPosition(1)?.itemView
        car1.shouldNotBeNull()
        car2.shouldNotBeNull()
        car1 as OwnCarListEntry
        car2 as OwnCarListEntry

        carIsNotParked(car1)
        carIsNotParked(car2)
        activity.supportActionBar?.title shouldEqual activity.getString(R.string.app_name)

        // Park a car
        car1.performClick()
        ShadowLooper.runUiThreadTasksIncludingDelayedTasks()
        carIsParked(car1)
        carIsNotParked(car2)
        activity.supportActionBar?.title shouldEqual activity.resources.getQuantityString(R.plurals.park_status_free, 5, 5)
        listOfParkedCarsShown(activity)

        // Unpark it again
        car1.performClick()
        ShadowLooper.runUiThreadTasksIncludingDelayedTasks()
        carIsNotParked(car1)
        carIsNotParked(car2)
        activity.supportActionBar?.title shouldEqual activity.getString(R.string.app_name)
        emptyGaragePlaceholderShown(activity)
    }


    @Test
    @org.robolectric.annotation.Config(qualifiers = "land")
    fun updateOwnCarsListOnServerStateChangeLandscapeTest() = updateOwnCarsListOnServerStateChangeTest()

    @Test
    @org.robolectric.annotation.Config(qualifiers = "port")
    fun updateOwnCarsListOnServerStateChangePortraitTest() = updateOwnCarsListOnServerStateChangeTest()

    private fun updateOwnCarsListOnServerStateChangeTest() {
        // Create a new activity just for this test with a special network manager
        val almostFull = MockNetworkManager(5)
        val full = MockNetworkManager(6)
        ParkApp.networkManager = almostFull
        val controller = Robolectric.buildActivity(ParkActivity::class.java)
        val activity = controller.create().start().resume().visible().get()

        // wait until we've gotten a response from the "server"
        ShadowLooper.runUiThreadTasksIncludingDelayedTasks()
        // After loading data the garage have cars and the list of cars is shown
        listOfParkedCarsShown(activity)

        val car = activity.own_cars_recycler_view.findViewHolderForAdapterPosition(1)?.itemView
        car.shouldNotBeNull()
        car as OwnCarListEntry

        carIsNotParked(car)
        activity.supportActionBar?.title shouldEqual activity.resources.getQuantityString(R.plurals.park_status_free, 1)
        /* TODO: Fix the swipe to refresh test
                 Robolectric doesn't seem to support the androidx SwipeRefreshLayout yet
                 so doing a test that relies on swipe to refresh is currently difficult.
                 Hopefully this test can be restored at some point in the future

        // Refresh list of parked cars from server, now the garage is full
        ParkApp.networkManager = full
        val pullToRefreshView = Shadows.shadowOf(activity.parked_cars_pull_to_refresh) as ShadowSwipeRefreshLayout
        pullToRefreshView.onRefreshListener.onRefresh()
        ShadowLooper.runUiThreadTasksIncludingDelayedTasks()
        carCanBePutOnWaitList(car)
        activity.supportActionBar?.title shouldEqual activity.getString(R.string.park_status_full)

        // Refresh list of parked cars from server, there is one spot free again
        ParkApp.networkManager = almostFull
        pullToRefreshView.onRefreshListener.onRefresh()
        ShadowLooper.runUiThreadTasksIncludingDelayedTasks()
        carIsNotParked(car)
        activity.supportActionBar?.title shouldEqual activity.resources.getQuantityString(R.plurals.park_status_free, 1)
*/
        controller.pause().stop().destroy()
    }

    private fun loadingPlaceholderShown(activity: ParkActivity) {
        activity.parked_cars_placeholder.shouldBeVisible()
        activity.loading_spinner.shouldBeVisible()
        activity.no_park_server_placeholder_button.shouldBeGone()
        activity.parked_cars_placeholder_text_view.text.toString() shouldEqual
                activity.getString(R.string.updating_status_placeholder)
    }

    private fun emptyGaragePlaceholderShown(activity: ParkActivity) {
        activity.parked_cars_placeholder.shouldBeVisible()
        activity.loading_spinner.shouldBeGone()
        activity.no_park_server_placeholder_button.shouldBeGone()
        activity.parked_cars_placeholder_text_view.text.toString() shouldEqual
                activity.getString(R.string.parked_cars_placeholder)
    }

    private fun listOfParkedCarsShown(activity: ParkActivity) {
        activity.parked_cars_placeholder.shouldBeGone()
        activity.parked_cars_recycler_view.shouldBeVisible()
        activity.supportActionBar?.title shouldNotEqual activity.getString(R.string.app_name)
    }

    private fun connectionErrorPlaceholderShown(activity: ParkActivity) {
        activity.parked_cars_placeholder.shouldBeVisible()
        activity.loading_spinner.shouldBeGone()
        activity.no_park_server_placeholder_button.shouldBeVisible()
        activity.parked_cars_placeholder_text_view.text.toString() shouldEqual
                activity.getString(R.string.unable_to_connect_placeholder_text, ParkApp.storageManager.getServer())
        activity.no_park_server_placeholder_button.text.toString() shouldEqual
                activity.getString(R.string.unable_to_connect_placeholder_button)
    }

    private fun noServerPlaceholderShown(activity: ParkActivity) {
        activity.parked_cars_placeholder.shouldBeVisible()
        activity.loading_spinner.shouldBeGone()
        activity.no_park_server_placeholder_button.shouldBeVisible()
        activity.parked_cars_placeholder_text_view.text.toString() shouldEqual
                activity.getString(R.string.no_server_placeholder_text, ParkApp.storageManager.getServer())
        activity.no_park_server_placeholder_button.text.toString() shouldEqual
                activity.getString(R.string.no_server_placeholder_button)
    }

    private fun carIsNotParked(car: OwnCarListEntry) {
        val button = car.park_button
        button.isEnabled.shouldBeTrue()
        button.text.shouldStartWith(context().getString(R.string.park_label).toUpperCase())
    }

    private fun carIsParked(car: OwnCarListEntry) {
        val button = car.park_button
        button.isEnabled.shouldBeTrue()
        button.text.shouldStartWith(context().getString(R.string.unpark_label).toUpperCase())
    }

    private fun carCanBePutOnWaitList(car: OwnCarListEntry) {
        val button = car.park_button
        button.isEnabled.shouldBeTrue()
        button.text.shouldStartWith(context().getString(R.string.wait_label).toUpperCase())
    }

    private fun carIsOnWaitList(car: OwnCarListEntry) {
        val button = car.park_button
        button.isEnabled.shouldBeTrue()
        button.text.shouldStartWith(context().getString(R.string.stop_waiting_label).toUpperCase())
    }

    private fun View.shouldBeGone() {
        this.visibility shouldEqual View.GONE
    }

    private fun View.shouldBeVisible() {
        this.visibility shouldEqual View.VISIBLE
    }
}


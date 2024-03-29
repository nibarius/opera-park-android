package se.barsk.park

import android.content.Intent
import android.os.Bundle
import android.os.Looper.getMainLooper
import android.view.View
import androidx.appcompat.app.AlertDialog
import com.google.firebase.FirebaseApp
import org.amshove.kluent.*
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.robolectric.Robolectric
import org.robolectric.Shadows
import org.robolectric.android.controller.ActivityController
import org.robolectric.annotation.LooperMode
import org.robolectric.shadows.ShadowLooper
import se.barsk.park.datatypes.MockCarCollection
import se.barsk.park.datatypes.ParkedCar
import se.barsk.park.mainui.OwnCarListEntry
import se.barsk.park.mainui.ParkActivity
import se.barsk.park.mainui.SpecifyServerDialog
import se.barsk.park.managecars.ManageCarsActivity
import se.barsk.park.network.MockNetworkManager
import se.barsk.park.settings.SettingsActivity
import java.util.*

@LooperMode(LooperMode.Mode.PAUSED)
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
        actualIntent.component shouldBeEqualTo expectedIntent.component
    }

    @Test
    fun clickManageCarsInMenuTest() {
        val shadowActivity = Shadows.shadowOf(activity)
        shadowActivity.clickMenuItem(R.id.menu_manage_cars)

        val expectedIntent = Intent(activity, ManageCarsActivity::class.java)
        val actualIntent = shadowActivity.nextStartedActivity
        actualIntent.component shouldBeEqualTo expectedIntent.component
    }

    @Test
    fun clickAddCarButtonTest() {
        val shadowActivity = Shadows.shadowOf(activity)
        activity.binding.content.noOwnCarsPlaceholderButton.performClick()
        Shadows.shadowOf(getMainLooper()).idle()

        val expectedIntent = Intent(activity, ManageCarsActivity::class.java)
        val actualIntent = shadowActivity.nextStartedActivity
        actualIntent.component shouldBeEqualTo expectedIntent.component
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
        activity.binding.content.ownCarsRecyclerView.shouldBeVisible()
        activity.binding.content.ownCarsPlaceholder.shouldBeGone()

        (ParkApp.carCollection as MockCarCollection).replaceContent(mutableListOf())
        ParkApp.carCollection.getCars().shouldBeEmpty()
        activity.binding.content.ownCarsRecyclerView.shouldBeGone()
        activity.binding.content.ownCarsPlaceholder.shouldBeVisible()
    }


    @Test
    @org.robolectric.annotation.Config(qualifiers = "land")
    fun parkedCarsListHasServerEmptyLandscapeTest() = parkedCarsListHasServerEmptyTest()

    @Test
    @org.robolectric.annotation.Config(qualifiers = "port")
    fun parkedCarsListHasServerEmptyPortraitTest() = parkedCarsListHasServerEmptyTest()

    private fun parkedCarsListHasServerEmptyTest() {
        // Create a new activity just for this test with a special network manager
        ParkApp.networkManager = MockNetworkManager(0)
        val controller = Robolectric.buildActivity(ParkActivity::class.java)
        val activity = controller.create().start().resume().visible().get()

        // First there is a placeholder with an a progress spinner
        loadingPlaceholderShown(activity)

        // wait until we've gotten a response from the "server"
        ShadowLooper.runUiThreadTasksIncludingDelayedTasks()

        // After loading data the garage is empty and the empty placeholder is shown
        emptyGaragePlaceholderShown(activity)

        controller.pause().stop().destroy()
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
        // Create a new activity just for this test with a special network manager
        ParkApp.networkManager = MockNetworkManager(0)
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
        activity.binding.content.noParkServerPlaceholderButton.performClick()
        Shadows.shadowOf(getMainLooper()).idle()

        // There should now be a placeholder with a spinner
        loadingPlaceholderShown(activity)

        // wait until we've gotten a response from the "server"
        ShadowLooper.runUiThreadTasksIncludingDelayedTasks()

        // After loading data the garage is empty and the empty placeholder is shown
        emptyGaragePlaceholderShown(activity)
        controller.pause().stop().destroy()
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
        activity.binding.content.noParkServerPlaceholderButton.performClick()
        Shadows.shadowOf(getMainLooper()).idle()

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
    fun difficultParkedCarsLandscapeTest() = difficultParkedCarsTest()

    @Test
    @org.robolectric.annotation.Config(qualifiers = "port")
    fun difficultParkedCarsPortraitTest() = difficultParkedCarsTest()

    private fun difficultParkedCarsTest() {
        val parkedCars = mutableListOf(
            ParkedCar("", "", "2017-10-01 08:05:15"), // Empty strings
            ParkedCar("  \t  ", "  \t  ", "2017-10-01 08:16:55"), // whitespace
            ParkedCar("あいうえお", "名前", "2017-10-01 08:21:06"), // non ascii
            ParkedCar(
                "\r\n\u000b\u000c\u0085\u00a0\u3000",
                "\n\r\u000B\u000c\u00a0\u3000",
                "2017-10-01 08:29:53"
            ), // more whitespace
            ParkedCar(
                "\u180e\u200b\u200c\u200d\u2060\ufeff",
                "\u180e\u200b\u200c\u200d\u2060\ufeff",
                "2017-10-01 09:01:33"
            ) // whitespace related, non-whitespace characters
        )

        // Create a new activity just for this test with a special network manager
        ParkApp.networkManager = MockNetworkManager(3, parkedCars)
        val controller = Robolectric.buildActivity(ParkActivity::class.java)
        val activity = controller.create().start().resume().visible().get()

        // First there is a placeholder with an a progress spinner
        loadingPlaceholderShown(activity)

        // wait until we've gotten a response from the "server"
        ShadowLooper.runUiThreadTasksIncludingDelayedTasks()

        // After loading data the garage have cars and the list of cars is shown
        // without crashing due to irregular names or licence plates
        listOfParkedCarsShown(activity)
        controller.pause().stop().destroy()
    }

    @Test
    @org.robolectric.annotation.Config(qualifiers = "land")
    fun parkedCarsListActivityDestroyResumeNotEmptyLandscape() =
        parkedCarsListActivityDestroyResumeNotEmptyTest()

    @Test
    @org.robolectric.annotation.Config(qualifiers = "port")
    fun parkedCarsListActivityDestroyResumeNotEmptyPortrait() =
        parkedCarsListActivityDestroyResumeNotEmptyTest()

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
        ParkApp.networkManager = MockNetworkManager(0)
        val controller = Robolectric.buildActivity(ParkActivity::class.java)
        val activity = controller.create().start().resume().visible().get()

        // First there is a placeholder with an a progress spinner
        noServerPlaceholderShown(activity)

        activity.binding.content.noParkServerPlaceholderButton.performClick()
        Shadows.shadowOf(getMainLooper()).idle()

        val specifyServerDialog = activity.supportFragmentManager.findFragmentByTag("specifyServer")
        specifyServerDialog.shouldNotBeNull()
        specifyServerDialog shouldBeInstanceOf SpecifyServerDialog::class
        specifyServerDialog as SpecifyServerDialog
        val dialog = specifyServerDialog.dialog as AlertDialog
        specifyServerDialog.editText.setText("http://park.example.com/")
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).performClick()
        Shadows.shadowOf(getMainLooper()).idle()

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

        activity.binding.content.noParkServerPlaceholderButton.performClick()
        Shadows.shadowOf(getMainLooper()).idle()

        val specifyServerDialog = activity.supportFragmentManager.findFragmentByTag("specifyServer")
        specifyServerDialog.shouldNotBeNull()
        specifyServerDialog shouldBeInstanceOf SpecifyServerDialog::class
        specifyServerDialog as SpecifyServerDialog
        val dialog = specifyServerDialog.dialog as AlertDialog
        dialog.shouldNotBeNull()
        specifyServerDialog.editText.setText("http://park.example.com/")
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).performClick()
        Shadows.shadowOf(getMainLooper()).idle()

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
        val car1 = activity.binding.content.ownCarsRecyclerView.findViewHolderForAdapterPosition(0)?.itemView
        val car2 = activity.binding.content.ownCarsRecyclerView.findViewHolderForAdapterPosition(1)?.itemView
        car1.shouldNotBeNull()
        car2.shouldNotBeNull()
        car1 as OwnCarListEntry
        car2 as OwnCarListEntry

        carIsNotParked(car1)
        carIsNotParked(car2)
        activity.supportActionBar?.title.toString() shouldBeEqualTo activity.resources.getString(R.string.park_status_one_free)

        // Park a car
        car1.performClick()
        ShadowLooper.runUiThreadTasksIncludingDelayedTasks()
        carIsParked(car1)
        // Todo: restore waitlist part when there is a backend with support for it
        // carCanBePutOnWaitList(car2)
        parkCarButtonIsDisabled(car2)
        activity.supportActionBar?.title.toString() shouldBeEqualTo activity.getString(R.string.park_status_full)

        // Unpark it again
        car1.performClick()
        ShadowLooper.runUiThreadTasksIncludingDelayedTasks()
        carIsNotParked(car1)
        carIsNotParked(car2)
        activity.supportActionBar?.title.toString() shouldBeEqualTo activity.resources.getString(R.string.park_status_one_free)

        controller.pause().stop().destroy()
    }

    /* Test disabled until there is a backend and wait list support
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

        activity.supportActionBar?.title.toString() shouldBeEqualTo activity.getString(R.string.park_status_full)

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
        ParkApp.theUser.isSignedIn shouldBe true

        // wait until we've gotten a response from the "server"
        ShadowLooper.runUiThreadTasksIncludingDelayedTasks()
        // After loading data the garage have cars and the list of cars is shown
        listOfParkedCarsShown(activity)

        val car = activity.own_cars_recycler_view.findViewHolderForAdapterPosition(1)?.itemView
        car.shouldNotBeNull()
        car as OwnCarListEntry
        carCanBePutOnWaitList(car)
        activity.supportActionBar?.title.toString() shouldBeEqualTo activity.getString(R.string.park_status_full)

        // Register on the wait list
        car.performClick()
        ShadowLooper.runUiThreadTasksIncludingDelayedTasks()
        carIsOnWaitList(car)

        //Remove from the wait list
        car.performClick()
        ShadowLooper.runUiThreadTasksIncludingDelayedTasks()
        carCanBePutOnWaitList(car)

        controller.pause().stop().destroy()
    }*/

    @Test
    @org.robolectric.annotation.Config(qualifiers = "land")
    fun parkCarsEmptyLandscapeTest() = parkCarsEmptyTest()

    @Test
    @org.robolectric.annotation.Config(qualifiers = "port")
    fun parkCarsEmptyPortraitTest() = parkCarsEmptyTest()

    private fun parkCarsEmptyTest() {
        ParkApp.networkManager = MockNetworkManager(0)
        val controller = Robolectric.buildActivity(ParkActivity::class.java)
        val activity = controller.create().start().resume().visible().get()

        // wait until we've gotten a response from the "server"
        ShadowLooper.runUiThreadTasksIncludingDelayedTasks()
        // After loading data the garage have cars and the list of cars is shown
        emptyGaragePlaceholderShown(activity)

        val car1 = activity.binding.content.ownCarsRecyclerView.findViewHolderForAdapterPosition(0)?.itemView
        val car2 = activity.binding.content.ownCarsRecyclerView.findViewHolderForAdapterPosition(1)?.itemView
        car1.shouldNotBeNull()
        car2.shouldNotBeNull()
        car1 as OwnCarListEntry
        car2 as OwnCarListEntry

        carIsNotParked(car1)
        carIsNotParked(car2)
        activity.supportActionBar?.title.toString() shouldBeEqualTo activity.getString(R.string.app_name)

        // Park a car
        car1.performClick()
        ShadowLooper.runUiThreadTasksIncludingDelayedTasks()
        carIsParked(car1)
        carIsNotParked(car2)
        activity.supportActionBar?.title.toString() shouldBeEqualTo activity.resources.getString(
            R.string.park_status_many_free,
            5
        )
        listOfParkedCarsShown(activity)

        // Unpark it again
        car1.performClick()
        ShadowLooper.runUiThreadTasksIncludingDelayedTasks()
        carIsNotParked(car1)
        carIsNotParked(car2)
        activity.supportActionBar?.title.toString() shouldBeEqualTo activity.getString(R.string.app_name)
        emptyGaragePlaceholderShown(activity)

        controller.pause().stop().destroy()
    }


    @Test
    @org.robolectric.annotation.Config(qualifiers = "land")
    fun updateOwnCarsListOnServerStateChangeLandscapeTest() =
        updateOwnCarsListOnServerStateChangeTest()

    @Test
    @org.robolectric.annotation.Config(qualifiers = "port")
    fun updateOwnCarsListOnServerStateChangePortraitTest() =
        updateOwnCarsListOnServerStateChangeTest()

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

        val car = activity.binding.content.ownCarsRecyclerView.findViewHolderForAdapterPosition(1)?.itemView
        car.shouldNotBeNull()
        car as OwnCarListEntry

        carIsNotParked(car)
        activity.supportActionBar?.title.toString() shouldBeEqualTo activity.resources.getString(R.string.park_status_one_free)
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
        activity.supportActionBar?.title shouldBeEqualTo activity.getString(R.string.park_status_full)

        // Refresh list of parked cars from server, there is one spot free again
        ParkApp.networkManager = almostFull
        pullToRefreshView.onRefreshListener.onRefresh()
        ShadowLooper.runUiThreadTasksIncludingDelayedTasks()
        carIsNotParked(car)
        activity.supportActionBar?.title shouldBeEqualTo activity.resources.getQuantityString(R.plurals.park_status_free, 1)
*/
        controller.pause().stop().destroy()
    }

    private fun loadingPlaceholderShown(activity: ParkActivity) {
        activity.binding.content.parkedCarsPlaceholder.shouldBeVisible()
        activity.binding.content.loadingSpinner.shouldBeVisible()
        activity.binding.content.noParkServerPlaceholderButton.shouldBeGone()
        activity.binding.content.parkedCarsPlaceholderTextView.text.toString() shouldBeEqualTo
                activity.getString(R.string.updating_status_placeholder)
    }

    private fun emptyGaragePlaceholderShown(activity: ParkActivity) {
        activity.binding.content.parkedCarsPlaceholder.shouldBeVisible()
        activity.binding.content.loadingSpinner.shouldBeGone()
        activity.binding.content.noParkServerPlaceholderButton.shouldBeGone()
        activity.binding.content.parkedCarsPlaceholderTextView.text.toString() shouldBeEqualTo
                activity.getString(R.string.parked_cars_placeholder)
    }

    private fun listOfParkedCarsShown(activity: ParkActivity) {
        activity.binding.content.parkedCarsPlaceholder.shouldBeGone()
        activity.binding.content.parkedCarsRecyclerView.shouldBeVisible()
        activity.supportActionBar?.title shouldNotBeEqualTo activity.getString(R.string.app_name)
    }

    private fun connectionErrorPlaceholderShown(activity: ParkActivity) {
        activity.binding.content.parkedCarsPlaceholder.shouldBeVisible()
        activity.binding.content.loadingSpinner.shouldBeGone()
        activity.binding.content.noParkServerPlaceholderButton.shouldBeVisible()
        activity.binding.content.parkedCarsPlaceholderTextView.text.toString() shouldBeEqualTo
                activity.getString(
                    R.string.unable_to_connect_placeholder_text,
                    ParkApp.storageManager.getServer()
                )
        activity.binding.content.noParkServerPlaceholderButton.text.toString() shouldBeEqualTo
                activity.getString(R.string.unable_to_connect_placeholder_button)
    }

    private fun noServerPlaceholderShown(activity: ParkActivity) {
        activity.binding.content.parkedCarsPlaceholder.shouldBeVisible()
        activity.binding.content.loadingSpinner.shouldBeGone()
        activity.binding.content.noParkServerPlaceholderButton.shouldBeVisible()
        activity.binding.content.parkedCarsPlaceholderTextView.text.toString() shouldBeEqualTo
                activity.getString(
                    R.string.no_server_placeholder_text,
                    ParkApp.storageManager.getServer()
                )
        activity.binding.content.noParkServerPlaceholderButton.text.toString() shouldBeEqualTo
                activity.getString(R.string.no_server_placeholder_button)
    }

    private fun carIsNotParked(car: OwnCarListEntry) {
        val button = car.parkButton
        button.isEnabled.shouldBeTrue()
        button.text.shouldStartWith(
            context().getString(R.string.park_label)
                .uppercase(Locale.getDefault())
        )
    }

    private fun carIsParked(car: OwnCarListEntry) {
        val button = car.parkButton
        button.isEnabled.shouldBeTrue()
        button.text.shouldStartWith(
            context().getString(R.string.unpark_label)
                .uppercase(Locale.getDefault())
        )
    }

    private fun parkCarButtonIsDisabled(car: OwnCarListEntry) {
        val button = car.parkButton
        button.isEnabled.shouldBeFalse()
    }

    private fun carCanBePutOnWaitList(car: OwnCarListEntry) {
        val button = car.parkButton
        button.isEnabled.shouldBeTrue()
        button.text.shouldStartWith(
            context().getString(R.string.wait_label)
                .uppercase(Locale.getDefault())
        )
    }

    private fun carIsOnWaitList(car: OwnCarListEntry) {
        val button = car.parkButton
        button.isEnabled.shouldBeTrue()
        button.text.shouldStartWith(
            context().getString(R.string.stop_waiting_label)
                .uppercase(Locale.getDefault())
        )
    }

    private fun View.shouldBeGone() {
        this.visibility shouldBeEqualTo View.GONE
    }

    private fun View.shouldBeVisible() {
        this.visibility shouldBeEqualTo View.VISIBLE
    }
}


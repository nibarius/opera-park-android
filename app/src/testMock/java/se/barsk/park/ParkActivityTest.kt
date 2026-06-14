package se.barsk.park

import android.content.Intent
import com.google.firebase.FirebaseApp
import org.amshove.kluent.shouldBeEqualTo
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.robolectric.Robolectric
import org.robolectric.Shadows
import org.robolectric.android.controller.ActivityController
import org.robolectric.annotation.LooperMode
import se.barsk.park.datatypes.MockCarCollection
import se.barsk.park.mainui.ParkActivity
import se.barsk.park.managecars.ManageCarsActivity
import se.barsk.park.network.MockNetworkManager
import se.barsk.park.settings.SettingsActivity
import se.barsk.park.storage.MockStorageManager

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
        (ParkApp.storageManager as MockStorageManager).reset()
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
    @org.robolectric.annotation.Config(qualifiers = "land")
    fun updateOwnCarsListOnServerStateChangeLandscapeTest() =
        updateOwnCarsListOnServerStateChangeTest()

    @Test
    @org.robolectric.annotation.Config(qualifiers = "port")
    fun updateOwnCarsListOnServerStateChangePortraitTest() =
        updateOwnCarsListOnServerStateChangeTest()

    private fun updateOwnCarsListOnServerStateChangeTest() {
        // Create a new activity just for this test with a special network manager
        ParkApp.networkManager = MockNetworkManager(5)
        val controller = Robolectric.buildActivity(ParkActivity::class.java)
        controller.create().start().resume().visible()
        controller.pause().stop().destroy()
    }
}

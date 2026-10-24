package se.barsk.park.mainui

import android.app.Application
import android.content.Context
import android.os.Looper
import org.amshove.kluent.shouldBeEqualTo
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.LooperMode
import se.barsk.park.MILLIS_IN_SECONDS
import se.barsk.park.ParkApp
import se.barsk.park.RobolectricTest
import se.barsk.park.network.MockNetworkManager
import se.barsk.park.network.NetworkManager
import se.barsk.park.network.Result
import se.barsk.park.storage.MockStorageManager
import java.time.Duration

@LooperMode(LooperMode.Mode.PAUSED)
class ParkViewModelTest : RobolectricTest() {

    /** Network manager that responds immediately and counts the number of status checks. */
    private class CountingNetworkManager : NetworkManager() {
        var statusChecks = 0
        override fun checkStatus(context: Context, resultReadyListener: (Result) -> Unit) {
            statusChecks++
            resultReadyListener(Result.Success(listOf()))
        }
    }

    private val updateInterval = 10L
    private lateinit var networkManager: CountingNetworkManager
    private lateinit var viewModel: ParkViewModel

    @Before
    fun setUp() {
        ParkApp.init(context())
        (ParkApp.storageManager as MockStorageManager).setAutomaticUpdateInterval(updateInterval)
        networkManager = CountingNetworkManager()
        ParkApp.networkManager = networkManager
        viewModel = ParkViewModel(context() as Application)
    }

    @After
    fun tearDown() {
        viewModel.onScreenPaused()
        ParkApp.networkManager = MockNetworkManager()
        (ParkApp.storageManager as MockStorageManager).reset()
    }

    private fun advanceTimeBy(seconds: Long) {
        shadowOf(Looper.getMainLooper()).idleFor(Duration.ofMillis(seconds * MILLIS_IN_SECONDS))
    }

    @Test
    fun automaticUpdateRunsWhileScreenIsResumed() {
        viewModel.onScreenResumed()
        networkManager.statusChecks shouldBeEqualTo 1

        advanceTimeBy(updateInterval)
        networkManager.statusChecks shouldBeEqualTo 2

        advanceTimeBy(updateInterval)
        networkManager.statusChecks shouldBeEqualTo 3
    }

    @Test
    fun automaticUpdateStopsWhenScreenIsPaused() {
        viewModel.onScreenResumed()
        networkManager.statusChecks shouldBeEqualTo 1

        viewModel.onScreenPaused()
        advanceTimeBy(updateInterval * 3)
        networkManager.statusChecks shouldBeEqualTo 1
    }
}

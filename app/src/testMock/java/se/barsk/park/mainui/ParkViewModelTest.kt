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

    /**
     * Network manager that counts the number of status checks. It responds immediately unless
     * [holdResponses] is set, then the response is sent when [respond] is called.
     */
    private class CountingNetworkManager : NetworkManager() {
        var statusChecks = 0
        var holdResponses = false
        private var pendingListener: ((Result) -> Unit)? = null

        override fun checkStatus(context: Context, resultReadyListener: (Result) -> Unit) {
            statusChecks++
            if (holdResponses) {
                pendingListener = resultReadyListener
            } else {
                resultReadyListener(Result.Success(listOf()))
            }
        }

        fun respond() {
            pendingListener?.invoke(Result.Success(listOf()))
            pendingListener = null
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

    @Test
    fun automaticUpdateDoesNotShowRefreshIndicator() {
        viewModel.onScreenResumed()
        networkManager.holdResponses = true

        advanceTimeBy(updateInterval)
        networkManager.statusChecks shouldBeEqualTo 2
        viewModel.isRefreshingFlow.value shouldBeEqualTo false
    }

    @Test
    fun manualRefreshShowsRefreshIndicatorUntilUpdateIsDone() {
        viewModel.onScreenResumed()
        networkManager.holdResponses = true

        viewModel.refresh()
        viewModel.isRefreshingFlow.value shouldBeEqualTo true

        networkManager.respond()
        viewModel.isRefreshingFlow.value shouldBeEqualTo false
    }
}

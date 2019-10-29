package se.barsk.park.mainui

import org.amshove.kluent.shouldBeFalse
import org.amshove.kluent.shouldBeTrue
import org.junit.Test

import org.junit.Assert.*

class NetworkStateTest {

    @Test
    fun hasMadeFailedRequestsOnly() {
        val networkState = NetworkState()
        networkState.hasMadeFailedRequestsOnly().shouldBeFalse()
        networkState.requestFinished(false)
        networkState.hasMadeFailedRequestsOnly().shouldBeTrue()
        networkState.requestFinished(false)
        networkState.hasMadeFailedRequestsOnly().shouldBeTrue()
        networkState.requestFinished(true)
        networkState.hasMadeFailedRequestsOnly().shouldBeFalse()
        networkState.requestFinished(false)
        networkState.hasMadeFailedRequestsOnly().shouldBeFalse()
    }

    @Test
    fun getUpdateInProgress() {
        val networkState = NetworkState()
        networkState.updateInProgress.shouldBeFalse()
        networkState.requestStarted()
        networkState.updateInProgress.shouldBeTrue()
        networkState.requestFinished(false)
        networkState.updateInProgress.shouldBeFalse()
        networkState.requestStarted()
        networkState.updateInProgress.shouldBeTrue()
        networkState.requestFinished(true)
        networkState.updateInProgress.shouldBeFalse()
    }

    @Test
    fun initialState() {
        testInitialState(NetworkState())
    }

    private fun testInitialState(networkState: NetworkState) {
        networkState.updateInProgress.shouldBeFalse()
        networkState.isWaitingForFirstResponse().shouldBeTrue()
    }

    @Test
    fun resetStateDuringRequest() {
        val networkState = NetworkState()
        networkState.requestStarted()
        networkState.resetState()
        testInitialState(networkState)
    }

    @Test
    fun resetStateAfterFailedRequest() {
        val networkState = NetworkState()
        networkState.requestStarted()
        networkState.requestFinished(false)
        networkState.resetState()
        testInitialState(networkState)
    }

    @Test
    fun resetStateAfterSuccessfulRequest() {
        val networkState = NetworkState()
        networkState.requestStarted()
        networkState.requestFinished(true)
        networkState.resetState()
        testInitialState(networkState)
    }
}
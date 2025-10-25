package se.barsk.park.mainui

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Used to keep track of the state of requests to be able to decide which placeholder
 * to show if there is no data from the server available.
 */
class NetworkState {

    /**
     *                                       Fail
     *                                    +---------+
     *                                    |         |
     * Init+-----+                     +--v---------+---------+
     *           |                     | Only failed requests |
     *           |                     +--^---------+---------+
     *  +--------v-------+    Fail        |         |
     *  |                +----------------+         |
     *  | First response |                          | Success
     *  |  not received  |   Success                |
     *  |                +----------------+         |
     *  +-----^----------+                |         |
     *        |                      +----v---------v-------------+
     *        +----------------------+Have made successful request|
     *              Reset state      +----^---------+-------------+
     *                                    |         |
     *                                    +---------+ Success, Fail
     */

    enum class State {
        // Once there's a server set up a request will be done automatically and a spinner is shown
        // so it can be assumed that we are waiting for a request when this class is initialized.
        FIRST_RESPONSE_NOT_RECEIVED,
        ONLY_FAILED_REQUESTS,
        HAVE_MADE_SUCCESSFUL_REQUEST
    }

    private var state: State = State.FIRST_RESPONSE_NOT_RECEIVED

    fun isWaitingForFirstResponse() = state == State.FIRST_RESPONSE_NOT_RECEIVED
    fun hasMadeFailedRequestsOnly() = state == State.ONLY_FAILED_REQUESTS

    private val _updateInProgress = MutableStateFlow(false)
    val updateInProgressFlow = _updateInProgress.asStateFlow()

    fun resetState() {
        state = State.FIRST_RESPONSE_NOT_RECEIVED
        _updateInProgress.value = false
    }

    fun requestStarted() {
        _updateInProgress.value = true
    }

    fun requestFinished(success: Boolean) {
        _updateInProgress.value = false
        if (success) {
            state = State.HAVE_MADE_SUCCESSFUL_REQUEST
        } else if (state != State.HAVE_MADE_SUCCESSFUL_REQUEST) {
            state = State.ONLY_FAILED_REQUESTS
        }
    }
}
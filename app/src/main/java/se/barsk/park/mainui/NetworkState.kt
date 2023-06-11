package se.barsk.park.mainui

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

    var updateInProgress: Boolean = false
        private set

    fun resetState() {
        state = State.FIRST_RESPONSE_NOT_RECEIVED
        updateInProgress = false
    }

    fun requestStarted() {
        updateInProgress = true
    }

    fun requestFinished(success: Boolean) {
        updateInProgress = false
        if (success) {
            state = State.HAVE_MADE_SUCCESSFUL_REQUEST
        } else if (state != State.HAVE_MADE_SUCCESSFUL_REQUEST) {
            state = State.ONLY_FAILED_REQUESTS
        }
    }
}
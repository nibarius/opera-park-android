package se.barsk.park.datatypes

/**
 * Listener interface for changes to the garage status.
 */
interface GarageStatusChangedListener {
    fun onGarageStatusChange()
    fun onGarageUpdateFail(errorMessage: String)
}
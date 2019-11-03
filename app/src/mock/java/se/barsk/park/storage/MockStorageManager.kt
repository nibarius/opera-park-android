package se.barsk.park.storage

import android.content.Context
import se.barsk.park.datatypes.OwnCar

/**
 * A mock implementation of the storage manager that doesn't persist anything to storage.
 */
class MockStorageManager(context: Context) : StorageManager(context) {
    private var server = "https://park.example.com/"
    private var onWaitList = false

    // Shared preferences interaction functions
    override fun hasServer() = server.isNotEmpty()

    override fun getServer() = server
    override fun setServer(server: String) {
        this.server = server
    }

    override fun onWaitList() = onWaitList
    override fun setOnWaitList(onWaitList: Boolean) {
        this.onWaitList = onWaitList
    }

    // No stats or crash reporting when mocking.
    override fun statsEnabled(): Boolean = false

    override fun crashReportingEnabled(): Boolean = false
    override fun getAutomaticUpdateInterval(): Long = 0

    override fun fetchAllCars(): MutableList<OwnCar> = mutableListOf()
    override fun insertOrReplace(ownCar: OwnCar, position: Int) = Unit
    override fun remove(ownCar: OwnCar) = Unit
}
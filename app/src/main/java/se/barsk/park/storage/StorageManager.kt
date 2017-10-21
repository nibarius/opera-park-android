package se.barsk.park.storage

import se.barsk.park.ParkApp
import se.barsk.park.datatypes.OwnCar


open class StorageManager {

    private val sharedPrefs: SharedPrefs
    private val database: Database

    init {
        val context = ParkApp.context
        database = Database(context)
        sharedPrefs = SharedPrefs(context, ParkApp.getSharedPreferences())
    }

    // Shared preferences interaction functions
    open fun hasServer() = sharedPrefs.hasServer()

    open fun getServer() = sharedPrefs.getServer()
    open fun setServer(server: String) = sharedPrefs.setServer(server)

    open fun statsEnabled(): Boolean = sharedPrefs.statsEnabled()
    open fun crashReportingEnabled(): Boolean = sharedPrefs.crashReportingEnabled()
    open fun getAutomaticUpdateInterval(): Long = sharedPrefs.getAutomaticUpdateInterval()

    // Database interaction functions
    open fun fetchAllCars(): MutableList<OwnCar> = database.fetchAllCars()

    /**
     * Adds a car to persistent storage if it doesn't already exist. If it exists the data
     * for the car is updated. The id of the car is used to check if the car already exists.
     */
    open fun insertOrReplace(ownCar: OwnCar, position: Int) = database.insertOrReplace(ownCar, position)

    open fun remove(ownCar: OwnCar) = database.remove(ownCar)
}
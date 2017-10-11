package se.barsk.park.storage

import se.barsk.park.BuildConfig
import se.barsk.park.ParkApp
import se.barsk.park.R
import se.barsk.park.datatypes.OwnCar


open class StorageManager {
    companion object {
        /**
         * Returns an appropriate storage manager depending on if it's a normal build
         * or a special build for creating screenshots with static content.
         */
        fun getInstance(): StorageManager {
            @Suppress("ConstantConditionIf")
            return if (BuildConfig.isScreenshotBuild) {
                MockStorageManager()
            } else {
                StorageManager()
            }
        }
    }

    private val sharedPrefs: SharedPrefs
    private val database: Database

    init {
        val context = ParkApp.context
        database = Database(context)
        val prefsFile = context.getString(R.string.shared_prefs_file_name)
        sharedPrefs = SharedPrefs(context, ParkApp.getSharedPreferences(prefsFile))
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
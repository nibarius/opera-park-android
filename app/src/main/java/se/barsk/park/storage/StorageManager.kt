package se.barsk.park.storage

import android.content.Context
import se.barsk.park.R
import se.barsk.park.datatypes.OwnCar


object StorageManager {
    private lateinit var sharedPrefs: SharedPrefs
    private lateinit var database: Database

    fun init(context: Context) {
        database = Database(context)
        val prefsFile = context.getString(R.string.shared_prefs_file_name)
        sharedPrefs = SharedPrefs(context, context.getSharedPreferences(prefsFile, Context.MODE_PRIVATE))
    }

    // Shared preferences interaction functions
    fun hasServer() = sharedPrefs.hasServer()

    fun getServer() = sharedPrefs.getServer()
    fun setServer(server: String) = sharedPrefs.setServer(server)

    fun statsEnabled(): Boolean = sharedPrefs.statsEnabled()
    fun crashReportingEnabled(): Boolean = sharedPrefs.crashReportingEnabled()
    fun getAutomaticUpdateInterval(): Long = sharedPrefs.getAutomaticUpdateInterval()

    // Database interaction functions
    fun fetchAllCars(): MutableList<OwnCar> = database.fetchAllCars()

    /**
     * Adds a car to persistent storage if it doesn't already exist. If it exists the data
     * for the car is updated. The id of the car is used to check if the car already exists.
     */
    fun insertOrReplace(ownCar: OwnCar, position: Int) = database.insertOrReplace(ownCar, position)
    fun remove(ownCar: OwnCar) = database.remove(ownCar)
}
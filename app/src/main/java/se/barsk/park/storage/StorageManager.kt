package se.barsk.park.storage

import android.content.Context
import se.barsk.park.datatypes.OwnCar


object StorageManager {
    const val SHARED_PREF_FILE_SETTINGS = "settings"
    private lateinit var sharedPrefs: SharedPrefs
    private lateinit var database: Database

    fun init(context: Context) {
        database = Database(context)
        sharedPrefs = SharedPrefs(context.getSharedPreferences(SHARED_PREF_FILE_SETTINGS, Context.MODE_PRIVATE))
    }

    // Shared preferences interaction functions
    fun hasServer() = sharedPrefs.hasServer()

    fun getServer() = sharedPrefs.getServer()
    fun setServer(server: String) = sharedPrefs.setServer(server)

    fun statsEnabled(): Boolean = sharedPrefs.statsEnabled()

    // Database interaction functions
    fun fetchAllCars(): MutableList<OwnCar> = database.fetchAllCars()
    fun insertOrReplace(ownCar: OwnCar, position: Int) = database.insertOrReplace(ownCar, position)
    fun remove(ownCar: OwnCar) = database.remove(ownCar)
}
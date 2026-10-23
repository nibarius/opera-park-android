package se.barsk.park.storage

import android.content.Context
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.runBlocking
import se.barsk.park.datatypes.OwnCar

open class StorageManager(context: Context) {

    private val dataStoreManager: DataStoreManager = DataStoreManager(context)
    private val database = ParkDatabase.createDatabase(context)

    var settingsChangeListener: SettingsChangeListener?
        get() = dataStoreManager.settingsChangeListener
        set(value) {
            dataStoreManager.settingsChangeListener = value
        }

    open val serverFlow: Flow<String> get() = dataStoreManager.serverFlow
    open val crashReportingFlow: Flow<Boolean> get() = dataStoreManager.crashReportingFlow
    open val automaticUpdateIntervalFlow: Flow<Long> get() = dataStoreManager.automaticUpdateIntervalFlow
    open val themeFlow: Flow<String> get() = dataStoreManager.themeFlow
    open val privacyOnBoardingFlow: Flow<Boolean> get() = dataStoreManager.privacyOnBoardingFlow

    // Preference interaction functions
    open fun hasServer(): Boolean = dataStoreManager.hasServer()

    open fun getServer(): String = dataStoreManager.getServer()

    open fun setServer(server: String) {
        runBlocking { dataStoreManager.setServer(server) }
    }

    open fun crashReportingEnabled(): Boolean = dataStoreManager.crashReportingEnabled()

    open fun getAutomaticUpdateInterval(): Long = dataStoreManager.getAutomaticUpdateInterval()

    open fun setAutomaticUpdateInterval(interval: Long) {
        runBlocking { dataStoreManager.setAutomaticUpdateInterval(interval) }
    }

    open fun getTheme(): String = dataStoreManager.getTheme()

    open fun setTheme(theme: String) {
        runBlocking { dataStoreManager.setTheme(theme) }
    }

    open fun hasSeenPrivacyOnBoarding(): Boolean = dataStoreManager.hasSeenPrivacyOnBoarding()

    open fun setPrivacyOnBoardingSeen() {
        runBlocking { dataStoreManager.setPrivacyOnBoardingSeen() }
    }

    open fun setCrashReportingEnabled(isEnabled: Boolean) {
        runBlocking { dataStoreManager.setCrashReportingEnabled(isEnabled) }
    }

    // Database interaction functions
    fun fetchAllCars() = database.fetchAllCars()

    /**
     * Adds a car to persistent storage if it doesn't already exist. If it exists the data
     * for the car is updated. The id of the car is used to check if the car already exists.
     */
    suspend fun insertOrReplace(ownCar: OwnCar, position: Int) = database.insertOrReplace(ownCar, position)

    suspend fun remove(ownCar: OwnCar) = database.remove(ownCar)
}

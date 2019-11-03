package se.barsk.park.storage

import android.content.Context
import se.barsk.park.ParkApp
import se.barsk.park.datatypes.OwnCar


open class StorageManager(context: Context) {

    private val sharedPrefs: SharedPrefs = SharedPrefs(context, ParkApp.getSharedPreferences(context))
    private val database: Database = Database(context)

    var settingsChangeListener: SettingsChangeListener?
        get() = sharedPrefs.settingsChangeListener
        set(value) {
            sharedPrefs.settingsChangeListener = value
        }

    // Shared preferences interaction functions
    open fun hasServer() = sharedPrefs.hasServer()

    open fun getServer() = sharedPrefs.getServer()
    open fun setServer(server: String) = sharedPrefs.setServer(server)

    open fun statsEnabled(): Boolean = sharedPrefs.statsEnabled()
    open fun crashReportingEnabled(): Boolean = sharedPrefs.crashReportingEnabled()
    open fun getAutomaticUpdateInterval(): Long = sharedPrefs.getAutomaticUpdateInterval()

    open fun onWaitList(): Boolean = sharedPrefs.onWaitList()
    open fun setOnWaitList(onWaitList: Boolean) = sharedPrefs.setOnWaitList(onWaitList)

    fun hasSeenPrivacyOnBoarding(): Boolean = sharedPrefs.hasSeenPrivacyOnBoarding()
    fun setPrivacyOnBoardingSeen() = sharedPrefs.setPrivacyOnBoardingSeen()
    fun giveStatsConsent() = sharedPrefs.giveAllStatsConsent(true)
    fun withdrawStatsConsent() = sharedPrefs.giveAllStatsConsent(false)
    fun recordCrashReportingConsentChange() = sharedPrefs.recordCrashReportingConsentChange()
    fun recordUsageStatisticsConsentChange() = sharedPrefs.recordUsageStatisticsConsentChange()

    // Database interaction functions
    open fun fetchAllCars(): MutableList<OwnCar> = database.fetchAllCars()

    /**
     * Adds a car to persistent storage if it doesn't already exist. If it exists the data
     * for the car is updated. The id of the car is used to check if the car already exists.
     */
    open fun insertOrReplace(ownCar: OwnCar, position: Int) = database.insertOrReplace(ownCar, position)

    open fun remove(ownCar: OwnCar) = database.remove(ownCar)
}
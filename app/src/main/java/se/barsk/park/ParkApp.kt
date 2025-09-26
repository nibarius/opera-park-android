package se.barsk.park

import android.content.Context
import android.content.SharedPreferences
import se.barsk.park.analytics.CrashReporting
import se.barsk.park.datatypes.CarCollection
import se.barsk.park.network.NetworkManager
import se.barsk.park.storage.StorageManager
import se.barsk.park.utils.Utils


/**
 * Singleton that can be used to get various global resources from anywhere.
 * Any potential entry point for the app (for example Activity.onCreate)
 * must call ParkApp.init(context) as soon as possible.
 */
object ParkApp {
    private var isInitiated = false
    lateinit var crashlytics: CrashReporting
    lateinit var carCollection: CarCollection
    lateinit var networkManager: NetworkManager
    lateinit var storageManager: StorageManager

    fun init(context: Context) {
        if (isInitiated) {
            return
        }
        val appContext = context.applicationContext
        storageManager = Injection.provideStorageManager(appContext)
        crashlytics = CrashReporting()
        crashlytics.enableIfAllowed()
        networkManager = Injection.provideNetworkManager()
        carCollection = Injection.provideCarCollection()

        Utils.setTheme(storageManager.getTheme())
        isInitiated = true
    }

    fun getSharedPreferences(context: Context): SharedPreferences {
        val prefsFile: String = getSharedPreferencesFileName(context)
        return context.getSharedPreferences(prefsFile, Context.MODE_PRIVATE)
    }

    fun getSharedPreferencesFileName(context: Context): String =
            Injection.provideSharedPreferencesFileName(context)
}


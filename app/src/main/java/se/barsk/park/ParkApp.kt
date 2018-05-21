package se.barsk.park

import android.content.Context
import android.content.SharedPreferences
import net.danlew.android.joda.JodaTimeAndroid
import se.barsk.park.analytics.Analytics
import se.barsk.park.analytics.CrashReporting
import se.barsk.park.datatypes.CarCollection
import se.barsk.park.datatypes.User
import se.barsk.park.network.NetworkManager
import se.barsk.park.storage.StorageManager


/**
 * Singleton that can be used to get various global resources from anywhere.
 * Any potential entry point for the app (for example Activity.onCreate)
 * must call ParkApp.init(context) as soon as possible.
 */
object ParkApp {
    private var isInitiated = false
    lateinit var analytics: Analytics
    lateinit var crashlytics: CrashReporting
    lateinit var carCollection: CarCollection
    lateinit var networkManager: NetworkManager
    lateinit var storageManager: StorageManager
    lateinit var theUser: User

    fun init(context: Context) {
        if (isInitiated) {
            return
        }
        val appContext = context.applicationContext
        storageManager = Injection.provideStorageManager(appContext)
        crashlytics = CrashReporting()
        crashlytics.enableIfAllowed(appContext)
        JodaTimeAndroid.init(appContext)
        analytics = Analytics(appContext)
        networkManager = Injection.provideNetworkManager()
        carCollection = Injection.provideCarCollection()
        theUser = User(appContext)

        isInitiated = true
    }

    /**
     * Returns true if the app is running (has been initiated). Useful to know when
     * receiving push notifications.
     */
    fun isRunning() = isInitiated

    fun getSharedPreferences(context: Context): SharedPreferences {
        val prefsFile: String = getSharedPreferencesFileName(context)
        return context.getSharedPreferences(prefsFile, Context.MODE_PRIVATE)
    }

    fun getSharedPreferencesFileName(context: Context): String =
            Injection.provideSharedPreferencesFileName(context)
}


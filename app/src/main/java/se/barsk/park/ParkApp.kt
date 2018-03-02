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
 * Singleton that can be used to get the application context and other global resources
 * from anywhere. Any potential entry point for the app (for example Activity.onCreate)
 * must call ParkApp.init(context) as soon as possible.
 */
object ParkApp {
    private var isInitiated = false
    lateinit var context: Context
    lateinit var analytics: Analytics
    lateinit private var crashlytics: CrashReporting
    lateinit var carCollection: CarCollection
    lateinit var networkManager: NetworkManager
    lateinit var storageManager: StorageManager
    lateinit var theUser: User

    fun init(context: Context) {
        if (isInitiated) {
            return
        }
        this.context = context.applicationContext
        storageManager = Injection.provideStorageManager()
        crashlytics = CrashReporting()
        crashlytics.enableIfAllowed()
        JodaTimeAndroid.init(this.context)
        analytics = Analytics()
        networkManager = Injection.provideNetworkManager()
        carCollection = Injection.provideCarCollection()
        theUser = User()

        isInitiated = true
    }

    fun getSharedPreferences(prefsFile: String = getSharedPreferencesFileName()): SharedPreferences =
            context.getSharedPreferences(prefsFile, Context.MODE_PRIVATE)

    fun getSharedPreferencesFileName(): String = Injection.provideSharedPreferencesFileName(context)
}


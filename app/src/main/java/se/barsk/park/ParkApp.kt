package se.barsk.park

import android.content.Context
import android.content.SharedPreferences
import se.barsk.park.analytics.Analytics
import se.barsk.park.analytics.CrashReporting
import se.barsk.park.datatypes.CarCollection


/**
 * Singleton that can be used to get the application context and other global resources
 * from anywhere. Any potential entry point for the app (for example Activity.onCreate)
 * must call ParkApp.ini(context) as soon as possible.
 */
object ParkApp {
    private var isInitiated = false
    lateinit var context: Context
    lateinit var analytics: Analytics
    lateinit private var crashlytics: CrashReporting
    lateinit var carCollection: CarCollection

    fun init(context: Context) {
        if (isInitiated) {
            return
        }
        this.context = context.applicationContext
        crashlytics = CrashReporting()
        crashlytics.enableIfAllowed()
        analytics = Analytics()
        carCollection = CarCollection()

        isInitiated = true
    }

    fun getSharedPreferences(prefsFile: String): SharedPreferences =
            context.getSharedPreferences(prefsFile, Context.MODE_PRIVATE)
}


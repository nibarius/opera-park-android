package se.barsk.park.analytics

import com.google.firebase.crashlytics.FirebaseCrashlytics
import se.barsk.park.BuildConfig
import se.barsk.park.ParkApp

/**
 * Object for handling crash reporting
 */
class CrashReporting {
    private var isStarted = false

    fun enableIfAllowed() {
        if (!isStarted && ParkApp.storageManager.crashReportingEnabled()) {
            val crashlytics = FirebaseCrashlytics.getInstance()
            crashlytics.setCrashlyticsCollectionEnabled(true)
            crashlytics.setCustomKey("public_release", BuildConfig.releaseBuild)
            isStarted = true
        }
    }
}
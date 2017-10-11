package se.barsk.park.analytics

import com.crashlytics.android.Crashlytics
import io.fabric.sdk.android.Fabric
import se.barsk.park.BuildConfig
import se.barsk.park.ParkApp
import se.barsk.park.storage.StorageManager

/**
 * Object for handling crash reporting
 */
class CrashReporting {
    fun enableIfAllowed() {
        if (ParkApp.storageManager.crashReportingEnabled()) {
            Fabric.with(ParkApp.context, Crashlytics())
            Crashlytics.setBool("public_release", BuildConfig.releaseBuild)
        }
    }
}
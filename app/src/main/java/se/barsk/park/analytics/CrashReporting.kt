package se.barsk.park.analytics

import com.crashlytics.android.Crashlytics
import io.fabric.sdk.android.Fabric
import se.barsk.park.ParkApp
import se.barsk.park.storage.StorageManager

/**
 * Object for handling crash reporting
 */
class CrashReporting {
    fun enableIfAllowed() {
        if (StorageManager.crashReportingEnabled()) {
            Fabric.with(ParkApp.context, Crashlytics())
        }
    }
}
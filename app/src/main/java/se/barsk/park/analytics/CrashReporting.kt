package se.barsk.park.analytics

import android.content.Context
import com.crashlytics.android.Crashlytics
import io.fabric.sdk.android.Fabric
import se.barsk.park.BuildConfig
import se.barsk.park.ParkApp
import se.barsk.park.storage.StorageManager

/**
 * Object for handling crash reporting
 */
class CrashReporting {
    fun enableIfAllowed(context: Context) {
        if (ParkApp.storageManager.crashReportingEnabled()) {
            Fabric.with(context, Crashlytics())
            Crashlytics.setBool("public_release", BuildConfig.releaseBuild)
        }
    }
}
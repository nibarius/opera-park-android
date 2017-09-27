package se.barsk.park.analytics

import android.content.Context
import com.crashlytics.android.Crashlytics
import io.fabric.sdk.android.Fabric
import se.barsk.park.storage.StorageManager

/**
 * Object for handling crash reporting
 */
object CrashReporting {
    fun init(context: Context) {
        if (StorageManager.crashReportingEnabled()) {
            Fabric.with(context, Crashlytics());
        }
    }
}
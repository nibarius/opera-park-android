package se.barsk.park.analytics

import android.content.Context
import com.google.firebase.analytics.FirebaseAnalytics
import se.barsk.park.storage.StorageManager

/**
 * Object for handling all analytics reporting.
 */
object Analytics {
    private lateinit var fa: FirebaseAnalytics
    fun init(context: Context) {
        fa = FirebaseAnalytics.getInstance(context)
        updateOptOutState()
    }

    fun optOutToggled() {
        updateOptOutState()
    }

    fun logEvent(event: AnalyticsEvent) {
        fa.logEvent(event.name, event.parameters)
    }

    private fun updateOptOutState() = fa.setAnalyticsCollectionEnabled(StorageManager.statsEnabled())
}
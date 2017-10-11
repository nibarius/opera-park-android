package se.barsk.park.analytics

import com.google.firebase.analytics.FirebaseAnalytics
import se.barsk.park.ParkApp
import se.barsk.park.storage.StorageManager

/**
 * Object for handling all analytics reporting.
 */
class Analytics {
    private val fa: FirebaseAnalytics = FirebaseAnalytics.getInstance(ParkApp.context)

    fun optOutToggled() = updateOptOutState()
    fun logEvent(event: AnalyticsEvent) = fa.logEvent(event.name, event.parameters)

    private fun updateOptOutState() = fa.setAnalyticsCollectionEnabled(ParkApp.storageManager.statsEnabled())
}
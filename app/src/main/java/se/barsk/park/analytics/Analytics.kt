package se.barsk.park.analytics

import android.content.Context
import com.google.firebase.analytics.FirebaseAnalytics
import se.barsk.park.ParkApp

/**
 * Object for handling all analytics reporting.
 */
class Analytics(context: Context) {
    private val fa: FirebaseAnalytics = FirebaseAnalytics.getInstance(context)

    fun optOutToggled() = updateOptOutState()
    fun logEvent(event: AnalyticsEvent) = fa.logEvent(event.name, event.parameters)
    fun setProperty(property: String, value: String) = fa.setUserProperty(property, value)

    private fun updateOptOutState() = fa.setAnalyticsCollectionEnabled(ParkApp.storageManager.statsEnabled())
}
package se.barsk.park.analytics

import android.content.Context
import com.google.firebase.analytics.FirebaseAnalytics

/**
 * Object for handling all analytics reporting.
 */
object Analytics {
    private lateinit var fa: FirebaseAnalytics
    fun init(context: Context) {
        fa = FirebaseAnalytics.getInstance(context)
    }

    fun logEvent(event: AnalyticsEvent) {
        fa.logEvent(event.name, event.parameters)
    }
}
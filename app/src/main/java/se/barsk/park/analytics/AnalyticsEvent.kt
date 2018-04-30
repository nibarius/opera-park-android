package se.barsk.park.analytics

import android.os.Bundle

/**
 * Base class for all analytics events
 * https://firebase.google.com/docs/reference/android/com/google/firebase/analytics/FirebaseAnalytics.Event
 * There can be up to 500 different events and each event can have up to 25 unique parameters.
 */
abstract class AnalyticsEvent {
    object Param {
        const val ACTION = "action"
        const val EXCEPTION = "exception"
        const val CARS_SHARED = "cars_shared"
        const val MESSAGE = "message"
    }

    val parameters: Bundle = Bundle()
    abstract val name: String
}
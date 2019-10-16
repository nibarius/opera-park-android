package se.barsk.park.analytics

import com.google.firebase.analytics.FirebaseAnalytics

/**
 * A share car event, sent when sharing cars from the manage cars activity.
 */
class ShareCarEvent(carsShared: Int) : AnalyticsEvent() {
    override val name: String = FirebaseAnalytics.Event.SHARE

    init {
        parameters.putInt(Param.CARS_SHARED, carsShared)
        parameters.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "car")
    }
}
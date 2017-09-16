package se.barsk.park.analytics

import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics

/**
 * A share car event, sent when sharing cars from the manage cars activity.
 */
class ShareCarEvent(carsShared: Int) : AnalyticsEvent {
    object Param {
        const val CARS_SHARED = "cars_shared"
    }

    override val parameters: Bundle = Bundle()
    override val name: String = FirebaseAnalytics.Event.SHARE

    init {
        parameters.putInt(Param.CARS_SHARED, carsShared)
        parameters.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "car")
    }
}
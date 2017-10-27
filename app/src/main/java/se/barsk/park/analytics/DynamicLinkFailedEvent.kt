package se.barsk.park.analytics

import android.os.Bundle

/**
 * Event sent whenever handling dynamic links failed
 */
class DynamicLinkFailedEvent(exception: String) : AnalyticsEvent {
    object Param {
        const val EXCEPTION = "exception"
    }

    override val name: String = "dynamic_link_failed"
    override val parameters: Bundle = Bundle()

    init {
        parameters.putString(Param.EXCEPTION,
                exception.substring(0, minOf(100, exception.length)))
    }
}
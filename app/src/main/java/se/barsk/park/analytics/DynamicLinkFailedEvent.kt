package se.barsk.park.analytics

/**
 * Event sent whenever handling dynamic links failed
 */
class DynamicLinkFailedEvent(exception: String) : AnalyticsEvent() {
    override val name: String = "dynamic_link_failed"

    init {
        parameters.putString(Param.EXCEPTION,
                exception.substring(0, minOf(100, exception.length)))
    }
}
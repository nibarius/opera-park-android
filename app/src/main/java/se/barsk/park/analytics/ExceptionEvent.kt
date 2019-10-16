package se.barsk.park.analytics

/**
 * Event for various generic errors that are expected to never happen.
 */
class ExceptionEvent(msg: String, exception: String? = null) : AnalyticsEvent() {
    override val name: String = "generic_failure"

    init {
        parameters.putString(Param.MESSAGE, msg)
        if (exception != null) {
            parameters.putString(Param.EXCEPTION,
                    exception.substring(0, minOf(100, exception.length)))
        }
    }
}
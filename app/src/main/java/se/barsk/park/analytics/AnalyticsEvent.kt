package se.barsk.park.analytics

import android.os.Bundle

/**
 * Interface for all analytics events
 */
interface AnalyticsEvent {
    val name: String
    val parameters: Bundle
}
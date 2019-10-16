package se.barsk.park.utils

import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.joda.time.Instant


object TimeUtils {
    private const val RESET_POINT_HOURS_AFTER_MIDNIGHT = 4
    private fun getResetPoint() = DateTime(DateTimeZone.UTC).withTimeAtStartOfDay().plusHours(RESET_POINT_HOURS_AFTER_MIDNIGHT)
    fun isAfterReset(time: Instant) = time > getResetPoint()
    fun isBeforeReset(time: Instant) = time < getResetPoint()
    fun now() = Instant()
}
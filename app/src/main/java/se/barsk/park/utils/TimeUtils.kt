package se.barsk.park.utils

import java.time.Instant
import java.time.ZoneOffset
import java.time.ZonedDateTime

object TimeUtils {
    private const val RESET_POINT_HOURS_AFTER_MIDNIGHT = 4L
    private fun getResetPoint(): Instant =
        ZonedDateTime.now(ZoneOffset.UTC)
            .toLocalDate()
            .atStartOfDay(ZoneOffset.UTC)
            .plusHours(RESET_POINT_HOURS_AFTER_MIDNIGHT)
            .toInstant()

    fun isAfterReset(time: Instant) = time.isAfter(getResetPoint())
    fun isBeforeReset(time: Instant) = time.isBefore(getResetPoint())
    fun now(): Instant = Instant.now()
}

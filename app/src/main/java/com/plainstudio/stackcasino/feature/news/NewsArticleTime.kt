package com.plainstudio.stackcasino.feature.news

import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * Renders an ISO 8601 publishedAt timestamp as the mockup
 * "MM/DD/YYYY - 2H AGO" string.
 *
 * Returns "JUST NOW" for anything under a minute, falls back to the
 * absolute date alone if NewsAPI sent a malformed timestamp the
 * parser can't handle (so the row never crashes on an edge entry).
 */
internal fun formatPublishedAt(
    isoTimestamp: String,
    now: Instant = Instant.now(),
    zone: ZoneId = ZoneId.systemDefault(),
): String {
    val parsed =
        runCatching { Instant.parse(isoTimestamp) }
            .getOrElse { return isoTimestamp.fallbackDate() ?: isoTimestamp }
    val date = LocalDate.ofInstant(parsed, zone).format(DATE_FORMAT)
    val relative = relativeLabel(Duration.between(parsed, now))
    return "$date · $relative"
}

private fun relativeLabel(elapsed: Duration): String {
    val minutes = elapsed.toMinutes()
    val hours = elapsed.toHours()
    val days = elapsed.toDays()
    val weeks = days / DAYS_PER_WEEK
    return when {
        elapsed.isNegative || minutes < 1 -> "Just now"
        minutes < MINUTES_PER_HOUR -> "${minutes}m ago"
        hours < HOURS_PER_DAY -> "${hours}h ago"
        days < DAYS_PER_WEEK -> "${days}d ago"
        weeks < WEEKS_PER_MONTH -> "${weeks}w ago"
        else -> "${weeks / WEEKS_PER_MONTH}mo ago"
    }
}

// Last-ditch parse for entries whose publishedAt is just the
// "YYYY-MM-DD" prefix without a time component; if even that fails
// the caller falls back to the raw string.
private fun String.fallbackDate(): String? =
    runCatching { take(YYYY_MM_DD_LEN).let { LocalDate.parse(it).format(DATE_FORMAT) } }.getOrNull()

private const val MINUTES_PER_HOUR = 60L
private const val HOURS_PER_DAY = 24L
private const val DAYS_PER_WEEK = 7L
private const val WEEKS_PER_MONTH = 4L
private const val YYYY_MM_DD_LEN = 10

private val DATE_FORMAT: DateTimeFormatter = DateTimeFormatter.ofPattern("M/d/yyyy", Locale.US)

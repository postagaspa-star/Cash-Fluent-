package com.cashfluent.app.domain.league

/**
 * One person as the board sees them: the id the server knows their phone by, the
 * nickname they typed, and their points. That is the whole of what the league holds
 * about anyone — there is no account behind it and nothing else to fetch.
 */
data class Entrant(
    val id: String,
    val name: String,
    val totalPoints: Int,
    val weekPoints: Int,
)

/** What a nickname is allowed to be: printable, one line, at most [MAX] characters. */
object Nickname {

    const val MAX = 20

    fun clean(raw: String): String = raw.filter { it >= ' ' }.trim().take(MAX)
}

/**
 * Weeks are counted from Monday 5 January 1970, in UTC, so every phone — and the server's
 * rules, which run the same arithmetic on their own clock — agree on which week it is
 * without asking anyone. Leagues reset on Monday.
 */
object Week {

    private const val DAY_MS = 86_400_000L

    fun index(nowMillis: Long = System.currentTimeMillis()): Int = ((nowMillis / DAY_MS + 3) / 7).toInt()

    fun mondayEpochDay(index: Int): Long = index * 7L - 3

    /** Whole days until the board resets: 1 on a Sunday, 7 on a Monday. */
    fun daysUntilNext(nowMillis: Long = System.currentTimeMillis()): Int =
        (mondayEpochDay(index(nowMillis) + 1) - nowMillis / DAY_MS).toInt()
}

package com.cashfluent.app.domain.league

import java.net.URLDecoder
import java.net.URLEncoder
import java.util.zip.CRC32

/**
 * Everything a league needs to know about one person, and nothing else: a random id
 * that stands in for an account, the nickname they typed, their points, and the rung
 * of the ladder they are on. It travels as one line of text — a message, not a request
 * to a server.
 */
data class LeagueCard(
    val id: String,
    val name: String,
    val totalPoints: Int,
    val week: Int,
    val weekPoints: Int,
    val tier: Tier,
) {
    /** A card from an earlier week has nothing on the board this week. */
    fun pointsThisWeek(currentWeek: Int): Int = if (week == currentWeek) weekPoints else 0
}

/**
 * Weeks are counted from Monday 5 January 1970, in UTC, so two phones agree on which
 * week a card belongs to without either of them asking anyone. Leagues reset on Monday.
 */
object Week {

    private const val DAY_MS = 86_400_000L

    fun index(nowMillis: Long = System.currentTimeMillis()): Int = ((nowMillis / DAY_MS + 3) / 7).toInt()

    fun mondayEpochDay(index: Int): Long = index * 7L - 3
}

/**
 * The card as text: `CF1|id|name|total|week|weekPoints|tier|crc`.
 *
 * The checksum catches a card mangled in transit; it is not a signature, and a league
 * of friends is an honour system, like a scoreboard on paper. Everything read from a
 * card is bounded before it is believed, because a card is text someone else typed.
 */
object LeagueCards {

    const val PREFIX = "CF1"
    const val MAX_NAME = 20
    const val MAX_POINTS = 9_999_999
    const val MAX_WEEK = 999_999

    private const val SEPARATOR = "|"
    private val ID = Regex("[0-9a-f]{8}")
    private val TOKEN = Regex(
        """CF1\|[0-9a-f]{8}\|[A-Za-z0-9%+*._-]{0,120}\|\d{1,7}\|\d{1,6}\|\d{1,7}\|[0-7]\|[0-9a-f]{8}""",
    )

    /** What a nickname is allowed to be: printable, no separator, at most [MAX_NAME] characters. */
    fun cleanName(raw: String): String = raw.filter { it >= ' ' && it != '|' }.trim().take(MAX_NAME)

    fun encode(card: LeagueCard): String {
        val payload = listOf(
            PREFIX,
            card.id,
            URLEncoder.encode(cleanName(card.name), "UTF-8"),
            card.totalPoints.coerceIn(0, MAX_POINTS),
            card.week.coerceIn(0, MAX_WEEK),
            card.weekPoints.coerceIn(0, MAX_POINTS),
            card.tier.digit,
        ).joinToString(SEPARATOR)
        return payload + SEPARATOR + checksum(payload)
    }

    /** Null for anything that is not a well-formed, intact card. Never throws. */
    fun decode(token: String): LeagueCard? {
        val fields = token.trim().split('|')
        if (fields.size != 8 || fields[0] != PREFIX) return null
        val payload = fields.dropLast(1).joinToString(SEPARATOR)
        if (fields[7] != checksum(payload)) return null

        val id = fields[1].takeIf { ID.matches(it) } ?: return null
        val name = runCatching { cleanName(URLDecoder.decode(fields[2], "UTF-8")) }
            .getOrNull()?.takeIf { it.isNotBlank() } ?: return null
        val total = fields[3].toIntOrNull()?.takeIf { it in 0..MAX_POINTS } ?: return null
        val week = fields[4].toIntOrNull()?.takeIf { it in 0..MAX_WEEK } ?: return null
        val weekPoints = fields[5].toIntOrNull()?.takeIf { it in 0..total } ?: return null
        val tier = fields[6].takeIf { it.length == 1 && it[0] in '0'..'7' }?.let { Tier.fromDigit(it[0]) } ?: return null

        return LeagueCard(id, name, total, week, weekPoints, tier)
    }

    /** Every intact card in a pasted message, one per person, in the order they appear. */
    fun findAll(text: String): List<LeagueCard> =
        TOKEN.findAll(text).mapNotNull { decode(it.value) }.distinctBy { it.id }.toList()

    private fun checksum(payload: String): String {
        val crc = CRC32()
        crc.update(payload.toByteArray(Charsets.UTF_8))
        return crc.value.toString(16).padStart(8, '0')
    }
}

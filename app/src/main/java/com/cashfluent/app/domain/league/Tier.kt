package com.cashfluent.app.domain.league

/**
 * The ladder. Everyone starts in Wood; a week in the promotion zone moves you up one
 * rung, a week in the demotion zone moves you down one. Elite has nothing above it.
 */
enum class Tier(val label: String) {
    WOOD("Wood"),
    BRONZE("Bronze"),
    SILVER("Silver"),
    GOLD("Gold"),
    RUBY("Ruby"),
    EMERALD("Emerald"),
    DIAMOND("Diamond"),
    ELITE("Elite");

    val next: Tier? get() = entries.getOrNull(ordinal + 1)
    val previous: Tier? get() = entries.getOrNull(ordinal - 1)

    companion object {
        val FIRST = WOOD

        /** Anything unexpected reads as the first rung, rather than throwing at a screen. */
        fun fromName(name: String?): Tier = entries.firstOrNull { it.name == name } ?: FIRST

        fun fromOrdinal(index: Int?): Tier = entries.getOrNull(index ?: -1) ?: FIRST
    }
}

enum class Zone { PROMOTION, SAFE, DEMOTION }

enum class Movement { PROMOTED, STAYED, DEMOTED }

/** What a finished week did to you, kept so the board can say it once. */
data class WeekOutcome(
    val week: Int,
    val from: Tier,
    val to: Tier,
    val position: Int,
    val size: Int,
    val weekPoints: Int,
) {
    val movement: Movement
        get() = when {
            to > from -> Movement.PROMOTED
            to < from -> Movement.DEMOTED
            else -> Movement.STAYED
        }
}

/**
 * The rules of the week, Duolingo-style. Top five go up. In a league big enough to have
 * a bottom, the bottom five go down. Nobody with zero points holds their place — a league
 * is for people who played.
 */
object Promotion {

    const val LEAGUE_SIZE = 20
    const val PROMOTE_TOP = 5
    const val DEMOTE_BOTTOM = 5

    /** Below this many people there is no relegation zone, only inactivity. */
    const val MIN_SIZE_TO_DEMOTE = 10

    fun zone(position: Int, size: Int, tier: Tier, weekPoints: Int): Zone = when {
        weekPoints <= 0 && tier.previous != null -> Zone.DEMOTION
        position <= PROMOTE_TOP && tier.next != null && weekPoints > 0 -> Zone.PROMOTION
        size >= MIN_SIZE_TO_DEMOTE && position > size - DEMOTE_BOTTOM && tier.previous != null -> Zone.DEMOTION
        else -> Zone.SAFE
    }

    fun outcome(week: Int, tier: Tier, position: Int, size: Int, weekPoints: Int): WeekOutcome {
        val to = when (zone(position, size, tier, weekPoints)) {
            Zone.PROMOTION -> tier.next ?: tier
            Zone.DEMOTION -> tier.previous ?: tier
            Zone.SAFE -> tier
        }
        return WeekOutcome(week, tier, to, position, size, weekPoints)
    }

    /** "1st", "2nd", "3rd", "11th", "22nd". */
    fun ordinal(position: Int): String {
        val suffix = when {
            position % 100 in 11..13 -> "th"
            position % 10 == 1 -> "st"
            position % 10 == 2 -> "nd"
            position % 10 == 3 -> "rd"
            else -> "th"
        }
        return "$position$suffix"
    }
}

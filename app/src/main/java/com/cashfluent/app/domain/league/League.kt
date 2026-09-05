package com.cashfluent.app.domain.league

/** One row of the board. */
data class Standing(
    val entrant: Entrant,
    val position: Int,
    val isYou: Boolean,
    val zone: Zone,
) {
    val weekPoints: Int get() = entrant.weekPoints
}

/**
 * A league is up to twenty people on the same rung in the same week, ranked on this
 * week's points — because a place in a list of twenty is legible and a place in a list
 * of a million is not. The zones follow the rules in [Promotion].
 *
 * Who sits with whom is decided by the server as people turn up: the first twenty on a
 * rung fill one league, the next twenty open another. The ids below name those leagues
 * so that every phone, and the rules on the server, spell them the same way.
 */
object League {

    const val SIZE = Promotion.LEAGUE_SIZE

    fun standings(entrants: List<Entrant>, yourId: String, tier: Tier): List<Standing> {
        val ordered = entrants.distinctBy { it.id }.sortedWith(
            compareByDescending<Entrant> { it.weekPoints }
                .thenByDescending { it.totalPoints }
                .thenBy { it.name.lowercase() }
                .thenBy { it.id },
        )
        return ordered.mapIndexed { index, entrant ->
            val position = index + 1
            Standing(entrant, position, entrant.id == yourId, Promotion.zone(position, ordered.size, tier, entrant.weekPoints))
        }
    }

    /**
     * What a closed board did to you. Someone whose row is not on it — the seat was
     * taken, the phone never came back — counts as last with nothing, which is what the
     * rule about zero points is for.
     */
    fun outcome(board: List<Entrant>, yourId: String, week: Int, tier: Tier): WeekOutcome {
        val mine = standings(board, yourId, tier).firstOrNull { it.isYou }
        return if (mine != null) {
            Promotion.outcome(week, tier, mine.position, board.size, mine.weekPoints)
        } else {
            Promotion.outcome(week, tier, board.size + 1, board.size + 1, 0)
        }
    }

    /**
     * The points that would carry you into the promotion zone: enough to pass whoever is
     * last inside it. Null when you are already there, when there is no zone to climb
     * into, or when the board is too small for the question to mean anything.
     */
    fun gapToPromotion(standings: List<Standing>, yourId: String): Int? {
        val you = standings.firstOrNull { it.entrant.id == yourId } ?: return null
        if (you.zone == Zone.PROMOTION) return null
        val cutoff = standings.getOrNull(Promotion.PROMOTE_TOP - 1) ?: return null
        if (cutoff.entrant.id == yourId) return null
        return (cutoff.weekPoints - you.weekPoints + 1).coerceAtLeast(1)
    }

    /**
     * You and your neighbours: the person above, you, the person below. At the top or the
     * bottom of the board it slides to keep the same number of rows, so the block never
     * changes height as you climb.
     */
    fun around(standings: List<Standing>, yourId: String, rows: Int = 3): List<Standing> {
        if (standings.size <= rows) return standings
        val index = standings.indexOfFirst { it.entrant.id == yourId }
        if (index < 0) return standings.take(rows)
        val start = (index - rows / 2).coerceIn(0, standings.size - rows)
        return standings.subList(start, start + rows)
    }

    /** `w2957-gold`: the queue every phone on a rung joins in a given week. */
    fun lobbyId(week: Int, tier: Tier): String = "w$week-${tier.name.lowercase()}"

    /** `w2957-gold-3`: the third league of twenty to open on that rung that week. */
    fun boardId(week: Int, tier: Tier, ordinal: Int): String = "${lobbyId(week, tier)}-$ordinal"
}

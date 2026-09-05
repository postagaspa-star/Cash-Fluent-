package com.cashfluent.app.domain.league

/** One row of the board. */
data class Standing(
    val card: LeagueCard,
    val position: Int,
    val isYou: Boolean,
    val weekPoints: Int,
)

data class MergeResult(
    val friends: List<LeagueCard>,
    val added: Int,
    val updated: Int,
    val unchanged: Int,
    val refusedFull: Int,
    val yourself: Int,
) {
    val changed: Int get() = added + updated
}

/**
 * A league is the people whose cards you have on your phone, and you. Twenty at most,
 * ranked on this week's points, because a place in a list of twenty is legible and a
 * place in a list of a million is not.
 */
object League {

    const val SIZE = 20
    const val MAX_FRIENDS = SIZE - 1

    fun standings(you: LeagueCard, friends: List<LeagueCard>, currentWeek: Int): List<Standing> {
        val everyone = friends.filter { it.id != you.id } + you
        val ordered = everyone.sortedWith(
            compareByDescending<LeagueCard> { it.pointsThisWeek(currentWeek) }
                .thenByDescending { it.totalPoints }
                .thenBy { it.name.lowercase() },
        )
        return ordered.mapIndexed { index, card ->
            Standing(card, index + 1, card.id == you.id, card.pointsThisWeek(currentWeek))
        }
    }

    /** Newer wins: a later week, or the same week with at least as many points. */
    fun isNewer(incoming: LeagueCard, existing: LeagueCard): Boolean =
        incoming.week > existing.week ||
            (incoming.week == existing.week && incoming.totalPoints >= existing.totalPoints)

    /**
     * Fold pasted cards into the friends you already have: a known id is refreshed if
     * the card is newer, a new one is added while there is room, your own is skipped.
     */
    fun merge(existing: List<LeagueCard>, incoming: List<LeagueCard>, yourId: String): MergeResult {
        val byId = LinkedHashMap(existing.associateBy { it.id })
        var added = 0
        var updated = 0
        var unchanged = 0
        var refusedFull = 0
        var yourself = 0
        for (card in incoming) {
            val known = byId[card.id]
            when {
                card.id == yourId -> yourself++
                known != null -> if (card != known && isNewer(card, known)) {
                    byId[card.id] = card
                    updated++
                } else {
                    unchanged++
                }
                byId.size >= MAX_FRIENDS -> refusedFull++
                else -> {
                    byId[card.id] = card
                    added++
                }
            }
        }
        return MergeResult(byId.values.toList(), added, updated, unchanged, refusedFull, yourself)
    }
}

package com.cashfluent.app.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.cashfluent.app.data.model.GameOutcome
import com.cashfluent.app.data.model.Player
import com.cashfluent.app.domain.league.League
import com.cashfluent.app.domain.league.LeagueCard
import com.cashfluent.app.domain.league.LeagueCards
import com.cashfluent.app.domain.league.MergeResult
import com.cashfluent.app.domain.league.Promotion
import com.cashfluent.app.domain.league.Tier
import com.cashfluent.app.domain.league.Week
import com.cashfluent.app.domain.league.WeekOutcome
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.util.UUID

private val Context.playerStore: DataStore<Preferences> by preferencesDataStore(name = "cashfluent_player")

/**
 * Points, the rung of the ladder, the nickname on your card and the cards of your
 * friends. On the device, like everything else. The only thing that ever leaves is the
 * card the person chooses to share — and it leaves through whatever app they hand it to.
 *
 * The week closes lazily: the first write or look after a Monday settles last week —
 * where you finished, whether you moved — and starts this one from zero.
 */
class PlayerRepository(
    private val context: Context,
    private val now: () -> Long = System::currentTimeMillis,
) {

    val player: Flow<Player> = context.playerStore.data.map { it.toPlayer(Week.index(now())) }

    /**
     * The result of the last paste or share-in, for the league screen to announce once.
     * Cards can arrive while that screen is not on show — shared from another app — so
     * it lives here rather than in a view model.
     */
    private val _lastImport = MutableStateFlow<MergeResult?>(null)
    val lastImport: StateFlow<MergeResult?> = _lastImport

    fun clearLastImport() {
        _lastImport.value = null
    }

    /** Mints the id if needed and settles a finished week. Safe to call as often as you like. */
    suspend fun settle() {
        val week = Week.index(now())
        context.playerStore.edit {
            it.ensureId()
            it.rollOverTo(week)
        }
    }

    suspend fun setName(name: String) {
        context.playerStore.edit {
            it.ensureId()
            it[NAME] = LeagueCards.cleanName(name)
        }
    }

    /** Adds a finished game to the record and says what changed. */
    suspend fun recordGame(gameId: String, score: Int): GameOutcome {
        val week = Week.index(now())
        var outcome = GameOutcome(score, score, true, score, score)
        context.playerStore.edit { prefs ->
            prefs.ensureId()
            prefs.rollOverTo(week)
            val previousBest = prefs[bestKey(gameId)] ?: 0
            val best = maxOf(previousBest, score)
            prefs[bestKey(gameId)] = best
            val total = (prefs[TOTAL] ?: 0) + score
            val weekPoints = (prefs[WEEK_POINTS] ?: 0) + score
            prefs[TOTAL] = total
            prefs[GAMES] = (prefs[GAMES] ?: 0) + 1
            prefs[WEEK_POINTS] = weekPoints
            outcome = GameOutcome(score, best, score > previousBest, weekPoints, total)
        }
        return outcome
    }

    /** Folds every card found in [text] into the league. Returns what happened, and remembers it. */
    suspend fun importCards(text: String): MergeResult {
        val incoming = LeagueCards.findAll(text)
        var result = MergeResult(emptyList(), 0, 0, 0, 0, 0)
        context.playerStore.edit { prefs ->
            prefs.ensureId()
            result = League.merge(prefs.friends(), incoming, yourId = prefs[ID].orEmpty())
            prefs[FRIENDS] = result.friends.map(LeagueCards::encode).toSet()
        }
        _lastImport.value = result
        return result
    }

    suspend fun removeFriend(id: String) {
        context.playerStore.edit { prefs ->
            prefs[FRIENDS] = prefs.friends().filterNot { it.id == id }.map(LeagueCards::encode).toSet()
        }
    }

    /** Your card, ready to hand to another app. */
    suspend fun myCard(): LeagueCard {
        settle()
        return player.first().card
    }

    /** Last week's verdict has been read; stop showing it. */
    suspend fun dismissOutcome() {
        context.playerStore.edit { it.remove(LAST_OUTCOME) }
    }

    /** Points, bests and the ladder go; the name, the id and the friends stay. */
    suspend fun resetScores() {
        context.playerStore.edit { prefs ->
            prefs.asMap().keys.filter { it.name.startsWith(BEST_PREFIX) }.forEach { prefs.remove(it) }
            prefs.remove(TOTAL)
            prefs.remove(GAMES)
            prefs.remove(WEEK)
            prefs.remove(WEEK_POINTS)
            prefs.remove(TIER)
            prefs.remove(LAST_OUTCOME)
        }
    }

    private companion object {
        val ID = stringPreferencesKey("player_id")
        val NAME = stringPreferencesKey("player_name")
        val TOTAL = intPreferencesKey("points_total")
        val GAMES = intPreferencesKey("games_played")
        val WEEK = intPreferencesKey("points_week")
        val WEEK_POINTS = intPreferencesKey("points_week_total")
        val TIER = stringPreferencesKey("league_tier")
        val LAST_OUTCOME = stringPreferencesKey("league_last_outcome")
        val FRIENDS = stringSetPreferencesKey("league_friends")
        const val BEST_PREFIX = "best_"

        fun bestKey(gameId: String) = intPreferencesKey("$BEST_PREFIX$gameId")

        fun MutablePreferences.ensureId() {
            if (this[ID].isNullOrBlank()) {
                this[ID] = UUID.randomUUID().toString().replace("-", "").take(8)
            }
        }

        /**
         * Closes the stored week if a newer one has begun: your finishing place among the
         * cards you held decides the rung you start the new week on. One step at most,
         * however many weeks went by — a long absence costs one rung, not the ladder.
         */
        fun MutablePreferences.rollOverTo(currentWeek: Int) {
            val stored = this[WEEK]
            if (stored == null) {
                this[WEEK] = currentWeek
                return
            }
            if (stored >= currentWeek) return
            val tier = Tier.fromName(this[TIER])
            val me = LeagueCard(
                id = this[ID].orEmpty(),
                name = this[NAME].orEmpty(),
                totalPoints = this[TOTAL] ?: 0,
                week = stored,
                weekPoints = this[WEEK_POINTS] ?: 0,
                tier = tier,
            )
            val board = League.standings(me, friends(), stored, tier)
            val mine = board.first { it.isYou }
            val outcome = Promotion.outcome(stored, tier, mine.position, board.size, mine.weekPoints)
            this[TIER] = outcome.to.name
            this[LAST_OUTCOME] = encode(outcome)
            this[WEEK] = currentWeek
            this[WEEK_POINTS] = 0
        }

        fun Preferences.friends(): List<LeagueCard> =
            (this[FRIENDS] ?: emptySet()).mapNotNull(LeagueCards::decode)

        fun encode(outcome: WeekOutcome): String =
            listOf(outcome.week, outcome.from.name, outcome.to.name, outcome.position, outcome.size, outcome.weekPoints)
                .joinToString("|")

        fun decodeOutcome(raw: String?): WeekOutcome? {
            val fields = raw?.split('|') ?: return null
            if (fields.size != 6) return null
            return WeekOutcome(
                week = fields[0].toIntOrNull() ?: return null,
                from = Tier.fromName(fields[1]),
                to = Tier.fromName(fields[2]),
                position = fields[3].toIntOrNull() ?: return null,
                size = fields[4].toIntOrNull() ?: return null,
                weekPoints = fields[5].toIntOrNull() ?: return null,
            )
        }

        fun Preferences.toPlayer(currentWeek: Int): Player = Player(
            id = this[ID].orEmpty(),
            name = this[NAME].orEmpty(),
            totalPoints = this[TOTAL] ?: 0,
            week = currentWeek,
            weekPoints = if (this[WEEK] == currentWeek) this[WEEK_POINTS] ?: 0 else 0,
            gamesPlayed = this[GAMES] ?: 0,
            tier = Tier.fromName(this[TIER]),
            lastOutcome = decodeOutcome(this[LAST_OUTCOME]),
            bests = asMap().filterKeys { it.name.startsWith(BEST_PREFIX) }
                .map { (key, value) -> key.name.removePrefix(BEST_PREFIX) to ((value as? Int) ?: 0) }
                .toMap(),
            friends = friends(),
        )
    }
}

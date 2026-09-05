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
import com.cashfluent.app.domain.game.Medal
import com.cashfluent.app.domain.league.League
import com.cashfluent.app.domain.league.LeagueCard
import com.cashfluent.app.domain.league.LeagueCards
import com.cashfluent.app.domain.league.MergeResult
import com.cashfluent.app.domain.league.Week
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.util.UUID

private val Context.playerStore: DataStore<Preferences> by preferencesDataStore(name = "cashfluent_player")

/**
 * Points, medals, the nickname on your card and the cards of your friends. On the
 * device, like everything else. The only thing that ever leaves is the card the person
 * chooses to share — and it leaves through whatever app they hand it to, not through
 * this one, which still has no way to reach the network.
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

    /** Mints the id if it does not exist yet. Safe to call as often as you like. */
    suspend fun ensureId() {
        context.playerStore.edit { it.ensureId() }
    }

    suspend fun setName(name: String) {
        context.playerStore.edit {
            it.ensureId()
            it[NAME] = LeagueCards.cleanName(name)
        }
    }

    /** Adds a finished game to the record and says what changed. */
    suspend fun recordGame(moduleId: String, score: Int): GameOutcome {
        val currentWeek = Week.index(now())
        var outcome = GameOutcome(score, score, true, Medal.forScore(score), Medal.NONE)
        context.playerStore.edit { prefs ->
            prefs.ensureId()
            val previousBest = prefs[bestKey(moduleId)] ?: 0
            val best = maxOf(previousBest, score)
            prefs[bestKey(moduleId)] = best
            prefs[TOTAL] = (prefs[TOTAL] ?: 0) + score
            prefs[GAMES] = (prefs[GAMES] ?: 0) + 1
            // A new week starts from nothing; the old total is not carried over.
            val weekPoints = if (prefs[WEEK] == currentWeek) prefs[WEEK_POINTS] ?: 0 else 0
            prefs[WEEK] = currentWeek
            prefs[WEEK_POINTS] = weekPoints + score
            outcome = GameOutcome(
                score = score,
                best = best,
                newBest = score > previousBest,
                medal = Medal.forScore(best),
                medalBefore = Medal.forScore(previousBest),
            )
        }
        return outcome
    }

    /** Folds every card found in [text] into the league. Returns what happened, and remembers it. */
    suspend fun importCards(text: String): MergeResult {
        val incoming = LeagueCards.findAll(text)
        var result = MergeResult(emptyList(), 0, 0, 0, 0, 0)
        context.playerStore.edit { prefs ->
            prefs.ensureId()
            val existing = prefs.friends()
            result = League.merge(existing, incoming, yourId = prefs[ID].orEmpty())
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

    /** Your card, as text, ready to hand to another app. */
    suspend fun myCard(moduleIds: List<String>): LeagueCard {
        ensureId()
        return player.first().card(moduleIds)
    }

    /** Points and medals go; the name, the id and the friends stay. */
    suspend fun resetScores() {
        context.playerStore.edit { prefs ->
            prefs.asMap().keys.filter { it.name.startsWith(BEST_PREFIX) }.forEach { prefs.remove(it) }
            prefs.remove(TOTAL)
            prefs.remove(GAMES)
            prefs.remove(WEEK)
            prefs.remove(WEEK_POINTS)
        }
    }

    private companion object {
        val ID = stringPreferencesKey("player_id")
        val NAME = stringPreferencesKey("player_name")
        val TOTAL = intPreferencesKey("points_total")
        val GAMES = intPreferencesKey("games_played")
        val WEEK = intPreferencesKey("points_week")
        val WEEK_POINTS = intPreferencesKey("points_week_total")
        val FRIENDS = stringSetPreferencesKey("league_friends")
        const val BEST_PREFIX = "best_"

        fun bestKey(moduleId: String) = intPreferencesKey("$BEST_PREFIX$moduleId")

        fun MutablePreferences.ensureId() {
            if (this[ID].isNullOrBlank()) {
                this[ID] = UUID.randomUUID().toString().replace("-", "").take(8)
            }
        }

        fun Preferences.friends(): List<LeagueCard> =
            (this[FRIENDS] ?: emptySet()).mapNotNull(LeagueCards::decode)

        fun Preferences.toPlayer(currentWeek: Int): Player = Player(
            id = this[ID].orEmpty(),
            name = this[NAME].orEmpty(),
            totalPoints = this[TOTAL] ?: 0,
            week = currentWeek,
            weekPoints = if (this[WEEK] == currentWeek) this[WEEK_POINTS] ?: 0 else 0,
            gamesPlayed = this[GAMES] ?: 0,
            bests = asMap().filterKeys { it.name.startsWith(BEST_PREFIX) }
                .map { (key, value) -> key.name.removePrefix(BEST_PREFIX) to ((value as? Int) ?: 0) }
                .toMap(),
            friends = friends(),
        )
    }
}

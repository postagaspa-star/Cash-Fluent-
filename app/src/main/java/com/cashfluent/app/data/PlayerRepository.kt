package com.cashfluent.app.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.cashfluent.app.data.league.PlayerStore
import com.cashfluent.app.data.model.Player
import com.cashfluent.app.data.model.Unsettled
import com.cashfluent.app.domain.league.Tier
import com.cashfluent.app.domain.league.WeekOutcome
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.playerStore: DataStore<Preferences> by preferencesDataStore(name = "cashfluent_player")

/**
 * The player's record on disk: points, bests, rung, nickname, this week's seat. One
 * preferences file, read as a whole and written as a whole, so [update] is the only way
 * to change it and every change is one atomic edit. The rules of the week live in
 * [com.cashfluent.app.data.league.LeagueService]; this class only remembers.
 */
class PlayerRepository(private val context: Context) : PlayerStore {

    override val player: Flow<Player> = context.playerStore.data.map { it.toPlayer() }

    override suspend fun update(transform: (Player) -> Player): Player {
        var result = Player()
        context.playerStore.edit { prefs ->
            val next = transform(prefs.toPlayer())
            prefs.clear()
            prefs.write(next)
            result = next
        }
        return result
    }

    private companion object {
        val ID = stringPreferencesKey("player_uid")
        val NAME = stringPreferencesKey("player_name")
        val TOTAL = intPreferencesKey("points_total")
        val GAMES = intPreferencesKey("games_played")
        val WEEK = intPreferencesKey("points_week")
        val WEEK_POINTS = intPreferencesKey("points_week_total")
        val TIER = stringPreferencesKey("league_tier")
        val LAST_OUTCOME = stringPreferencesKey("league_last_outcome")
        val LEAGUE_ID = stringPreferencesKey("league_id")
        val UNSETTLED_ID = stringPreferencesKey("league_unsettled_id")
        val UNSETTLED_WEEK = intPreferencesKey("league_unsettled_week")
        const val BEST_PREFIX = "best_"

        fun bestKey(gameId: String) = intPreferencesKey("$BEST_PREFIX$gameId")

        fun Preferences.toPlayer(): Player = Player(
            id = this[ID].orEmpty(),
            name = this[NAME].orEmpty(),
            totalPoints = this[TOTAL] ?: 0,
            week = this[WEEK] ?: 0,
            weekPoints = this[WEEK_POINTS] ?: 0,
            gamesPlayed = this[GAMES] ?: 0,
            tier = Tier.fromName(this[TIER]),
            lastOutcome = decodeOutcome(this[LAST_OUTCOME]),
            bests = asMap().filterKeys { it.name.startsWith(BEST_PREFIX) }
                .map { (key, value) -> key.name.removePrefix(BEST_PREFIX) to ((value as? Int) ?: 0) }
                .toMap(),
            leagueId = this[LEAGUE_ID],
            unsettled = this[UNSETTLED_ID]?.let { Unsettled(it, this[UNSETTLED_WEEK] ?: 0) },
        )

        fun MutablePreferences.write(player: Player) {
            this[ID] = player.id
            this[NAME] = player.name
            this[TOTAL] = player.totalPoints
            this[GAMES] = player.gamesPlayed
            this[WEEK] = player.week
            this[WEEK_POINTS] = player.weekPoints
            this[TIER] = player.tier.name
            player.lastOutcome?.let { this[LAST_OUTCOME] = encode(it) }
            player.leagueId?.let { this[LEAGUE_ID] = it }
            player.unsettled?.let {
                this[UNSETTLED_ID] = it.leagueId
                this[UNSETTLED_WEEK] = it.week
            }
            player.bests.forEach { (gameId, best) -> this[bestKey(gameId)] = best }
        }

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
    }
}

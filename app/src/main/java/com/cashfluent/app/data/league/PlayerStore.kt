package com.cashfluent.app.data.league

import com.cashfluent.app.data.model.Player
import kotlinx.coroutines.flow.Flow

/**
 * Where the player's record is kept. The app keeps it in DataStore; the tests keep it in
 * memory. Every change goes through [update] so that a read-modify-write is one step and
 * two screens settling the week at the same moment cannot tread on each other.
 */
interface PlayerStore {

    val player: Flow<Player>

    /** Applies [transform] to what is stored, atomically, and returns what is stored now. */
    suspend fun update(transform: (Player) -> Player): Player
}

package com.cashfluent.app.data.firebase

import com.cashfluent.app.domain.league.Entrant
import com.cashfluent.app.domain.league.League
import com.cashfluent.app.domain.league.LeagueBackend
import com.cashfluent.app.domain.league.Nickname
import com.cashfluent.app.domain.league.Tier
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

/**
 * The league on Firebase: anonymous sign-in for the id, Firestore for the boards.
 *
 * Three collections. `lobbies/{week}-{rung}` is the queue a phone joins: it names the
 * league currently filling on that rung and how many seats are taken. `leagues/{id}` is
 * one board of twenty, and `leagues/{id}/members/{uid}` is one row on it — the only
 * document a phone ever writes about a person, and only about itself. What each may
 * contain is spelt out in `firestore.rules` at the root of the repository, and a row on
 * a board whose week has ended cannot be written at all.
 *
 * Writes are never waited for: Firestore queues them while the phone is offline and
 * sends them when it can, so a game scored on a train still reaches the board.
 */
class FirestoreLeagueBackend(
    private val auth: FirebaseAuth,
    private val db: FirebaseFirestore,
) : LeagueBackend {

    override suspend fun signIn(): String =
        auth.currentUser?.uid
            ?: auth.signInAnonymously().await().user?.uid
            ?: throw IllegalStateException("Signed in, but with no user")

    override suspend fun board(leagueId: String): List<Entrant> = members(leagueId).get().await().toEntrants()

    override fun watch(leagueId: String): Flow<List<Entrant>> = callbackFlow {
        val registration = members(leagueId).addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
            } else if (snapshot != null) {
                trySend(snapshot.toEntrants())
            }
        }
        awaitClose { registration.remove() }
    }

    /**
     * One transaction: read the queue, sit in the league it names — or open the next one
     * when that is full — and write your row. Two phones arriving for the last seat at
     * once are serialised by Firestore, so one of them opens the new league.
     */
    override suspend fun takeSeat(week: Int, tier: Tier, me: Entrant): String {
        val lobby = db.collection(LOBBIES).document(League.lobbyId(week, tier))
        return db.runTransaction { tx ->
            val queue = tx.get(lobby)
            val opened = queue.getLong(OPENED)?.toInt() ?: 0
            val seats = queue.getLong(SEATS)?.toInt() ?: 0
            val filling = queue.getString(LEAGUE)
            val leagueId: String
            if (filling == null || seats >= League.SIZE) {
                leagueId = League.boardId(week, tier, opened + 1)
                tx.set(lobby, mapOf(WEEK to week, TIER to tier.ordinal, LEAGUE to leagueId, SEATS to 1, OPENED to opened + 1))
                tx.set(
                    db.collection(LEAGUES).document(leagueId),
                    mapOf(WEEK to week, TIER to tier.ordinal, SEATS to 1, CREATED to FieldValue.serverTimestamp()),
                )
            } else {
                leagueId = filling
                tx.update(lobby, SEATS, seats + 1)
                tx.update(db.collection(LEAGUES).document(leagueId), SEATS, seats + 1)
            }
            tx.set(row(leagueId, me.id), me.toRow() + (JOINED to FieldValue.serverTimestamp()))
            leagueId
        }.await()
    }

    override fun publish(leagueId: String, me: Entrant) {
        row(leagueId, me.id).set(me.toRow(), SetOptions.merge())
    }

    override fun leave(leagueId: String, id: String) {
        row(leagueId, id).delete()
    }

    private fun members(leagueId: String): Query =
        db.collection(LEAGUES).document(leagueId).collection(MEMBERS)
            .orderBy(WEEK_POINTS, Query.Direction.DESCENDING)
            .limit(League.SIZE.toLong())

    private fun row(leagueId: String, id: String): DocumentReference =
        db.collection(LEAGUES).document(leagueId).collection(MEMBERS).document(id)

    private fun Entrant.toRow(): Map<String, Any> = mapOf(
        NAME to Nickname.clean(name),
        TOTAL to totalPoints,
        WEEK_POINTS to weekPoints,
        UPDATED to FieldValue.serverTimestamp(),
    )

    /** Rows are other people's text: bounded on the way in, whatever the rules already promised. */
    private fun QuerySnapshot.toEntrants(): List<Entrant> = documents.map { row ->
        Entrant(
            id = row.id,
            name = Nickname.clean(row.getString(NAME).orEmpty()),
            totalPoints = (row.getLong(TOTAL) ?: 0L).coerceIn(0L, MAX_POINTS).toInt(),
            weekPoints = (row.getLong(WEEK_POINTS) ?: 0L).coerceIn(0L, MAX_POINTS).toInt(),
        )
    }

    private companion object {
        const val LOBBIES = "lobbies"
        const val LEAGUES = "leagues"
        const val MEMBERS = "members"
        const val WEEK = "week"
        const val TIER = "tier"
        const val LEAGUE = "league"
        const val SEATS = "seats"
        const val OPENED = "opened"
        const val CREATED = "createdAt"
        const val JOINED = "joinedAt"
        const val UPDATED = "updatedAt"
        const val NAME = "name"
        const val TOTAL = "totalPoints"
        const val WEEK_POINTS = "weekPoints"
        const val MAX_POINTS = 9_999_999L
    }
}

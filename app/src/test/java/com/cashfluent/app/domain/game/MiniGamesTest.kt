package com.cashfluent.app.domain.game

import com.cashfluent.app.content.Modules
import com.cashfluent.app.domain.finance.Currency
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

/**
 * A mini-game is random by design, so these run every one across many seeds and check
 * the things that must hold for every deal: the answer sits inside its slider, the
 * options are distinct, nothing hard codes a currency, and the explanation shows numbers.
 */
class MiniGamesTest {

    private val symbols = Currency.entries.map { it.symbol }

    @Test
    fun `there are dozens of games, with unique ids, on every lesson's topic`() {
        assertTrue("${MiniGames.all.size} games", MiniGames.all.size >= 48)
        assertEquals(MiniGames.all.size, MiniGames.all.map { it.id }.toSet().size)
        Modules.all.forEach { module ->
            assertTrue("${module.id} has too few games", MiniGames.forTopic(module.id).size >= 4)
        }
        MiniGames.all.forEach { game ->
            assertTrue("${game.id} points at no lesson", Modules.byId(game.topicId) != null)
            assertEquals(game, MiniGames.byId(game.id))
        }
        assertEquals(null, MiniGames.byId("nope"))
    }

    @Test
    fun `every mechanic is used, and the two-way ones really have two options`() {
        Mechanic.entries.forEach { mechanic ->
            assertTrue("no game uses $mechanic", MiniGames.all.any { it.mechanic == mechanic })
        }
        MiniGames.all.filter { it.mechanic == Mechanic.TRUE_FALSE || it.mechanic == Mechanic.HIGHER_LOWER }
            .forEach { game ->
                game.game(5).rounds.forEach { round ->
                    assertTrue("${game.id} should deal choices", round is ChoiceRound)
                    assertEquals("${game.id}: two options", 2, (round as ChoiceRound).options.size)
                }
            }
        MiniGames.all.filter { it.mechanic == Mechanic.SLIDER }.forEach { game ->
            game.game(5).rounds.forEach { round -> assertTrue("${game.id} should deal numbers", round is NumberRound) }
        }
    }

    @Test
    fun `every game deals valid rounds for a hundred seeds`() {
        for (game in MiniGames.all) {
            for (seed in 1L..100L) {
                val dealt = game.game(seed)
                assertEquals(game.rounds, dealt.rounds.size)
                dealt.rounds.forEachIndexed { index, round ->
                    val where = "${game.id} seed $seed round $index"
                    assertTrue("$where: blank prompt", round.prompt.isNotBlank())
                    assertTrue("$where: blank explanation", round.explanation.isNotBlank())
                    assertTrue("$where: explanation has no numbers", round.explanation.any { it.isDigit() })
                    symbols.forEach { symbol ->
                        assertFalse("$where hard codes $symbol", round.prompt.contains(symbol))
                        assertFalse("$where hard codes $symbol", round.explanation.contains(symbol))
                    }
                    when (round) {
                        is NumberRound -> {
                            assertTrue("$where: answer ${round.truth} outside ${round.min}..${round.max}", round.truth in round.min..round.max)
                            assertTrue("$where: answer is not finite", round.truth.isFinite())
                            val stops = round.span / round.step
                            assertTrue("$where: $stops slider stops", stops in 2.0..1000.0)
                        }
                        is ChoiceRound -> round.options.forEach { option ->
                            assertTrue("$where: blank option", option.isNotBlank())
                            symbols.forEach { assertFalse("$where hard codes $it", option.contains(it)) }
                        }
                    }
                }
            }
        }
    }

    @Test
    fun `the same seed always deals the same game, and different seeds differ`() {
        MiniGames.all.forEach { game ->
            assertEquals(game.id, game.game(7), game.game(7))
            assertNotEquals(game.id, game.game(1), game.game(2))
        }
    }

    @Test
    fun `a round is never a question about words`() {
        // Every round puts numbers in front of the reader, in the prompt or in the options.
        MiniGames.all.forEach { game ->
            for (seed in 1L..60L) {
                game.game(seed).rounds.forEach { round ->
                    val shown = round.prompt + (round as? ChoiceRound)?.options?.joinToString().orEmpty()
                    assertTrue("${game.id}: ${round.prompt}", shown.any { it.isDigit() })
                }
            }
        }
    }

    @Test
    fun `surprise me never hands back the game you just played`() {
        val random = Random(3)
        repeat(50) {
            val picked = MiniGames.random(random, excludingId = "budget-future-share")
            assertNotEquals("budget-future-share", picked.id)
        }
    }

    @Test
    fun `a game is four rounds out of four hundred`() {
        assertEquals(4, GameRules.ROUNDS)
        assertEquals(400, GameRules.MAX_SCORE)
        MiniGames.all.forEach { assertEquals(it.id, 400, it.maxScore) }
    }
}

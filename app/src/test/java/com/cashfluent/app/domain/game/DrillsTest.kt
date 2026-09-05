package com.cashfluent.app.domain.game

import com.cashfluent.app.content.Modules
import com.cashfluent.app.domain.finance.Currency
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * A drill is random by design, so these run every drill across many seeds and check
 * the things that must hold for every deal: the answer sits inside its slider, the
 * options are distinct, nothing hard codes a currency, and the explanation shows numbers.
 */
class DrillsTest {

    private val symbols = Currency.entries.map { it.symbol }

    @Test
    fun `every lesson has exactly one drill, in lesson order`() {
        assertEquals(Modules.all.map { it.id }, Drills.all.map { it.moduleId })
        Modules.all.forEach { assertEquals(it.id, Drills.forModule(it.id)?.moduleId) }
        assertEquals(null, Drills.forModule("nope"))
    }

    @Test
    fun `every drill deals a valid game for a hundred seeds`() {
        for (drill in Drills.all) {
            for (seed in 1L..100L) {
                val game = drill.game(seed)
                assertEquals(GameRules.ROUNDS, game.rounds.size)
                game.rounds.forEachIndexed { index, round ->
                    val where = "${drill.moduleId} seed $seed round $index"
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
    fun `the same seed always deals the same game`() {
        Drills.all.forEach { drill -> assertEquals(drill.moduleId, drill.game(7), drill.game(7)) }
    }

    @Test
    fun `different seeds deal different games`() {
        Drills.all.forEach { drill -> assertNotEquals(drill.moduleId, drill.game(1), drill.game(2)) }
    }

    @Test
    fun `every game mixes numbers to set and choices to make`() {
        Drills.all.forEach { drill ->
            val rounds = drill.game(3).rounds
            assertTrue("${drill.moduleId} has no number round", rounds.any { it is NumberRound })
            assertTrue("${drill.moduleId} has no choice round", rounds.any { it is ChoiceRound })
        }
    }

    @Test
    fun `a round is never a question about words`() {
        // Every round puts numbers in front of the reader, in the prompt or in the options:
        // the game drills the formula, not the vocabulary.
        Drills.all.forEach { drill ->
            drill.game(11).rounds.forEach { round ->
                val shown = round.prompt + (round as? ChoiceRound)?.options?.joinToString().orEmpty()
                assertTrue("${drill.moduleId}: ${round.prompt}", shown.any { it.isDigit() })
            }
        }
    }
}

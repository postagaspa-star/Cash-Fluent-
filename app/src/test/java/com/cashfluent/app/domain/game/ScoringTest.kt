package com.cashfluent.app.domain.game

import org.junit.Assert.assertEquals
import org.junit.Test

class ScoringTest {

    private val thousand = NumberRound(
        prompt = "p", quantity = Quantity.AMOUNT, truth = 1_000.0,
        min = 0.0, max = 2_000.0, step = 20.0, explanation = "e",
    )

    @Test
    fun `within five percent is full marks`() {
        assertEquals(100, Scoring.number(thousand, 1_000.0))
        assertEquals(100, Scoring.number(thousand, 1_049.0))
        assertEquals(100, Scoring.number(thousand, 951.0))
        assertEquals(100, Scoring.number(thousand, 1_050.0))
    }

    @Test
    fun `the points fall in a straight line to nothing at half out`() {
        assertEquals(89, Scoring.number(thousand, 1_100.0))
        assertEquals(44, Scoring.number(thousand, 1_300.0))
        assertEquals(0, Scoring.number(thousand, 1_500.0))
        assertEquals(0, Scoring.number(thousand, 0.0))
        assertEquals(0, Scoring.number(thousand, 2_000.0))
    }

    @Test
    fun `an answer near zero is scored against the slider, not against zero`() {
        val realReturn = NumberRound(
            prompt = "p", quantity = Quantity.PERCENT_PRECISE, truth = 0.0,
            min = -0.08, max = 0.08, step = 0.001, explanation = "e",
        )
        assertEquals(100, Scoring.number(realReturn, 0.0001))
        assertEquals(42, Scoring.number(realReturn, 0.001))
        assertEquals(0, Scoring.number(realReturn, 0.01))
    }

    @Test
    fun `a choice is all or nothing`() {
        val round = ChoiceRound("p", listOf("a", "b", "c"), correctIndex = 1, explanation = "e")
        assertEquals(100, Scoring.choice(round, 1))
        assertEquals(0, Scoring.choice(round, 0))
        assertEquals(0, Scoring.choice(round, 2))
    }

    @Test
    fun `a round refuses an answer outside its own slider`() {
        val thrown = runCatching {
            NumberRound("p", Quantity.AMOUNT, truth = 3.0, min = 0.0, max = 2.0, step = 0.1, explanation = "e")
        }.exceptionOrNull()
        assertEquals(IllegalArgumentException::class, thrown!!::class)
    }
}

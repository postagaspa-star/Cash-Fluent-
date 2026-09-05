package com.cashfluent.app.domain.game

import kotlin.math.abs
import kotlin.math.roundToInt

/**
 * A round is worth up to 100. A number within 5% of the answer earns all of it, and
 * the points fall in a straight line to nothing at 50% out. The scale is the answer
 * itself, or a sliver of the slider's range when the answer is close to zero, so a
 * guess of 0.1% against a truth of 0.0% is not scored as infinitely wrong.
 */
object Scoring {

    const val MAX_ROUND = 100
    const val FULL_MARKS_WITHIN = 0.05
    const val NOTHING_BEYOND = 0.50

    fun number(round: NumberRound, guess: Double): Int {
        val scale = maxOf(abs(round.truth), round.span * 0.02)
        val error = abs(guess - round.truth) / scale
        return when {
            error <= FULL_MARKS_WITHIN -> MAX_ROUND
            error >= NOTHING_BEYOND -> 0
            else -> {
                val fraction = (error - FULL_MARKS_WITHIN) / (NOTHING_BEYOND - FULL_MARKS_WITHIN)
                (MAX_ROUND * (1.0 - fraction)).roundToInt()
            }
        }
    }

    fun choice(round: ChoiceRound, picked: Int): Int =
        if (picked == round.correctIndex) MAX_ROUND else 0
}

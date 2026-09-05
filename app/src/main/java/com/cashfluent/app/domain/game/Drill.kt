package com.cashfluent.app.domain.game

import com.cashfluent.app.domain.finance.Money
import com.cashfluent.app.domain.game.drill.BudgetDrill
import com.cashfluent.app.domain.game.drill.CompoundDrill
import com.cashfluent.app.domain.game.drill.CreditDrill
import com.cashfluent.app.domain.game.drill.DebtDrill
import com.cashfluent.app.domain.game.drill.FeesDrill
import com.cashfluent.app.domain.game.drill.InflationDrill
import com.cashfluent.app.domain.game.drill.InstalmentsDrill
import com.cashfluent.app.domain.game.drill.MortgageDrill
import com.cashfluent.app.domain.game.drill.PayslipDrill
import com.cashfluent.app.domain.game.drill.SideIncomeDrill
import kotlin.math.abs
import kotlin.math.floor
import kotlin.math.log10
import kotlin.math.pow
import kotlin.random.Random

/**
 * One game per lesson. A drill turns the lesson's own calculator into rounds: every
 * answer is computed by the code the lesson was written against, so a game can never
 * disagree with the lesson that taught it. Nothing here asks what a word means — every
 * round is the formula, run on numbers the reader has not seen before.
 */
interface Drill {
    val moduleId: String

    /** Round number [index] of [GameRules.ROUNDS], with its numbers drawn from [random]. */
    fun round(random: Random, index: Int): Round
}

object GameRules {
    const val ROUNDS = 5
    const val MAX_SCORE = ROUNDS * Scoring.MAX_ROUND
}

data class Game(val moduleId: String, val rounds: List<Round>)

/** The same seed always deals the same game, which is what makes a drill testable. */
fun Drill.game(seed: Long): Game {
    val random = Random(seed)
    return Game(moduleId, List(GameRules.ROUNDS) { round(random, it) })
}

object Drills {

    val all: List<Drill> = listOf(
        BudgetDrill,
        CompoundDrill,
        DebtDrill,
        InflationDrill,
        FeesDrill,
        PayslipDrill,
        InstalmentsDrill,
        CreditDrill,
        SideIncomeDrill,
        MortgageDrill,
    )

    fun forModule(moduleId: String): Drill? = all.firstOrNull { it.moduleId == moduleId }
}

// ---- Shared by every drill. Internal so the screens cannot reach for them. ----------

internal fun <T> Random.pick(options: List<T>): T = options[nextInt(options.size)]

/** "{c}1,200" — the placeholder is replaced once, on screen, like the lessons. */
internal fun money(value: Double, decimals: Int = 0): String = Money.template(value, decimals)

/** "1,200" — a bare number inside a calculation. */
internal fun num(value: Double, decimals: Int = 0): String = Money.number(value, decimals)

internal fun pct(fraction: Double, decimals: Int = 0): String = Money.percent(fraction, decimals)

internal fun roundTo(value: Double, unit: Double): Double = Math.round(value / unit) * unit

/** The smallest of 1, 2 or 5 times a power of ten that is at least [x]. */
internal fun niceCeil(x: Double): Double {
    if (x <= 0.0) return 1.0
    val base = 10.0.pow(floor(log10(x)))
    val mantissa = x / base
    val nice = when {
        mantissa <= 1.0 -> 1.0
        mantissa <= 2.0 -> 2.0
        mantissa <= 5.0 -> 5.0
        else -> 10.0
    }
    return nice * base
}

/**
 * A number round whose slider holds the answer without pointing at it: the top of the
 * range is a round number between one and a half and three times the answer, in a
 * hundred even steps.
 */
internal fun numberRound(
    prompt: String,
    quantity: Quantity,
    truth: Double,
    random: Random,
    explanation: String,
): NumberRound {
    val top = niceCeil(abs(truth) * random.pick(listOf(1.5, 2.0, 2.5, 3.0)))
    return NumberRound(prompt, quantity, truth, min = 0.0, max = top, step = top / 100, explanation = explanation)
}

package com.cashfluent.app.domain.game

import com.cashfluent.app.domain.finance.Money
import com.cashfluent.app.domain.game.games.BudgetGames
import com.cashfluent.app.domain.game.games.CompoundGames
import com.cashfluent.app.domain.game.games.CreditGames
import com.cashfluent.app.domain.game.games.DebtGames
import com.cashfluent.app.domain.game.games.FeesGames
import com.cashfluent.app.domain.game.games.InflationGames
import com.cashfluent.app.domain.game.games.InstalmentsGames
import com.cashfluent.app.domain.game.games.MortgageGames
import com.cashfluent.app.domain.game.games.PayslipGames
import com.cashfluent.app.domain.game.games.SideIncomeGames
import kotlin.math.abs
import kotlin.math.floor
import kotlin.math.log10
import kotlin.math.pow
import kotlin.random.Random

/** How a mini-game is played. The label is what the catalogue shows next to the title. */
enum class Mechanic(val label: String) {
    SLIDER("set the number"),
    PICK("pick one"),
    HIGHER_LOWER("higher or lower"),
    TRUE_FALSE("true or false"),
}

/**
 * One mini-game: a name, a topic it draws on, a mechanic, and a recipe that deals a
 * fresh round from random numbers. Every answer is computed by the calculator in
 * `domain/finance`, so a game can never disagree with the lesson on the same topic.
 *
 * Games live in their own section and are not tied to finishing a lesson — they only
 * talk about the same things.
 */
data class MiniGame(
    val id: String,
    val topicId: String,
    val title: String,
    val blurb: String,
    val mechanic: Mechanic,
    val rounds: Int = GameRules.ROUNDS,
    val deal: (Random) -> Round,
) {
    val maxScore: Int get() = rounds * Scoring.MAX_ROUND
}

object GameRules {
    /** Rounds in one mini-game. Short on purpose: a game is a minute, not a lesson. */
    const val ROUNDS = 4
    const val MAX_SCORE = ROUNDS * Scoring.MAX_ROUND
}

data class Game(val miniGameId: String, val rounds: List<Round>)

/** The same seed always deals the same game, which is what makes a recipe testable. */
fun MiniGame.game(seed: Long): Game {
    val random = Random(seed)
    return Game(id, List(rounds) { deal(random) })
}

object MiniGames {

    val all: List<MiniGame> = listOf(
        BudgetGames.all,
        CompoundGames.all,
        DebtGames.all,
        InflationGames.all,
        FeesGames.all,
        PayslipGames.all,
        InstalmentsGames.all,
        CreditGames.all,
        SideIncomeGames.all,
        MortgageGames.all,
    ).flatten()

    private val byId: Map<String, MiniGame> = all.associateBy { it.id }

    fun byId(id: String): MiniGame? = byId[id]

    fun forTopic(topicId: String): List<MiniGame> = all.filter { it.topicId == topicId }

    /** Any game but [excludingId], for the "surprise me" button. */
    fun random(random: Random, excludingId: String? = null): MiniGame {
        val pool = all.filter { it.id != excludingId }.ifEmpty { all }
        return pool[random.nextInt(pool.size)]
    }
}

// ---- Shared by every recipe. Internal so the screens cannot reach for them. ---------

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

/** A statement to judge. The truth is computed, never typed. */
internal fun trueFalse(prompt: String, isTrue: Boolean, explanation: String): ChoiceRound =
    ChoiceRound(prompt, listOf("True", "False"), if (isTrue) 0 else 1, explanation)

/** Two things to compare; [firstIsHigher] is computed from the calculator. */
internal fun higherLower(
    prompt: String,
    first: String,
    second: String,
    firstIsHigher: Boolean,
    explanation: String,
): ChoiceRound = ChoiceRound(prompt, listOf(first, second), if (firstIsHigher) 0 else 1, explanation)

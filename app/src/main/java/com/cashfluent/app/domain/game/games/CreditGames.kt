package com.cashfluent.app.domain.game.games

import com.cashfluent.app.domain.finance.Credit
import com.cashfluent.app.domain.game.ChoiceRound
import com.cashfluent.app.domain.game.Mechanic
import com.cashfluent.app.domain.game.MiniGame
import com.cashfluent.app.domain.game.NumberRound
import com.cashfluent.app.domain.game.Quantity
import com.cashfluent.app.domain.game.Round
import com.cashfluent.app.domain.game.money
import com.cashfluent.app.domain.game.num
import com.cashfluent.app.domain.game.numberRound
import com.cashfluent.app.domain.game.pct
import com.cashfluent.app.domain.game.pick
import com.cashfluent.app.domain.game.roundTo
import com.cashfluent.app.domain.game.trueFalse
import kotlin.math.abs
import kotlin.random.Random

/** Topic 08, the credit record. The ratio, the payment that moves it, and the day the photo is taken. */
object CreditGames {

    private const val TOPIC = "credit-record"

    private class Card(random: Random) {
        val limit = random.pick(listOf(500.0, 1_000.0, 1_500.0, 2_000.0, 3_000.0))
        val balance = roundTo(limit * random.pick(listOf(0.45, 0.6, 0.75)), 10.0)
        val utilisation = Credit.utilisation(balance, limit)
        val ceiling = Credit.balanceCeiling(limit)
    }

    val all: List<MiniGame> = listOf(
        MiniGame(
            id = "credit-utilisation", topicId = TOPIC, mechanic = Mechanic.SLIDER,
            title = "The ratio",
            blurb = "Balance on statement day over the limit. The number that gets recorded.",
            deal = ::utilisation,
        ),
        MiniGame(
            id = "credit-pay-to-30", topicId = TOPIC, mechanic = Mechanic.SLIDER,
            title = "Pay down to 30%",
            blurb = "The one formula rearranged into a number you can actually pay.",
            deal = ::payToTarget,
        ),
        MiniGame(
            id = "credit-best-move", topicId = TOPIC, mechanic = Mechanic.PICK,
            title = "Best move",
            blurb = "Pay some, get more limit, or close the old card. Only one is a trap.",
            deal = ::bestMove,
        ),
        MiniGame(
            id = "credit-ceiling", topicId = TOPIC, mechanic = Mechanic.SLIDER,
            title = "The ceiling",
            blurb = "The most you can owe on statement day and still read as 30%.",
            deal = ::ceiling,
        ),
        MiniGame(
            id = "credit-snapshot", topicId = TOPIC, mechanic = Mechanic.PICK,
            title = "The photograph",
            blurb = "Paid in full, days before it was due. So what got recorded?",
            deal = ::snapshot,
        ),
        MiniGame(
            id = "credit-true-false", topicId = TOPIC, mechanic = Mechanic.TRUE_FALSE,
            title = "Credit file claims",
            blurb = "Statements about limits, timing and old cards. Judge each one.",
            deal = ::claims,
        ),
    )

    private fun utilisation(random: Random): Round {
        val c = Card(random)
        return NumberRound(
            prompt = "${money(c.balance)} is on the card when the statement is cut, on a ${money(c.limit)} limit. What " +
                "utilisation gets recorded?",
            quantity = Quantity.PERCENT,
            truth = c.utilisation,
            min = 0.0, max = 1.0, step = 0.01,
            explanation = "${num(c.balance)} ÷ ${num(c.limit)} = ${pct(c.utilisation)}. Above 30% reads as heavy use, " +
                "however promptly you pay afterwards.",
        )
    }

    private fun payToTarget(random: Random): Round {
        val c = Card(random)
        val pay = Credit.paymentToReach(c.balance, c.limit)
        return numberRound(
            prompt = "${money(c.balance)} on a ${money(c.limit)} limit. How much do you pay before the statement date " +
                "to be recorded at 30%?",
            quantity = Quantity.AMOUNT,
            truth = pay,
            random = random,
            explanation = "30% of ${num(c.limit)} is ${money(c.ceiling)}. ${num(c.balance)} − ${num(c.ceiling)} = " +
                "${money(pay)}, paid before the statement is cut — not before the bill is due.",
        )
    }

    private fun bestMove(random: Random): Round {
        val c = Card(random)
        val unused = random.pick(listOf(300.0, 500.0, 1_000.0))
        var pay = random.pick(listOf(100.0, 150.0, 200.0))
        val extra = random.pick(listOf(500.0, 1_000.0))
        val now = Credit.utilisation(c.balance, c.limit + unused)
        var paying = Credit.utilisation(c.balance - pay, c.limit + unused)
        val bigger = Credit.utilisationWithExtraLimit(c.balance, c.limit + unused, extra)
        if (abs(paying - bigger) < 1e-9) {
            pay += 10.0
            paying = Credit.utilisation(c.balance - pay, c.limit + unused)
        }
        val closing = Credit.utilisation(c.balance, c.limit)
        val results = listOf(paying, bigger, closing)
        return ChoiceRound(
            prompt = "Two cards: a ${money(c.limit)} limit with ${money(c.balance)} on it, and a ${money(unused)} card " +
                "you never use. Utilisation now: ${pct(now)}. Which move gets it lowest?",
            options = listOf(
                "Pay ${money(pay)} before the statement",
                "Open a third card with a ${money(extra)} limit",
                "Close the unused ${money(unused)} card",
            ),
            correctIndex = results.indexOf(results.min()),
            explanation = "Paying: ${num(c.balance - pay)} ÷ ${num(c.limit + unused)} = ${pct(paying, 1)}. A bigger total " +
                "limit: ${num(c.balance)} ÷ ${num(c.limit + unused + extra)} = ${pct(bigger, 1)}. Closing the unused " +
                "card: ${num(c.balance)} ÷ ${num(c.limit)} = ${pct(closing, 1)} — the limit that vanished was doing " +
                "you a favour.",
        )
    }

    private fun ceiling(random: Random): Round {
        val c = Card(random)
        return NumberRound(
            prompt = "A ${money(c.limit)} limit. What is the biggest balance that still reads as 30% when the " +
                "statement is cut?",
            quantity = Quantity.AMOUNT,
            truth = c.ceiling,
            min = 0.0, max = c.limit, step = c.limit / 100,
            explanation = "${num(c.limit)} × 0.30 = ${money(c.ceiling)}. Anything above it on statement day, and the " +
                "recorded ratio is past the line lenders look for.",
        )
    }

    private fun snapshot(random: Random): Round {
        val c = Card(random)
        return ChoiceRound(
            prompt = "The statement is cut on the 5th with ${money(c.balance)} on a ${money(c.limit)} limit. You pay all " +
                "of it on the 20th, days before it's due. What ratio does the file record?",
            options = listOf(
                "0% — it was paid in full",
                pct(c.utilisation),
                "Somewhere between the two, averaged over the month",
            ),
            correctIndex = 1,
            explanation = "The file records a snapshot from the 5th: ${num(c.balance)} ÷ ${num(c.limit)} = " +
                "${pct(c.utilisation)}. Paying on the 20th protects you from interest and from a late mark; it does " +
                "not change a photograph already taken.",
        )
    }

    private fun claims(random: Random): Round {
        val c = Card(random)
        return when (random.nextInt(3)) {
            0 -> {
                val unused = random.pick(listOf(300.0, 500.0, 1_000.0))
                val before = Credit.utilisation(c.balance, c.limit + unused)
                trueFalse(
                    prompt = "${money(c.balance)} on a ${money(c.limit)} card, plus an unused ${money(unused)} card. " +
                        "Closing the unused one lowers your utilisation, because it is one less debt.",
                    isTrue = false,
                    explanation = "An unused card is not a debt: it adds its limit to the bottom of the fraction and " +
                        "nothing to the top. Now: ${num(c.balance)} ÷ ${num(c.limit + unused)} = ${pct(before, 1)}. " +
                        "Close it: ${num(c.balance)} ÷ ${num(c.limit)} = ${pct(c.utilisation, 1)}.",
                )
            }
            1 -> {
                val pay = roundTo(c.balance * random.pick(listOf(0.2, 0.4, 0.6)), 10.0)
                val after = Credit.utilisation(c.balance - pay, c.limit)
                val inside = after <= Credit.COMFORTABLE_TARGET
                trueFalse(
                    prompt = "With ${money(c.balance)} on a ${money(c.limit)} limit, paying ${money(pay)} before the " +
                        "statement gets you recorded at 30% or under.",
                    isTrue = inside,
                    explanation = "${num(c.balance)} − ${num(pay)} = ${num(c.balance - pay)}, and ${num(c.balance - pay)} ÷ " +
                        "${num(c.limit)} = ${pct(after, 1)}. ${if (inside) "Inside the line." else "Still over: the ceiling is " + money(c.ceiling) + "."}",
                )
            }
            else -> trueFalse(
                prompt = "A payment more than 30 days late does about the same damage to your file as a high ratio.",
                isTrue = false,
                explanation = "A payment more than 30 days late is a different category of damage and stays on the " +
                    "file for years. The ratio resets every month — 12 fresh snapshots a year. If you only ever do " +
                    "1 thing, set up the direct debit for the minimum.",
            )
        }
    }
}

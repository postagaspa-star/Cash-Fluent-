package com.cashfluent.app.domain.game

/**
 * What kind of number a round is asking for. The drill decides; the screen formats,
 * because only the screen knows which currency symbol the reader picked.
 */
enum class Quantity { AMOUNT, AMOUNT_CENTS, PERCENT, PERCENT_PRECISE, MONTHS, YEARS }

/**
 * One question in a game. Two shapes only: a number you set with a slider, or a choice
 * between a few options. Both end the same way — with [explanation], the calculation
 * written out with the real numbers, which is the part that teaches.
 *
 * Text carries `{c}` wherever a currency symbol belongs, exactly like the lessons.
 */
sealed interface Round {
    val prompt: String
    val explanation: String
}

data class NumberRound(
    override val prompt: String,
    val quantity: Quantity,
    val truth: Double,
    val min: Double,
    val max: Double,
    val step: Double,
    override val explanation: String,
) : Round {
    init {
        require(max > min) { "a round needs a range" }
        require(step > 0.0) { "step must be positive" }
        require(truth in min..max) { "the answer $truth must sit inside $min..$max" }
    }

    val span: Double get() = max - min
}

data class ChoiceRound(
    override val prompt: String,
    val options: List<String>,
    val correctIndex: Int,
    override val explanation: String,
) : Round {
    init {
        require(options.size >= 2) { "a choice needs at least two options" }
        require(options.toSet().size == options.size) { "options must be distinct: $options" }
        require(correctIndex in options.indices) { "correctIndex $correctIndex is not an option" }
    }
}

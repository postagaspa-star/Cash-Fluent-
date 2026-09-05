package com.cashfluent.app.domain.finance

import java.util.Locale
import kotlin.math.abs

/**
 * The currency the user picked in Settings. It changes the symbol only — the example
 * numbers in the content never change, because converting them at some invented
 * exchange rate would make them wrong rather than local.
 */
enum class Currency(val code: String, val symbol: String) {
    USD("USD", "$"),
    EUR("EUR", "€"),
    GBP("GBP", "£");

    companion object {
        val DEFAULT = USD
        fun fromCode(code: String?): Currency = entries.firstOrNull { it.code == code } ?: DEFAULT
    }
}

object Money {

    /**
     * "131,757" — grouped, no decimals. What result tiles and worked examples use.
     *
     * A negative amount reads "-$120", sign first, never "$-120". Anything that rounds
     * to zero drops the sign, so "-0" cannot appear either.
     */
    fun amount(value: Double, currency: Currency, decimals: Int = 0): String =
        signed(value, decimals) { digits -> currency.symbol + digits }

    /** "1,200" — a grouped number with no symbol at all, for arithmetic shown in text. */
    fun number(value: Double, decimals: Int = 0): String = signed(value, decimals) { it }

    /**
     * "{c}1,200" — the same as [amount], but carrying the placeholder instead of a
     * symbol, for text written before anyone knows which currency the reader picked.
     */
    fun template(value: Double, decimals: Int = 0): String =
        signed(value, decimals) { digits -> CURRENCY_PLACEHOLDER + digits }

    private inline fun signed(value: Double, decimals: Int, wrap: (String) -> String): String {
        val digits = String.format(Locale.US, "%,.${decimals}f", abs(value))
        val negative = value < 0.0 && digits.any { it in '1'..'9' }
        return (if (negative) "-" else "") + wrap(digits)
    }

    /** "1,631.67" — for anything monthly, where the cents carry meaning. */
    fun preciseAmount(value: Double, currency: Currency): String = amount(value, currency, decimals = 2)

    /** 0.184 -> "18.4%". */
    fun percent(fraction: Double, decimals: Int = 1): String =
        String.format(Locale.US, "%.${decimals}f", fraction * 100) + "%"

    /**
     * Content strings carry {c} where a currency symbol belongs, so the same sentence
     * works in every currency without duplicating the copy.
     */
    fun applyCurrency(text: String, currency: Currency): String =
        text.replace(CURRENCY_PLACEHOLDER, currency.symbol)

    const val CURRENCY_PLACEHOLDER = "{c}"
}

package com.cashfluent.app.domain.finance

import java.util.Locale

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

    /** "131,757" — grouped, no decimals. What result tiles and worked examples use. */
    fun amount(value: Double, currency: Currency, decimals: Int = 0): String =
        currency.symbol + String.format(Locale.US, "%,.${decimals}f", value)

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

package com.cashfluent.app.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.unit.sp

/*
 * The scale is the decision; the family is a swap.
 *
 * Cashfluent is specified with Archivo for interface and IBM Plex Mono for numbers and
 * formulas. Those two font files are not bundled yet — dropping them into res/font/ and
 * changing [UiFamily] and [MonoFamily] below is the whole change, and nothing else in
 * the app has to move. Until then the platform families are used, which keeps every
 * size, weight and line height exactly as specified.
 *
 * Two rules that are not negotiable, whatever the family:
 *   - reading text never goes below 17sp; this app is for people who find the subject
 *     hard enough already,
 *   - anything with digits uses the monospace family, so columns of numbers line up.
 */

private val UiFamily = FontFamily.SansSerif
private val MonoFamily = FontFamily.Monospace

private val TightLineHeight = LineHeightStyle(
    alignment = LineHeightStyle.Alignment.Center,
    trim = LineHeightStyle.Trim.None,
)

val CashfluentTypography = Typography(
    // Module title
    headlineMedium = TextStyle(
        fontFamily = UiFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 28.sp,
        lineHeight = 34.sp,
        letterSpacing = (-0.56).sp,
        lineHeightStyle = TightLineHeight,
    ),
    // Section title / completion headline
    titleLarge = TextStyle(
        fontFamily = UiFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        letterSpacing = (-0.22).sp,
        lineHeightStyle = TightLineHeight,
    ),
    // Card title, quiz question
    titleMedium = TextStyle(
        fontFamily = UiFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 17.sp,
        lineHeight = 24.sp,
        letterSpacing = (-0.17).sp,
        lineHeightStyle = TightLineHeight,
    ),
    // The reading size. Everything explanatory uses this.
    bodyLarge = TextStyle(
        fontFamily = UiFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 17.sp,
        lineHeight = 27.sp,
        letterSpacing = 0.sp,
        lineHeightStyle = TightLineHeight,
    ),
    // Secondary reading text: hooks, variable meanings
    bodyMedium = TextStyle(
        fontFamily = UiFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 15.sp,
        lineHeight = 22.sp,
        lineHeightStyle = TightLineHeight,
    ),
    // Captions, units, footnotes
    bodySmall = TextStyle(
        fontFamily = UiFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        lineHeightStyle = TightLineHeight,
    ),
    // Block labels: THE IDEA / THE MECHANISM / REAL NUMBERS
    labelMedium = TextStyle(
        fontFamily = UiFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.96.sp,
    ),
    labelSmall = TextStyle(
        fontFamily = UiFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 11.sp,
        lineHeight = 14.sp,
        letterSpacing = 0.88.sp,
    ),
)

/**
 * Material 3 has no monospace slot, and every number in this app needs one.
 */
object CashfluentType {

    /** Formulas, displayed whole. */
    val formula = TextStyle(
        fontFamily = MonoFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = (-0.32).sp,
    )

    /** The big result of a simulator. */
    val value = TextStyle(
        fontFamily = MonoFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 26.sp,
        lineHeight = 30.sp,
        letterSpacing = (-0.78).sp,
    )

    /** A number inside a row or tile. */
    val valueSmall = TextStyle(
        fontFamily = MonoFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 19.sp,
        lineHeight = 24.sp,
        letterSpacing = (-0.57).sp,
    )

    /** Variable symbols, slider readouts, step numbers. */
    val data = TextStyle(
        fontFamily = MonoFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
    )

    /** Status pills, progress counters, the smallest monospace text. */
    val dataSmall = TextStyle(
        fontFamily = MonoFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.6.sp,
    )
}

/** One scale for the whole app, so spacing never gets invented at the call site. */
object Space {
    val xs = 4
    val s = 8
    val m = 12
    val l = 16
    val xl = 24
    val xxl = 32
    val xxxl = 48
}

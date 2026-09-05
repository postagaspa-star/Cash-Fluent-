package com.cashfluent.app.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

/*
 * Two accents, and neither is decorative.
 *
 *   grow  = what you keep or gain      (results, progress, a right answer)
 *   cost  = what it takes away         (interest, fees, lost buying power, a wrong answer)
 *   gold  = where to look next         (start here, try it) — never body text
 *
 * After two screens the reader is meant to read the colour before the number, which is
 * also why Material You dynamic colour stays off: if the system swapped these hues for
 * the ones on someone's wallpaper, the meaning would go with them.
 */

// Light
val PaperLight = Color(0xFFF4F6F3)
val SurfaceLight = Color(0xFFFFFFFF)
val SurfaceAltLight = Color(0xFFEAEFE9)
val SunkLight = Color(0xFFF0F3EF)
val InkLight = Color(0xFF101A17)
val InkSecondaryLight = Color(0xFF394943)
val MutedLight = Color(0xFF6C7A75)
val LineLight = Color(0xFFDBE3DC)
val LineStrongLight = Color(0xFFC6D2C8)
val GrowLight = Color(0xFF0E6B54)
val GrowSoftLight = Color(0xFFDCEFE7)
val GrowInkLight = Color(0xFF0A5442)
val CostLight = Color(0xFFA34527)
val CostSoftLight = Color(0xFFF7E4DB)
val CostInkLight = Color(0xFF8A3A20)
val GoldLight = Color(0xFFB5851F)
val GoldSoftLight = Color(0xFFF6EBD1)
val GoldInkLight = Color(0xFF7A5A12)

// Dark — given the same care as light, not a naive inversion.
val PaperDark = Color(0xFF0C1210)
val SurfaceDark = Color(0xFF141C19)
val SurfaceAltDark = Color(0xFF1C2622)
val SunkDark = Color(0xFF111917)
val InkDark = Color(0xFFE7EDE9)
val InkSecondaryDark = Color(0xFFBDC9C3)
val MutedDark = Color(0xFF8B9B94)
val LineDark = Color(0xFF26332E)
val LineStrongDark = Color(0xFF33443D)
val GrowDark = Color(0xFF5CC3A0)
val GrowSoftDark = Color(0xFF123028)
val GrowInkDark = Color(0xFF7FD6B8)
val CostDark = Color(0xFFE08A6A)
val CostSoftDark = Color(0xFF38201A)
val CostInkDark = Color(0xFFEDA285)
val GoldDark = Color(0xFFD9AE55)
val GoldSoftDark = Color(0xFF312712)
val GoldInkDark = Color(0xFFE6C378)

/**
 * The semantic palette. Material 3 has no slot for "this is what it costs you", so the
 * meaningful colours live here and are reached through [CashfluentTheme.colors].
 */
@Immutable
data class CashfluentColors(
    val paper: Color,
    val surface: Color,
    val surfaceAlt: Color,
    val sunk: Color,
    val ink: Color,
    val inkSecondary: Color,
    val muted: Color,
    val line: Color,
    val lineStrong: Color,
    val grow: Color,
    val growSoft: Color,
    val growInk: Color,
    val cost: Color,
    val costSoft: Color,
    val costInk: Color,
    val gold: Color,
    val goldSoft: Color,
    val goldInk: Color,
    val isDark: Boolean,
)

val LightCashfluentColors = CashfluentColors(
    paper = PaperLight,
    surface = SurfaceLight,
    surfaceAlt = SurfaceAltLight,
    sunk = SunkLight,
    ink = InkLight,
    inkSecondary = InkSecondaryLight,
    muted = MutedLight,
    line = LineLight,
    lineStrong = LineStrongLight,
    grow = GrowLight,
    growSoft = GrowSoftLight,
    growInk = GrowInkLight,
    cost = CostLight,
    costSoft = CostSoftLight,
    costInk = CostInkLight,
    gold = GoldLight,
    goldSoft = GoldSoftLight,
    goldInk = GoldInkLight,
    isDark = false,
)

val DarkCashfluentColors = CashfluentColors(
    paper = PaperDark,
    surface = SurfaceDark,
    surfaceAlt = SurfaceAltDark,
    sunk = SunkDark,
    ink = InkDark,
    inkSecondary = InkSecondaryDark,
    muted = MutedDark,
    line = LineDark,
    lineStrong = LineStrongDark,
    grow = GrowDark,
    growSoft = GrowSoftDark,
    growInk = GrowInkDark,
    cost = CostDark,
    costSoft = CostSoftDark,
    costInk = CostInkDark,
    gold = GoldDark,
    goldSoft = GoldSoftDark,
    goldInk = GoldInkDark,
    isDark = true,
)

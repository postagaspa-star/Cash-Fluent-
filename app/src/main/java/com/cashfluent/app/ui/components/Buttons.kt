package com.cashfluent.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.cashfluent.app.content.UiStrings
import com.cashfluent.app.domain.game.Medal
import com.cashfluent.app.ui.theme.CashfluentTheme

/** The one filled button on a screen. Green, because pressing it is what you keep. */
@Composable
fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val colors = CashfluentTheme.colors
    Box(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 52.dp)
            .background(if (enabled) colors.grow else colors.surfaceAlt, RoundedCornerShape(12.dp))
            .clickable(enabled = enabled, role = Role.Button, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = if (enabled) colors.paper else colors.muted,
        )
    }
}

/** A quiet, centred text action for the second choice under a primary button. */
@Composable
fun TextAction(text: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val colors = CashfluentTheme.colors
    Box(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 48.dp)
            .clickable(role = Role.Button, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(text = text, style = MaterialTheme.typography.bodyMedium, color = colors.grow)
    }
}

/**
 * Medal colours borrow from the palette rather than adding to it: gold is the brass
 * already used for "where to look next", silver is the quiet grey, bronze the warm clay.
 */
@Composable
fun medalColors(medal: Medal): Pair<Color, Color> {
    val colors = CashfluentTheme.colors
    return when (medal) {
        Medal.GOLD -> colors.goldInk to colors.goldSoft
        Medal.SILVER -> colors.inkSecondary to colors.surfaceAlt
        Medal.BRONZE -> colors.costInk to colors.costSoft
        Medal.NONE -> colors.muted to colors.surfaceAlt
    }
}

@Composable
fun MedalPill(medal: Medal, modifier: Modifier = Modifier) {
    val (foreground, background) = medalColors(medal)
    Pill(UiStrings.medalName(medal).lowercase(), foreground, background, modifier)
}

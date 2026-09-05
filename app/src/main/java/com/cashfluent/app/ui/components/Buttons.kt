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
import com.cashfluent.app.domain.league.Tier
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
 * The rungs borrow from the palette where they can — bronze is the clay, gold the brass,
 * emerald the green of what you keep — and use the two hues that exist only for this,
 * ruby and diamond, where they cannot. Elite is ink on paper, reversed.
 */
@Composable
fun tierColors(tier: Tier): Pair<Color, Color> {
    val colors = CashfluentTheme.colors
    return when (tier) {
        Tier.WOOD -> colors.inkSecondary to colors.surfaceAlt
        Tier.BRONZE -> colors.costInk to colors.costSoft
        Tier.SILVER -> colors.ink to colors.lineStrong
        Tier.GOLD -> colors.goldInk to colors.goldSoft
        Tier.RUBY -> colors.ruby to colors.rubySoft
        Tier.EMERALD -> colors.growInk to colors.growSoft
        Tier.DIAMOND -> colors.diamond to colors.diamondSoft
        Tier.ELITE -> colors.paper to colors.ink
    }
}

@Composable
fun TierBadge(tier: Tier, modifier: Modifier = Modifier) {
    val (foreground, background) = tierColors(tier)
    Pill(tier.label, foreground, background, modifier)
}

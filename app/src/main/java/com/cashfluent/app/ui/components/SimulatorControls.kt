package com.cashfluent.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.unit.dp
import com.cashfluent.app.content.Tone
import com.cashfluent.app.ui.theme.CashfluentTheme
import com.cashfluent.app.ui.theme.CashfluentType
import kotlin.math.roundToInt

/**
 * Every slider shows its current value, in monospace, above the track — and its limits
 * below it. There is no Calculate button anywhere in the app: watching the number move
 * while you drag is the teaching.
 */
@Composable
fun LabeledSlider(
    label: String,
    display: String,
    value: Float,
    onValueChange: (Float) -> Unit,
    valueRange: ClosedFloatingPointRange<Float>,
    step: Float,
    minLabel: String,
    maxLabel: String,
    modifier: Modifier = Modifier,
) {
    val colors = CashfluentTheme.colors
    // Rounded, not truncated: 2.5f / 0.05f is not exactly 50 in floating point, and a
    // truncated count would put every stop of the slider slightly off its label.
    val stepCount = (((valueRange.endInclusive - valueRange.start) / step).roundToInt() - 1)
        .coerceAtLeast(0)

    Column(modifier = modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = colors.inkSecondary,
                modifier = Modifier.weight(1f),
            )
            Text(text = display, style = CashfluentType.data, color = colors.ink)
        }
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = valueRange,
            steps = stepCount,
            colors = SliderDefaults.colors(
                thumbColor = colors.grow,
                activeTrackColor = colors.grow,
                inactiveTrackColor = colors.surfaceAlt,
                activeTickColor = Color.Transparent,
                inactiveTickColor = Color.Transparent,
            ),
            // TalkBack should say "100 per month", not "eighteen percent".
            modifier = Modifier.semantics { stateDescription = "$label: $display" },
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(minLabel, style = CashfluentType.dataSmall, color = colors.muted)
            Text(maxLabel, style = CashfluentType.dataSmall, color = colors.muted)
        }
    }
}

@Composable
fun ResultTile(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    valueColor: Color = CashfluentTheme.colors.ink,
) {
    val colors = CashfluentTheme.colors
    Column(
        modifier = modifier
            .background(colors.surface, RoundedCornerShape(12.dp))
            .border(1.dp, colors.line, RoundedCornerShape(12.dp))
            .padding(horizontal = 14.dp, vertical = 12.dp),
    ) {
        Text(text = label.uppercase(), style = MaterialTheme.typography.labelSmall, color = colors.muted)
        Spacer(Modifier.height(4.dp))
        Text(text = value, style = CashfluentType.valueSmall, color = valueColor)
    }
}

/** The headline result, given its own strip so it cannot be mistaken for a detail. */
@Composable
fun TotalStrip(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    tone: Tone = Tone.GOOD,
) {
    val colors = CashfluentTheme.colors
    val background = if (tone == Tone.GOOD) colors.growSoft else colors.costSoft
    val foreground = if (tone == Tone.GOOD) colors.growInk else colors.costInk
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(background, RoundedCornerShape(12.dp))
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = foreground,
            modifier = Modifier.weight(1f),
        )
        Text(text = value, style = CashfluentType.value, color = foreground)
    }
}

/**
 * The sentence under every simulator. It says in words what the chart says in shape,
 * which makes the app work for someone who does not read charts and for someone using
 * a screen reader.
 */
@Composable
fun PlainEnglishResult(
    text: String,
    modifier: Modifier = Modifier,
    tone: Tone? = null,
) {
    val colors = CashfluentTheme.colors
    val background = when (tone) {
        Tone.GOOD -> colors.growSoft
        Tone.COST -> colors.costSoft
        null -> colors.sunk
    }
    val foreground = when (tone) {
        Tone.GOOD -> colors.growInk
        Tone.COST -> colors.costInk
        null -> colors.inkSecondary
    }
    Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium,
        color = foreground,
        modifier = modifier
            .fillMaxWidth()
            .background(background, RoundedCornerShape(12.dp))
            .padding(horizontal = 16.dp, vertical = 14.dp),
    )
}

/** One tap back to the worked example, so no demo can get stranded in a weird state. */
@Composable
fun ResetLink(text: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val colors = CashfluentTheme.colors
    Box(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 48.dp)
            .clickable(role = Role.Button, onClick = onClick),
        contentAlignment = Alignment.CenterStart,
    ) {
        Text(text = text, style = MaterialTheme.typography.bodyMedium, color = colors.grow)
    }
}

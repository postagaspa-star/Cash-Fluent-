package com.cashfluent.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.cashfluent.app.content.ExampleStep
import com.cashfluent.app.content.Tone
import com.cashfluent.app.content.UiStrings
import com.cashfluent.app.content.Variable
import com.cashfluent.app.domain.finance.Currency
import com.cashfluent.app.domain.finance.Money
import com.cashfluent.app.ui.theme.CashfluentTheme
import com.cashfluent.app.ui.theme.CashfluentType

/** Content carries {c} wherever a currency symbol belongs. */
fun String.withCurrency(currency: Currency): String = Money.applyCurrency(this, currency)

@Composable
fun SectionLabel(text: String, modifier: Modifier = Modifier, color: Color = CashfluentTheme.colors.grow) {
    Text(
        text = text.uppercase(),
        style = MaterialTheme.typography.labelMedium,
        color = color,
        modifier = modifier,
    )
}

/**
 * Starts a block with a rule above it. Without a boundary the three blocks run into one
 * another and the whole lesson reads as one wall — which is exactly how it felt on a
 * real phone.
 */
@Composable
fun SectionHeader(
    label: String,
    modifier: Modifier = Modifier,
    color: Color = CashfluentTheme.colors.grow,
) {
    val colors = CashfluentTheme.colors
    Column(modifier = modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(colors.line),
        )
        Spacer(Modifier.height(22.dp))
        SectionLabel(label, color = color)
    }
}

/** A quieter label for grouping inside a block. */
@Composable
fun SubLabel(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text.uppercase(),
        style = MaterialTheme.typography.labelSmall,
        color = CashfluentTheme.colors.muted,
        modifier = modifier,
    )
}

@Composable
fun BodyText(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = CashfluentTheme.colors.inkSecondary,
) {
    Text(text = text, style = MaterialTheme.typography.bodyLarge, color = color, modifier = modifier)
}

/** A formula is never squeezed or wrapped: it scrolls sideways instead. */
@Composable
fun FormulaCard(formula: String, modifier: Modifier = Modifier) {
    val colors = CashfluentTheme.colors
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(colors.sunk, RoundedCornerShape(12.dp))
            .border(1.dp, colors.line, RoundedCornerShape(12.dp))
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 14.dp),
    ) {
        Text(text = formula, style = CashfluentType.formula, color = colors.ink, maxLines = 1)
    }
}

@Composable
fun VariableRow(variable: Variable, currency: Currency, modifier: Modifier = Modifier) {
    val colors = CashfluentTheme.colors
    Column(modifier = modifier.fillMaxWidth().padding(vertical = 10.dp)) {
        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                text = variable.symbol,
                style = CashfluentType.data,
                color = colors.grow,
                modifier = Modifier.width(58.dp),
            )
            Text(
                text = variable.name,
                style = MaterialTheme.typography.bodyMedium,
                color = colors.ink,
                modifier = Modifier.weight(1f),
            )
            Text(
                text = variable.example.withCurrency(currency),
                style = CashfluentType.data,
                color = colors.inkSecondary,
            )
        }
        Text(
            text = variable.meaning.withCurrency(currency),
            style = MaterialTheme.typography.bodySmall,
            color = colors.muted,
            modifier = Modifier.padding(top = 3.dp),
        )
    }
}

@Composable
fun NumberedStep(index: Int, text: String, modifier: Modifier = Modifier) {
    val colors = CashfluentTheme.colors
    Row(modifier = modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = index.toString(),
            style = CashfluentType.data,
            color = colors.grow,
            modifier = Modifier.width(16.dp),
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge,
            color = colors.inkSecondary,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
fun WorkedStep(index: Int, step: ExampleStep, currency: Currency, modifier: Modifier = Modifier) {
    val colors = CashfluentTheme.colors
    Row(modifier = modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = index.toString(),
            style = CashfluentType.data,
            color = colors.grow,
            modifier = Modifier.width(16.dp),
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = step.text.withCurrency(currency),
                style = MaterialTheme.typography.bodyLarge,
                color = colors.inkSecondary,
            )
            if (step.math != null) {
                Box(
                    modifier = Modifier
                        .padding(top = 6.dp)
                        .fillMaxWidth()
                        .background(colors.sunk, RoundedCornerShape(8.dp))
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                ) {
                    Text(step.math, style = CashfluentType.data, color = colors.ink, maxLines = 1)
                }
            }
        }
    }
}

/** Tinted block, no accent rail: a coloured ground reads better than a stripe. */
@Composable
fun Callout(
    label: String,
    body: String,
    background: Color,
    foreground: Color,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(background, RoundedCornerShape(12.dp))
            .padding(horizontal = 16.dp, vertical = 14.dp),
    ) {
        Text(text = label.uppercase(), style = MaterialTheme.typography.labelSmall, color = foreground)
        Spacer(Modifier.height(6.dp))
        Text(text = body, style = MaterialTheme.typography.bodyMedium, color = foreground)
    }
}

/** The one sentence a module is really about. */
@Composable
fun Punchline(text: String, tone: Tone, modifier: Modifier = Modifier) {
    val colors = CashfluentTheme.colors
    val background = if (tone == Tone.GOOD) colors.growSoft else colors.costSoft
    val foreground = if (tone == Tone.GOOD) colors.growInk else colors.costInk
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        color = foreground,
        modifier = modifier
            .fillMaxWidth()
            .background(background, RoundedCornerShape(12.dp))
            .padding(horizontal = 16.dp, vertical = 14.dp),
    )
}

@Composable
fun Pill(text: String, foreground: Color, background: Color, modifier: Modifier = Modifier) {
    Text(
        text = text,
        style = CashfluentType.dataSmall,
        color = foreground,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        modifier = modifier
            .background(background, CircleShape)
            .padding(horizontal = 10.dp, vertical = 4.dp),
    )
}

/**
 * Back arrow drawn rather than imported, so the app pulls in no icon dependency. The
 * glyph means nothing to a screen reader, so the button carries its own name and the
 * arrow itself is silent.
 */
@Composable
fun BackButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    val colors = CashfluentTheme.colors
    Box(
        modifier = modifier
            .size(48.dp)
            .clickable(role = Role.Button, onClick = onClick)
            .semantics { contentDescription = UiStrings.BACK },
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = "←",
            style = MaterialTheme.typography.titleLarge,
            color = colors.muted,
            modifier = Modifier.clearAndSetSemantics {},
        )
    }
}

@Composable
fun TopBar(
    title: String,
    onBack: (() -> Unit)?,
    modifier: Modifier = Modifier,
    trailing: @Composable () -> Unit = {},
) {
    val colors = CashfluentTheme.colors
    Row(
        modifier = modifier.fillMaxWidth().padding(end = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (onBack != null) BackButton(onBack) else Spacer(Modifier.width(16.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.bodyMedium,
            color = colors.ink,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f),
        )
        trailing()
    }
}

/** Where you are inside a module: idea, mechanism, real numbers, try it, check. */
@Composable
fun SectionProgress(reached: Int, total: Int, modifier: Modifier = Modifier) {
    val colors = CashfluentTheme.colors
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        repeat(total) { index ->
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(4.dp)
                    .background(
                        if (index < reached) colors.grow else colors.lineStrong,
                        RoundedCornerShape(2.dp),
                    ),
            )
        }
    }
}

@Composable
fun ProgressBar(fraction: Float, modifier: Modifier = Modifier) {
    val colors = CashfluentTheme.colors
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(6.dp)
            .background(colors.surfaceAlt, RoundedCornerShape(3.dp)),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(fraction.coerceIn(0f, 1f))
                .height(6.dp)
                .background(colors.grow, RoundedCornerShape(3.dp)),
        )
    }
}

package com.cashfluent.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.cashfluent.app.ui.theme.CashfluentTheme
import com.cashfluent.app.ui.theme.CashfluentType

/*
 * Drawn by hand on a Canvas rather than pulled from a charting library. Three reasons:
 * no dependency to keep working, exact control of the two semantic colours, and a real
 * contentDescription that says what the shape means instead of the word "chart".
 */

data class ChartSeries(
    val values: List<Float>,
    val color: Color,
    val dashed: Boolean = false,
    val filled: Boolean = false,
)

/**
 * [contentDescription] must describe the movement in words — it is the whole chart for
 * anyone using a screen reader, and the sentence under every simulator does the same job
 * for everyone else.
 */
@Composable
fun LineChart(
    series: List<ChartSeries>,
    contentDescription: String,
    modifier: Modifier = Modifier,
    startLabel: String? = null,
    endLabel: String? = null,
    chartHeight: Dp = 150.dp,
) {
    val colors = CashfluentTheme.colors
    val allValues = series.flatMap { it.values }
    val maxValue = (allValues.maxOrNull() ?: 1f).coerceAtLeast(1f)
    val minValue = (allValues.minOrNull() ?: 0f).coerceAtMost(0f)
    val span = (maxValue - minValue).coerceAtLeast(1f)

    Column(modifier = modifier.fillMaxWidth()) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(chartHeight)
                .semantics { this.contentDescription = contentDescription },
        ) {
            val w = size.width
            val h = size.height
            fun y(value: Float) = h - ((value - minValue) / span) * h
            fun x(index: Int, count: Int) = if (count <= 1) 0f else w * index / (count - 1)

            // A single faint guide at the halfway mark, and the zero line if it is in view.
            drawLine(
                color = colors.line,
                start = Offset(0f, h / 2f),
                end = Offset(w, h / 2f),
                strokeWidth = 1.dp.toPx(),
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 10f)),
            )
            drawLine(
                color = colors.lineStrong,
                start = Offset(0f, y(0f)),
                end = Offset(w, y(0f)),
                strokeWidth = 1.dp.toPx(),
            )

            series.forEach { line ->
                if (line.values.size < 2) return@forEach
                val path = Path()
                line.values.forEachIndexed { index, value ->
                    val px = x(index, line.values.size)
                    val py = y(value)
                    if (index == 0) path.moveTo(px, py) else path.lineTo(px, py)
                }

                if (line.filled) {
                    val area = Path()
                    area.addPath(path)
                    area.lineTo(w, y(0f))
                    area.lineTo(0f, y(0f))
                    area.close()
                    drawPath(
                        path = area,
                        brush = Brush.verticalGradient(
                            colors = listOf(line.color.copy(alpha = 0.26f), Color.Transparent),
                            startY = 0f,
                            endY = h,
                        ),
                    )
                }

                drawPath(
                    path = path,
                    color = line.color,
                    style = Stroke(
                        width = if (line.dashed) 1.8.dp.toPx() else 2.6.dp.toPx(),
                        cap = StrokeCap.Round,
                        join = StrokeJoin.Round,
                        pathEffect = if (line.dashed) {
                            PathEffect.dashPathEffect(floatArrayOf(14f, 10f))
                        } else {
                            null
                        },
                    ),
                )

                // The end of the solid line is the number the reader came for.
                if (!line.dashed) {
                    val last = line.values.last()
                    drawCircle(
                        color = line.color,
                        radius = 4.dp.toPx(),
                        center = Offset(w, y(last)),
                    )
                }
            }
        }

        if (startLabel != null || endLabel != null) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(startLabel.orEmpty(), style = CashfluentType.dataSmall, color = colors.muted)
                Text(endLabel.orEmpty(), style = CashfluentType.dataSmall, color = colors.muted)
            }
        }
    }
}

/** A small key, used only where two lines would otherwise be ambiguous. */
@Composable
fun ChartLegend(
    items: List<Pair<String, Color>>,
    modifier: Modifier = Modifier,
) {
    val colors = CashfluentTheme.colors
    Row(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        items.forEach { (label, color) ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                Spacer(
                    Modifier
                        .width(14.dp)
                        .height(3.dp)
                        .background(color, RoundedCornerShape(2.dp)),
                )
                Spacer(Modifier.width(6.dp))
                Text(label, style = MaterialTheme.typography.bodySmall, color = colors.muted)
            }
        }
    }
}

data class BarPart(val label: String, val value: Float, val color: Color)

/** One month, or one salary, split into its parts. */
@Composable
fun StackedBar(
    parts: List<BarPart>,
    contentDescription: String,
    modifier: Modifier = Modifier,
    barHeight: Dp = 34.dp,
) {
    val colors = CashfluentTheme.colors
    val total = parts.sumOf { it.value.toDouble() }.toFloat().coerceAtLeast(0.0001f)

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(barHeight)
                .background(colors.surfaceAlt, RoundedCornerShape(8.dp))
                .semantics { this.contentDescription = contentDescription },
        ) {
            parts.forEach { part ->
                val share = (part.value / total).coerceAtLeast(0f)
                if (share > 0f) {
                    Spacer(
                        Modifier
                            .weight(share)
                            .fillMaxHeight()
                            .background(part.color),
                    )
                }
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            parts.forEach { part ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Spacer(
                        Modifier
                            .width(10.dp)
                            .height(10.dp)
                            .background(part.color, RoundedCornerShape(3.dp)),
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = part.label,
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.muted,
                    )
                }
            }
        }
    }
}

/** Two outcomes side by side, scaled against the larger one. */
@Composable
fun ComparisonBars(
    leftLabel: String,
    leftValue: Float,
    leftDisplay: String,
    rightLabel: String,
    rightValue: Float,
    rightDisplay: String,
    contentDescription: String,
    modifier: Modifier = Modifier,
) {
    val colors = CashfluentTheme.colors
    val peak = maxOf(leftValue, rightValue).coerceAtLeast(0.0001f)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .semantics { this.contentDescription = contentDescription },
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        listOf(
            Triple(leftLabel, leftValue, leftDisplay) to colors.grow,
            Triple(rightLabel, rightValue, rightDisplay) to colors.cost,
        ).forEach { (bar, color) ->
            val (label, value, display) = bar
            Column {
                Row(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.bodyMedium,
                        color = colors.inkSecondary,
                        modifier = Modifier.weight(1f),
                    )
                    Text(text = display, style = CashfluentType.data, color = colors.ink)
                }
                Spacer(Modifier.height(6.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(16.dp)
                        .background(colors.surfaceAlt, RoundedCornerShape(8.dp)),
                ) {
                    Spacer(
                        Modifier
                            .fillMaxWidth((value / peak).coerceIn(0f, 1f))
                            .fillMaxHeight()
                            .background(color, RoundedCornerShape(8.dp)),
                    )
                }
            }
        }
    }
}

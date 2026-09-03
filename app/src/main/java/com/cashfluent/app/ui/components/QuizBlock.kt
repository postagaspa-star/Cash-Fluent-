package com.cashfluent.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.cashfluent.app.content.Question
import com.cashfluent.app.content.UiStrings
import com.cashfluent.app.ui.theme.CashfluentTheme
import com.cashfluent.app.ui.theme.CashfluentType

/**
 * No score, no streak, no single attempt. A wrong answer opens the explanation, which is
 * the actual content — and the right option is highlighted at the same time, so nobody is
 * ever left looking at a red screen with no answer on it.
 *
 * Correctness is never signalled by colour alone: there is a tick or a cross too.
 */
@Composable
fun QuestionCard(
    question: Question,
    index: Int,
    total: Int,
    selected: Int?,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = CashfluentTheme.colors
    val answered = selected != null

    Column(modifier = modifier.fillMaxWidth()) {
        SubLabel("Question ${UiStrings.questionProgress(index + 1, total)}")
        Spacer(Modifier.height(12.dp))

        Text(
            text = question.prompt,
            style = MaterialTheme.typography.titleMedium,
            color = colors.ink,
        )
        Spacer(Modifier.height(16.dp))

        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            question.options.forEachIndexed { optionIndex, option ->
                val isCorrect = optionIndex == question.correctIndex
                val isPicked = selected == optionIndex

                val background = when {
                    answered && isCorrect -> colors.growSoft
                    isPicked -> colors.costSoft
                    else -> colors.surface
                }
                val border = when {
                    answered && isCorrect -> colors.grow
                    isPicked -> colors.cost
                    else -> colors.line
                }
                val foreground = when {
                    answered && isCorrect -> colors.growInk
                    isPicked -> colors.costInk
                    else -> colors.inkSecondary
                }
                val marker = when {
                    answered && isCorrect -> "✓"
                    isPicked -> "✕"
                    else -> ('A' + optionIndex).toString()
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 56.dp)
                        .background(background, RoundedCornerShape(12.dp))
                        .border(
                            width = if (answered && (isCorrect || isPicked)) 1.5.dp else 1.dp,
                            color = border,
                            shape = RoundedCornerShape(12.dp),
                        )
                        .clickable(role = Role.RadioButton) { onSelect(optionIndex) }
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = marker,
                        style = CashfluentType.data,
                        color = foreground,
                        modifier = Modifier.width(22.dp),
                    )
                    Spacer(Modifier.width(10.dp))
                    Text(
                        text = option,
                        style = MaterialTheme.typography.bodyMedium,
                        color = foreground,
                    )
                }
            }
        }

        if (answered) {
            Spacer(Modifier.height(16.dp))
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(colors.growSoft, RoundedCornerShape(12.dp))
                    .padding(horizontal = 16.dp, vertical = 14.dp),
            ) {
                Text(
                    text = UiStrings.WHY.uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    color = colors.growInk,
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    text = question.why,
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.growInk,
                )
                Spacer(Modifier.height(14.dp))
                Text(
                    text = UiStrings.WHY_NOT.uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    color = colors.growInk,
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    text = question.whyNotOthers,
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.growInk,
                )
            }
        }
    }
}

package com.cashfluent.app.ui.about

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.cashfluent.app.content.AboutContent
import com.cashfluent.app.content.UiStrings
import com.cashfluent.app.ui.components.BodyText
import com.cashfluent.app.ui.components.SectionLabel
import com.cashfluent.app.ui.components.TopBar
import com.cashfluent.app.ui.theme.CashfluentTheme
import com.cashfluent.app.ui.theme.CashfluentType

@Composable
fun AboutScreen(onBack: () -> Unit) {
    val colors = CashfluentTheme.colors

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.paper)
            .safeDrawingPadding(),
    ) {
        TopBar(title = UiStrings.ABOUT_TITLE, onBack = onBack)

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 40.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            item {
                Text(
                    text = UiStrings.TAGLINE,
                    style = MaterialTheme.typography.headlineMedium,
                    color = colors.ink,
                )
            }

            items(AboutContent.problem.size) { index ->
                BodyText(AboutContent.problem[index])
            }

            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(colors.growSoft, RoundedCornerShape(14.dp))
                        .padding(16.dp),
                ) {
                    Text(
                        text = AboutContent.METHOD_TITLE,
                        style = MaterialTheme.typography.titleMedium,
                        color = colors.growInk,
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = AboutContent.METHOD_BODY,
                        style = MaterialTheme.typography.bodyMedium,
                        color = colors.growInk,
                    )
                }
            }

            item {
                SectionLabel(AboutContent.NOT_TITLE, color = colors.costInk)
            }

            items(AboutContent.notList.size) { index ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Text(
                        text = "—",
                        style = MaterialTheme.typography.bodyLarge,
                        color = colors.cost,
                        modifier = Modifier.width(16.dp),
                    )
                    Text(
                        text = AboutContent.notList[index],
                        style = MaterialTheme.typography.bodyMedium,
                        color = colors.inkSecondary,
                        modifier = Modifier.weight(1f),
                    )
                }
            }

            item {
                Text(
                    text = AboutContent.TYPE_CREDIT,
                    style = CashfluentType.dataSmall,
                    color = colors.muted,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                )
                Text(
                    text = AboutContent.CREDIT,
                    style = CashfluentType.dataSmall,
                    color = colors.muted,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                )
            }
        }
    }
}

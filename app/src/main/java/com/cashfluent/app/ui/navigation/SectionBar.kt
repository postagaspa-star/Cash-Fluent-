package com.cashfluent.app.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cashfluent.app.content.UiStrings
import com.cashfluent.app.ui.theme.CashfluentTheme

/** The four places the app is always one tap from. */
enum class Section(val route: String, val label: String) {
    HOME(Destinations.HOME, UiStrings.HOME),
    GAMES("games", UiStrings.GAMES),
    LEAGUE(Destinations.LEAGUE, UiStrings.LEAGUE),
    SETTINGS(Destinations.SETTINGS, UiStrings.SETTINGS),
}

/**
 * Four words along the bottom edge, and no icons: this app draws its own back arrow
 * rather than import an icon set, and four little pictograms would be the first
 * decoration in a design that has none.
 *
 * It appears on the four screens that are places — home, the catalogue, the board,
 * settings — and never on a lesson or a game round, which are tasks with their own way
 * forward and their own back.
 */
@Composable
fun SectionBar(current: Section, onSelect: (Section) -> Unit, modifier: Modifier = Modifier) {
    val colors = CashfluentTheme.colors
    // Four labels have to fit across one phone. They grow with the system text size up to
    // 150% and then stop, because a navigation bar that reads "Settin…" is worse than one
    // whose words stop a little short of everything else on screen.
    val labelSize = (14f * minOf(1f, 1.5f / LocalDensity.current.fontScale)).sp

    Column(modifier = modifier.fillMaxWidth().background(colors.paper)) {
        HorizontalDivider(color = colors.line)
        Row(modifier = Modifier.fillMaxWidth().selectableGroup()) {
            Section.entries.forEach { section ->
                val selected = section == current
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .selectable(
                            selected = selected,
                            role = Role.Tab,
                            onClick = { if (!selected) onSelect(section) },
                        )
                        .heightIn(min = 56.dp)
                        .padding(horizontal = 2.dp, vertical = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    Box(
                        modifier = Modifier
                            .width(22.dp)
                            .height(3.dp)
                            .background(
                                if (selected) colors.grow else Color.Transparent,
                                RoundedCornerShape(2.dp),
                            ),
                    )
                    Spacer(Modifier.height(7.dp))
                    Text(
                        text = section.label,
                        style = MaterialTheme.typography.labelLarge,
                        fontSize = labelSize,
                        color = if (selected) colors.grow else colors.muted,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = TextAlign.Center,
                    )
                }
            }
        }
    }
}

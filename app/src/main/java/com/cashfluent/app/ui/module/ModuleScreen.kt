package com.cashfluent.app.ui.module

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.cashfluent.app.content.Module
import com.cashfluent.app.content.Modules
import com.cashfluent.app.content.UiStrings
import com.cashfluent.app.content.inCurrency
import com.cashfluent.app.domain.finance.Currency
import com.cashfluent.app.ui.components.BodyText
import com.cashfluent.app.ui.components.Callout
import com.cashfluent.app.ui.components.FormulaCard
import com.cashfluent.app.ui.components.NumberedStep
import com.cashfluent.app.ui.components.Pill
import com.cashfluent.app.ui.components.PrimaryButton
import com.cashfluent.app.ui.components.Punchline
import com.cashfluent.app.ui.components.QuestionCard
import com.cashfluent.app.ui.components.SectionHeader
import com.cashfluent.app.ui.components.SectionLabel
import com.cashfluent.app.ui.components.SubLabel
import com.cashfluent.app.ui.components.SectionProgress
import com.cashfluent.app.ui.components.TopBar
import com.cashfluent.app.ui.components.VariableRow
import com.cashfluent.app.ui.components.WorkedStep
import com.cashfluent.app.ui.components.withCurrency
import com.cashfluent.app.ui.simulator.SimulatorPanel
import com.cashfluent.app.ui.theme.CashfluentTheme
import com.cashfluent.app.ui.theme.CashfluentType

/**
 * Each of the seven blocks below is exactly one LazyColumn item. That is what lets the
 * bar at the top say honestly which of the five parts you are looking at: the item index
 * is the section index, with no guessing from scroll offsets.
 */
private const val SECTION_COUNT = 5

@Composable
fun ModuleScreen(
    moduleId: String,
    onBack: () -> Unit,
    onOpenModule: (String) -> Unit,
    onOpenGames: (String) -> Unit,
    viewModel: ModuleViewModel = viewModel(),
) {
    val colors = CashfluentTheme.colors
    LaunchedEffect(moduleId) { viewModel.bind(moduleId) }
    val state by viewModel.state.collectAsStateWithLifecycle()
    // Every {c} in the module is replaced once, here. Nothing below this line has to
    // remember to do it, and nothing below this line can forget.
    val module = state.module?.inCurrency(state.currency)

    // One part on screen at a time, and the bar at the top says which. A lesson used to be
    // one long scroll: five sections of real teaching arrived as a single wall, and the
    // only way to know how much was left was to keep going.
    var page by rememberSaveable(moduleId) { mutableIntStateOf(0) }
    val scroll = rememberScrollState()
    LaunchedEffect(page) { scroll.scrollTo(0) }
    // Back steps through the parts before it leaves the lesson, so a stray swipe does not
    // throw away where you were.
    BackHandler(enabled = page > 0) { page -= 1 }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.paper)
            .safeDrawingPadding(),
    ) {
        TopBar(
            title = if (module == null) "" else "${module.displayNumber} · ${module.title}",
            onBack = { if (page > 0) page -= 1 else onBack() },
        )
        SectionProgress(
            reached = page + 1,
            total = SECTION_COUNT,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
        )

        if (module == null) return@Column

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .verticalScroll(scroll)
                .padding(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(44.dp),
        ) {
                when (page) {
                    0 -> {
                        Hero(module, state.showMethodCard, viewModel::dismissMethodCard)
                        IdeaBlock(module)
                    }
                    1 -> MechanismBlock(module, state.currency)
                    2 -> RealNumbersBlock(module, state.currency)
                    3 -> SimulatorPanel(kind = module.simulator, currency = state.currency)
                    else -> {
                        CheckBlock(
                            module = module,
                            answers = state.answers,
                            onAnswer = viewModel::answer,
                        )
                        if (state.allAnswered) {
                            CompletionBlock(
                                module = module,
                                onOpenGames = onOpenGames,
                                onOpenModule = onOpenModule,
                                onBack = onBack,
                            )
                        }
                    }
            }
        }

        // The way on sits below the text rather than over it: a button that floats covers
        // the last two lines of every page, which is exactly where a paragraph ends.
        if (page < SECTION_COUNT - 1) {
            HorizontalDivider(color = colors.line)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.End,
            ) {
                NextPart(onClick = { page += 1 })
            }
        }
    }
}

/** The way on, in the same corner on every part, over whatever is being read. */
@Composable
private fun NextPart(onClick: () -> Unit, modifier: Modifier = Modifier) {
    val colors = CashfluentTheme.colors
    Box(
        modifier = modifier
            .heightIn(min = 52.dp)
            .background(colors.grow, RoundedCornerShape(26.dp))
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = 26.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = UiStrings.NEXT_PART,
            style = MaterialTheme.typography.bodyMedium,
            color = colors.paper,
        )
    }
}

@Composable
private fun Hero(module: Module, showMethod: Boolean, onDismissMethod: () -> Unit) {
    val colors = CashfluentTheme.colors
    Column(modifier = Modifier.padding(top = 4.dp)) {
        // The three parts are named where they are about to be seen. On Home the same card
        // explained the inside of a lesson to someone who had not opened one yet.
        if (showMethod) {
            MethodStrip(onDismiss = onDismissMethod)
            Spacer(Modifier.height(24.dp))
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = module.displayNumber,
                style = CashfluentType.dataSmall,
                color = colors.growInk,
                modifier = Modifier
                    .background(colors.growSoft, RoundedCornerShape(8.dp))
                    .padding(horizontal = 8.dp, vertical = 5.dp),
            )
            Spacer(Modifier.height(0.dp))
            Pill(
                text = "${module.minutes} min",
                foreground = colors.muted,
                background = colors.surfaceAlt,
                modifier = Modifier.padding(start = 8.dp),
            )
        }
        Spacer(Modifier.height(12.dp))
        Text(
            text = module.title,
            style = MaterialTheme.typography.headlineMedium,
            color = colors.ink,
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = module.hook,
            style = MaterialTheme.typography.bodyLarge,
            color = colors.muted,
        )
    }
}

/** One quiet card. It names the three blocks once, above the first of them, and goes. */
@Composable
private fun MethodStrip(onDismiss: () -> Unit) {
    val colors = CashfluentTheme.colors
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(colors.surfaceAlt, RoundedCornerShape(12.dp))
            .padding(horizontal = 16.dp, vertical = 14.dp),
    ) {
        Text(
            text = UiStrings.METHOD_TITLE,
            style = MaterialTheme.typography.bodyMedium,
            color = colors.ink,
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = UiStrings.METHOD_CHIPS.joinToString("   "),
            style = CashfluentType.dataSmall,
            color = colors.muted,
        )
        Box(
            modifier = Modifier
                .align(Alignment.End)
                .heightIn(min = 44.dp)
                .clickable(role = Role.Button, onClick = onDismiss)
                .padding(horizontal = 4.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = UiStrings.METHOD_DISMISS,
                style = MaterialTheme.typography.bodyMedium,
                color = colors.grow,
            )
        }
    }
}

@Composable
private fun IdeaBlock(module: Module) {
    val colors = CashfluentTheme.colors
    Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
        SectionHeader(UiStrings.SECTION_IDEA)
        module.idea.paragraphs.forEach { BodyText(it) }
        Callout(
            label = UiStrings.WHY_SCHOOL,
            body = module.idea.whySchoolSkipsIt,
            background = colors.surfaceAlt,
            foreground = colors.inkSecondary,
        )
    }
}

@Composable
private fun MechanismBlock(module: Module, currency: Currency) {
    val colors = CashfluentTheme.colors
    val mechanism = module.mechanism

    Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
        SectionHeader(UiStrings.SECTION_MECHANISM)
        BodyText(mechanism.intro)

        mechanism.formulas.forEach { FormulaCard(it) }

        Row {
            Text(
                text = UiStrings.PLAIN_ENGLISH,
                style = MaterialTheme.typography.bodyMedium,
                color = colors.inkSecondary,
            )
        }
        Text(
            text = mechanism.plainEnglish,
            style = MaterialTheme.typography.bodyMedium,
            color = colors.muted,
            modifier = Modifier.padding(top = 0.dp),
        )

        SubLabel("What each symbol means")
        Column(modifier = Modifier.padding(top = 4.dp)) {
            mechanism.variables.forEachIndexed { index, variable ->
                VariableRow(variable, currency)
                if (index != mechanism.variables.lastIndex) {
                    HorizontalDivider(color = colors.line)
                }
            }
        }

        if (mechanism.bands.isNotEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(colors.surface, RoundedCornerShape(12.dp))
                    .border(1.dp, colors.line, RoundedCornerShape(12.dp))
                    .padding(horizontal = 16.dp, vertical = 12.dp),
            ) {
                mechanism.bands.forEach { band ->
                    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
                        Text(
                            text = band.range,
                            style = CashfluentType.data,
                            color = colors.inkSecondary,
                            modifier = Modifier.weight(1f),
                        )
                        Text(text = band.rate, style = CashfluentType.data, color = colors.ink)
                    }
                }
                if (mechanism.bandsNote != null) {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = mechanism.bandsNote,
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.muted,
                    )
                }
            }
        }

        SubLabel(UiStrings.STEP_BY_STEP)
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            mechanism.steps.forEachIndexed { index, step ->
                NumberedStep(index + 1, step)
            }
        }

        Callout(
            label = UiStrings.WATCH_OUT,
            body = mechanism.watchOut,
            background = colors.costSoft,
            foreground = colors.costInk,
        )
    }
}

@Composable
private fun RealNumbersBlock(module: Module, currency: Currency) {
    val colors = CashfluentTheme.colors
    val real = module.realNumbers

    Column(verticalArrangement = Arrangement.spacedBy(22.dp)) {
        SectionHeader(UiStrings.SECTION_REAL)
        Text(
            text = real.persona.withCurrency(currency),
            style = MaterialTheme.typography.bodyMedium,
            color = colors.ink,
            modifier = Modifier
                .fillMaxWidth()
                .background(colors.surfaceAlt, RoundedCornerShape(10.dp))
                .padding(horizontal = 14.dp, vertical = 12.dp),
        )
        Column(verticalArrangement = Arrangement.spacedBy(24.dp)) {
            real.steps.forEachIndexed { index, step ->
                WorkedStep(index + 1, step, currency)
            }
        }
        Punchline(real.punchline.withCurrency(currency), real.punchlineTone)
        Callout(
            label = UiStrings.REALITY_CHECK,
            body = real.realityCheck,
            background = colors.surfaceAlt,
            foreground = colors.inkSecondary,
        )
    }
}

@Composable
private fun CheckBlock(
    module: Module,
    answers: Map<Int, Int>,
    onAnswer: (Int, Int) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(40.dp)) {
        SectionHeader(UiStrings.SECTION_CHECK)
        module.check.forEachIndexed { index, question ->
            QuestionCard(
                question = question,
                index = index,
                total = module.check.size,
                selected = answers[index],
                onSelect = { option -> onAnswer(index, option) },
            )
        }
    }
}

@Composable
private fun CompletionBlock(
    module: Module,
    onOpenGames: (String) -> Unit,
    onOpenModule: (String) -> Unit,
    onBack: () -> Unit,
) {
    val colors = CashfluentTheme.colors
    val next = Modules.next(module.id)

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(
            text = UiStrings.MODULE_COMPLETE,
            style = MaterialTheme.typography.titleLarge,
            color = colors.grow,
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(colors.surface, RoundedCornerShape(14.dp))
                .border(1.dp, colors.line, RoundedCornerShape(14.dp))
                .padding(16.dp),
        ) {
            SectionLabel(UiStrings.TAKEAWAY)
            Spacer(Modifier.height(8.dp))
            Text(
                text = module.takeaway,
                style = MaterialTheme.typography.titleMedium,
                color = colors.ink,
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(colors.goldSoft, RoundedCornerShape(14.dp))
                .padding(16.dp),
        ) {
            SectionLabel(UiStrings.ACTION, color = colors.goldInk)
            Spacer(Modifier.height(8.dp))
            Text(
                text = module.action,
                style = MaterialTheme.typography.bodyMedium,
                color = colors.goldInk,
            )
        }

        // The games on this topic: the same formula, on numbers the lesson never showed.
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            PrimaryButton(text = UiStrings.GAMES_ON_TOPIC, onClick = { onOpenGames(module.id) })
            Text(
                text = UiStrings.GAMES_ON_TOPIC_SUB,
                style = MaterialTheme.typography.bodySmall,
                color = colors.muted,
            )
        }

        if (next != null) {
            SectionLabel(UiStrings.UP_NEXT, color = colors.muted)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(colors.surface, RoundedCornerShape(14.dp))
                    .border(1.dp, colors.line, RoundedCornerShape(14.dp))
                    .clickable(role = Role.Button) { onOpenModule(next.id) }
                    .padding(15.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = next.displayNumber,
                    style = CashfluentType.dataSmall,
                    color = colors.growInk,
                    modifier = Modifier
                        .background(colors.growSoft, RoundedCornerShape(8.dp))
                        .padding(horizontal = 7.dp, vertical = 5.dp),
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = next.title,
                        style = MaterialTheme.typography.bodyMedium,
                        color = colors.ink,
                    )
                    Text(
                        text = next.hook,
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.muted,
                    )
                }
                Text(text = "→", style = MaterialTheme.typography.bodyLarge, color = colors.muted)
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 48.dp)
                .clickable(role = Role.Button, onClick = onBack),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = UiStrings.BACK_TO_ALL,
                style = MaterialTheme.typography.bodyMedium,
                color = colors.grow,
            )
        }
    }
}

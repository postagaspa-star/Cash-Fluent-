package com.cashfluent.app.content

import com.cashfluent.app.domain.finance.Currency
import com.cashfluent.app.domain.finance.Money

/**
 * Content is written with {c} wherever a currency symbol belongs, so one sentence serves
 * every currency. Something has to replace it, and doing that field by field at each
 * Text() is how you end up shipping a literal "{c}6" in the one field somebody forgot.
 *
 * So it happens once, here, at the boundary between the content and the screen: the
 * screen asks for the module in a currency and gets one with no placeholders left in it
 * anywhere. Adding a field to [Module] and forgetting it in this file is a compile error,
 * because every copy() below names its arguments and the constructors are exhaustive.
 */
fun Module.inCurrency(currency: Currency): Module = copy(
    title = title.money(currency),
    hook = hook.money(currency),
    idea = idea.copy(
        paragraphs = idea.paragraphs.map { it.money(currency) },
        whySchoolSkipsIt = idea.whySchoolSkipsIt.money(currency),
    ),
    mechanism = mechanism.copy(
        intro = mechanism.intro.money(currency),
        formulas = mechanism.formulas.map { it.money(currency) },
        plainEnglish = mechanism.plainEnglish.money(currency),
        variables = mechanism.variables.map {
            it.copy(
                symbol = it.symbol.money(currency),
                name = it.name.money(currency),
                meaning = it.meaning.money(currency),
                example = it.example.money(currency),
            )
        },
        steps = mechanism.steps.map { it.money(currency) },
        watchOut = mechanism.watchOut.money(currency),
        bands = mechanism.bands.map {
            it.copy(range = it.range.money(currency), rate = it.rate.money(currency))
        },
        bandsNote = mechanism.bandsNote?.money(currency),
    ),
    realNumbers = realNumbers.copy(
        persona = realNumbers.persona.money(currency),
        steps = realNumbers.steps.map {
            it.copy(text = it.text.money(currency), math = it.math?.money(currency))
        },
        punchline = realNumbers.punchline.money(currency),
        realityCheck = realNumbers.realityCheck.money(currency),
    ),
    check = check.map {
        it.copy(
            prompt = it.prompt.money(currency),
            options = it.options.map { option -> option.money(currency) },
            why = it.why.money(currency),
            whyNotOthers = it.whyNotOthers.money(currency),
        )
    },
    takeaway = takeaway.money(currency),
    action = action.money(currency),
)

private fun String.money(currency: Currency): String = Money.applyCurrency(this, currency)

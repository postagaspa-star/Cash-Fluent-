package com.cashfluent.app.content.module

import com.cashfluent.app.content.ExampleStep
import com.cashfluent.app.content.Idea
import com.cashfluent.app.content.Mechanism
import com.cashfluent.app.content.Module
import com.cashfluent.app.content.Question
import com.cashfluent.app.content.RealNumbers
import com.cashfluent.app.content.SimulatorKind
import com.cashfluent.app.content.Tone
import com.cashfluent.app.content.Variable

val SideIncomeModule = Module(
    id = "side-income",
    number = 9,
    title = "Tax nobody deducts for you",
    hook = "Money from a job arrives already taxed. Money you earn yourself doesn't",
    minutes = 7,
    simulator = SimulatorKind.SIDE_INCOME,

    idea = Idea(
        paragraphs = listOf(
            "There is one difference between a wage and everything else you might earn, and " +
                "almost nobody spells it out: an employer takes the tax off before the money " +
                "reaches you. Nobody does that for a gig, a commission, a weekend of " +
                "photography or an evening of tutoring.",
            "So the whole amount lands in your account and looks like yours. It behaves like " +
                "yours. You spend it like yours. And a chunk of it was never yours — it was " +
                "always going to be collected, just later, in one lump, on a date you " +
                "probably don't have in your calendar.",
            "This is the mistake that turns a good year into a bad one. Not overspending, not " +
                "bad luck: a bill arriving for money that was earned twelve months ago and " +
                "spent eleven months ago. The fix is one habit, and it takes about four " +
                "seconds per payment.",
        ),
        whySchoolSkipsIt = "School prepares you for employment, where somebody else handles " +
            "this. Then a whole generation ends up earning through apps, platforms and " +
            "one-off jobs, where nobody does — and finds out from the bill.",
    ),

    mechanism = Mechanism(
        intro = "Three steps: work out what's actually taxable, work out the bill, then turn " +
            "that into something you can do every time a payment arrives instead of once a " +
            "year in a panic.",
        formulas = listOf(
            "Profit = Income − Allowable expenses",
            "Tax due = Profit × Rate",
            "Set aside = Each payment × Rate",
        ),
        plainEnglish = "You're taxed on what the work earned you, not on what landed in your " +
            "account — the difference is what it cost you to do the work. The last line is the " +
            "habit: hold back the rate on everything that arrives, and the bill is already paid " +
            "before it exists.",
        variables = listOf(
            Variable("Income", "What arrived", "Everything the work paid you, before anything.", "{c}6,000"),
            Variable("Expenses", "What it cost", "What you had to spend to do the work.", "{c}900"),
            Variable("Profit", "What's taxable", "Income minus expenses. The number tax applies to.", "{c}5,100"),
            Variable("Rate", "The combined rate", "Income tax plus social contributions, together.", "0.25"),
            Variable("Set aside", "Per payment", "What you move out the moment money arrives.", "{c}125"),
        ),
        steps = listOf(
            "Add up the year: 500 a month is 6,000. Then subtract what the work cost you — " +
                "gear, travel, software: 900. Taxable profit is 5,100.",
            "Apply the rate: 5,100 × 0.25 = 1,275. That's the bill, and it is smaller than a " +
                "quarter of what arrived, because expenses came off first.",
            "Now make it a habit instead of an event. Hold back 25% of every payment as it " +
                "lands — 125 a month — and by the time the bill comes you've collected 1,500 " +
                "for a 1,275 bill. The 225 left over is yours.",
        ),
        watchOut = "The 25% here is a single illustrative rate standing in for income tax plus " +
            "whatever social contributions apply where you live — the real figures differ by " +
            "country, by how much you earn, and often by how the work is registered. Look yours " +
            "up: now that you know the shape, the real numbers will make sense. And two things " +
            "that hold everywhere — an expense has to be genuinely for the work, and the only " +
            "way any of this survives is if the set-aside money sits somewhere you don't " +
            "casually spend from.",
    ),

    realNumbers = RealNumbers(
        persona = "Sam, 19, photographs parties at weekends for about {c}500 a month.",
        steps = listOf(
            ExampleStep(
                text = "A full year of weekends. This is what arrived in the account, and what " +
                    "it felt like they'd earned.",
                math = "500 × 12 = 6,000",
            ),
            ExampleStep(
                text = "Lens repair, editing software, train fares. All of it genuinely for " +
                    "the work, so all of it comes off before tax.",
                math = "6,000 − 900 = 5,100",
            ),
            ExampleStep(
                text = "The bill, at a combined 25%.",
                math = "5,100 × 0.25 = 1,275",
            ),
            ExampleStep(
                text = "Held back from each payment as it arrived, that bill is 125 a month — " +
                    "and collecting it that way over-shoots slightly, in your favour.",
                math = "125 × 12 = 1,500, leaving 225",
            ),
            ExampleStep(
                text = "What was actually Sam's, all year, once the gear and the tax are out.",
                math = "6,000 − 900 − 1,275 = 3,825",
            ),
        ),
        punchline = "Sam earned {c}6,000 and kept {c}3,825. Both numbers are true. Only one of " +
            "them was ever safe to spend.",
        punchlineTone = Tone.COST,
        realityCheck = "Notice the effective rate: 1,275 on 6,000 is 21.25%, not 25%. Expenses " +
            "pull the real rate below the headline one every time, which is why keeping " +
            "receipts is worth actual money rather than being an admin chore. And notice which " +
            "way the habit errs — holding back the flat rate on gross collects slightly too " +
            "much. That surplus is the whole point. A tax plan that only just covers the bill " +
            "fails the first time you have a good month.",
    ),

    check = listOf(
        Question(
            prompt = "{c}6,000 arrived from freelance work and {c}900 of it went on gear and " +
                "travel for that work. What gets taxed?",
            options = listOf(
                "{c}6,000 — that's what you were paid",
                "{c}5,100",
                "{c}900",
            ),
            correctIndex = 1,
            why = "Tax applies to profit, not to turnover. What the work genuinely cost you " +
                "comes off first: 6,000 − 900 = 5,100.",
            whyNotOthers = "Being taxed on the full 6,000 would mean paying tax on money that " +
                "was never yours to keep. And 900 is the expense itself, not the profit.",
        ),
        Question(
            prompt = "You hold back 25% of every payment, and your expenses turn out to be " +
                "{c}900. Come tax time you'll have —",
            options = listOf(
                "Slightly too little, because 25% wasn't enough",
                "Slightly too much, and the difference is yours",
                "Exactly the right amount",
            ),
            correctIndex = 1,
            why = "You held back 25% of everything that arrived (1,500), but the bill is 25% of " +
                "profit after expenses (1,275). Expenses make the real bill smaller, so the " +
                "simple habit over-collects by 225.",
            whyNotOthers = "It can't fall short as long as your rate is right and expenses are " +
                "positive. And it only lands exactly right in the case where you had no " +
                "expenses at all — which is rare, and not something to plan around.",
        ),
        Question(
            prompt = "Why is a wage different from money you invoice for yourself?",
            options = listOf(
                "A wage is taxed at a lower rate",
                "The tax is taken out before a wage reaches you",
                "Money you earn yourself isn't taxed until you earn a lot",
            ),
            correctIndex = 1,
            why = "It's a difference in who does the collecting and when — the employer deducts " +
                "it and pays it over, so your net pay is genuinely yours. Nobody does that for " +
                "money you invoice.",
            whyNotOthers = "The rates are broadly the same kind of thing; it's the timing that " +
                "differs. And while most countries have a threshold below which nothing is due, " +
                "assuming you're under it is how people end up with a bill they can't pay.",
        ),
    ),

    takeaway = "The moment money you earned yourself arrives, part of it is already spoken " +
        "for. Move that part out the same day and the bill never surprises you.",
    action = "Next time you're paid for anything that isn't a wage, move a quarter of it into a " +
        "separate account before you do anything else. Not later, not at the end of the month. " +
        "Then treat the account that's left as the whole of what you earned.",
)

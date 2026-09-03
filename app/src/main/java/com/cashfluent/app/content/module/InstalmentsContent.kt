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

val InstalmentsModule = Module(
    id = "instalments",
    number = 7,
    title = "Buy now, pay later",
    hook = "Genuinely 0% — right up until the day you're late",
    minutes = 6,
    simulator = SimulatorKind.INSTALMENTS,

    idea = Idea(
        paragraphs = listOf(
            "Split it into four. No interest, no credit check, one tap at checkout. It is " +
                "the most frictionless way to borrow money that has ever existed, and it is " +
                "aimed squarely at people your age.",
            "The strange part is that the 0% is true. There genuinely is no interest. The " +
                "money is made somewhere else — partly from the shop, and partly from the " +
                "share of people who miss a payment. Which means the price of the plan " +
                "depends entirely on whether that person is you.",
            "There's a second thing the plan changes, quieter than the fee: it changes what " +
                "the price feels like. Thirty feels affordable in a way a hundred and twenty " +
                "does not, even though they are the same thing. Four of those running at once " +
                "and you have a monthly commitment you never decided to take on.",
        ),
        whySchoolSkipsIt = "These plans are newer than most syllabuses. Nobody teaching you " +
            "had one at your age, so nobody thinks to warn you about one — and the plans are " +
            "designed to feel like a payment method rather than a loan.",
    ),

    mechanism = Mechanism(
        intro = "Nothing here is interest, so the ordinary APR maths doesn't apply. What you " +
            "need instead is a way to price a flat fee — and to see what that fee is worth " +
            "when you scale it to a year, which is the only way to compare it with anything.",
        formulas = listOf(
            "Instalment = Price ÷ n",
            "Fee share = Fee ÷ Price",
            "Annual rate = (Fee ÷ Instalment) × (365 ÷ Days late)",
        ),
        plainEnglish = "The first is what leaves your account each time. The second is what a " +
            "slip added to the thing you bought. The third turns that flat fee into a yearly " +
            "rate, so you can put it next to a card and see which one is actually expensive.",
        variables = listOf(
            Variable("Price", "What it costs", "The sticker price of the thing.", "{c}120"),
            Variable("n", "Instalments", "How many payments the plan splits it into.", "4"),
            Variable("Fee", "Late fee", "The flat charge for missing one payment.", "{c}6"),
            Variable("Days late", "How long", "Days between the missed date and paying up.", "14"),
            Variable("Annual rate", "The real price", "What that fee is worth as a yearly rate.", "521%"),
        ),
        steps = listOf(
            "Split the price: 120 ÷ 4 = 30. The first 30 goes at the till, so you only ever " +
                "borrow 90.",
            "Miss one payment and a flat 6 is added. As a share of the order that's 6 ÷ 120 = 5%.",
            "Now price it properly. You kept 30 for 14 extra days and it cost 6. That's 6 ÷ 30 " +
                "= 20% for a fortnight, and 365 ÷ 14 = 26 fortnights in a year: 20% × 26 = 521% " +
                "a year.",
        ),
        watchOut = "521% is an annualised rate, not a bill — you won't pay it unless you keep " +
            "being late all year. It exists to make the comparison possible, and the " +
            "comparison is brutal: a flat fee on a small amount for a short time is the most " +
            "expensive shape borrowing comes in. That's also why the fee looks harmless. " +
            "Small number, small window, enormous rate.",
    ),

    realNumbers = RealNumbers(
        persona = "Trainers, {c}120, split into four payments of {c}30 a fortnight apart.",
        steps = listOf(
            ExampleStep(
                text = "Everything on time. This is the honest case, and it really is free.",
                math = "4 × 30 = 120, fees 0",
            ),
            ExampleStep(
                text = "The third payment bounces — payday moved. One flat fee, and you " +
                    "settle two weeks later.",
                math = "120 + 6 = 126",
            ),
            ExampleStep(
                text = "Price that fortnight properly: 6 charged on the 30 you kept.",
                math = "(6 ÷ 30) × (365 ÷ 14) = 521%",
            ),
            ExampleStep(
                text = "Borrowing that same 30 for the same 14 days on a card at 20% APR " +
                    "would have cost 23 cents in interest.",
                math = "30 × 0.20 ÷ 365 × 14 = 0.23",
            ),
        ),
        punchline = "One missed payment on the 0% plan cost {c}6. The 20% card everyone warns " +
            "you about would have cost {c}0.23 for the identical fortnight.",
        punchlineTone = Tone.COST,
        realityCheck = "None of which makes these plans a trap you should never touch. Paid on " +
            "time they cost nothing, and that is a real option. The risk isn't one plan — it's " +
            "four of them running at once, each one small enough to say yes to, on four " +
            "different dates you are no longer tracking. Before you tap, ask the question the " +
            "checkout is designed to stop you asking: would I buy this today at full price?",
    ),

    check = listOf(
        Question(
            prompt = "You pay all four instalments on time. What did the plan cost you?",
            options = listOf(
                "A small hidden interest charge",
                "Nothing",
                "A percentage taken at checkout",
            ),
            correctIndex = 1,
            why = "Paid on time, these plans really are free to you. The provider is paid by " +
                "the shop, which accepts a smaller amount in exchange for you buying more.",
            whyNotOthers = "There's no hidden interest and nothing is skimmed from you at the " +
                "till — which is exactly why the fee for being late catches people off guard.",
        ),
        Question(
            prompt = "A {c}6 late fee on a {c}30 instalment, settled 14 days later. Roughly " +
                "what yearly rate is that?",
            options = listOf(
                "About 20%",
                "About 60%",
                "Over 500%",
            ),
            correctIndex = 2,
            why = "6 on 30 is 20% — for a fortnight. There are about 26 fortnights in a year, " +
                "so annualised it's 20% × 26 = 521%.",
            whyNotOthers = "20% is the fee as a share of the instalment, before you account " +
                "for how short the loan was. Any answer under 100% ignores the length of the " +
                "window, which is the whole reason a small flat fee prices so badly.",
        ),
        Question(
            prompt = "Which is riskier: one {c}400 plan, or four {c}100 plans on different dates?",
            options = listOf(
                "The single {c}400 plan — bigger commitment",
                "The four {c}100 plans",
                "Identical: it's {c}400 either way",
            ),
            correctIndex = 1,
            why = "Four plans means four dates to track and four chances to be charged. Each " +
                "one was easy to say yes to precisely because it was small, and together they " +
                "are a monthly commitment nobody ever sat down and agreed to.",
            whyNotOthers = "The total is the same, which is what makes this feel like a trick " +
                "question — but the fee risk multiplies with the number of plans, not the size " +
                "of them, and so does the chance of losing track.",
        ),
    ),

    takeaway = "The plan is free until it isn't, and the moment it isn't, it's the most " +
        "expensive money you'll ever borrow.",
    action = "Open your banking app and search the last three months for any instalment " +
        "provider. Count how many plans are still running and write down the total left to " +
        "pay. Most people find one more than they remembered.",
)

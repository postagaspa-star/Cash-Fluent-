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

val CompoundInterestModule = Module(
    id = "compound-interest",
    number = 2,
    title = "Compound interest",
    hook = "Why your years are worth more than your salary",
    minutes = 7,
    simulator = SimulatorKind.COMPOUND,

    idea = Idea(
        paragraphs = listOf(
            "The most valuable thing you own right now isn't money. It's the number of " +
                "years between you and forty.",
            "Interest doesn't just get added on. It joins the pile, and then it earns " +
                "interest of its own. Do that for a few years and it's barely noticeable. Do " +
                "it for thirty and it stops behaving like addition altogether.",
            "Which makes this the one thing in personal finance that rewards the person with " +
                "less money and more time. That's a strange and genuinely good piece of news " +
                "when you're sixteen.",
        ),
        whySchoolSkipsIt = "School does teach the maths — powers, exponential growth, all of " +
            "it. It just never mentions that the exponent is your own life, and that the " +
            "variable you control most is when you start.",
    ),

    mechanism = Mechanism(
        intro = "Two formulas cover almost everything you'll need. One for a lump sum left " +
            "alone, one for money added every month.",
        formulas = listOf(
            "FV = PV × (1 + r)ⁿ",
            "FV = PMT × [ ((1 + i)ⁿ − 1) ÷ i ]",
        ),
        plainEnglish = "The first says what a sum becomes if you leave it alone. The second " +
            "says what a repeated monthly deposit turns into.",
        variables = listOf(
            Variable("FV", "Future value", "What you end up with at the end.", "{c}131,757"),
            Variable("PV", "Present value", "What you have right now, before you add anything.", "{c}0"),
            Variable("PMT", "Payment", "What you put in every month.", "{c}100"),
            Variable("r", "Yearly rate", "The return over a year. 7% is written 0.07.", "0.07"),
            Variable("i", "Monthly rate", "r ÷ 12 — because you're paying every month, not once a year.", "0.005833"),
            Variable("n", "Periods", "How many payments in total: years × 12.", "120"),
        ),
        steps = listOf(
            "Month one: your 100 just sits there.",
            "Month two: you add 100, and the first 100 has already earned a little.",
            "Year thirty: most of the pile was never deposited by you. It was earned by the " +
                "money that arrived early.",
        ),
        watchOut = "7% is a historical average, not a promise. Real years are messy and some " +
            "are strongly negative. The maths only works if you don't sell during those — " +
            "which is the genuinely hard part, and the reason this belongs to money you " +
            "won't need soon.",
    ),

    realNumbers = RealNumbers(
        persona = "Alex and Sam. Same {c}100 a month, same 7%. The only difference is when.",
        steps = listOf(
            ExampleStep("Alex pays 100 a month from 18 to 28. That's 120 payments and 12,000 " +
                "in total — then Alex stops completely and never adds another one."),
            ExampleStep("At 28 the pile is 17,308. Nothing spectacular yet: 12,000 of " +
                "deposits and about 5,300 of growth."),
            ExampleStep(
                text = "Then it sits untouched for thirty more years at 7%.",
                math = "17,308 × 1.07³⁰ = 131,757",
            ),
            ExampleStep("Sam starts at 28 and pays 100 a month for thirty straight years — " +
                "360 payments, 36,000 in total, three times what Alex put in. At 58: 121,997."),
        ),
        punchline = "Alex paid in {c}24,000 less and finished {c}9,760 ahead. Nothing was " +
            "invested better. It just arrived earlier.",
        punchlineTone = Tone.GOOD,
        // This paragraph is not optional softening. Without it the module tells a
        // twenty-four-year-old they have already lost, which is the exact opposite of
        // what it is for.
        realityCheck = "Read that the right way. It does not say you've already lost at 25. " +
            "Sam still ends with 122,000 out of 100 a month, and that's a life-changing " +
            "number for most people. What it says is that waiting costs more than the amount " +
            "does — so the conclusion is \"start with whatever is small\", never \"you should " +
            "have been born earlier\".",
    ),

    check = listOf(
        Question(
            prompt = "Which is worth more by 60: 200 a month for ten years starting now, or " +
                "200 a month for ten years starting in ten years?",
            options = listOf(
                "Starting now",
                "Starting in ten years — you'd be earning more by then anyway",
                "Exactly the same. It's the same money either way",
            ),
            correctIndex = 0,
            why = "Identical deposits, identical rate. The only difference is that the first " +
                "version's money gets ten extra years to compound — and those are the years " +
                "doing the heavy lifting.",
            whyNotOthers = "What you earn later doesn't change what these particular deposits " +
                "do. And \"same money in\" is the trap: the total paid in is identical, the " +
                "time is not.",
        ),
        Question(
            prompt = "What is i in the formula?",
            options = listOf(
                "The interest you've earned so far",
                "The monthly rate — the yearly rate divided by 12",
                "Inflation",
            ),
            correctIndex = 1,
            why = "You're depositing every month, so the rate has to be expressed per month. " +
                "7% a year is 0.07 ÷ 12 = 0.005833 a month.",
            whyNotOthers = "Interest earned is what comes out, not what goes in. Inflation is " +
                "a separate idea — it turns up in module 4.",
        ),
        Question(
            prompt = "If you double the monthly amount, does the final number double?",
            options = listOf("Yes", "No, it more than doubles", "No, it less than doubles"),
            correctIndex = 0,
            why = "The formula multiplies by PMT, so twice the deposit is exactly twice the " +
                "result. Worth knowing — and worth noticing that doubling the years does far " +
                "more than doubling. That asymmetry is the whole module.",
            whyNotOthers = "The exponential part lives in the years, not in the amount. " +
                "Doubling what you put in is powerful in the ordinary way; starting earlier " +
                "is powerful in the other way.",
        ),
    ),

    takeaway = "You can't get more money by wanting it. You can get more time by starting now.",
    action = "Decide the amount you could put aside every month without noticing. Not the " +
        "amount you think you should — the amount you wouldn't feel. Write it down. That's " +
        "your real number.",
)

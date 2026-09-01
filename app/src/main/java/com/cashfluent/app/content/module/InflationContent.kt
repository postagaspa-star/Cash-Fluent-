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

val InflationModule = Module(
    id = "inflation",
    number = 4,
    title = "Why cash quietly shrinks",
    hook = "Same balance, less money",
    minutes = 6,
    simulator = SimulatorKind.INFLATION,

    idea = Idea(
        paragraphs = listOf(
            "Money in a current account feels like the safe choice. It is safe from going " +
                "down. It is not safe from being worth less.",
            "Prices rise a little every year. The number in your account doesn't. Nothing " +
                "happens, no alert arrives, and the money quietly buys less than it did.",
            "So doing nothing is also a decision, and it has a price. Not a disaster — just " +
                "a price, worth knowing about before you choose it.",
        ),
        whySchoolSkipsIt = "Inflation gets taught as a chapter of macroeconomics, with charts " +
            "of national statistics. It's almost never taught as a line on your own bank " +
            "balance.",
    ),

    mechanism = Mechanism(
        intro = "One idea, two ways of writing it: what your money is worth once prices have " +
            "moved.",
        formulas = listOf(
            "Real value = Nominal ÷ (1 + π)ⁿ",
            "Real return = (1 + r) ÷ (1 + π) − 1   ≈   r − π",
        ),
        plainEnglish = "Divide by how much prices went up, and you get what the money is " +
            "actually worth — in today's money, which is the only money you can picture.",
        variables = listOf(
            Variable("Nominal", "Nominal value", "The number the bank shows you.", "{c}3,075.75"),
            Variable("π", "Inflation", "How much prices go up in a year. 3% is written 0.03.", "0.03"),
            Variable("r", "Your rate", "What the money earns where you're keeping it.", "0.005"),
            Variable("n", "Years", "How long it stays there.", "5"),
        ),
        steps = listOf(
            "Grow the money by its own rate. That's the nominal number, the one the bank " +
                "shows you.",
            "Grow prices by inflation over the same number of years.",
            "Divide one by the other. What comes out is what that pile can actually buy.",
        ),
        watchOut = "This module is not telling you to invest everything. Cash has one job it " +
            "does better than anything else: being there instantly when something goes wrong. " +
            "Three to six months of essential costs, liquid, untouched, on purpose. The " +
            "question is only what happens to the money beyond that.",
    ),

    realNumbers = RealNumbers(
        persona = "{c}3,000 set aside and left alone for five years, with inflation at 3%.",
        steps = listOf(
            ExampleStep(
                text = "Prices after five years. Everything costs about 16% more than it did.",
                math = "1.03⁵ = 1.159",
            ),
            ExampleStep(
                text = "In a current account paying 0.5%. The number went up by 76.",
                math = "3,000 × 1.005⁵ = 3,075.75",
            ),
            ExampleStep(
                text = "What it's worth in today's money.",
                math = "3,075.75 ÷ 1.159 = 2,653",
            ),
            ExampleStep(
                text = "Invested at 6% instead — which is 3,463 in today's money.",
                math = "3,000 × 1.06⁵ = 4,014.68",
            ),
        ),
        punchline = "The balance rose by {c}76 and lost {c}347 of buying power. Nobody sends " +
            "a notification about that.",
        punchlineTone = Tone.COST,
        realityCheck = "And still: if those 3,000 are your emergency fund, they belong " +
            "exactly where they are. Losing 347 over five years is the price of being able " +
            "to handle a broken laptop or a dentist on a Tuesday without asking anyone for " +
            "anything. That's a price worth paying. The mistake isn't paying it — it's " +
            "paying it without knowing.",
    ),

    check = listOf(
        Question(
            prompt = "Your account pays 1% and inflation is 3%. Are you gaining or losing?",
            options = listOf(
                "Gaining — the balance is going up",
                "Losing about 2% a year in what the money can buy",
                "Neither. They cancel each other out",
            ),
            correctIndex = 1,
            why = "The number on the screen grows by 1% while the cost of things grows by 3%. " +
                "The gap is roughly 2% a year of buying power, even though nothing looks " +
                "wrong anywhere.",
            whyNotOthers = "A rising balance and rising value are not the same thing. And 1% " +
                "against 3% doesn't cancel out — it leaves a 2% hole.",
        ),
        Question(
            prompt = "Where should an emergency fund be kept?",
            options = listOf(
                "Invested, so it at least keeps up with inflation",
                "In cash, liquid, even though inflation eats at it",
                "Split evenly between the two",
            ),
            correctIndex = 1,
            why = "An emergency fund has exactly one job: being available immediately, in " +
                "full, on a bad day. Investments can be down precisely when you need them, " +
                "which is the one thing this money isn't allowed to do.",
            whyNotOthers = "Chasing a return with this particular money defeats its purpose. " +
                "And splitting it means only half of it can do its job.",
        ),
    ),

    takeaway = "Cash isn't safe or risky. It's the right tool for one job and the wrong one " +
        "for the other.",
    action = "Look up the interest rate your own account actually pays. Almost nobody has " +
        "ever checked. Then look up this year's inflation and compare the two.",
)

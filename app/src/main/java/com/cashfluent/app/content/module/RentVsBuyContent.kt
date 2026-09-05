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

val RentVsBuyModule = Module(
    id = "rent-vs-buy",
    number = 10,
    title = "Rent or buy",
    hook = "\"Renting is throwing money away\" is a slogan, not a calculation",
    minutes = 8,
    simulator = SimulatorKind.MORTGAGE,

    idea = Idea(
        paragraphs = listOf(
            "You will hear that renting is throwing money away, usually from someone who " +
                "bought a home a long time ago. It's the most repeated piece of money advice " +
                "there is, and it's the one least often accompanied by any arithmetic.",
            "Buying is not the opposite of throwing money away. Part of what an owner pays " +
                "every month is theirs — it comes off what they owe. The rest is interest, " +
                "upkeep, insurance and fees, and every bit of that is gone in exactly the way " +
                "rent is gone. In the early years the gone part is much bigger than the " +
                "kept part.",
            "Which doesn't make buying wrong. It makes it a calculation instead of a slogan, " +
                "and the calculation turns on things you can actually look up: the rate, the " +
                "rent, and how long you plan to stay. This is the one lesson here you probably " +
                "won't use for a decade — which is exactly why it's worth being able to run " +
                "before someone runs it at you.",
        ),
        whySchoolSkipsIt = "It's treated as something you'll work out when you get there. But " +
            "\"when you get there\" is the moment you're sitting opposite someone whose job is " +
            "to sell you the loan, and that is a bad time to be seeing the formula for the " +
            "first time.",
    ),

    mechanism = Mechanism(
        intro = "One formula does the heavy lifting, and you have already met it. In module 02 " +
            "it turned a repeated payment into a final pile. Run it backwards and it turns a " +
            "pile you owe into the repeated payment that clears it.",
        formulas = listOf(
            "M = P × [ i(1+i)ⁿ ] ÷ [ (1+i)ⁿ − 1 ]",
            "Cost of owning = M + upkeep",
            "Upkeep ≈ 1% of the price, per year",
        ),
        plainEnglish = "M is the monthly payment that clears the loan exactly on the last " +
            "month, interest included. It is not the cost of owning — add roughly 1% of the " +
            "home's value a year for the things that break, and the fees you paid to get in.",
        variables = listOf(
            Variable("P", "Principal", "The price minus your deposit — what you borrow.", "{c}180,000"),
            Variable("i", "Monthly rate", "The yearly rate ÷ 12.", "0.00375"),
            Variable("n", "Payments", "Years × 12.", "360"),
            Variable("M", "Monthly payment", "What clears the loan on schedule.", "{c}912"),
            Variable("Upkeep", "What breaks", "About 1% of the price a year, spread monthly.", "{c}167"),
        ),
        steps = listOf(
            "Find what you actually borrow. A 200,000 home with 10% down is 20,000 of deposit " +
                "and 180,000 of loan.",
            "Turn the yearly rate into a monthly one and count the payments: 4.5% ÷ 12 = " +
                "0.00375, and 30 years is 360 payments. Then run the formula: M = 912.03.",
            "Add what the formula doesn't know about. Upkeep at 1% a year is 2,000, so 167 a " +
                "month — and getting in cost about 4% of the price in fees, 8,000, which is " +
                "gone the day you sign. Owning costs 1,079 a month, not 912.",
        ),
        watchOut = "Two things the arithmetic can't tell you. It assumes the rate holds for " +
            "thirty years, which it will not in most countries — a fixed period ends and you " +
            "re-run the whole calculation at whatever rate exists then. And it assumes the " +
            "price of the home doesn't move, which is the single biggest number in the real " +
            "answer and the one nobody can predict. Anyone who tells you which way it will go " +
            "is guessing in a confident voice.",
    ),

    realNumbers = RealNumbers(
        persona = "A {c}200,000 flat with 10% down, at 4.5% over 30 years — against renting " +
            "the same flat for {c}950 a month. Five years either way.",
        steps = listOf(
            ExampleStep(
                text = "The payment the formula gives, on 180,000 borrowed.",
                math = "M = 912.03",
            ),
            ExampleStep(
                text = "The real monthly cost of owning, once upkeep is in.",
                math = "912.03 + 166.67 = 1,078.70",
            ),
            ExampleStep(
                text = "Five years of owning: sixty payments, sixty months of upkeep, plus the " +
                    "20,000 deposit and 8,000 in fees paid on day one.",
                math = "64,722 + 28,000 = 92,722",
            ),
            ExampleStep(
                text = "Five years of renting the same flat.",
                math = "950 × 60 = 57,000",
            ),
            ExampleStep(
                text = "So owning cost 35,722 more in cash. But some of that came back: after " +
                    "five years, 15,916 of the loan is repaid, and the deposit is still yours.",
                math = "20,000 + 15,916 = 35,916 owned",
            ),
        ),
        punchline = "Owning cost {c}35,722 more and left you owning {c}35,916. After five " +
            "years it's a draw — to within {c}194.",
        punchlineTone = Tone.GOOD,
        realityCheck = "That result is far closer than either side of the argument expects, and " +
            "it is missing two real things. Rent tends to rise while a fixed payment doesn't, " +
            "which favours buying the longer you stay. But the 28,000 you handed over on day " +
            "one could have been invested instead — at 5% it would have earned about 7,700 over " +
            "those same five years, and that is the true cost of the deposit. Run it over " +
            "fifteen years and owning pulls clearly ahead, because more of each payment is " +
            "principal by then. Which is the actual answer: it's not about the money, it's " +
            "about how long you're staying.",
    ),

    check = listOf(
        Question(
            prompt = "The monthly payment on the loan is {c}912. What does owning the place " +
                "cost per month?",
            options = listOf(
                "{c}912 — that's the payment",
                "More than {c}912",
                "Less, because part of the payment comes back to you",
            ),
            correctIndex = 1,
            why = "The payment is what the lender needs. The building still needs a roof, a " +
                "boiler and insurance — roughly 1% of the value a year, which here is another " +
                "167 a month.",
            whyNotOthers = "Part of the payment genuinely does come back to you as equity, but " +
                "that's a separate question from what leaves your account each month, and it " +
                "doesn't reduce the bill.",
        ),
        Question(
            prompt = "You'll be in the city for two years. Which is usually the better " +
                "arithmetic?",
            options = listOf(
                "Buy — every month of rent is wasted",
                "Rent",
                "It makes no difference over two years",
            ),
            correctIndex = 1,
            why = "The fees to buy are paid on day one and never come back, and in the early " +
                "years almost all of the payment is interest rather than principal. Two years " +
                "is nowhere near long enough to earn that back.",
            whyNotOthers = "\"Rent is wasted\" ignores that interest, fees and upkeep are just " +
                "as gone as rent is. And the difference over two years is large, not neutral — " +
                "it's just pointing the other way from the slogan.",
        ),
        Question(
            prompt = "What is the cost of putting {c}28,000 into a deposit and fees?",
            options = listOf(
                "Nothing — the deposit is still yours",
                "The {c}8,000 in fees",
                "The fees, plus everything the whole {c}28,000 would have earned elsewhere",
            ),
            correctIndex = 2,
            why = "The fees are simply gone. The deposit isn't gone, but it is committed — and " +
                "money committed to one thing can't be earning in another. At 5% that's about " +
                "7,700 over five years, and it's a real cost even though no bill ever arrives " +
                "for it.",
            whyNotOthers = "Calling the deposit free ignores what it could have been doing. " +
                "Counting only the fees is closer, but it stops one step short of the whole " +
                "cost.",
        ),
    ),

    takeaway = "Buying doesn't beat renting. Staying long enough beats moving — and the " +
        "formula tells you how long that is.",
    action = "Look up the monthly rent on a flat you'd actually want, and the asking price of " +
        "one like it nearby. Put both into the simulator with today's rate. You now know " +
        "something about your own city that most adults have never checked.",
)

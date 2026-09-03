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

val DebtModule = Module(
    id = "debt",
    number = 3,
    title = "Good debt, bad debt",
    hook = "Debt isn't evil. It has a price, and nobody shows you the tag",
    minutes = 7,
    simulator = SimulatorKind.DEBT,

    idea = Idea(
        paragraphs = listOf(
            "Debt usually gets discussed as a moral failing, which is useless. Debt is a " +
                "tool with a price tag, and only two questions decide whether it was a good " +
                "idea: what the price is, and what you bought with it.",
            "Borrowing for something that grows — a qualification, a tool that earns, " +
                "sometimes a home — is a different object from borrowing for something " +
                "you'll have used up before you've finished paying for it. Same word, " +
                "different thing.",
            "And the price is never the number on the tag. It's the number on the tag plus " +
                "everything the interest adds while you're still paying.",
        ),
        whySchoolSkipsIt = "Talking about debt with teenagers feels uncomfortable, so it gets " +
            "postponed. Then the first card arrives at eighteen and the conversation happens " +
            "anyway — with the lender doing the talking.",
    ),

    mechanism = Mechanism(
        intro = "Two things to work out: what the debt costs you this month, and how long it " +
            "takes to clear if you pay a fixed amount.",
        formulas = listOf(
            "Monthly interest = Balance × (APR ÷ 12)",
            "n = − ln(1 − (i × B) ÷ P) ÷ ln(1 + i)",
        ),
        plainEnglish = "The first is what the debt charges you just for existing this month. " +
            "The second is how many months it takes to disappear if you pay the same amount " +
            "every time.",
        variables = listOf(
            Variable("B", "Balance", "What you owe right now.", "{c}800"),
            Variable("APR", "Yearly rate", "The yearly rate the card or loan advertises.", "0.20"),
            Variable("i", "Monthly rate", "APR ÷ 12 — what you're actually charged each month.", "0.01667"),
            Variable("P", "Payment", "What you pay every month, kept the same.", "{c}25"),
            Variable("n", "Months", "How many months until it's gone.", "46.1"),
        ),
        steps = listOf(
            "Work out this month's interest first: balance × APR ÷ 12. On 800 at 20%, that's 13.33.",
            "Anything you pay above that comes off the balance. Anything below it gets added " +
                "to the balance.",
            "Next month the interest is worked out on the new balance — which is why paying a " +
                "little more shortens things faster than it feels like it should.",
        ),
        watchOut = "Here's the condition that decides everything: your payment has to be " +
            "bigger than balance × monthly rate. If it isn't, you are paying and owing more " +
            "at the same time. That's the minimum-payment trap, and it isn't a rare edge " +
            "case — it's the normal outcome of paying the minimum on a card.",
    ),

    realNumbers = RealNumbers(
        persona = "An {c}800 jacket, bought on a card at 20% APR.",
        steps = listOf(
            ExampleStep(
                text = "The month's interest. Every payment has to beat this number before a " +
                    "single unit comes off the debt.",
                math = "800 × 0.20 ÷ 12 = 13.33",
            ),
            ExampleStep("Paying 200 a month: gone in 4.2 months. Total paid 835. Interest 35."),
            ExampleStep("Paying 25 a month: 46.1 months — three years and ten months. Total " +
                "paid 1,153. Interest 353."),
            ExampleStep("Paying 13 a month: 13 is less than 13.33. The balance goes up every " +
                "single month, forever, no matter how reliably you pay."),
        ),
        punchline = "Same jacket, same price on the tag: {c}835, or {c}1,153, or never. The " +
            "price wasn't the problem. The payment was.",
        punchlineTone = Tone.COST,
        realityCheck = "Now put it next to the other kind. 5,000 at 4% for a qualification " +
            "that raises what you earn is a completely different object from 800 at 20% for " +
            "clothes: cheaper per unit borrowed, and buying something that pays you back. " +
            "Debt isn't the enemy. Expensive debt for things that shrink is.",
    ),

    check = listOf(
        Question(
            prompt = "You pay the minimum on your card every month, on time, without fail. " +
                "Does the balance always go down?",
            options = listOf(
                "Yes — that's what a payment does",
                "Not necessarily",
                "Only if you stop using the card",
            ),
            correctIndex = 1,
            why = "If the minimum is smaller than the month's interest, the interest is added " +
                "faster than your payment removes it, and the balance grows while you pay. " +
                "Paying on time protects your credit record — it doesn't guarantee progress.",
            whyNotOthers = "A payment only reduces a debt by whatever is left once the " +
                "interest is covered. And even with the card untouched, a payment below the " +
                "interest still loses ground.",
        ),
        Question(
            prompt = "Which is the worse debt: 5,000 at 4% for a course that raises your " +
                "income, or 800 at 20% for clothes?",
            options = listOf(
                "The 5,000 — it's the much bigger number",
                "The 800",
                "Neither. All debt is equally bad",
            ),
            correctIndex = 1,
            why = "20% is five times the rate, and it bought something that started losing " +
                "value immediately. Size isn't the measure — the rate is, plus whether the " +
                "thing you bought grows.",
            whyNotOthers = "The bigger number feels scarier, but it's cheaper per unit " +
                "borrowed and it's buying earning power. And \"all debt is equally bad\" is " +
                "the belief that stops people taking the one loan that would have paid for " +
                "itself.",
        ),
        Question(
            prompt = "Your payment is exactly equal to the month's interest. What happens?",
            options = listOf(
                "The debt clears, just slowly",
                "You pay forever and the balance never moves",
                "The debt grows",
            ),
            correctIndex = 1,
            why = "The payment covers the interest exactly, so there's nothing left over to " +
                "reduce the balance. Next month is identical to this one, and so is the one " +
                "after that.",
            whyNotOthers = "Nothing comes off the balance, so it can't clear. And it can't " +
                "grow either, because the interest is being covered — it just sits there.",
        ),
    ),

    takeaway = "The price on the tag is not the price you pay. The payment is.",
    action = "Find the APR on any card or loan you have — yours, or a family member's. It's " +
        "on the statement or in the app, usually in small print. Knowing the number is most " +
        "of the work.",
)

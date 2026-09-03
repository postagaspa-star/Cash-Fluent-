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

val CreditRecordModule = Module(
    id = "credit-record",
    number = 8,
    title = "Your credit record",
    hook = "A file about you exists already, and it decides what money costs you",
    minutes = 7,
    simulator = SimulatorKind.CREDIT,

    idea = Idea(
        paragraphs = listOf(
            "Somewhere there is a file about how you handle money. You never applied for it " +
                "and you can't opt out of it. Every lender you ever approach reads it first, " +
                "and what it says decides whether you get a yes, and at what price.",
            "It is not a judgement of you as a person, however much it feels like one. It is a " +
                "prediction, built from a handful of measurable things: whether you pay on " +
                "time, how much of your available credit you're using, how long you've been " +
                "doing it. Nothing in there is about being good with money in the abstract.",
            "Which is the useful part. A prediction built from measurable things can be " +
                "changed by changing those things — and two of them respond within a single " +
                "month. Most people never find that out, because nobody tells them the file " +
                "exists until the day they're turned down.",
        ),
        whySchoolSkipsIt = "Credit records feel like an adult problem, so they get filed under " +
            "later. But the file starts the moment you take out your first phone contract or " +
            "card, and the early years are the ones that set the pattern everything else is " +
            "measured against.",
    ),

    mechanism = Mechanism(
        intro = "Two things carry most of the weight. Paying on time is the heaviest and has " +
            "no formula — you either did or you didn't. The second one is arithmetic, and it " +
            "is the one you can move this month.",
        formulas = listOf(
            "Utilisation = Balance ÷ Limit",
            "Payment to reach a target = Balance − (Limit × Target)",
        ),
        plainEnglish = "The first is how much of the credit you've been offered you're " +
            "actually using. The second is the same formula rearranged to answer the only " +
            "question that matters: how much do I pay, and by when.",
        variables = listOf(
            Variable("Balance", "What you owe", "What's on the card when the statement is cut.", "{c}450"),
            Variable("Limit", "What you're offered", "The most the card lets you borrow.", "{c}1,000"),
            Variable("Utilisation", "The ratio", "Balance ÷ Limit, as a percentage.", "45%"),
            Variable("Target", "Where you want it", "The share most lenders read as comfortable.", "30%"),
        ),
        steps = listOf(
            "Work out the ratio: 450 ÷ 1,000 = 0.45, so 45%. Nothing is wrong here — you're " +
                "inside your limit and paying on time. It still reads as heavier use than a " +
                "lender likes.",
            "Rearrange to find the payment. The most you can owe and still be at 30% is 1,000 " +
                "× 0.30 = 300, so pay 450 − 300 = 150.",
            "Timing is the part everyone misses. The number that gets reported is the balance " +
                "on the day your statement is cut, not the day the bill is due. Pay the 150 " +
                "before that date and 30% is what gets recorded.",
        ),
        watchOut = "The 30% line is a widely used convention, not a law — the exact weighting " +
            "differs by country and by agency, and none of them publish the recipe. Two things " +
            "hold everywhere, though: a payment more than 30 days late is a different category " +
            "of damage from a high ratio and stays on the file for years, and closing an old " +
            "card removes its limit from the sum, which pushes your ratio up rather than down.",
    ),

    realNumbers = RealNumbers(
        persona = "Maya, 22, one card with a {c}1,000 limit, and a phone she just bought on it.",
        steps = listOf(
            ExampleStep(
                text = "The phone was 450. Her statement is cut two days later with the full " +
                    "amount still sitting on the card.",
                math = "450 ÷ 1,000 = 45%",
            ),
            ExampleStep(
                text = "She pays it in full a week later and owes nothing. But 45% is what was " +
                    "recorded, because the snapshot was taken before she paid.",
            ),
            ExampleStep(
                text = "Next month she does one thing differently: she pays 360 of it before " +
                    "the statement date instead of after.",
                math = "90 ÷ 1,000 = 9%",
            ),
            ExampleStep(
                text = "Same phone. Same money. Same month. The only change was which side of " +
                    "one date the payment landed on.",
            ),
        ),
        punchline = "45% or 9%, for identical behaviour. The file doesn't record what you did — " +
            "it records what you owed on the day it looked.",
        punchlineTone = Tone.GOOD,
        realityCheck = "Two honest limits. First, this is the smaller of the two levers: " +
            "utilisation resets every month, while a missed payment follows you for years, so " +
            "if you only ever do one thing, set up the direct debit for the minimum and never " +
            "think about it again. Second, a thin file — no history at all — isn't a good score, " +
            "it's an unknown one, and unknown is priced like risk. Time is the ingredient you " +
            "can't hurry, which is the one argument for starting small and early.",
    ),

    check = listOf(
        Question(
            prompt = "You owe {c}450 on a {c}1,000 limit and want to be at 30% when the " +
                "statement is cut. What do you pay?",
            options = listOf(
                "{c}300",
                "{c}150",
                "{c}135",
            ),
            correctIndex = 1,
            why = "30% of a 1,000 limit is a 300 balance, and you're at 450. The payment is " +
                "the gap: 450 − 300 = 150.",
            whyNotOthers = "300 is the balance you're aiming to be left with, not the payment. " +
                "135 is 30% of the 450 you owe, which answers a question nobody asked — the " +
                "target is a share of the limit, not of the balance.",
        ),
        Question(
            prompt = "You clear the card in full every month, always on time. Can your " +
                "utilisation still read high?",
            options = listOf(
                "No — a cleared card reads as 0%",
                "Yes, if the statement is cut before you pay",
                "Only if you go over the limit",
            ),
            correctIndex = 1,
            why = "The reported number is a snapshot on the statement date. If your spending " +
                "sits on the card when that snapshot is taken, that's what gets recorded, no " +
                "matter how promptly you pay afterwards.",
            whyNotOthers = "Clearing in full protects you from interest and from a late mark — " +
                "it just doesn't change a photograph that was already taken. And going over " +
                "the limit is a separate problem entirely.",
        ),
        Question(
            prompt = "You have an old card you never use. Closing it will —",
            options = listOf(
                "Help: one less debt to your name",
                "Push your ratio up, because its limit vanishes from the total",
                "Change nothing at all",
            ),
            correctIndex = 1,
            why = "Utilisation is measured across your total available credit. An unused card " +
                "contributes its limit to the bottom of the fraction and nothing to the top, " +
                "which is the best thing a card can do for the ratio. Close it and the same " +
                "balance is suddenly a bigger share of a smaller total.",
            whyNotOthers = "An unused card isn't a debt — there's nothing on it. And it isn't " +
                "neutral either: it's also usually your oldest account, and length of history " +
                "counts for something.",
        ),
    ),

    takeaway = "Two levers, and they work on different clocks: paying on time takes years to " +
        "build, and the ratio resets every single month.",
    action = "Find out when your statement date is — it's in the app, usually on the last " +
        "statement, and it is almost never the same as the payment due date. That one date is " +
        "when the photograph gets taken.",
)

package com.cashfluent.app.content.module

import com.cashfluent.app.content.ExampleStep
import com.cashfluent.app.content.Idea
import com.cashfluent.app.content.Mechanism
import com.cashfluent.app.content.Module
import com.cashfluent.app.content.Question
import com.cashfluent.app.content.RealNumbers
import com.cashfluent.app.content.SimulatorKind
import com.cashfluent.app.content.TaxBandRow
import com.cashfluent.app.content.Tone
import com.cashfluent.app.content.Variable

val PayslipModule = Module(
    id = "payslip",
    number = 6,
    title = "Gross vs net",
    hook = "The ad's number isn't the one that arrives",
    minutes = 7,
    simulator = SimulatorKind.PAYSLIP,

    idea = Idea(
        paragraphs = listOf(
            "The number in the job ad is not the number that arrives. Most people find that " +
                "out on their first payslip, and it feels like something went wrong.",
            "Nothing went wrong. Part of the gap is social contributions — pension, " +
                "healthcare, whatever your country funds that way — and part is income tax. " +
                "Both come off before the money reaches you.",
            "Knowing the mechanism is worth more than the reassurance, though. It's what lets " +
                "you read a payslip and notice when something on it doesn't add up.",
        ),
        whySchoolSkipsIt = "It's probably the single most immediately useful thing on this " +
            "list, and a real payslip almost never makes it into a classroom.",
    ),

    mechanism = Mechanism(
        intro = "Three steps, always in this order. The order matters, because tax is worked " +
            "out after contributions, not before.",
        formulas = listOf(
            "Net = Gross − Contributions − Income tax",
            "Contributions = Gross × c      Taxable = Gross − Contributions",
        ),
        plainEnglish = "Take contributions off the gross, then apply tax in bands to whatever " +
            "is left.",
        variables = listOf(
            Variable("Gross", "Gross pay", "The number in the job ad, before anything comes off.", "{c}24,000"),
            Variable("c", "Contribution rate", "The flat percentage taken for pension and healthcare.", "9%"),
            Variable("Taxable", "Taxable income", "What the tax bands are applied to — gross minus contributions.", "{c}21,840"),
            Variable("Net", "Net pay", "What actually reaches your account.", "{c}19,580"),
        ),
        steps = listOf(
            "Contributions come off first, as a flat percentage of the gross.",
            "What's left is your taxable income. The first slice of it isn't taxed at all.",
            "Each band's rate applies only to the part of your income inside that band — " +
                "never to the whole amount.",
        ),
        watchOut = "These rates are simplified and illustrative. They show how the machine " +
            "works; they are not your country's numbers, and real systems add deductions, " +
            "credits and regional variations on top. Look yours up — now that you know the " +
            "shape, they'll make sense.",
        bands = listOf(
            TaxBandRow("0 – 8,000", "0%"),
            TaxBandRow("8,000 – 20,000", "15%"),
            TaxBandRow("20,000 – 40,000", "25%"),
            TaxBandRow("above 40,000", "35%"),
        ),
        bandsNote = "A band's rate applies only to the part of your income inside that band, " +
            "never to all of it. Moving into a higher band doesn't touch what's already been " +
            "taxed below.",
    ),

    realNumbers = RealNumbers(
        persona = "A first contract: {c}24,000 gross a year, with contributions at 9%.",
        steps = listOf(
            ExampleStep(text = "Contributions", math = "24,000 × 9% = 2,160"),
            ExampleStep(text = "Taxable income", math = "24,000 − 2,160 = 21,840"),
            ExampleStep(text = "The first 8,000 isn't taxed. Nothing to pay on it.", math = "0"),
            ExampleStep(text = "From 8,000 to 20,000", math = "12,000 × 15% = 1,800"),
            ExampleStep(text = "From 20,000 to 21,840", math = "1,840 × 25% = 460"),
            ExampleStep(text = "Total tax", math = "1,800 + 460 = 2,260"),
            ExampleStep(
                text = "Net — which is 1,631.67 a month.",
                math = "24,000 − 2,160 − 2,260 = 19,580",
            ),
            ExampleStep("Your average deduction is 18.4%. Your marginal rate is 25%. Two " +
                "different numbers — and mixing them up is where the myth comes from."),
        ),
        punchline = "So here's the myth, killed with arithmetic: a raise from 24,000 to " +
            "26,000 takes your net from {c}19,580 to {c}20,945. That's {c}1,365 more in your " +
            "pocket — 68% of the raise. You never lose money by earning more.",
        punchlineTone = Tone.GOOD,
        realityCheck = "What can genuinely change is a benefit or a threshold that cuts off " +
            "at a certain income — those are real, and worth checking before you take on " +
            "extra hours. But the tax bands themselves can never take away more than the new " +
            "money you just earned.",
    ),

    check = listOf(
        Question(
            prompt = "A raise moves you into a higher tax band. Can you end up with less " +
                "money in your pocket?",
            options = listOf(
                "Yes — that's why a raise can backfire",
                "No — never",
                "Only if the raise is small",
            ),
            correctIndex = 1,
            why = "The higher rate applies only to the part of your income inside that band. " +
                "On a 2,000 raise you keep 1,365 — 68% of it.",
            whyNotOthers = "Nothing you already earned gets taxed again when you cross a " +
                "band, so the size of the raise never changes the answer.",
        ),
        Question(
            prompt = "You're offered 24,000 gross a year. Roughly what arrives each month?",
            options = listOf("2,000", "About 1,630", "About 1,200"),
            correctIndex = 1,
            why = "2,160 of contributions and 2,260 of tax come off, leaving 19,580 a year — " +
                "1,631.67 a month on these illustrative rates.",
            whyNotOthers = "2,000 is the gross divided by twelve, which is exactly the " +
                "mistake this module exists to prevent. And 1,200 is too pessimistic: the " +
                "total deduction here is 18%, not 40%.",
        ),
        Question(
            prompt = "What's the difference between your average rate and your marginal rate?",
            options = listOf(
                "The average is what you pay overall; the marginal is what you'd pay on the " +
                    "next unit you earn",
                "They're two names for the same thing",
                "The marginal one is last year's rate",
            ),
            correctIndex = 0,
            why = "Average is total deductions divided by total income — 18.4% here. Marginal " +
                "is the rate on your next unit earned — 25%. You use the marginal one to " +
                "decide whether extra work is worth it, and the average one to know what " +
                "actually arrives.",
            whyNotOthers = "They're only equal in a flat-rate system with no allowance, which " +
                "isn't how bands work anywhere.",
        ),
    ),

    takeaway = "Nobody ever lost money by earning more. Now you can prove it.",
    action = "Get hold of a real payslip — yours, or ask someone who'll show you theirs — and " +
        "find three numbers on it: gross, contributions, net. Check that they add up.",
)

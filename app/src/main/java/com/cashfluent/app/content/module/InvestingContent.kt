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

val InvestingModule = Module(
    id = "investing",
    number = 5,
    title = "What you're actually buying",
    hook = "A share is a slice of a real company",
    minutes = 8,
    simulator = SimulatorKind.FEES,

    idea = Idea(
        paragraphs = listOf(
            "For most people the first contact with investing is an app full of green " +
                "numbers and confetti, which makes the whole thing look like a game that " +
                "happens to use money.",
            "It isn't. A share is a slice of an actual company — one with employees, " +
                "customers, and something it sells. An index fund is a single purchase that " +
                "buys you a small slice of hundreds of them at once.",
            "And risk isn't a word for \"might be a scam\". It means how much the value " +
                "swings while you're holding it. Bigger swings, bigger long-run average — " +
                "that's the trade, and the only real question is whether you can sit through " +
                "the bad years.",
        ),
        whySchoolSkipsIt = "It looks like a topic for adults who already have money, so it " +
            "gets left out. Which means the explaining is done by whoever is selling something.",
    ),

    mechanism = Mechanism(
        intro = "The mechanism worth learning here isn't how to pick anything. It's what the " +
            "thing costs you every year just for existing.",
        formulas = listOf(
            "Annual cost = Value × TER",
            "Effective return = r − TER",
        ),
        plainEnglish = "The fee isn't a bill you get. It's taken out of the pile, every year, " +
            "before the return ever reaches you.",
        variables = listOf(
            Variable(
                symbol = "TER",
                name = "Total expense ratio",
                meaning = "The fund's yearly fee, as a percentage of everything you have in " +
                    "it. It's one line in the fund's document.",
                example = "0.20%",
            ),
            Variable("r", "Gross return", "What the fund earns before its fee is taken out.", "7%"),
            Variable(
                symbol = "—",
                name = "Diversification",
                meaning = "Owning many companies instead of one. Removes the risk of a single " +
                    "company failing; not the risk of the market falling.",
                example = "—",
            ),
        ),
        steps = listOf(
            "A fund charges a yearly percentage of everything you hold in it — 0.20%, or " +
                "1.20%, or more.",
            "It's deducted from the inside, so a 7% year with a 1.2% fee reaches you as 5.8%.",
            "And because it's taken every year, you don't only lose the fee. You lose " +
                "everything that fee would have earned for the rest of the time. That's the " +
                "part that gets big.",
        ),
        watchOut = "A historical average is not a guarantee, and a risk level you can't sleep " +
            "through is the wrong one for you no matter what a chart says. Also worth being " +
            "precise: diversification removes the risk of one company failing. It does not " +
            "remove the risk of the whole market falling.",
    ),

    realNumbers = RealNumbers(
        persona = "Two funds tracking the same index. {c}150 a month for thirty years, 7% " +
            "before fees.",
        steps = listOf(
            ExampleStep("Fund A charges 0.20%, so your effective return is 6.8%."),
            ExampleStep("Fund B charges 1.20%, so yours is 5.8%. Same index, same companies, " +
                "same everything else."),
            ExampleStep("Fund A after thirty years: 175,935."),
            ExampleStep("Fund B after thirty years: 145,040. You paid in 54,000 either way."),
        ),
        punchline = "One percent a year cost {c}30,895 — more than half of everything you " +
            "ever deposited.",
        punchlineTone = Tone.COST,
        realityCheck = "This is the one number a beginner can genuinely control. You can't " +
            "choose your returns and you can't time anything. You can read a fee: it's one " +
            "line in the fund's document, and it takes about thirty seconds.",
    ),

    check = listOf(
        Question(
            prompt = "What do you actually own when you buy a share?",
            options = listOf(
                "A bet on a price going up",
                "A slice of a real company",
                "A promise from a bank to pay you back",
            ),
            correctIndex = 1,
            why = "A share is part-ownership of a business. Its price moves because the " +
                "business's prospects move — and because people's opinions about those " +
                "prospects move.",
            whyNotOthers = "You can treat it as a bet, but that describes what you're doing, " +
                "not what the thing is. A promise to pay you back describes a bond or a " +
                "deposit, which is a different instrument entirely.",
        ),
        Question(
            prompt = "Two funds track the same index. One charges 0.2%, the other 1.2%. Does " +
                "the more expensive one return more?",
            options = listOf(
                "Yes — you get what you pay for",
                "No — same index, one point less every year",
                "It depends on the year",
            ),
            correctIndex = 1,
            why = "They hold the same companies, so they earn the same before costs. The fee " +
                "comes off after that, every year, from your side of it.",
            whyNotOthers = "Price signals quality in plenty of markets. Index funds are the " +
                "clearest place where it doesn't — the product is identical by construction, " +
                "whatever year it is.",
        ),
        Question(
            prompt = "Does diversification protect you from a general market crash?",
            options = listOf(
                "Yes — that's what it's for",
                "No — it protects you from one company failing",
                "Only if you hold more than fifty companies",
            ),
            correctIndex = 1,
            why = "Spreading across many companies means no single failure can take you down " +
                "with it. When the whole market falls, it falls across all of them at once, " +
                "and owning more of them doesn't help.",
            whyNotOthers = "There's no number of companies that removes market-wide risk. " +
                "That one is handled by time, and by not needing the money soon.",
        ),
    ),

    takeaway = "You can't pick the returns. You can read the fee.",
    action = "Pick any fund — one you own, one someone mentioned, one from an ad — and find " +
        "its TER. It's one line in the fund's document. You don't have to do anything about " +
        "it. Just find it.",
)

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

val BudgetingModule = Module(
    id = "budgeting",
    number = 1,
    title = "Budgeting",
    hook = "Your money doesn't vanish. It leaves in pieces you never decided on",
    minutes = 6,
    simulator = SimulatorKind.BUDGET,

    idea = Idea(
        paragraphs = listOf(
            "Money rarely disappears in one dramatic moment. It leaves in fifteen small " +
                "pieces you never quite decided on — a delivery, a subscription you forgot " +
                "about, a top-up, a round of drinks. At the end of the month the number is " +
                "gone and you genuinely cannot say where.",
            "A budget is not a punishment, and it is not a spreadsheet for accountants. It " +
                "is one decision made in advance: where the money goes, before it goes " +
                "somewhere on its own.",
            "Almost nobody who says \"I'm bad with money\" is actually bad with money. They " +
                "have just never seen the shape of their own month written down. That takes " +
                "about ten minutes, once.",
        ),
        whySchoolSkipsIt = "It looks too simple to be worth a lesson, so it never gets one. " +
            "Everybody works it out alone, late, and usually after something has already " +
            "gone wrong.",
    ),

    mechanism = Mechanism(
        intro = "There are two things worth knowing: where the money went, and roughly where " +
            "it should have gone. The first is arithmetic. The second is a target you steer by.",
        formulas = listOf(
            "Net income − Needs − Wants = What's left",
            "Needs = 0.50 × Net    Wants = 0.30 × Net    Future = 0.20 × Net",
        ),
        plainEnglish = "Out of every 100 that arrives: 50 keeps your life running, 30 makes " +
            "it worth living, 20 builds the next one.",
        variables = listOf(
            Variable(
                symbol = "Net",
                name = "Net income",
                meaning = "What actually lands in your account. Not the number on the " +
                    "contract — that one is before tax and contributions.",
                example = "{c}1,200",
            ),
            Variable(
                symbol = "Needs",
                name = "Needs",
                meaning = "If you stop paying, something breaks: rent, food, the travel " +
                    "that gets you to work, your phone.",
                example = "{c}600",
            ),
            Variable(
                symbol = "Wants",
                name = "Wants",
                meaning = "Life is not a punishment. Eating out, subscriptions, clothes " +
                    "beyond the ones you actually need.",
                example = "{c}360",
            ),
            Variable(
                symbol = "Future",
                name = "Future",
                meaning = "Savings, investing, and anything you pay above the minimum on a debt.",
                example = "{c}240",
            ),
        ),
        steps = listOf(
            "Write down what actually arrived this month. One number.",
            "Split what you spent into the two piles. Be honest about which pile a thing is " +
                "in — a gym you actually use is a want you chose, not a need.",
            "Whatever's left is the Future pile, even if it's zero. Especially if it's zero: " +
                "that's the number this module is about.",
        ),
        watchOut = "50/30/20 is a target you steer by, not a law you can fail. In an " +
            "expensive city rent alone can pass 50%, and that's normal, not a personal " +
            "failure. The rule's job is to show you the size of the gap, not to grade you.",
    ),

    realNumbers = RealNumbers(
        persona = "Maya, 19. She works part-time and {c}1,200 lands in her account each month.",
        steps = listOf(
            ExampleStep("The target first: 50% of 1,200 is 600 for needs, 30% is 360 for " +
                "wants, 20% is 240 for her future."),
            ExampleStep("What actually happened: rent share 450, groceries 160, travel 45, " +
                "phone 15. Needs came to 670 — that's 56%."),
            ExampleStep("Wants came to 390, which is 32%. Close enough to the target that it " +
                "isn't the story."),
            ExampleStep("Which leaves 140 for the future. That's 12%, not 20%."),
        ),
        punchline = "The gap is {c}100 a month. Over a year that's {c}1,200 — a whole month " +
            "of her income that never gets to exist.",
        punchlineTone = Tone.COST,
        realityCheck = "Here's the part that matters: the fix is in the big lines, not the " +
            "small ones. Maya's largest number is 450 of rent. A flatmate, a yearly travel " +
            "pass instead of single tickets, one subscription cancelled — those move more " +
            "than thirty small acts of self-denial, and they only have to be decided once. " +
            "Cutting coffee is the advice people give because it's easy to say, not because " +
            "it works.",
    ),

    check = listOf(
        Question(
            prompt = "Your rent alone is 55% of what you earn. Have you failed the 50/30/20 rule?",
            options = listOf(
                "Yes — the rule says 50%, and you're over it",
                "No — it's a target you steer by, so you rebalance the other two",
                "Yes, unless you earn more than average",
            ),
            correctIndex = 1,
            why = "The rule exists to show you the size of a gap, not to grade you. If needs " +
                "take 55%, the honest move is to decide what gives — a smaller wants pile, a " +
                "cheaper fixed cost, or a smaller future pile for now, chosen on purpose " +
                "instead of by accident.",
            whyNotOthers = "Nothing in the rule is enforceable, and it doesn't change with " +
                "income — the percentages are the same at 800 a month and at 8,000.",
        ),
        Question(
            prompt = "Which one of these is a need?",
            options = listOf(
                "The streaming subscription you use every single day",
                "The bus pass that gets you to work",
                "A new phone while your current one still works",
            ),
            correctIndex = 1,
            why = "A need is something that breaks your life if you stop paying it. Without " +
                "the bus pass you don't get to work, and without work there is no income at all.",
            whyNotOthers = "A subscription you love is a want you chose deliberately — that's " +
                "exactly what the 30% is for, and there's nothing wrong with it. And a phone " +
                "that still works is a want no matter how much you'd like the new one.",
        ),
        Question(
            prompt = "You get a 100 raise. Under 50/30/20, how much of it goes to your future?",
            options = listOf("20", "50", "All of it — raises should be saved"),
            correctIndex = 0,
            why = "The split applies to every unit of money that arrives, including new ones. " +
                "20% of 100 is 20.",
            whyNotOthers = "50 is the needs share. And \"save all of it\" is advice that " +
                "sounds responsible and lasts about two months — a rule you actually keep " +
                "beats a stricter one you abandon.",
        ),
    ),

    takeaway = "A budget isn't about spending less. It's about knowing which of your " +
        "numbers is the big one.",
    action = "Write down every payment that left your account in the last 7 days. Don't " +
        "judge it, don't change it. Just find the biggest line.",
)

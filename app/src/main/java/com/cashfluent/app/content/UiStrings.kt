package com.cashfluent.app.content

/**
 * Everything on screen that is not module content. Kept beside the modules rather than
 * in strings.xml so all the app's words live in one place and stay compile-checked.
 */
object UiStrings {

    const val APP_NAME = "Cashfluent"
    const val TAGLINE = "The money class you never had."

    // Home
    const val METHOD_TITLE = "Every module has three parts"
    val METHOD_CHIPS = listOf("① the idea", "② the formula", "③ real numbers")
    const val METHOD_DISMISS = "Got it"
    const val SECTION_CONTINUE = "Continue where you left off"
    const val SECTION_START = "Start here"
    const val SECTION_ALL = "All lessons"
    const val ACTION_START = "Start this lesson"
    const val ACTION_CONTINUE = "Pick this back up"
    const val ALL_DONE_TITLE = "You've finished every lesson"
    const val ALL_DONE_BODY = "Come back to any of them whenever a real decision turns up — that is when they are worth rereading."
    const val STATUS_NEW = "New"
    const val STATUS_IN_PROGRESS = "In progress"
    const val STATUS_DONE = "Done"
    const val BADGE_START_HERE = "Start here"
    const val DISCLAIMER = "Educational content. Not financial advice."
    fun progress(done: Int, total: Int) = "$done of $total done"
    fun locked(moduleNumber: String) = "Finish module $moduleNumber to open this"

    // Module
    const val SECTION_IDEA = "① The idea"
    const val SECTION_MECHANISM = "② The mechanism"
    const val SECTION_REAL = "③ Real numbers"
    const val SECTION_TRY_IT = "Try it with your numbers"
    const val SECTION_CHECK = "Quick check"
    const val WHY_SCHOOL = "Why school skips this"
    const val PLAIN_ENGLISH = "In plain English:"
    const val STEP_BY_STEP = "Step by step"
    const val WATCH_OUT = "Watch out"
    const val REALITY_CHECK = "Reality check"
    const val RESET_EXAMPLE = "Reset to the example"
    const val MODULE_COMPLETE = "Module complete"
    const val TAKEAWAY = "Takeaway"
    const val ACTION = "One thing to do this week"
    const val UP_NEXT = "Up next"
    const val BACK_TO_ALL = "Back to all modules"
    const val BACK = "Back"

    // Check
    const val WHY = "Why"
    const val WHY_NOT = "Why not the others"
    fun questionProgress(index: Int, total: Int) = "$index of $total"

    // Settings
    const val SETTINGS = "Settings"
    const val GROUP_DISPLAY = "Display"
    const val CURRENCY_TITLE = "Currency symbol"
    const val CURRENCY_SUB = "Changes the symbol only — the example numbers stay the same"
    const val GROUP_LEARNING = "Learning"
    const val GUIDED_TITLE = "Guided path"
    const val GUIDED_SUB = "Unlock modules one at a time, in order. Off means everything is open"
    const val RESET_TITLE = "Reset progress"
    const val RESET_SUB = "Clears which modules you've finished. Nothing else is stored"
    const val RESET_ACTION = "Reset"
    const val RESET_CONFIRM_TITLE = "Reset your progress?"
    const val RESET_CONFIRM_BODY = "Every module goes back to New. This can't be undone."
    const val RESET_CONFIRM_OK = "Reset"
    const val RESET_CONFIRM_CANCEL = "Keep it"
    const val GROUP_ABOUT = "About"
    const val ABOUT_TITLE = "Why Cashfluent exists"
    const val ABOUT_SUB = "The problem, the method, and what this app is not"
    const val PRIVACY_NOTE =
        "No account. No ads. No tracking. Nothing you type here leaves your phone — " +
            "there is no server to send it to."
    fun version(build: String) = "Cashfluent $build · GatewayHacks 2026"
}

/** The About screen, which is also the clearest statement of what the project is for. */
object AboutContent {

    val problem = listOf(
        "Money skills are inherited, not taught. If someone at home talked about compound " +
            "interest, about gross versus net, about what a fund quietly charges you every " +
            "year, you start adult life roughly a decade ahead. If nobody did, you learn the " +
            "same things by making expensive mistakes.",
        "School mostly doesn't close that gap. So the gap follows the family you happened to " +
            "be born into — which is exactly the kind of gap education is supposed to remove.",
        "Cashfluent isn't a budgeting app and it isn't an investing app. It's a teaching app. " +
            "Every topic arrives in the same three parts: why it matters, the actual formula " +
            "with every symbol explained, and a real example you can run again with your own " +
            "numbers.",
    )

    const val METHOD_TITLE = "Why three parts, every time"
    const val METHOD_BODY =
        "Most money apps hand you a definition and a quiz. But knowing what an ETF is doesn't " +
            "tell you what a 1% yearly fee costs you over thirty years. The formula is the part " +
            "that transfers: once you can run it, you can answer questions nobody wrote a " +
            "lesson for."

    const val NOT_TITLE = "What this app is not"
    val notList = listOf(
        "It is not financial advice. It teaches mechanisms. What you do with your money " +
            "stays your call.",
        "It has nothing to sell you. No ads, no affiliate links, no broker to sign up with, " +
            "no premium tier.",
        "It doesn't want your data. There is no account and no server — everything stays on " +
            "this phone.",
        "The tax and contribution rates in these examples are simplified and illustrative. " +
            "Real rates differ by country and change from year to year. Look yours up — now " +
            "that you know the shape, they'll make sense.",
    )

    const val TYPE_CREDIT =
        "Set in Archivo and IBM Plex Mono, both under the SIL Open Font License 1.1."

    const val CREDIT = "Built for GatewayHacks 2026 · Equity in Education"
}

package com.cashfluent.app.content

import com.cashfluent.app.domain.finance.Money
import com.cashfluent.app.domain.league.League
import com.cashfluent.app.domain.league.LeagueCard
import com.cashfluent.app.domain.league.MergeResult
import com.cashfluent.app.domain.league.Movement
import com.cashfluent.app.domain.league.Promotion
import com.cashfluent.app.domain.league.Tier
import com.cashfluent.app.domain.league.WeekOutcome

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
    const val PLAY_ARROW = "Play →"
    const val LEAGUE_ARROW = "League →"
    fun gamesStrip(count: Int) = "$GAMES · $count mini-games"
    const val LEAGUE_STRIP_EMPTY = "Play a game to earn your first points"
    fun leagueStrip(tier: Tier, weekPoints: Int) = "${leagueName(tier)} · ${Money.number(weekPoints.toDouble())} pts this week"

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
    const val GAMES_ON_TOPIC = "Play the games on this topic"
    const val GAMES_ON_TOPIC_SUB = "Six mini-games, a minute each, on the formula you just read. Points count towards your league."

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
    const val RESET_SUB = "Clears finished modules, points and best scores, and drops you back to the Wood league. Your name and your friends' cards stay"
    const val RESET_ACTION = "Reset"
    const val RESET_CONFIRM_TITLE = "Reset your progress?"
    const val RESET_CONFIRM_BODY = "Every module goes back to New, your points and best scores go to zero, and you start again in the Wood league. This can't be undone."
    const val RESET_CONFIRM_OK = "Reset"
    const val RESET_CONFIRM_CANCEL = "Keep it"
    const val GROUP_ABOUT = "About"
    const val ABOUT_TITLE = "Why Cashfluent exists"
    const val ABOUT_SUB = "The problem, the method, and what this app is not"
    const val PRIVACY_NOTE =
        "No account. No ads. No tracking. Nothing you type here leaves your phone — " +
            "there is no server to send it to."
    fun version(build: String) = "Cashfluent $build · GatewayHacks 2026"

    // Games — the catalogue
    const val GAMES = "Games"
    const val GAMES_INTRO = "Sixty mini-games on the same ten topics as the lessons. Each one takes a " +
        "minute: four rounds, up to 100 points a round. Every answer is computed by the calculator " +
        "behind the lesson, so a game can never disagree with it."
    const val SURPRISE_ME = "Surprise me"
    const val NOT_PLAYED = "not played yet"
    fun bestShort(best: Int, max: Int) = "best $best of $max"
    fun gamesCount(count: Int) = if (count == 1) "1 game" else "$count games"

    // Games — one game
    const val GAME_INTRO = "Four rounds. Each one is the formula behind this topic, on numbers you haven't " +
        "seen yet. Within 5% of the answer is full marks."
    fun round(index: Int, total: Int) = "Round $index of $total"
    const val YOUR_ANSWER = "Your answer"
    const val THE_ANSWER = "The answer"
    const val LOCK_IN = "Lock in"
    const val NEXT_ROUND = "Next round"
    const val SEE_SCORE = "See your score"
    const val CALCULATION = "The calculation"
    const val YOUR_SCORE = "Your score"
    const val SCORE = "Score"
    const val BEST = "Best"
    const val NEW_BEST = "New best"
    const val PLAY_AGAIN = "Play again"
    const val ANOTHER_GAME = "Another game"
    const val ALL_GAMES = "All games"
    const val SEE_LEAGUE = "See the league"
    const val PICK_ONE = "Pick one to lock in"
    fun points(n: Int) = "${Money.number(n.toDouble())} pts"
    fun pointsSoFar(n: Int) = "$n pts so far"
    fun outOf(score: Int, max: Int) = "$score of $max"
    fun roundVerdict(points: Int) = when {
        points >= 100 -> "Spot on: $points points."
        points >= 70 -> "Close: $points points."
        points > 0 -> "Not far: $points points."
        else -> "Way off: 0 points. The calculation is below."
    }

    // League
    const val LEAGUE = "League"
    const val LEAGUE_TITLE = "Your league"
    fun leagueName(tier: Tier) = "${tier.label} league"
    const val THIS_WEEK = "This week"
    const val WEEK_SHORT = "Week"
    const val ALL_TIME = "All time"
    const val GAMES_PLAYED = "Games"
    const val YOUR_NAME = "Your name on the card"
    const val NAME_HINT = "A nickname. It travels with your card, so keep it to what you'd write on a team sheet."
    const val SHARE_CARD = "Share my card"
    const val PASTE_CARD = "Paste a friend's card"
    const val NAME_FIRST = "Pick a name first, so friends know whose card it is."
    const val LEAGUE_RULES = "Top five go up a league on Monday. In a board of ten or more, the bottom five " +
        "go down. Nobody with zero points holds their place."
    const val LEAGUE_HOW_TITLE = "How a league works here"
    const val LEAGUE_HOW = "Your card is one line of text: send it to friends on any app you already use, " +
        "and paste theirs here. The board ranks everyone on this week's points and starts again every " +
        "Monday, and where you finish decides the rung you start the next week on. Twenty people at most."
    const val LEAGUE_EMPTY = "Nobody else here yet. Share your card, then paste the ones that come back."
    const val LADDER = "The ladder"
    const val YOU_ARE_HERE = "you are here"
    const val ZONE_UP = "↑"
    const val ZONE_DOWN = "↓"
    const val ZONE_UP_DESC = "promotion zone"
    const val ZONE_DOWN_DESC = "demotion zone"
    const val OUTCOME_DISMISS = "Got it"
    const val YOU = "you"
    fun remove(name: String) = "Remove $name"
    const val NOTHING_FOUND = "No card in what you pasted. A card is one line that starts with CF1."
    const val CLIPBOARD_EMPTY = "Nothing to paste. Copy a friend's card first."
    fun imported(result: MergeResult): String {
        val parts = buildList {
            if (result.added > 0) add("${result.added} added")
            if (result.updated > 0) add("${result.updated} updated")
            if (result.unchanged > 0) add("${result.unchanged} already here")
            if (result.refusedFull > 0) add("${result.refusedFull} left out, the league is full at ${League.SIZE}")
            if (result.yourself > 0) add("your own card skipped")
        }
        if (parts.isEmpty()) return NOTHING_FOUND
        return parts.joinToString(", ").replaceFirstChar { it.uppercase() } + "."
    }
    fun outcomeBanner(outcome: WeekOutcome): String {
        val place = "${Promotion.ordinal(outcome.position)} of ${outcome.size}"
        return when (outcome.movement) {
            Movement.PROMOTED ->
                "Up to the ${leagueName(outcome.to)}. You finished $place last week with ${outcome.weekPoints} pts."
            Movement.DEMOTED ->
                if (outcome.weekPoints == 0) {
                    "Down to the ${leagueName(outcome.to)}: no points last week. One game a week holds your place."
                } else {
                    "Down to the ${leagueName(outcome.to)}. You finished $place last week."
                }
            Movement.STAYED ->
                "You hold your place in the ${leagueName(outcome.to)} — $place last week with ${outcome.weekPoints} pts."
        }
    }
    fun shareText(card: LeagueCard, token: String) =
        "Cashfluent league card\n" +
            "${card.name}: ${card.weekPoints} pts this week, ${card.totalPoints} all time, " +
            "${leagueName(card.tier)}.\n\n$token\n\n" +
            "To add me: open Cashfluent, tap League, then Paste a friend's card."
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

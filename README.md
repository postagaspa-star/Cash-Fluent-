# Cashfluent

**The money class you never had.**

A financial literacy tutor for 16–25 year olds, built for **GatewayHacks 2026** in the
*Equity in Education* category. Android, Kotlin, Jetpack Compose.

---

## The problem

Money skills are inherited, not taught. If someone at home talked about compound interest,
about gross versus net, about what a fund quietly charges you every year, you start adult
life roughly a decade ahead. If nobody did, you learn the same things by making expensive
mistakes — and school mostly doesn't close that gap.

Existing apps miss this reader in three specific ways. Budget trackers assume you already
have money to track. Quiz apps teach vocabulary instead of mechanisms: knowing what an ETF
is doesn't tell you what a 1% yearly fee costs you over thirty years. And most of the rest
is written by finance people, for finance people.

## The method

Every module arrives in the same three blocks, always in this order:

| | |
|---|---|
| **① The idea** | Why it matters. No numbers at all. |
| **② The mechanism** | The real formula, with every symbol named and given an example value. |
| **③ Real numbers** | A worked example with realistic figures, then the same calculation with *your* numbers. |

Then a short check with an explanation of why the right answer is right, and one concrete
thing to actually do this week.

Ten modules, in reading order: budgeting, compound interest, good debt and bad debt,
saving vs investing, shares and funds, gross vs net pay, buy now pay later, your credit
record, tax nobody deducts for you, and rent vs buy.

## The games, the points, the league

Games are their own section: **sixty mini-games** on the same ten topics as the lessons,
six per topic, each a minute long — four rounds, up to 100 points a round. Four
mechanics: set a number with a slider, pick one of a few options, higher or lower, true
or false. Every answer is computed by the calculator behind the lesson on that topic, so a
game can never disagree with the lesson, and every round ends with the calculation written
out — the reveal is the teaching, the score is what keeps you playing. *Surprise me* deals
any game at random.

Points add up per game, for the week and for all time. The **league** is weekly and shaped
like Duolingo's: a board of up to twenty ranked on this week's points, and a ladder of
eight rungs — Wood, Bronze, Silver, Gold, Ruby, Emerald, Diamond, Elite. On Monday the top
five go up a rung; in a board of ten or more the bottom five go down; nobody with zero
points holds their place.

The board is real and it is shared. The first time you open the league screen the app
signs your phone in anonymously — no email, no password, no sign-up screen — and seats you
with up to nineteen other people on your rung, that week. Nobody has to be invited and
nobody has to be found. What the server holds about you is a random id, the nickname you
typed, and two numbers.

Everything else keeps working with the network switched off: the lessons, the games, the
simulators and your points are on the phone. A game scored on a train is counted straight
away and reaches the board when there is a signal. If the league cannot be reached the
screen says so in one line, and nothing is lost.

## Status

The app is complete and building. Ten lessons, ten simulators, sixty games, a league,
seven screens.

- [x] Gradle build, Android manifest, resources
- [x] Design system — semantic colour, type scale, light and dark
- [x] Local persistence (DataStore) for progress, settings, points and league
- [x] Financial engine — ten calculators, 82 unit tests
- [x] All ten modules of content, with a content integrity test
- [x] Home, module, settings and about screens
- [x] Ten interactive simulators with hand-drawn charts
- [x] Bundled Archivo and IBM Plex Mono, static weights, no runtime download
- [x] Sixty mini-games in their own section, scored against the calculators — 12 tests
- [x] Points, the weekly board, and the eight-rung ladder with its rules — 25 tests
- [x] Online league on Firebase: anonymous sign-in, leagues of twenty assigned
      automatically, live board — with the security rules tested against the emulator
- [ ] Accessibility pass on a real device with TalkBack (layout at 200% text checked)
- [ ] App icon — currently a marked placeholder

Everything still to do, and every decision taken so far, is in
**[BACKLOG.md](BACKLOG.md)**.

## Install it

Every push builds a debug APK on CI. Open the **most recent green run** under
[Actions](../../actions) — not an older one — and download the `cashfluent-apk-<run>`
artifact at the bottom of the page. On your phone, open the `.apk` and allow installs
from that source when asked.

To check which build you are actually running, open **Settings** in the app: the line at
the bottom reads `Cashfluent 1.0.<run> (<commit>)`. It matches the run number in the
Actions URL, so there is never any doubt about whether an update took.

A newer build installs straight over an older one, progress intact: every build — on CI
or on a laptop — is signed with the same debug key, committed at `app/debug.keystore`.
That key signs debug builds only.

A tagged release (`v*`) also attaches the APK to a GitHub release, which is the link to
hand to someone without a GitHub account.

## Build

Requires Android Studio (Ladybug or newer) and JDK 17.

```bash
./gradlew assembleDebug     # build
./gradlew testDebugUnitTest # run the financial engine, games, league and content tests
```

Without the Android SDK you can still run the pure-Kotlin half — the ten calculators,
the sixty games, the whole league including its weekly settlement, and the curriculum —
on a plain JVM:

```bash
cd tools/verify && gradle test
```

The security rules are tested too, against the Firestore emulator rather than the real
project:

```bash
cd tools/rules && npm install && npm test
```

## What's inside

```
app/src/main/java/com/cashfluent/app/
├── content/         the ten lessons, as typed Kotlin rather than JSON
├── data/            DataStore repositories — progress, settings and the player
├── data/league/     the week, end to end: sign in, seat, score, settle. No Android
├── data/firebase/   the one implementation that talks to a server
├── di/              a service locator, no DI framework
├── domain/finance/  the calculators: pure Kotlin, no Android imports
├── domain/game/     the sixty mini-games, each built on one calculator
├── domain/league/   the board, the ladder, and the rules of the week
└── ui/              theme, navigation, screens, simulators, games, league
firestore.rules      what a phone may read and write, and nothing more
tools/               development only, ships nothing — see tools/README.md
```

The calculators carry no Android dependency on purpose, so the numbers on screen — in
the lessons and in the games — are covered by unit tests that run in about a second.

## What leaves the phone, and what doesn't

No account. No ads. No tracking. No analytics. **One permission, `INTERNET`, and one
feature uses it**: the league sends a random id, a nickname and two numbers, and reads
back the nineteen other rows on your board. That is the whole of it, and
[`firestore.rules`](firestore.rules) is where a reader can check rather than take our
word for it — a phone can write one document, its own, on a board whose week has not
ended, with every field bounded.

Everything else is local and stays local: the lessons, every answer you type into a
simulator, your progress and your settings. The app opens, teaches and scores with the
network off, because equity means the person with a cheap phone and unreliable wifi opens
the same app. Only the board needs a signal, and it says so when there isn't one.

Points are declared by the phone. The rules bound what can be claimed — a week's total
cannot exceed what a week of playing could produce — but a determined person could still
inflate a score. For a league of classmates that is the right trade; it is written here
rather than dressed up.

Material You dynamic colour is also off. Green means *what you keep* and clay means *what
it costs you* throughout the app; letting the system swap those hues would delete the
meaning along with them.

## A note on the numbers

Every figure in the worked examples is computed, not estimated, and pinned by a unit test.
Tax and contribution rates are **simplified and illustrative** — they show how the machine
works, not any particular country's rules, and the app says so on screen every time they
appear.

Educational content. Not financial advice.

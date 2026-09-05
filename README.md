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

## The game, the points, the league

Every lesson ends in a game: five rounds of that lesson's formula, on numbers the lesson
never showed. Set a number with a slider, or pick one of a few options; within 5% of the
answer is full marks. Every round ends with the calculation written out — the reveal is
the teaching, the score is what keeps you playing. The answers are computed by the same
calculators the worked examples are pinned to, so a game can never disagree with the
lesson that taught it.

Points add up per game (100 a round, 500 a game), and the best score on each lesson
earns a medal: bronze at 200, silver at 350, gold at 450.

A **league** is up to twenty people ranked on this week's points, and it has no server.
Your standing is a *card* — one line of text carrying a random id, your nickname, your
points and your medals. Send it to friends on any app you already use; they share it back
into Cashfluent, or paste it. The board starts again every Monday. Nothing about you is
stored anywhere but on your phone and in the message you chose to send, and the app still
has no `INTERNET` permission. It is an honour system, like a scoreboard on paper: the
checksum catches a mangled paste, it does not stop a friend typing themselves a bigger
number.

## Status

The app is complete and building. Ten lessons, ten simulators, ten games, a league, six
screens.

- [x] Gradle build, Android manifest, resources
- [x] Design system — semantic colour, type scale, light and dark
- [x] Local persistence (DataStore) for progress and settings
- [x] Financial engine — ten calculators, 82 unit tests
- [x] All ten modules of content, with a content integrity test
- [x] Home, module, settings and about screens
- [x] Ten interactive simulators with hand-drawn charts
- [x] Bundled Archivo and IBM Plex Mono, static weights, no runtime download
- [x] Ten games, one per lesson, scored against the calculators — 14 tests
- [x] Points, medals, and a league of up to twenty with no server — 12 tests
- [ ] Accessibility pass on a real device at 200% text size
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
./gradlew testDebugUnitTest # run the financial engine and content tests
```

Without the Android SDK you can still run the pure-Kotlin half — the ten calculators and
the games, the league cards and the whole curriculum, 122 tests — on a plain JVM:

```bash
cd tools/verify && gradle test
```

## What's inside

```
app/src/main/java/com/cashfluent/app/
├── content/         the ten lessons, as typed Kotlin rather than JSON
├── data/            DataStore repositories — progress, settings, points and league
├── di/              a thirteen-line service locator, no DI framework
├── domain/finance/  the calculators: pure Kotlin, no Android imports
├── domain/game/     the ten games, each built on one calculator
├── domain/league/   the league card, its text form, and the board
└── ui/              theme, navigation, screens, simulators, game, league
tools/               development only, ships nothing — see tools/README.md
```

The calculators carry no Android dependency on purpose, so the numbers on screen are
covered by unit tests that run in about a second.

## Deliberately absent

No account. No ads. No tracking. **No `INTERNET` permission** — there is no server to send
anything to, and the manifest is the proof. Everything works offline, which is the point:
equity means the person with a cheap phone and unreliable wifi opens the same app. The
league needs none of it either: a card is a message you send, not a request the app makes.

Material You dynamic colour is also off. Green means *what you keep* and clay means *what
it costs you* throughout the app; letting the system swap those hues would delete the
meaning along with them.

## A note on the numbers

Every figure in the worked examples is computed, not estimated, and pinned by a unit test.
Tax and contribution rates are **simplified and illustrative** — they show how the machine
works, not any particular country's rules, and the app says so on screen every time they
appear.

Educational content. Not financial advice.

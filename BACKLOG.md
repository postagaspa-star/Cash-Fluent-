# Backlog and decisions

Everything agreed but not yet built, everything still to do, and the decisions that are
settled so nobody spends the deadline re-arguing them.

`README.md` says what the app is. This file says what is left.

**Submission: 1 October 2026, 23:59 EDT.** Solo entry, GatewayHacks 2026, Equity in
Education.

Judged on **Social Impact 40% · Technical Execution 30% · Innovation 20% · Design & UX
10%**. When two pieces of work compete for the same evening, that ordering decides.

---

## 1. Open — waiting on the product owner

### The league goes online

Built on 5 September: a league made of *cards* — one line of text per person, sent
through any chat app and pasted back in — so the app could stay offline. The product
owner's answer, the same day: being offline is not an argument for social impact, it is a
block that stops the leaderboard being built properly, and the leaderboard is the
gamification. The developer agrees; the "no INTERNET" line was inherited from this file's
first version and repeated without being questioned. The lessons stay offline either way.

What an online league buys: weekly groups of twenty assigned automatically, promotion and
relegation that mean something, live updates, no sharing by hand. What it costs, and how
each cost is handled:

| Cost | Answer |
|---|---|
| Accounts and minors' data | Firebase **anonymous sign-in**: no email, no password, no sign-up screen. Stored: a random id, the nickname, points, rung. A one-paragraph privacy note. |
| Money | Firebase **Spark plan, €0, no card**. Free limits: 50,000 reads and 20,000 writes a day, 1 GB. Ample for hundreds of players. |
| A dependency during judging | Firestore on the free plan does not pause and does not need keeping alive. The lessons and games never depend on it. |
| Cheating | Points are declared by the phone; rules can only bound them. Acceptable for a hackathon, and said plainly. |

The Firebase CLI on the laptop is already logged in to the product owner's Google
account, so the project can be created and configured from there. **Waiting on one
thing: the OK to create a Firebase project on that account.** Then: `INTERNET`
permission, anonymous auth, Firestore with rules, leagues formed by tier and week in
groups of twenty, the board as a live query, and the cards retired.

### How the lessons are written

> "in seguito parleremo di come rendere le lezioni intrattenenti e coinvolgenti con un
> bel linguaggio"

Raised by the product owner, to be discussed later. This is a pass over the copy of all
ten lessons, not a structural change: the three blocks stay exactly as they are (§4).

The developer's position: the copy already reads well, and every number in it is pinned
by a test, so a blind rewrite risks more than it gains. Better to agree what
"entertaining" means first — one persona carried through all ten lessons, more humour,
shorter paragraphs, a running example — and then rewrite two lessons as a sample before
touching the rest.

## 2. Built on 5 September

**Games, as their own section.** `domain/game/games/`, sixty mini-games, six per topic,
reachable from Home, from the end of every lesson and from the league. A mini-game is a
minute long: four rounds, 100 points a round. Four mechanics — set a number with a
slider, pick one of a few options, higher or lower, true or false. Every answer is
computed by the calculator in `domain/finance/`, so a game can never disagree with the
lesson on the same topic, and every round ends with the calculation written out. This
replaced the first version's one five-round game per lesson, at the product owner's
request: games are not a tail on a lesson, they are a section, and there are dozens.

**Points.** Per game, for the week and for all time. A best score per mini-game.

**The ladder.** Eight rungs — Wood, Bronze, Silver, Gold, Ruby, Emerald, Diamond, Elite —
and the weekly rules in `Promotion`: top five go up, in a board of ten or more the
bottom five go down, nobody with zero points holds their place, nobody leaves Wood
downwards or Elite upwards. Monday's verdict is shown once at the top of the board.
This replaced per-lesson medals, at the product owner's request.

**The card league (interim).** Twenty people at most, ranked on this week's points, built
from cards pasted in. It carries the ladder and the zones already, so the online version
changes where the board comes from, not what it shows.

## 3. Still to do before submission

- [ ] **Online league** — see §1; blocked on the OK to create the Firebase project.
- [ ] **Accessibility pass with TalkBack**, by ear. Layout at 200% text was checked on a
      real phone on 5 September and holds; roles and selected states are in place.
- [ ] **App icon.** The current one is a marked placeholder. *Product owner's job, not
      the developer's — explicitly.*
- [x] **Make the repository public** — done 5 September. Keep it so: the install path in
      the README goes through Actions artifacts and GitHub releases, and neither is
      visible to a judge on a private repository.
- [ ] **Devpost page.**
- [ ] **Demo video, 5 minutes or under.** Lesson → game → board, in that order.
- [ ] **Refresh the Copy Deck artifact.** It still carries the six original lesson titles
      from before the retitling and the expansion to ten. It is currently wrong.
- [ ] **Re-render the artboards in `tools/design/`** for the screens that have changed
      since they were drawn. `Main` is current; the rest predate the ten-lesson list, and
      none of them know about the games or the league.

## 4. Settled — do not reopen

These were decided deliberately. They are listed so they survive a context reset.

**The three blocks, always in this order.** ① the idea, no numbers at all ② the
mechanism, the real formula with every symbol named ③ the real numbers, one worked
example with realistic figures for a 16-25 year old. This is the heart of the product.
Enforced by the type system and by `ContentIntegrityTest`.

**One level.** No beginner/intermediate/advanced. One beginner level that still carries
real formulas and real cases — not a simplified one.

**No filler.** Nothing gets added to look complete.

**Everything unlocked by default**, with an opt-in linear path in Settings. This is the
inverse of a "judging mode": the app does not hide anything and says why.

**Lessons, progress and settings are local and stay local**, whatever the league does.

**Games are a section, not a tail.** Dozens of mini-games on the lessons' topics, each a
minute long, each scored against the calculator behind the topic. No answer key is ever
typed by hand.

**The ladder is Duolingo's shape.** Wood to Elite in eight rungs, weekly, top five up,
bottom five down. Points are 100 a round.

**Currency is a symbol, never a conversion.** Content is written with `{c}` where a
symbol belongs; `Module.inCurrency()` replaces it once, at the screen boundary. Example
numbers never change with the currency, because converting them at an invented exchange
rate would make them wrong rather than local. Game rounds carry the same placeholder.

**2-3 check questions per lesson**, each with why the right answer is right *and* why the
others are wrong.

**One micro-action per lesson** — a free, concrete thing to do this week.

**Titles name the subject, hooks carry the reason.** "Good debt, bad debt" over "What
borrowing actually costs".

**Ten lessons**, expanded from the original six.

**Name: Cashfluent.** App language English; the working conversation is in Italian.

**Ask questions as plain text in chat.** The structured question widget misbehaves in
this project — the product owner asked for chat text instead.

---

## 5. Working notes

Things that have already cost time once.

**The Android SDK is unreachable from the dev container.** `dl.google.com` answers 403
through the egress proxy and `maven.google.com` does not resolve, so `assembleDebug`
cannot run there. **GitHub Actions is the compiler** from the container. Maven Central,
the Gradle plugin portal, Google Fonts and raw.githubusercontent.com *are* reachable.

**Building on the Windows laptop works.** JDK = the one Android Studio ships
(`C:\Program Files\Android\Android Studio\jbr`), and SDK platform 35 is installed.
`local.properties` must use forward slashes — `sdk.dir=C:/Users/.../Android/Sdk` —
because a Java properties file eats single backslashes and Gradle then fails with
"Invalid file path". `gradlew.bat testDebugUnitTest assembleDebug` takes about three
minutes. The exit code after a pipe is not the build's: look for `BUILD SUCCESSFUL`.

**The test phone locks itself.** Motorola Edge 50 Fusion, Android 16. adb works once USB
debugging is authorised, but the pattern lock comes back whenever the screen goes off,
and only the owner can clear it. Ask, then move fast.

**`tools/verify/` runs the pure-Kotlin half locally** — the calculators, the games, the
league and the whole curriculum, no Android SDK needed. Use it before every push; CI is
for the Compose half. See `tools/README.md`.

**Do not change the git remote in the container.** The repository was renamed to
`Cash-Fluent-`, but the container's remote must stay
`https://github.com/postagaspa-star/Prova-1-da-cambiare-`. GitHub redirects the old URL
so push and fetch work; changing it breaks credential injection there (`fatal: could not
read Username`). The laptop clone uses the new name directly.

**Every build says which build it is.** `versionCode` is the CI run number, Settings
shows `Cashfluent 1.0.<run> (<sha>)`, and the artifact is `cashfluent-apk-<run>`. When
"the APK looks unchanged" comes up, check that line first — the one time it came up it
was an old download, not a failed build.

**Every build is signed with the same key.** `app/debug.keystore` is committed on
purpose and wired up in `app/build.gradle.kts`. Before that, each CI run signed with a
fresh random debug key — runs 9 and 10 were checked and carried two different
certificates — so a newer APK refused to install over an older one until the app was
uninstalled, which also wiped the progress. The key signs debug builds only; never reuse
it for a release.

**A shared-in card is untrusted text.** It arrives through `ACTION_SEND`, is cut at
20,000 characters before it is read, and every field is bounded before it is believed —
see `LeagueCards.decode`. Nothing else enters the app from outside.

**Semantic colour, and it means something.** Green is what you keep, clay is what it
costs you, brass is where to look next. Material You dynamic colour is deliberately off:
a phone's wallpaper must not be able to recolour a chart whose colours carry meaning. The
ladder borrows from the palette where it can — bronze is the clay, gold the brass,
emerald the green — and adds two hues, ruby and diamond, that appear on tier badges and
nowhere else.

**Verify every number in Python before it becomes copy, then pin it as a test
expectation.** That is why the worked examples and the calculators cannot drift apart.

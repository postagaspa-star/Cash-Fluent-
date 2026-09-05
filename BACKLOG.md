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

### One click in the Firebase console

The league is built and the project is live, but Firebase Authentication has never been
switched on for `cashfluent-league`, and it cannot be switched on from a script: the REST
call that provisions it (`identityPlatform:initializeAuth`) answers
`BILLING_NOT_ENABLED`, because that endpoint belongs to the paid product. The free path
is a button in the console, and only the account owner can press it.

1. <https://console.firebase.google.com/project/cashfluent-league/authentication/providers>
2. **Get started** → **Anonymous** → enable → **Save**.

Until then the app behaves exactly as it does with no signal: lessons, games, points and
the ladder all work, and the league screen says there is no board yet. Nothing is lost,
and nothing has to be rebuilt afterwards — the next time the screen opens it signs in and
takes a seat.

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
lesson on the same topic, and every round ends with the calculation written out.

**Points.** Per game, for the week and for all time. A best score per mini-game.

**The ladder.** Eight rungs — Wood, Bronze, Silver, Gold, Ruby, Emerald, Diamond, Elite —
and the weekly rules in `Promotion`: top five go up, in a board of ten or more the
bottom five go down, nobody with zero points holds their place. Monday's verdict is
shown once at the top of the board.

**The league went online.** The first version built a league out of *cards* — one line of
text per person, sent through any chat app and pasted back in — so that the app could
keep its "no `INTERNET` permission" line. The product owner's answer, the same day: being
offline is not an argument for social impact, it is a block that stops the leaderboard
being built properly, and the leaderboard is the gamification. The developer agrees; the
line was inherited from this file's first version and repeated without being questioned.

What replaced it, on Firebase:

| | |
|---|---|
| Accounts | **Anonymous sign-in.** No email, no password, no sign-up screen. |
| Held on the server | A random id, the nickname, this week's points, points all time. |
| Money | **Spark plan, €0, no card.** 50,000 reads and 20,000 writes a day, 1 GB. |
| A dependency during judging | Firestore on the free plan does not pause. The lessons and the games never touch it. |
| Cheating | Points are declared by the phone. The rules bound them; the README says so plainly. |

Shaped so the network is never in the way: `LeagueService` owns the week, `LeagueBackend`
is the only thing that talks to a server, and every step that needs it can fail without
losing anything — sign in, read last week's verdict off its board, take this week's seat.
A failure stops at that step and is picked up the next time a screen opens. The rules in
`firestore.rules` are covered by ten emulator tests, including the one that would fail
silently: that the server counts weeks exactly as `Week.index` does on the phone.

## 3. Still to do before submission

- [ ] **Enable anonymous sign-in** — see §1. One click, product owner's account.
- [ ] **Two phones on one board**, seen with both in hand. The rules and the seating are
      tested; the round trip through the real project is not, because of §1.
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

**Lessons, progress, settings and every number typed into a simulator are local and stay
local.** The league is the one thing that leaves, and it carries a random id, a nickname
and two numbers.

**Games are a section, not a tail.** Dozens of mini-games on the lessons' topics, each a
minute long, each scored against the calculator behind the topic. No answer key is ever
typed by hand.

**The ladder is Duolingo's shape.** Wood to Elite in eight rungs, weekly, top five up,
bottom five down. Points are 100 a round.

**The league is online, and offline is not a feature.** Leagues of twenty are assigned by
the server, not shared by hand. Everything else works with no signal, and the league says
so when there is none.

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

**That JDK is also what the Firestore emulator needs**, and it is not on the path. In Git
Bash, `export PATH="/c/Program Files/Android/Android Studio/jbr/bin:$PATH"` — the
Windows-style `C:/...` form breaks `PATH`, because the colon is the separator.

**Firebase is wired up without `google-services.json`.** Three identifiers in
`app/build.gradle.kts` become `BuildConfig` fields and `ServiceLocator` builds
`FirebaseOptions` from them. They are not secrets — every Firebase app ships them — and
`firestore.rules` is what actually decides who may do what. It also means there is no
generated file to go stale and nothing extra for a judge to install.

**The test phone locks itself and drops off USB.** Motorola Edge 50 Fusion, Android 16.
adb works once USB debugging is authorised, but the pattern lock comes back whenever the
screen goes off and only the owner can clear it. Ask, then move fast.

**`tools/verify/` runs the pure-Kotlin half locally** — the calculators, the games, the
league including its weekly settlement, and the whole curriculum, no Android SDK needed.
`tools/rules/` runs the security rules against the emulator. Use both before every push;
CI is for the Compose half. See `tools/README.md`.

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

**A row read off a board is somebody else's text.** The rules bound it on the way in and
`FirestoreLeagueBackend` bounds it again on the way out — the name is cut to twenty
characters and the numbers are clamped — because a rule that is later loosened must not
be able to reach the screen.

**Semantic colour, and it means something.** Green is what you keep, clay is what it
costs you, brass is where to look next. Material You dynamic colour is deliberately off:
a phone's wallpaper must not be able to recolour a chart whose colours carry meaning. The
ladder borrows from the palette where it can — bronze is the clay, gold the brass,
emerald the green — and adds two hues, ruby and diamond, that appear on tier badges and
nowhere else.

**Verify every number in Python before it becomes copy, then pin it as a test
expectation.** That is why the worked examples and the calculators cannot drift apart.

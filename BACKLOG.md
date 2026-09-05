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

### How the lessons are written

> "in seguito parleremo di come rendere le lezioni intrattenenti e coinvolgenti con un
> bel linguaggio"

Raised by the product owner, not yet discussed. This is a pass over the copy of all ten
lessons, not a structural change: the three blocks stay exactly as they are (§4).

The developer's position, 5 September: the copy already reads well, and every number in
it is pinned by a test, so a blind rewrite risks more than it gains. Better to agree what
"entertaining" means first — one persona carried through all ten lessons, more humour,
shorter paragraphs, a running example — and then rewrite two lessons as a sample before
touching the rest.

## 2. Built on 5 September — the decisions of §2 as they stood, minus the server

**Mini-games — one per lesson, ten in total.** `domain/game/drill/`. A game is five
rounds of that lesson's own formula on numbers the lesson never showed, scored against
the calculator in `domain/finance/`, so a game can never disagree with the lesson that
taught it. Two shapes of round only: set a number with a slider, or pick one of a few
options. Every round ends with the calculation written out — that is the teaching; the
score is what keeps you playing. Within 5% of the answer is full marks.

**Points.** Up to 100 a round, 500 a game. Every game played adds to your total and to
this week's count; the best score per lesson earns a medal (bronze at 200, silver at
350, gold at 450). Held on the phone, not against an account.

**Leagues of up to 20 people, with no server.** This was the open question in the
previous version of this file (Firebase and accounts, or local-only). What was built is
neither: your standing is a *card* — one line of text carrying a random id, your
nickname, your points and your medals. You send it to friends through any app you
already use; they share it back into Cashfluent, or paste it. The board ranks everyone
on this week's points and starts again on Monday. The app still has no `INTERNET`
permission, no account and no privacy policy to write, and the strongest argument for
the 40% category survives intact.

Why this and not Firebase: an online league costs the no-data claim, a privacy policy,
the handling of minors' data before 1 October, a sign-up screen in front of a judge who
has five minutes, and a live dependency that can fail during judging. What it buys is
automatic updates — which a class group chat provides for free. The honest limit: a card
is an honour system, like a scoreboard on paper. The checksum catches a mangled paste; it
does not stop a friend typing themselves a bigger number.

**Account creation with terms and conditions.** Not built, and no longer needed: there is
nothing to create an account for.

## 3. Still to do before submission

- [ ] **Accessibility pass on a real device** — 200% text size, TalkBack end to end. The
      roles and selected states went in on 5 September; nothing has been tested by ear.
- [ ] **App icon.** The current one is a marked placeholder. *Product owner's job, not
      the developer's — explicitly.*
- [x] **Make the repository public** — done 5 September. Keep it so: the install path in
      the README goes through Actions artifacts and GitHub releases, and neither is
      visible to a judge on a private repository.
- [ ] **Devpost page.**
- [ ] **Demo video, 5 minutes or under.** Show a card going from one phone to another
      through a chat: it is the one thing a judge cannot try alone.
- [ ] **Refresh the Copy Deck artifact.** It still carries the six original lesson titles
      from before the retitling and the expansion to ten. It is currently wrong.
- [ ] **Re-render the artboards in `tools/design/`** for the screens that have changed
      since they were drawn. `Main` is current; the rest predate the ten-lesson list, and
      none of them know about the game or the league.

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

**Local only.** Progress, settings, points, medals and friends' cards in DataStore, on
the device, and nothing else anywhere.

**Leagues have no server.** A league is the cards on your phone. If that ever changes,
the privacy note in Settings, the About screen and the README all change with it.

**Games score against the calculators.** A round's answer is computed by the same code
the lesson's worked example was pinned to. No answer key is ever typed by hand.

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
minutes.

**`tools/verify/` runs the pure-Kotlin half locally** — the calculators, the games, the
league cards and the whole curriculum, no Android SDK needed. Use it before every push;
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

**A shared-in card is untrusted text.** It arrives through `ACTION_SEND`, is cut at
20,000 characters before it is read, and every field is bounded before it is believed —
see `LeagueCards.decode`. Nothing else enters the app from outside.

**Semantic colour, and it means something.** Green is what you keep, clay is what it
costs you, brass is where to look next. Material You dynamic colour is deliberately off:
a phone's wallpaper must not be able to recolour a chart whose colours carry meaning.
Medals borrow from the same palette: gold is brass, silver the quiet grey, bronze the clay.

**Verify every number in Python before it becomes copy, then pin it as a test
expectation.** That is why the worked examples and the calculators cannot drift apart.

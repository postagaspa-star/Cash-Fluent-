# Backlog and decisions

Everything agreed but not yet built, everything still to do, and the decisions that are
settled so nobody spends the deadline re-arguing them.

`README.md` says what the app is. This file says what is left.

**Submission: 1 October 2026, 23:59 EDT.** Solo entry, GatewayHacks 2026, Equity in
Education.

Judged on **Social Impact 40% · Technical Execution 30% · Innovation 20% · Design & UX
10%**. When two pieces of work compete for the same evening, that ordering decides.

---

## 1. Open — blocked on a product decision

### The leagues need a server, and that is a trade, not a detail

Leagues of up to 20 people are decided (§2). Real people in a real league means an
account, a backend and network access. Firebase's free tier covers the traffic, so cost
is not the obstacle. What it costs is the claim the app currently makes:

> No account. No server. **No `INTERNET` permission in the manifest.**

That claim is stated in Settings, in About and in the README, and it is the strongest
single argument available for the 40% category. Going online also brings a privacy
policy and the handling of minors' data into scope before 1 October.

Two ways forward:

| | What we build | What we give up |
|---|---|---|
| **A. Online** | Firebase (Firestore + anonymous auth), account with T&C, leagues of 20 real people | The no-data claim; needs a privacy policy before submission |
| **B. Offline** | Local points, medals per lesson, comparison against your own history — "340 this week, 210 last week" | Nobody else to measure yourself against, which was the reason for leagues |

Asked in chat on 4 September; **no answer yet.** The mini-games and the points system are
needed under either option, so they are not blocked by this — the leagues are.

### How the lessons are written

> "in seguito parleremo di come rendere le lezioni intrattenenti e coinvolgenti con un
> bel linguaggio"

Raised by the product owner, not yet discussed. This is a pass over the copy of all ten
lessons, not a structural change: the three blocks stay exactly as they are (§4).

---

## 2. Decided, not yet built

**Mini-games — one per lesson, ten in total.** Each one drills that lesson's actual
formula rather than testing recall of it. Every lesson already has a verified calculator
in `domain/finance/`, and the games should be scored against it so a game can never
disagree with the lesson that taught it. *Paused by the product owner on 4 September —
resume when told.*

**Points.** Earned per game played, held against the account.

**Leagues of up to 20 people**, Duolingo-style, instead of a worldwide or national
leaderboard. The product owner's reason, recorded because it is the design constraint:
points that buy nothing and rank you against nobody are worth nothing. A league of 20 is
small enough that a place in it is legible.

**Account creation with terms and conditions accepted at sign-up.** The product owner
judges this sufficient for GDPR and for minors. Only relevant under option A above.

---

## 3. Still to do before submission

- [ ] **Accessibility pass on a real device** — 200% text size, TalkBack end to end. The
      charts already carry real `contentDescription`s; nothing has been tested by ear.
- [ ] **App icon.** The current one is a marked placeholder. *Product owner's job, not
      the developer's — explicitly.*
- [ ] **Privacy policy** — only under option A.
- [ ] **Make the repository public before submitting** — or put the APK somewhere
      public. The install path in the README goes through Actions artifacts and GitHub
      releases, and neither is visible to a judge who cannot open a private repository.
- [ ] **Devpost page.**
- [ ] **Demo video, 5 minutes or under.**
- [ ] **Refresh the Copy Deck artifact.** It still carries the six original lesson titles
      from before the retitling and the expansion to ten. It is currently wrong.
- [ ] **Re-render the artboards in `tools/design/`** for the screens that have changed
      since they were drawn. `Main` is current; the rest predate the ten-lesson list.

---

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

**Local only.** Progress and settings in DataStore, on the device, and nothing else
anywhere. (Option A in §1 is the one thing that would change this.)

**Currency is a symbol, never a conversion.** Content is written with `{c}` where a
symbol belongs; `Module.inCurrency()` replaces it once, at the screen boundary. Example
numbers never change with the currency, because converting them at an invented exchange
rate would make them wrong rather than local.

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
cannot run locally. **GitHub Actions is the compiler.** Maven Central, the Gradle plugin
portal, Google Fonts and raw.githubusercontent.com *are* reachable.

**`tools/verify/` runs the pure-Kotlin half locally** — 94 tests, no Android SDK needed.
Use it before every push; CI is for the Compose half. See `tools/README.md`.

**Do not change the git remote.** The repository was renamed to `Cash-Fluent-`, but the
remote must stay `https://github.com/postagaspa-star/Prova-1-da-cambiare-`. GitHub
redirects the old URL so push and fetch work; changing it breaks credential injection in
this environment (`fatal: could not read Username`).

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

**Semantic colour, and it means something.** Green is what you keep, clay is what it
costs you, brass is where to look next. Material You dynamic colour is deliberately off:
a phone's wallpaper must not be able to recolour a chart whose colours carry meaning.

**Verify every number in Python before it becomes copy, then pin it as a test
expectation.** That is why the worked examples and the calculators cannot drift apart.

# Development tools

Neither of these ships in the APK. They exist because of one constraint: the machine
this project is usually worked on cannot reach `dl.google.com` or `maven.google.com`,
so the Android SDK cannot be downloaded and `./gradlew assembleDebug` cannot run
locally. **GitHub Actions is the compiler** — see `.github/workflows/android.yml`.

That leaves two gaps, and these two tools fill them.

## `verify/` — run the tests without the Android SDK

Most of the value in this app is in code that has no Android imports at all: the ten
financial calculators and the whole curriculum. That code can run on a plain JVM.

```
cd tools/verify && gradle test
```

It points `srcDir` at the real files in `app/src/`, so it cannot drift from what ships.
It needs Maven Central and a Gradle on the path; it does not need the Android SDK.
Anything importing `android.*` or Compose is out of its reach and is verified by CI.

## `rules/` — test what the server will and will not accept

`firestore.rules` is the only thing between a public app and twenty strangers' scores, so
it is tested rather than trusted. Ten cases run against the Firestore emulator: who may
read, who may write their own row, what a score is allowed to be, and — the one that
would fail silently — that the rules count the week with the same arithmetic as
`Week.index` on the phone.

```
cd tools/rules && npm install && npm test
```

It needs Node and a JDK on the path (the emulator is a Java process), and it never
touches the real project: the emulator runs under the id `demo-cashfluent`.

## `design/` — render the screens without installing anything

Waiting for CI, downloading an APK and reinstalling is a slow loop for a spacing
change. These artboards are HTML copies of the real screens, rendered at a true
360×800 phone viewport at 2× so they can be looked at directly.

```
cd tools/design && npm install && node shoot.js Main Mechanism Simulator
```

`CHROME` and `SHOTS_OUT` are environment variables if the defaults do not fit your
machine. Output lands in `tools/design/out/`.

**These are renders of the design, not screenshots of the build.** They are only worth
having while they agree with the Kotlin — when a screen changes, change the artboard in
the same commit or delete it. An artboard that has quietly drifted is worse than none,
because it is believed.

// Runs the app's pure-Kotlin packages on the JVM, because the Android SDK is not
// reachable from every machine this project gets worked on — see tools/README.md.
//
// It points at the real sources in the repo rather than copying them, so it cannot
// drift from what ships. Anything that imports android.* belongs in the app module
// and is verified by CI instead.
plugins { kotlin("jvm") version "2.1.0" }

repositories { mavenCentral() }

dependencies { testImplementation("junit:junit:4.13.2") }

val repo = rootDir.parentFile.parentFile

kotlin {
    sourceSets["main"].kotlin.srcDir(File(repo, "app/src/main/java/com/cashfluent/app/domain/finance"))
    sourceSets["main"].kotlin.srcDir(File(repo, "app/src/main/java/com/cashfluent/app/content"))
    sourceSets["test"].kotlin.srcDir(File(repo, "app/src/test/java/com/cashfluent/app/domain/finance"))
    sourceSets["test"].kotlin.srcDir(File(repo, "app/src/test/java/com/cashfluent/app/content"))
}

tasks.test {
    testLogging {
        events("passed", "failed", "skipped")
        showStandardStreams = false
    }
}

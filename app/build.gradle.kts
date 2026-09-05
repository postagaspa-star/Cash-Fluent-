import org.jetbrains.kotlin.gradle.dsl.JvmTarget

// CI passes the run number and the commit it built, so an installed APK can say
// exactly which build it is. Locally these fall back to a "dev" label.
val buildNumber = (project.findProperty("buildNumber") as String?)?.toIntOrNull() ?: 1
val buildLabel = (project.findProperty("buildLabel") as String?) ?: "dev"

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.cashfluent.app"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.cashfluent.app"
        // 24 keeps Cashfluent installable on older, cheaper phones. Equity is not only
        // about the price of the app.
        minSdk = 24
        targetSdk = 35
        versionCode = buildNumber
        versionName = "1.0.$buildNumber ($buildLabel)"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables { useSupportLibrary = true }
    }

    // Debug builds are signed with the key committed at app/debug.keystore, not with
    // whatever ~/.android/debug.keystore happens to exist on the machine. A fresh CI
    // runner generates a new random key on every run, and Android refuses to install an
    // APK over one signed with a different key — so each build from Actions demanded an
    // uninstall first, and took the progress with it. The key signs debug builds only;
    // the password is the public Android default.
    signingConfigs {
        getByName("debug") {
            storeFile = file("debug.keystore")
            storePassword = "android"
            keyAlias = "androiddebugkey"
            keyPassword = "android"
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        compose = true
        // so the app can show which build it is
        buildConfig = true
    }

    packaging { resources { excludes += "/META-INF/{AL2.0,LGPL2.1}" } }
}

kotlin {
    compilerOptions { jvmTarget.set(JvmTarget.JVM_17) }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.datastore.preferences)

    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    debugImplementation(libs.androidx.compose.ui.tooling)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
}

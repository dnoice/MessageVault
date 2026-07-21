/*
 * ✒ Metadata
 *     - Title: App Build Script (Message Vault Edition - v1.0)
 *     - File Name: build.gradle.kts
 *     - Relative Path: app/build.gradle.kts
 *     - Artifact Type: config
 *     - Version: 1.0.0
 *     - Date: 2026-06-22
 *     - Update: Monday, June 22, 2026
 *     - Author: Dennis 'dendogg' Smaltz
 *     - A.I. Acknowledgement: Anthropic - Claude Opus 4.8 (1M context)
 *     - Signature: ︻デ═─── ✦ ✦ ✦ | Aim Twice, Shoot Once!
 *
 * ✒ Changelog:
 *     - 1.0.0 (2026-06-17) [Anthropic - Claude Opus 4.8] — Initial scaffold + full-standard docstring.
 *
 * ✒ Description:
 *     The Gradle build recipe for the app module itself — the file you edit most.
 *     It applies the plugins, configures how the app is built (SDK levels, build
 *     types, Java/Kotlin settings), and lists every library the code uses. When
 *     the IDE says "Sync," it is mostly re-reading this file.
 *
 * ✒ Key Features:
 *     - plugins { }: applies the Android app, Kotlin, and Compose compiler plugins to this module (for real, no `apply false`).
 *     - android { }: namespace (R class package), compileSdk (newest API compiled against), minSdk (oldest install target), targetSdk (tested behavior), and versionCode/versionName.
 *     - buildTypes { }: debug is the sideload build (applicationIdSuffix ".debug" lets it coexist with release); release enables R8 minification for a shipped APK.
 *     - compileOptions / kotlinOptions: pin the Java/Kotlin language level to 21 to match the JDK runtime.
 *     - buildFeatures { compose = true }: switches on Jetpack Compose so @Composable code compiles.
 *     - dependencies { }: implementation(...) runtime+compile deps, the platform(compose-bom) Bill of Materials pinning one version for all Compose libraries, and debugImplementation(...) debug-only tooling.
 *
 * ✒ Other Important Information:
 *     - Dependencies: AGP (Android Gradle Plugin), Kotlin Gradle plugin, Compose compiler plugin; versions resolved via the libs version catalog. Pinned to a deliberate known-good island (AGP 8.7.3 era) — see UPGRADING.md before moving to AGP 9.x.
 *     - Compatible platforms: Android (minSdk 29, compileSdk 35), JVM 21.
 * ---------
 */
// Explicit import: inside a Gradle Kotlin DSL script `java` resolves to the Java plugin
// extension, which shadows the java.* package and makes java.util.Properties unresolvable.
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

// Release signing. keystore.properties is gitignored and carries the key's path and
// passwords, so no secret ever reaches the repository. If it is absent — a fresh clone,
// or a machine without the key — the release build stays unsigned instead of failing.
val keystorePropsFile = rootProject.file("keystore.properties")
val keystoreProps = Properties()
if (keystorePropsFile.exists()) {
    keystorePropsFile.inputStream().use { stream -> keystoreProps.load(stream) }
}

android {
    namespace = "com.digispace.messagevault"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.digispace.messagevault"
        minSdk = 29
        targetSdk = 35
        versionCode = 1
        versionName = "1.0.0"
    }

    signingConfigs {
        create("release") {
            if (keystoreProps.isNotEmpty()) {
                storeFile = file(keystoreProps.getProperty("storeFile"))
                storePassword = keystoreProps.getProperty("storePassword")
                keyAlias = keystoreProps.getProperty("keyAlias")
                keyPassword = keystoreProps.getProperty("keyPassword")
            }
        }
    }

    buildTypes {
        debug {
            // Sideload target — this is the build you run on the Fold 6.
            isMinifyEnabled = false
            applicationIdSuffix = ".debug"
        }
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            // Signed only when the local keystore is present; otherwise this produces
            // the unsigned APK that Android will refuse to install.
            if (keystoreProps.isNotEmpty()) {
                signingConfig = signingConfigs.getByName("release")
            }
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
    kotlinOptions { jvmTarget = "21" }
    buildFeatures { compose = true }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.vmcompose)
    implementation(libs.androidx.activity.compose)

    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.material.icons.extended)
    implementation(libs.androidx.navigation.compose)
    // Vault door: BiometricPrompt with device-credential (PIN/pattern) fallback.
    // Also supplies androidx.fragment, which BiometricPrompt requires of its host.
    implementation(libs.androidx.biometric)
    debugImplementation(libs.androidx.ui.tooling)

    implementation(libs.kotlinx.coroutines.android)
}

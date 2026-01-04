import com.android.build.gradle.internal.dsl.NdkOptions.DebugSymbolLevel.SYMBOL_TABLE
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.google.gms.google.services)
    alias(libs.plugins.google.firebase.crashlytics)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.google.android.gms.oss.licenses)
}

android {
    namespace = "com.eysamarin.squadplay"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    val properties = Properties()
    val localPropertiesFile = project.rootProject.file("local.properties")
    if (localPropertiesFile.exists()) {
        properties.load(localPropertiesFile.inputStream())
    }

    defaultConfig {
        applicationId = "com.eysamarin.squadplay"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = 9
        versionName = "0.8"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        val firebaseDbUrl = System.getenv("FIREBASE_DATABASE_URL") ?: properties.getProperty("FIREBASE_DATABASE_URL")
        val googleWebClientId = System.getenv("GOOGLE_WEB_CLIENT_ID") ?: properties.getProperty("GOOGLE_WEB_CLIENT_ID")

        buildConfigField(
            type = "String",
            name = "FIREBASE_DATABASE_URL",
            value = "\"$firebaseDbUrl\""
        )
        buildConfigField(
            "String",
            "GOOGLE_WEB_CLIENT_ID",
            "\"$googleWebClientId\""
        )
    }

    signingConfigs {
        create("release") {
            if (System.getenv("CI")?.toBoolean() == true) {
                // CI environment: read from environment variables (GitHub secrets)
                storeFile = rootProject.file("release.jks")
                storePassword = System.getenv("KEYSTORE_PASSWORD")
                keyAlias = System.getenv("KEY_ALIAS")
                keyPassword = System.getenv("KEY_PASSWORD")
            } else if (properties.getProperty("KEYSTORE_PATH") != null) {
                // Local environment: read from local.properties
                storeFile = file(properties.getProperty("KEYSTORE_PATH"))
                storePassword = properties.getProperty("KEYSTORE_PASSWORD")
                keyAlias = properties.getProperty("KEY_ALIAS")
                keyPassword = properties.getProperty("KEY_PASSWORD")
            }
        }
    }

    buildTypes {
        debug {
            isMinifyEnabled = false
        }
        release {
            signingConfig = signingConfigs.getByName("release")
            isMinifyEnabled = false
            ndk {
                debugSymbolLevel = SYMBOL_TABLE.toString()
            }
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }
}

kotlin {
    jvmToolchain(jdkVersion = 17)
}

dependencies {
    implementation(project(":models"))
    implementation(project(":domain"))
    implementation(project(":data"))
    implementation(project(":contract"))

    implementation(libs.io.insert.koin.compose)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)

    implementation(libs.io.coil.kt.coil3.compose)
    implementation(libs.io.coil.kt.coil3.network.okhttp)

    implementation(libs.androidx.credentials.credentials)
    implementation(libs.androidx.credentials.credentials.play.services.auth)
    implementation(libs.com.google.android.libraries.identity.googleid)

    implementation(libs.com.github.anhaki.pick.time.compose)

    implementation(platform(libs.com.google.firebase.bom))
    implementation(libs.com.google.firebase.crashlytics)
    implementation(libs.com.google.firebase.firestore)
    implementation(libs.com.google.firebase.auth)
    implementation(libs.com.google.firebase.messaging)

    implementation(libs.com.google.android.gms.oss.licenses)

    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.navigation.navigationCompose)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material3.windowSize)

    implementation(libs.org.jetbrains.kotlinx.serialization.json)

    testImplementation(libs.io.insert.koin.test.junit4)
    testImplementation(libs.junit)

    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)

    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}
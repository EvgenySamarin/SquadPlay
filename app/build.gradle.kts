import com.android.build.gradle.internal.dsl.NdkOptions.DebugSymbolLevel.SYMBOL_TABLE
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.google.gms.google.services)
    alias(libs.plugins.google.firebase.crashlytics)
}

android {
    namespace = "com.eysamarin.squadplay"
    compileSdk = 35

    val properties = Properties()
        .also { it.load(project.rootProject.file("local.properties").inputStream()) }

    defaultConfig {
        applicationId = "com.eysamarin.squadplay"
        minSdk = 28
        targetSdk = 35
        versionCode = 2
        versionName = "0.2"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        buildConfigField(
            type = "String",
            name = "FIREBASE_DATABASE_URL",
            value = "\"${properties.getProperty("FIREBASE_DATABASE_URL")}\""
        )
        buildConfigField(
            "String",
            "GOOGLE_WEB_CLIENT_ID",
            "\"${properties.getProperty("GOOGLE_WEB_CLIENT_ID")}\""
        )
    }

    signingConfigs {
        create("release") {
            storeFile = file(properties.getProperty("KEYSTORE_PATH"))
            storePassword = properties.getProperty("KEYSTORE_PASSWORD")
            keyAlias = properties.getProperty("KEY_ALIAS")
            keyPassword = properties.getProperty("KEY_PASSWORD")
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
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
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

    implementation(platform(libs.com.google.firebase.bom))
    implementation(libs.com.google.firebase.crashlytics)
    implementation(libs.com.google.firebase.firestore)
    implementation(libs.com.google.firebase.auth)

    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.navigation.navigationCompose)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material3.windowSize)

    testImplementation(libs.io.insert.koin.test.junit4)
    testImplementation(libs.junit)

    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)

    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}
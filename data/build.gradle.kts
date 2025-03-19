plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "com.eysamarin.squadplay.data"
    compileSdk = 35

    defaultConfig {
        minSdk = 28
        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
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
}

dependencies {
    implementation(project(":models"))
    implementation(project(":contract"))

    implementation(libs.com.google.android.libraries.identity.googleid)

    implementation(platform(libs.com.google.firebase.bom))
    implementation(libs.com.google.firebase.database)
    implementation(libs.com.google.firebase.firestore)
    implementation(libs.com.google.firebase.auth)
}
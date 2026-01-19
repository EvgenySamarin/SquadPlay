plugins {
    alias(libs.plugins.android.library)
}

android {
    namespace = "com.eysamarin.squadplay.data"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        minSdk = libs.versions.android.minSdk.get().toInt()
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
}

kotlin {
    jvmToolchain(jdkVersion = 17)
}

dependencies {
    implementation(project(":models"))
    implementation(project(":contract"))

    implementation(libs.com.google.android.libraries.identity.googleid)

    implementation(platform(libs.com.google.firebase.bom))
    implementation(libs.com.google.firebase.firestore)
    implementation(libs.com.google.firebase.auth)
    implementation(libs.com.google.firebase.messaging)
}
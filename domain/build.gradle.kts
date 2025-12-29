plugins {
    id("java-library")
    alias(libs.plugins.jetbrains.kotlin.jvm)
}

kotlin {
    jvmToolchain(17)
}

dependencies {
    implementation(project(":models"))
    implementation(project(":contract"))

    implementation(libs.org.jetbrains.kotlinx.coroutines.core)
}

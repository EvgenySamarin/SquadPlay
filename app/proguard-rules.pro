# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.

# Preserve source line numbers and file names for Crashlytics & Sentry de-obfuscation
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# Kotlinx Serialization
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.SerializationKt

-keepclassmembers class * {
    @kotlinx.serialization.SerialName <fields>;
}

-keepclasseswithmembers class * {
    @kotlinx.serialization.Serializable <fields>;
}

-keepclassmembers class * {
    *** Companion;
}

-keepclassmembers class * {
    *** $serializer;
}

# Preserve Serializable models and Navigation 3 route arguments
-keep class com.eysamarin.squadplay.models.** { *; }

# Firestore data entities (preserve properties and constructors for document mapping)
-keep class com.eysamarin.squadplay.data.entity.** { *; }
-keepclassmembers class com.eysamarin.squadplay.data.entity.** { *; }

# ViewModels accessed via reflection or Koin factory
-keep class * extends androidx.lifecycle.ViewModel {
    <init>(...);
}
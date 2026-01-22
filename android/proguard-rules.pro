# ProGuard rules for Lilith's Throne

# Keep all game classes
-keep class com.lilithsthrone.** { *; }
-keep class com.badlogic.gdx.** { *; }

# Keep game entry points
-keep public class com.lilithsthrone.main.GameActivity { *; }
-keep public class com.lilithsthrone.main.AndroidLauncher { *; }
-keep public class com.lilithsthrone.ui.LibGdxApp { *; }

# Keep game serialization
-keep class * implements java.io.Serializable { *; }

# Keep game events
-keepclassmembers class * {
    public void on*(***);
}

# Keep native methods
-keepclasseswithmembernames class * {
    native <methods>;
}

# Keep view constructors for view injection
-keepclasseswithmembers class * {
    public <init>(android.content.Context, android.util.AttributeSet);
}

# Keep game resources
-keep class **.R$* {
    public static <fields>;
}

# Keep LibGDX (minimal obfuscation)
-dontwarn com.badlogic.gdx.**
-keep class com.badlogic.gdx.** { *; }

# Keep Android support libraries
-dontwarn androidx.**
-keep class androidx.** { *; }

# Keep logging
-dontwarn org.slf4j.**
-keep class org.slf4j.** { *; }
-dontwarn ch.qos.logback.**
-keep class ch.qos.logback.** { *; }

# Preserve line numbers for crash reporting
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# Allow access to synthetic methods
-keepclassmembers class * {
    synthetic <methods>;
}

# Remove logging calls
-assumenosideeffects class android.util.Log {
    public static *** d(...);
    public static *** v(...);
    public static *** i(...);
}

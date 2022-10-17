# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.

# Keep Rule for Config classes
-keepattributes InnerClasses
-keep class org.edx.mobile.util.Config** {
    <fields>;
    <methods>;
}

# Keep Rule for Model classes
-keep class org.edx.mobile.model.** {*;}
-keep class org.edx.mobile.http.model.** {*;}
-keep class org.edx.mobile.module.registration.model.** {*;}

# Keep Rule for Webviews with JS
-keepattributes JavascriptInterface
-keepclassmembers class * {
    @android.webkit.JavascriptInterface <methods>;
}

# Keep Rules for Microsoft Auth
-keepclassmembers class com.microsoft.identity.** {
    <fields>;
    <methods>;
}

# Keep Rule to preserve the line number information for debugging stack traces
-keepattributes SourceFile, LineNumberTable

# Keep Rule to hide the original source file name on stack trace
-renamesourcefileattribute SourceFile

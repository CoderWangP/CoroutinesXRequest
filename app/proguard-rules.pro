# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile


# # -------------------------------------------
# #  ######## Retrofit混淆 ##########
# # -------------------------------------------
-dontnote retrofit2.Platform
-keepattributes Signature
-keepattributes Exceptions
-dontwarn retrofit2.**
-keep class retrofit2.** { *; }

# # -------------------------------------------
# #  ########OkHttp混淆##########
# # -------------------------------------------
-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn javax.annotation.**
# A resource is loaded with a relative path so the package of this class must be preserved.
-keepnames class okhttp3.internal.publicsuffix.PublicSuffixDatabase
-dontwarn okhttp3.logging.**
-keep class okhttp3.internal.**{*;}

# # -------------------------------------------
# #  ########Stetho混淆##########
# # -------------------------------------------
-keep class com.facebook.stetho.** { *; }
-dontwarn com.facebook.stetho.**

# # -------------------------------------------
# # gson
# # -------------------------------------------
-keep class com.google.gson.stream.** { *; }
-keepattributes EnclosingMethod
-keepattributes Signature
-keepattributes *Annotation*
-keepclassmembers enum * { *; }

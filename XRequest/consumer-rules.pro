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
# # gson
# # -------------------------------------------
-keep class com.google.gson.stream.** { *; }
-keepattributes EnclosingMethod
-keepattributes Signature
-keepattributes *Annotation*
-keepclassmembers enum * { *; }


-keep,allowobfuscation,allowshrinking class com.wp.xrequest.http.ApiResponse

# Keep generic signature of Call, Response (R8 full mode strips signatures from non-kept items).
-keep,allowobfuscation,allowshrinking interface retrofit2.Call
-keep,allowobfuscation,allowshrinking class retrofit2.Response

# With R8 full mode generic signatures are stripped for classes that are not
# kept. Suspend functions are wrapped in continuations where the type argument
# is used.
-keep,allowobfuscation,allowshrinking class kotlin.coroutines.Continuation

-dontwarn java.lang.invoke.StringConcatFactory
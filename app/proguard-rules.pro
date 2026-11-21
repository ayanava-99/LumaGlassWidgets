# Kotlinx Serialization
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt
-keepclassmembers class kotlinx.serialization.json.** {
    *** Companion;
}
-keepclasseswithmembers class kotlinx.serialization.json.** {
    kotlinx.serialization.KSerializer serializer(...);
}
-keep,includedescriptorclasses class com.lumaglass.widgets.data.**$$serializer { *; }
-keepclassmembers class com.lumaglass.widgets.data.** {
    *** Companion;
}

# Jetpack Glance
-keep class androidx.glance.** { *; }
-keep class com.lumaglass.widgets.glance.** { *; }

# Google Play Billing Library
-keep class com.android.billingclient.api.** { *; }

# Google Mobile Ads
-keep class com.google.android.gms.ads.** { *; }

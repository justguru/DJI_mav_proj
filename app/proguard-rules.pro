# Keep DJI SDK
-keep class dji.** { *; }
-keep interface dji.** { *; }
-dontwarn dji.**

# Keep Mapbox
-keep class com.mapbox.** { *; }
-dontwarn com.mapbox.**

# Apache POI
-dontwarn org.apache.poi.**
-dontwarn org.openxmlformats.**
-dontwarn org.etsi.uri.**
-dontwarn schemasMicrosoftComOfficeExcel.**
-dontwarn schemasMicrosoftComVml.**

# iText
-dontwarn com.itextpdf.**

# Kotlinx Serialization
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt
-keepclassmembers class kotlinx.serialization.json.** {
    *** Companion;
}
-keepclasseswithmembers class kotlinx.serialization.json.** {
    kotlinx.serialization.KSerializer serializer(...);
}

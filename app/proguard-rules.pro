# Add project specific ProGuard rules here.

# TensorFlow Lite / MediaPipe
-keep class org.tensorflow.** { *; }
-keep class com.google.mediapipe.** { *; }
-dontwarn org.tensorflow.**
-dontwarn com.google.mediapipe.**

# Hilt
-dontwarn dagger.hilt.**
-keep class dagger.hilt.** { *; }
-keep @dagger.hilt.android.HiltAndroidApp class * { *; }
-keep @dagger.hilt.android.AndroidEntryPoint class * { *; }

# Room
-keep class * extends androidx.room.RoomDatabase { *; }
-keep @androidx.room.Entity class * { *; }
-keep @androidx.room.Dao interface * { *; }
-dontwarn androidx.room.**

# Kotlin / Coroutines
-keep class kotlin.** { *; }
-keep class kotlinx.coroutines.** { *; }
-dontwarn kotlinx.coroutines.**

# Kotlin serialization
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt

# MongoDB Kotlin driver
-keep class com.mongodb.** { *; }
-keep class org.bson.** { *; }
-dontwarn com.mongodb.**
-dontwarn org.bson.**
-keepattributes Signature

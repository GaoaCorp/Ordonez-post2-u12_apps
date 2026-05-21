# Reglas de ProGuard / R8 para release build

# Mantener clases de modelo (Room, Retrofit)
-keep class com.gaaocorp.taskflow.domain.model.** { *; }
-keep class com.gaaocorp.taskflow.data.local.** { *; }
-keep class com.gaaocorp.taskflow.data.remote.dto.** { *; }

# Hilt / Dagger
-keep,allowobfuscation,allowshrinking class dagger.hilt.android.internal.managers.** { *; }
-keep,allowobfuscation,allowshrinking class * extends androidx.lifecycle.ViewModel
-keep,allowobfuscation,allowshrinking @dagger.hilt.android.lifecycle.HiltViewModel class *

# Room
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *

# Retrofit / OkHttp
-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn retrofit2.**
-keepattributes Signature, *Annotation*

# Compose
-keep class androidx.compose.runtime.** { *; }

# Coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}

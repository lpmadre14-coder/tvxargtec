# ProGuard rules for TVXargtec Online App

# Mantener clases de aplicación principal
-keep class com.tvxargtec.online.** { *; }
-keep class com.tvxargtec.online.activity.** { *; }
-keep class com.tvxargtec.online.fragment.** { *; }
-keep class com.tvxargtec.online.api.** { *; }
-keep class com.tvxargtec.online.models.** { *; }
-keep class com.tvxargtec.online.database.** { *; }
-keep class com.tvxargtec.online.repository.** { *; }
-keep class com.tvxargtec.online.utils.** { *; }
-keep class com.tvxargtec.online.manager.** { *; }
-keep class com.tvxargtec.online.adapter.** { *; }

# Mantener clases de Retrofit
-keep class retrofit2.** { *; }
-keep interface retrofit2.** { *; }
-keep class okhttp3.** { *; }
-keep interface okhttp3.** { *; }
-dontwarn okhttp3.**
-dontwarn okio.**

# Mantener clases de Gson
-keep class com.google.gson.** { *; }
-keep interface com.google.gson.** { *; }
-keep class * extends com.google.gson.TypeAdapter
-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer

# Mantener clases de Room
-keep class androidx.room.** { *; }
-keep interface androidx.room.** { *; }
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-keep @androidx.room.Dao interface *

# Mantener clases de Firebase
-keep class com.google.firebase.** { *; }
-keep interface com.google.firebase.** { *; }
-keep class com.google.android.gms.cast.** { *; }
-keep class com.google.android.gms.** { *; }
-keep interface com.google.android.gms.** { *; }

# Mantener clases de Glide
-keep class com.bumptech.glide.** { *; }
-keep interface com.bumptech.glide.** { *; }
-keep public enum com.bumptech.glide.load.ImageHeaderParser$ImageType { *; }
-keep class * implements com.bumptech.glide.module.GlideModule
-keep class * extends com.bumptech.glide.AppGlideModule

# Mantener clases de Media3/ExoPlayer
-keep class androidx.media3.** { *; }
-keep interface androidx.media3.** { *; }
-keep class com.google.android.exoplayer2.** { *; }
-keep interface com.google.android.exoplayer2.** { *; }

# Mantener clases de Stripe
-keep class com.stripe.** { *; }
-keep interface com.stripe.** { *; }

# Mantener métodos nativos
-keepclasseswithmembernames class * {
    native <methods>;
}

# Mantener constructores de View
-keepclasseswithmembers class * {
    public <init>(android.content.Context, android.util.AttributeSet);
}

# Mantener métodos de enumeraciones
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# Mantener métodos de Parcelable
-keep class * implements android.os.Parcelable {
    public static final android.os.Parcelable$Creator *;
}

# Mantener métodos de serialización
-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}

# Optimizaciones
-optimizationpasses 5
-dontusemixedcaseclassnames
-verbose

# Eliminar logs en producción
-assumenosideeffects class android.util.Log {
    public static *** d(...);
    public static *** v(...);
    public static *** i(...);
}

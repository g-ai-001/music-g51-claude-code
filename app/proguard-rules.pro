# Add project specific ProGuard rules here.
-keepattributes *Annotation*
-keep class androidx.room.** { *; }
-keep class app.music_g51_claude_code.data.entity.** { *; }
-dontwarn kotlinx.coroutines.**

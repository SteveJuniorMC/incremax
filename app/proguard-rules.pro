# Add project specific ProGuard rules here.

# Keep Hilt classes
-keepclassmembers,allowobfuscation class * {
    @com.google.dagger.hilt.android.lifecycle.HiltViewModel *;
}

# Keep Room entities
-keep class com.incremax.data.local.entity.** { *; }

# Add project specific ProGuard rules here.
-keep class com.kread.terminal.** { *; }
-keepclassmembers class * {
    native <methods>;
}

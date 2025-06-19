# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html


##############################################################################
#   WorkoutTimerPP – custom shrink rules
##############################################################################

# ────────────────────────────────────────────────────────────────────────────
# 1. -- Fix Gson TypeToken crash  (need generic signatures)
# ────────────────────────────────────────────────────────────────────────────
-keepattributes Signature

# Keep the TypeToken class hierarchy (generic info needed at runtime)
-keep class com.google.gson.reflect.TypeToken {
    <fields>;
    <methods>;
}

# Keep adapter annotations and Gson internals that rely on reflection
-keep @com.google.gson.annotations.SerializedName class *   { *; }
-keep class com.google.gson.TypeAdapterFactory
-keep class com.google.gson.internal.**                     { *; }

# ────────────────────────────────────────────────────────────────────────────
# 2. Keep ViewModels (instantiated by reflection in Compose)
# ────────────────────────────────────────────────────────────────────────────
-keep public class * extends androidx.lifecycle.ViewModel   { <init>(...); }

# ────────────────────────────────────────────────────────────────────────────
# 3. Keep Room entities / DAOs (referenced by generated code)
# ────────────────────────────────────────────────────────────────────────────
-keep @androidx.room.Dao class *                            { *; }
-keep @androidx.room.Entity class *                         { *; }
-keep @androidx.room.Database class *                       { *; }

# ────────────────────────────────────────────────────────────────────────────
# 4. Keep your Application & engine classes (TimerEngine via reflection)
# ────────────────────────────────────────────────────────────────────────────
-keep class xyz.negmawon.workouttimerpp.App                 { *; }
-keep class xyz.negmawon.workouttimerpp.engine.**           { *; }

# ────────────────────────────────────────────────────────────────────────────
# 5. Compose runtime & generated code (safe to keep; tiny impact on size)
# ────────────────────────────────────────────────────────────────────────────
-keep class androidx.compose.runtime.**                     { *; }
-keep class androidx.compose.ui.tooling.**                  { *; }

# ────────────────────────────────────────────────────────────────────────────
# 6. Strip android.util.Log calls (smaller APK; no impact on logic)
# ────────────────────────────────────────────────────────────────────────────
-assumenosideeffects class android.util.Log {
    public static int v(...);
    public static int d(...);
    public static int i(...);
    public static int w(...);
    public static int e(...);
}

##############################################################################
#  Gson TypeToken -- extra rule so anonymous subclasses survive
##############################################################################

# keep ANY  class that directly extends com.google.gson.reflect.TypeToken
# (including the anonymous ones the Kotlin compiler generates)
-keep class * extends com.google.gson.reflect.TypeToken

# keep the generic signatures + inner-class metadata
-keepattributes Signature,InnerClasses
##############################################################################
#  End of rules
##############################################################################

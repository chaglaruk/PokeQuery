# PokeQuery ProGuard / R8 rules (Package 6)
#
# PokeQuery uses kotlinx.serialization with @Serializable on data.model classes. The
# serialization compiler plugin generates its own serializer classes at compile time, so
# no runtime reflection is required; the rules below are conservative safety keeps only.

# Keep kotlinx.serialization generated serializers and their companion lookup.
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.**

# Keep the app's serializable data models (used by NavKey data classes and persistence).
-keep class com.caglar.pokequery.data.model.** { *; }
-keepclassmembers class com.caglar.pokequery.data.model.** {
    *** Companion;
}

# Keep NavKey data classes (Navig3 back-stack entries are @Serializable).
-keep class com.caglar.pokequery.** extends androidx.navigation3.runtime.NavKey { *; }
-keep @kotlinx.serialization.Serializable class com.caglar.pokequery.**

# Compose / Material3 runtime keeps are provided by their consumer rules already.

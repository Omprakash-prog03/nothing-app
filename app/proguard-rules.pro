# POKÉCHARGE proguard / R8 rules.
# Compose + standard AndroidX libraries ship consumer rules, so this file
# only needs project-specific exceptions.

# Keep data model classes that are reflectively inspected by nothing right
# now, but kept defensively since org.json parsing walks plain fields.
-keep class com.pokecharge.data.model.** { *; }
-keep class com.pokecharge.charging.model.** { *; }

# Keep AppWidgetProvider / Services / Receivers (referenced only from the
# manifest, so R8 can't see the usage otherwise).
-keep class com.pokecharge.widget.** { *; }
-keep class com.pokecharge.charging.receiver.** { *; }
-keep class com.pokecharge.charging.service.** { *; }

// Top-level build file. Plugin versions are declared here and applied
// with `apply false` so each module (currently just :app) can opt in.
plugins {
    id("com.android.application") version "8.5.2" apply false
    id("org.jetbrains.kotlin.android") version "1.9.24" apply false
}

tasks.register("clean", Delete::class) {
    delete(rootProject.layout.buildDirectory)
}

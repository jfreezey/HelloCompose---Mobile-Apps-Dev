plugins {
    id("com.android.application") version "9.3.3" apply false
    id("org.jetbrains.kotlin.android") version "2.1.10" apply false
    id("org.jetbrains.kotlin.plugin.compose") version "2.1.10" apply false
    id("com.google.devtools.ksp") version "2.1.10-1.0.30" apply false
    id("com.google.dagger.hilt.android") version "2.60.1" apply false
    // Firebase (Chapter 11): membaca app/google-services.json saat build
    id("com.google.gms.google-services") version "4.5.0" apply false
}

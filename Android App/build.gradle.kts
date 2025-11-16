// C:/Users/vakil/Documents/GitHub/Invernadero-tfg/Android App/build.gradle.kts
buildscript {
    dependencies {
        // Updated to a stable version matching what you have in libs.versions.toml
        classpath("com.android.tools.build:gradle:8.13.1")
        // Updated to a version compatible with AGP 8.13.1
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:2.0.21")
    }

repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

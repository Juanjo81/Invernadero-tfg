plugins {

    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose") version "2.0.21"

}

android {
    namespace = "com.example.invernaderomqtt"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.invernaderomqtt"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.3"
    }

    packaging {
        resources {
            excludes += "META-INF/INDEX.LIST"
            excludes += "META-INF/io.netty.versions.properties"


        }
    }
    dependencies {
        implementation(libs.androidx.core.ktx)
        implementation(libs.androidx.lifecycle.runtime.ktx)
        implementation(libs.androidx.activity.compose) // <--- ADD THIS LINE
        implementation(platform(libs.androidx.compose.bom))
        implementation(libs.androidx.compose.ui)
        implementation(libs.androidx.compose.ui.graphics)
        implementation(libs.androidx.compose.ui.tooling.preview)
        implementation(libs.androidx.compose.material3) // Or material if you are using Material 2
        testImplementation(libs.junit)
        androidTestImplementation(libs.androidx.junit)
        androidTestImplementation(libs.androidx.espresso.core)
        androidTestImplementation(platform(libs.androidx.compose.bom))
        androidTestImplementation(libs.androidx.compose.ui.test.junit4)
        debugImplementation(libs.androidx.compose.ui.tooling)
        debugImplementation(libs.androidx.compose.ui.test.manifest)
        implementation("androidx.navigation:navigation-compose:2.7.7")
        implementation("androidx.compose.ui:ui:1.5.3")
        implementation("androidx.compose.material:material:1.5.3") // ← Esto es clave para `Text`
        implementation("androidx.compose.ui:ui-tooling-preview:1.5.3")
        debugImplementation("androidx.compose.ui:ui-tooling:1.5.3")
        implementation("androidx.compose.material:material-icons-extended:1.5.3")
        implementation("com.hivemq:hivemq-mqtt-client:1.3.0")


    }
    }
dependencies {
    implementation(libs.androidx.ui)
    implementation(libs.androidx.compose.foundation)
}

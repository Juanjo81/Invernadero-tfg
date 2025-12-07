plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose") version "2.0.21"
    id("kotlin-kapt")
}


android {
    namespace = "com.example.invernaderomqtt"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.invernaderomqtt"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.1.1"
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
        kotlinCompilerExtensionVersion = "1.5.13" // ✅ Compatible con Kotlin 1.9.23
    }


    packaging {
        resources {
            excludes += "META-INF/INDEX.LIST"
            excludes += "META-INF/io.netty.versions.properties"
        }
    }
}

dependencies {
    implementation("io.coil-kt:coil-compose:2.4.0")
    implementation("androidx.compose.material:material-icons-extended:1.5.3")
    implementation("androidx.compose.material:material:1.5.3")

    // Room
    implementation("androidx.room:room-runtime:2.6.1")
    kapt("androidx.room:room-compiler:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")

    // Compose
    implementation("androidx.activity:activity-compose:1.8.0")
    implementation("androidx.compose.ui:ui:1.5.3")
    implementation("androidx.compose.material3:material3:1.1.2")
    implementation("androidx.compose.ui:ui-tooling-preview:1.5.3")
    debugImplementation("androidx.compose.ui:ui-tooling:1.5.3")


    // Navegación
    implementation("androidx.navigation:navigation-compose:2.7.7")

    // Lifecycle y ViewModel
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.6.2")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.6.2")
    implementation("androidx.activity:activity-compose:1.8.1")

    // Compatibilidad
    implementation("androidx.appcompat:appcompat:1.6.1")

    // Otros
    implementation("com.google.accompanist:accompanist-systemuicontroller:0.30.1")
    implementation("org.videolan.android:libvlc-all:3.5.1")
    implementation("com.hivemq:hivemq-mqtt-client:1.3.0")

    // Testing
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation("androidx.compose.ui:ui-test-junit4:1.5.3")
    debugImplementation("androidx.compose.ui:ui-test-manifest:1.5.3")
}


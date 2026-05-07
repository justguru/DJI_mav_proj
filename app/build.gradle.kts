plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.dagger.hilt.android")
    id("kotlin-kapt")
    id("org.jetbrains.kotlin.plugin.serialization")
}

android {
    namespace = "com.lossurvey.drone"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.lossurvey.drone"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0.0"

        // DJI App Key — replace with real key from DJI developer portal
        manifestPlaceholders["DJI_APP_KEY"] = "YOUR_DJI_APP_KEY_HERE"

        ndk {
            abiFilters += listOf("arm64-v8a", "armeabi-v7a")
        }
        vectorDrawables { useSupportLibrary = true }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        debug {
            isMinifyEnabled = false
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
        buildConfig = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.8"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
            excludes += "META-INF/rxjava.properties"
            excludes += "META-INF/DEPENDENCIES"
            excludes += "META-INF/INDEX.LIST"
            excludes += "META-INF/io.netty.versions.properties"
            pickFirsts += "lib/*/libc++_shared.so"
        }
    }
}

dependencies {
    // Compose BOM
    val composeBom = platform("androidx.compose:compose-bom:2024.02.00")
    implementation(composeBom)
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.activity:activity-compose:1.8.2")
    implementation("androidx.navigation:navigation-compose:2.7.6")

    // Material Motion
    implementation("androidx.compose.animation:animation")

    // Core
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.7.0")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.7.0")

    // Hilt DI
    implementation("com.google.dagger:hilt-android:2.50")
    kapt("com.google.dagger:hilt-android-compiler:2.50")
    implementation("androidx.hilt:hilt-navigation-compose:1.1.0")

    // Room
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    kapt("androidx.room:room-compiler:2.6.1")

    // DJI MSDK v5 (uncomment after configuring DJI maven repo / placing AAR)
    // implementation("com.dji:dji-sdk-v5-aircraft:5.9.0")
    // compileOnly("com.dji:dji-sdk-v5-aircraft-provided:5.9.0")
    // runtimeOnly("com.dji:dji-sdk-v5-networkImp:5.9.0")

    // Mapbox — requires MAPBOX_DOWNLOADS_TOKEN in ~/.gradle/gradle.properties.
    // Comment out until you've configured the Mapbox secret token; the
    // bundled MissionMapPreview Canvas works without it.
    // implementation("com.mapbox.maps:android:11.1.0")

    // Apache POI (Excel)
    implementation("org.apache.poi:poi:5.2.5")
    implementation("org.apache.poi:poi-ooxml:5.2.5")

    // OpenCSV
    implementation("com.opencsv:opencsv:5.9")

    // iText7 PDF
    implementation("com.itextpdf:itext7-core:7.2.5")

    // Coil Image Loading
    implementation("io.coil-kt:coil-compose:2.5.0")

    // Kotlinx Serialization
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.2")

    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")

    // Accompanist Permissions
    implementation("com.google.accompanist:accompanist-permissions:0.32.0")

    // DataStore
    implementation("androidx.datastore:datastore-preferences:1.0.0")

    // Debug
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}

kapt {
    correctErrorTypes = true
}

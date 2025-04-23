plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    alias(libs.plugins.ksp)
    id("com.google.dagger.hilt.android")
}

android {
    namespace = "com.technovix.quiznova"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.technovix.quiznova"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "com.technovix.quiznova.HiltTestRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        debug {
            // Debug build'lerde genellikle minify etmeyiz ki debug yapması kolay olsun.
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
        kotlinCompilerExtensionVersion = libs.versions.composeCompiler.get()
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
            excludes += "/META-INF/LICENSE.md"
            excludes += "/META-INF/LICENSE-notice.md"
            excludes += "/META-INF/LICENSE" // Bazen sadece LICENSE olabilir
            excludes += "/META-INF/NOTICE.md"
            excludes += "/META-INF/NOTICE"   // Bazen sadece NOTICE olabilir
            excludes += "/META-INF/notice.txt"
            excludes += "/META-INF/license.txt"
            excludes += "/META-INF/DEPENDENCIES"
            // Bazen modül bilgileri de çakışabilir (nadiren)
            excludes += "META-INF/versions/9/module-info.class"
            excludes += "module-info.class"
        }
    }
}

dependencies {

    // --- Temel Android & Lifecycle ---
    implementation(libs.androidx.core.ktx) // androidx.core:core-ktx zaten ekli, tekrar eklemeye gerek yok
    implementation(libs.androidx.lifecycle.runtime.ktx) // androidx.lifecycle:lifecycle-runtime-ktx zaten ekli
    implementation(libs.androidx.activity.compose) // androidx.activity:activity-compose zaten ekli

    // --- Compose ---
    implementation(platform(libs.androidx.compose.bom)) // BOM'u bir kere tanımlamak yeterli
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation("androidx.compose.material:material-icons-extended:1.6.7") // Versiyonu kontrol edin

    // --- Navigation ---
    implementation("androidx.navigation:navigation-compose:2.7.7") // Versiyonu kontrol edin

    // --- Dependency Injection (Hilt) ---
    implementation("com.google.dagger:hilt-android:2.51.1")
    implementation(libs.androidx.navigation.testing) // Versiyonu libs.toml ile eşleştirin veya güncelleyin
    ksp("com.google.dagger:hilt-compiler:2.51.1") // libs.hilt.compiler referansı daha iyi olurdu
    implementation("androidx.hilt:hilt-navigation-compose:1.2.0") // Versiyonu kontrol edin

    // --- Coroutines ---
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.0") // Versiyonu kontrol edin
    // implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3") // Genellikle -android olan bunu içerir, gerekirse ekli kalsın

    // --- Networking (Retrofit & OkHttp) ---
    implementation("com.squareup.retrofit2:retrofit:2.9.0") // Veya daha yeni versiyon
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.okhttp3:okhttp:4.12.0") // Veya daha yeni versiyon
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")

    // --- Database (Room) ---
    implementation("androidx.room:room-runtime:2.6.1") // Versiyonu libs.toml ile eşleştirin veya güncelleyin
    ksp("androidx.room:room-compiler:2.6.1") // libs.room.compiler referansı daha iyi olurdu
    implementation("androidx.room:room-ktx:2.6.1")

    // --- Lottie (Animations) ---
    implementation("com.airbnb.android:lottie-compose:6.4.0") // Versiyonu kontrol edin

    // --- Security (Opsiyonel) ---
    implementation("androidx.security:security-crypto:1.1.0-alpha06") // Veya daha stabil versiyon

    // --- DataStore ---
    implementation("androidx.datastore:datastore-preferences:1.1.1") // Versiyonu kontrol edin

    // --- Logging (Timber) ---
    implementation("com.jakewharton.timber:timber:5.0.1")

    //-----------------------------------------------------
    // --- Test Bağımlılıkları ---
    //-----------------------------------------------------

    // --- Birim Testleri (testImplementation) ---
    testImplementation(libs.junit)
    testImplementation(libs.mockk.android) // Mockk (Temel + Android context mockları)
    testImplementation(libs.mockk.agent.jvm) // Mockk Agent (final sınıflar için)
    testImplementation(libs.kotlinx.coroutines.test) // Coroutine Testleri
    testImplementation(libs.turbine) // Flow Testleri
    testImplementation(libs.truth) // Assertion (Opsiyonel)
    testImplementation(libs.androidx.arch.core.testing) // ViewModel Testleri (Opsiyonel)

    // --- Enstrümantasyon Testleri (androidTestImplementation) ---
    androidTestImplementation(libs.junit) // JUnit (Runner için gerekli)
    androidTestImplementation(libs.androidx.junit) // AndroidX JUnit uzantısı
    androidTestImplementation(libs.androidx.test.runner) // AndroidJUnitRunner
    androidTestImplementation(libs.androidx.espresso.core) // Espresso Temel
    androidTestImplementation(platform(libs.androidx.compose.bom)) // Compose BOM (Tekrar tanımlanması iyi pratik)
    androidTestImplementation(libs.androidx.compose.ui.test.junit4) // Compose UI Testleri
    androidTestImplementation(libs.mockk.android) // Mockk (Android Testleri için)
    androidTestImplementation(libs.kotlinx.coroutines.test) // Coroutine Testleri
    androidTestImplementation(libs.turbine) // Flow Testleri
    androidTestImplementation(libs.truth) // Assertion (Opsiyonel)
    androidTestImplementation(libs.androidx.arch.core.testing) // ViewModel Testleri (Opsiyonel)
    androidTestImplementation(libs.androidx.room.testing) // Room Testleri
    androidTestImplementation(libs.hilt.android.testing) // Hilt Android Testleri
    kspAndroidTest(libs.hilt.compiler) // Hilt Test Derleyicisi (KSP)

    // --- Compose Debug Yardımcıları (debugImplementation) ---
    debugImplementation(platform(libs.androidx.compose.bom)) // Tekrar tanımlanması iyi pratik
    debugImplementation(libs.androidx.ui.tooling) // Preview vb.
    debugImplementation(libs.androidx.ui.test.manifest) // Test Manifest



    // Test Hilt
    //testImplementation("com.google.dagger:hilt-android-testing:2.51")
    //kaptTest("com.google.dagger:hilt-compiler:2.51")
    //androidTestImplementation("com.google.dagger:hilt-android-testing:2.51")
    //kaptAndroidTest("com.google.dagger:hilt-compiler:2.51")
}
plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.jetbrainsKotlinAndroid)
    id("com.google.gms.google-services")
}

android {
    namespace = "com.fadlurahmanfdev.example"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.fadlurahmanfdev.example"
        minSdk = 21
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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

    aaptOptions {
        noCompress("tflite")
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    implementation(project(":feature_face_detection"))

    implementation("com.fadlurahmanfdev:kotlin_feature_camera:0.1.1")
    implementation("com.fadlurahmanfdev:pixmed:0.0.1")

    implementation(platform("com.google.firebase:firebase-bom:33.9.0"))

    implementation("org.tensorflow:tensorflow-lite:2.17.0")
    implementation("org.tensorflow:tensorflow-lite-select-tf-ops:2.16.1")
    implementation("org.tensorflow:tensorflow-lite-support:0.5.0")

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.9.0") // Coroutine core
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.9.0") // Coroutine Android support
}
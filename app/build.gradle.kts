import java.io.FileInputStream
import java.util.Properties

plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.jetbrainsKotlinAndroid)
    id("com.google.gms.google-services")
}

val securePropertiesFile = rootProject.file("secure.properties")
val secureProperties = Properties()
secureProperties.load(FileInputStream(securePropertiesFile))

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

    buildFeatures {
        buildConfig = true
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

    flavorDimensions.add("environment")

    productFlavors {
        create("pocvida"){
            dimension = "environment"
            applicationId = secureProperties["PACKAGE_NAME"] as String
            buildConfigField("String", "VIDA_API_KEY", "\"${secureProperties["API_KEY"] as String}\"")
            buildConfigField("String", "VIDA_LICENSE_KEY", "\"${secureProperties["LICENSE_KEY"] as String}\"")
            buildConfigField("String", "VIDA_ACTIVATION_KEY", "\"${secureProperties["ACTIVATION_KEY"] as String}\"")
            addManifestPlaceholders(mapOf(
                "vidaActivationKey" to  secureProperties["ACTIVATION_KEY"] as String
            ))
        }

        create("example"){
            dimension = "environment"
            applicationId = "com.fadlurahmanfdev.example"
            buildConfigField("String", "VIDA_API_KEY", "\"incorrect-flavor-apps\"")
            buildConfigField("String", "VIDA_LICENSE_KEY", "\"incorrect-flavor-apps\"")
            buildConfigField("String", "VIDA_ACTIVATION_KEY", "\"incorrect-flavor-apps\"")
            addManifestPlaceholders(mapOf(
                "vidaActivationKey" to  "fake"
            ))
        }
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
    implementation(project(":livefacex"))

    implementation("com.fadlurahmanfdev:kotlin_feature_camera:0.1.1")
    implementation("com.fadlurahmanfdev:pixmed:0.0.1")

    implementation(platform("com.google.firebase:firebase-bom:33.9.0"))

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.9.0") // Coroutine core
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.9.0") // Coroutine Android support

    add("pocvidaImplementation", "id.vida:liveness-sandbox:1.7.5")
    add("exampleImplementation", "id.vida:liveness-sandbox:1.7.5")

}
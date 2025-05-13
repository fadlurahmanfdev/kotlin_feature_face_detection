import com.vanniktech.maven.publish.SonatypeHost

plugins {
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.jetbrainsKotlinAndroid)
    id("maven-publish")

    id("com.vanniktech.maven.publish") version "0.29.0"
}


android {
    namespace = "com.fadlurahmanfdev.feature_face_detection"
    compileSdk = 34

    defaultConfig {
        minSdk = 21

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
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
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    api("com.google.mlkit:face-detection:16.1.6")
    val camerax_version = "1.3.3"
    implementation("androidx.camera:camera-core:${camerax_version}")

    api("org.tensorflow:tensorflow-lite:2.17.0")
    api("org.tensorflow:tensorflow-lite-select-tf-ops:2.16.1")
    api("org.tensorflow:tensorflow-lite-support:0.5.0")
}

mavenPublishing {
    publishToMavenCentral(SonatypeHost.CENTRAL_PORTAL)
    signAllPublications()

    coordinates("com.fadlurahmanfdev", "feature_face_detection", "0.0.1")

    pom {
        name.set("Kotlin Library Feature Face Detection")
        description.set("Android Library to simplified face detection (include liveness)")
        inceptionYear.set("2024")
        url.set("https://github.com/fadlurahmanfdev/kotlin_feature_face_detection/")
        licenses {
            license {
                name.set("The Apache License, Version 2.0")
                url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                distribution.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
            }
        }
        developers {
            developer {
                id.set("fadlurahmanfdev")
                name.set("Taufik Fadlurahman Fajari")
                url.set("https://github.com/fadlurahmanfdev/")
            }
        }
        scm {
            url.set("https://github.com/fadlurahmanfdev/kotlin_feature_face_detection/")
            connection.set("scm:git:git://github.com/fadlurahmanfdev/kotlin_feature_face_detection.git")
            developerConnection.set("scm:git:ssh://git@github.com/fadlurahmanfdev/kotlin_feature_face_detection.git")
        }
    }
}
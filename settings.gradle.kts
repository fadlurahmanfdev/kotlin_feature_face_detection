import java.io.FileInputStream
import java.util.Properties

val securePropertiesFile = file("secure.properties")
val secureProperties = Properties()
secureProperties.load(FileInputStream(securePropertiesFile))

pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://jitpack.io") }
        maven {
            url = uri("https://sdk-repo.dev.vida.id/android")
            credentials(HttpHeaderCredentials::class) {
                name = "x-api-key"
                value = secureProperties["X_API_KEY"] as String
            }
            authentication {
                create<HttpHeaderAuthentication>("header")
            }
        }
    }
}

rootProject.name = "Feature Face Detection"
include(":app")
include(":feature_face_detection")

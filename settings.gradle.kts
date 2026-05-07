pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.PREFER_SETTINGS)
    repositories {
        google()
        mavenCentral()
        // Mapbox: uncomment and set MAPBOX_DOWNLOADS_TOKEN in ~/.gradle/gradle.properties
        //   to fetch the v11 SDK once you wire it in.
        // maven {
        //     url = uri("https://api.mapbox.com/downloads/v2/releases/maven")
        //     authentication { create<BasicAuthentication>("basic") }
        //     credentials {
        //         username = "mapbox"
        //         password = providers.gradleProperty("MAPBOX_DOWNLOADS_TOKEN").orNull ?: ""
        //     }
        // }
    }
}

rootProject.name = "LOSSurvey"
include(":app")

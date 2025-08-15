// Root project name
rootProject.name = "ForeverRusher"

pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
    }
}

// ✅ Ensure the app module is included
include(":app")

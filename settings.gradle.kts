enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "MoinoGeo"

include(":core")
project(":core").projectDir = file("kotlin/core")

include(":flags")
project(":flags").projectDir = file("kotlin/flags")

include(":maps")
project(":maps").projectDir = file("kotlin/maps")

include(":app")
project(":app").projectDir = file("android/app")

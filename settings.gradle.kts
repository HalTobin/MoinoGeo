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

include(":flags-lipis")
project(":flags-lipis").projectDir = file("kotlin/flags-lipis")

include(":flags-flagpack")
project(":flags-flagpack").projectDir = file("kotlin/flags-flagpack")

include(":flags-circle")
project(":flags-circle").projectDir = file("kotlin/flags-circle")

include(":maps")
project(":maps").projectDir = file("kotlin/maps")

include(":shared")
project(":shared").projectDir = file("kotlin/demo/shared")

include(":android")
project(":android").projectDir = file("kotlin/demo/android")

include(":jvm")
project(":jvm").projectDir = file("kotlin/demo/jvm")

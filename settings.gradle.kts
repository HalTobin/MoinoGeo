rootProject.name = "MoinoGeo"
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

pluginManagement {
    repositories {
        google {
            mavenContent {
                includeGroupAndSubgroups("androidx")
                includeGroupAndSubgroups("com.android")
                includeGroupAndSubgroups("com.google")
            }
        }
        mavenCentral()
        gradlePluginPortal()
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
        maven("https://maven.pkg.jetbrains.space/kotlin/p/wasm/experimental")
        maven("https://androidx.dev/storage/compose-compiler/repository")
        maven("https://maven.pkg.jetbrains.space/kotlin/p/kotlin/dev")
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

dependencyResolutionManagement {
    repositories {
        google {
            mavenContent {
                includeGroupAndSubgroups("androidx")
                includeGroupAndSubgroups("com.android")
                includeGroupAndSubgroups("com.google")
            }
        }
        mavenCentral()
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
        maven("https://maven.pkg.jetbrains.space/kotlin/p/wasm/experimental")
        maven("https://androidx.dev/storage/compose-compiler/repository")
        maven("https://maven.pkg.jetbrains.space/kotlin/p/kotlin/dev")
    }
}

include(":core")
project(":core").projectDir = file("kotlin/core")

include(":flags")
project(":flags").projectDir = file("kotlin/flags")

include(":flags:core")
project(":flags:core").projectDir = file("kotlin/flags/core")

include(":flags:lipis-flag-icons-square")
project(":flags:lipis-flag-icons-square").projectDir = file("kotlin/flags/lipis-flag-icons-square")

include(":flags:lipis-flag-icons-rectangle")
project(":flags:lipis-flag-icons-rectangle").projectDir = file("kotlin/flags/lipis-flag-icons-rectangle")

include(":flags:yummygum-flagpack")
project(":flags:yummygum-flagpack").projectDir = file("kotlin/flags/yummygum-flagpack")

include(":flags:hatscripts-flags-circle")
project(":flags:hatscripts-flags-circle").projectDir = file("kotlin/flags/hatscripts-flags-circle")

include(":maps")
project(":maps").projectDir = file("kotlin/maps")

include(":maps:core")
project(":maps:core").projectDir = file("kotlin/maps/core")

include(":maps:borders")
project(":maps:borders").projectDir = file("kotlin/maps/borders")

include(":maps:topography")
project(":maps:topography").projectDir = file("kotlin/maps/topography")

include(":maps:plane")
project(":maps:plane").projectDir = file("kotlin/maps/plane")

include(":maps:sphere")
project(":maps:sphere").projectDir = file("kotlin/maps/sphere")

include(":shared")
project(":shared").projectDir = file("kotlin/demo/shared")

include(":android")
project(":android").projectDir = file("kotlin/demo/android")

include(":jvm")
project(":jvm").projectDir = file("kotlin/demo/jvm")

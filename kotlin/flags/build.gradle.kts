plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.library)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.compose.compiler)
}

kotlin {
    androidTarget {
        publishLibraryVariants("release", "debug")
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
        }
    }

    iosArm64()
    iosSimulatorArm64()

    sourceSets {
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.ui)
            implementation(libs.coil.core)
        }
        commonTest.dependencies {
            implementation(kotlin("test"))
        }
    }
}

android {
    namespace = "com.moinofactory.moinogeo.flags"
    compileSdk = 37
    defaultConfig {
        minSdk = 24
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    testOptions {
        unitTests {
            isIncludeAndroidResources = true
        }
    }
}

androidComponents {
    beforeVariants { variantBuilder ->
        try {
            variantBuilder.javaClass.getMethod("setEnableUnitTest", Boolean::class.javaPrimitiveType)
                .invoke(variantBuilder, true)
        } catch (_: Throwable) {}
    }
}

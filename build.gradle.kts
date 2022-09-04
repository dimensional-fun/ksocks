plugins {
    kotlin("multiplatform") version "1.7.10"
}

allprojects {
    group = "dimensional.ksocks"

    repositories {
        mavenCentral()

        maven(url = "https://maven.dimensional.fun/releases")
        maven(url = "https://jitpack.io")
    }
}

kotlin {
    explicitApi()

    jvm {
        compilations.all {
            kotlinOptions.jvmTarget = "11"
        }

        testRuns["test"].executionTask.configure {
            useJUnitPlatform()
        }
    }

    jvmToolchain {
        languageVersion.set(JavaLanguageVersion.of(11)) // "8"
    }

    linuxX64()


    sourceSets["commonMain"].dependencies {
        implementation(libs.bundles.common)

        implementation(libs.ktor.network)
    }

    sourceSets["jvmTest"].dependencies {
        implementation(libs.logback)
    }
}

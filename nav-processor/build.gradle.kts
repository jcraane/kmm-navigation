val kspVersion: String by project

plugins {
    kotlin("multiplatform")
    id("maven-publish")
}

group = "dev.jamiecraane"
version = "0.1-SNAPSHOT"

kotlin {
    jvm() {
        val main by compilations.getting {
            kotlinOptions {
                jvmTarget = "1.8"
            }
        }
    }
    sourceSets {
        val jvmMain by getting {
            dependencies {
                val kotlinpoetVersion = "1.12.0"
                implementation("com.google.devtools.ksp:symbol-processing-api:$kspVersion")
                implementation("com.squareup:kotlinpoet:$kotlinpoetVersion")
                implementation("com.squareup:kotlinpoet-metadata:$kotlinpoetVersion")
                implementation("com.squareup:kotlinpoet-ksp:$kotlinpoetVersion")

                implementation(project(":annotations"))
            }
            kotlin.srcDir("src/main/kotlin")
            resources.srcDir("src/main/resources")
        }
    }
}

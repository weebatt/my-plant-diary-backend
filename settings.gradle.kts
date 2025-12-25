dependencyResolutionManagement {
    // Use Maven Central as the default repository (where Gradle will download dependencies) in all subprojects.
    @Suppress("UnstableApiUsage")
    repositories {
        mavenCentral()
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}

// Monolith setup: keep only monolith app and shared contracts
include(":contracts")
include(":apps:monolith")
include(":apps:telegram-adapter")

rootProject.name = "my-plant-diary-backend"

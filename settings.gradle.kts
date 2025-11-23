// The settings file is the entry point of every Gradle build.
// Its primary purpose is to define the subprojects.
// It is also used for some aspects of project-wide configuration, like managing plugins, dependencies, etc.
// https://docs.gradle.org/current/userguide/settings_file_basics.html

dependencyResolutionManagement {
    // Use Maven Central as the default repository (where Gradle will download dependencies) in all subprojects.
    @Suppress("UnstableApiUsage")
    repositories {
        mavenCentral()
    }
}

plugins {
    // Use the Foojay Toolchains plugin to automatically download JDKs required by subprojects.
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}

// Include subprojects in the build.
// Learn more about structuring projects with Gradle - https://docs.gradle.org/8.7/userguide/multi_project_builds.html
// `app` module removed; API module provides the runnable entrypoint

// Contracts (shared DTO/events)
include(":contracts")

// Services (independent deployables)
include(":services:gateway")
include(":services:user-service")
include(":services:diary-service")
include(":services:dictionary-service")
include(":services:notification-orchestrator")
include(":services:scheduler")
include(":services:outbox-publisher")
include(":services:adapters:telegram-service")
include(":services:adapters:avito-service")
include(":services:adapters:webpush-service")

// Infrastructure (shared clients)
include(":infra:broker")
include(":infra:stores:postgres")
include(":infra:stores:graphdb")

rootProject.name = "my-plant-diary-backend"

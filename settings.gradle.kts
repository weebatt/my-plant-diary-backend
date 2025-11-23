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

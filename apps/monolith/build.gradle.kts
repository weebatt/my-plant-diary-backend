plugins {
    id("buildsrc.convention.kotlin-jvm")
    alias(libs.plugins.springBoot)
    alias(libs.plugins.springDependencyManagement)
    alias(libs.plugins.kotlinPluginSpring)
    alias(libs.plugins.kotlinPluginJpa)
    jacoco
}

dependencies {
    implementation(project(":contracts"))
    implementation(libs.springBootStarterWeb)
    implementation(libs.springBootStarterActuator)
    implementation(libs.springBootStarterAmqp)
    implementation(libs.bundles.kotlinxEcosystem)
    // Ensure Jackson Kotlin module can use Kotlin reflection for data class deserialization
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation(libs.jacksonModuleKotlin)
    implementation(libs.springDotenv)
    implementation(libs.micrometerRegistryPrometheus)
    // Security & OAuth2 Resource Server
    implementation(libs.springBootStarterSecurity)
    implementation(libs.springBootStarterOAuth2ResourceServer)
    // Persistence
    implementation(libs.springBootStarterDataJpa)
    implementation(libs.liquibaseCore)
    runtimeOnly(libs.postgresql)
    // OpenAPI
    implementation(libs.springdocOpenapiUi)
    // Validation
    implementation(libs.springBootStarterValidation)
    testImplementation(libs.springBootStarterTest)
    testImplementation(libs.testcontainersJunitJupiter)
    testImplementation(libs.testcontainersRabbitmq)
    testImplementation(libs.testcontainersPostgresql)
}

jacoco {
    toolVersion = "0.8.12"
}

tasks.test {
    useJUnitPlatform()
    finalizedBy(tasks.jacocoTestReport)
}

tasks.jacocoTestReport {
    dependsOn(tasks.test)
    reports {
        xml.required.set(true)
        html.required.set(true)
    }
}

tasks.register<JacocoCoverageVerification>("jacocoCoverageVerification") {
    dependsOn(tasks.jacocoTestReport)
    violationRules {
        rule {
            limit {
                minimum = "0.20".toBigDecimal()
            }
        }
    }
}

tasks.check {
    dependsOn("jacocoCoverageVerification")
}

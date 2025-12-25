plugins {
    id("buildsrc.convention.kotlin-jvm")
    alias(libs.plugins.springBoot)
    alias(libs.plugins.springDependencyManagement)
    alias(libs.plugins.kotlinPluginSpring)
    alias(libs.plugins.kotlinPluginJpa)
}

dependencies {
    implementation(project(":contracts"))
    implementation(libs.springBootStarterWeb)
    implementation(libs.springBootStarterActuator)
    implementation(libs.springBootStarterAmqp)
    implementation(libs.springBootStarterDataJpa)
    implementation(libs.liquibaseCore)
    runtimeOnly(libs.postgresql)
    implementation(libs.springBootStarterSecurity)
    implementation(libs.springBootStarterOAuth2ResourceServer)
    implementation(libs.springdocOpenapiUi)
    implementation(libs.micrometerRegistryPrometheus)
    implementation(libs.jacksonModuleKotlin)
    implementation(libs.springDotenv)
    implementation(libs.bundles.kotlinxEcosystem)
    testImplementation(libs.springBootStarterTest)
    testImplementation(libs.testcontainersJunitJupiter)
    testImplementation(libs.testcontainersPostgresql)
    testImplementation(libs.testcontainersRabbitmq)
}

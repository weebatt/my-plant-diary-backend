plugins {
    id("buildsrc.convention.kotlin-jvm")
    alias(libs.plugins.springBoot)
    alias(libs.plugins.springDependencyManagement)
    alias(libs.plugins.kotlinPluginSpring)
}

dependencies {
    implementation(project(":contracts"))
    implementation(project(":infra:broker"))
    implementation(project(":infra:stores:postgres"))
    implementation(libs.springBootStarterActuator)
    implementation(libs.springBootStarterWeb)
    implementation(libs.springBootStarterAmqp)
    implementation(libs.springBootStarterDataJpa)
    implementation(libs.liquibaseCore)
    implementation(libs.jacksonModuleKotlin)
    implementation(libs.springdocOpenapiUi)
    runtimeOnly(libs.postgresql)
    testImplementation(libs.springBootStarterTest)
}

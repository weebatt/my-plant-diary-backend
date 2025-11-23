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
    implementation(libs.springBootStarterWeb)
    implementation(libs.springBootStarterActuator)
    implementation(libs.springBootStarterDataJpa)
    implementation(libs.liquibaseCore)
    implementation(libs.jacksonModuleKotlin)
    runtimeOnly(libs.postgresql)
    testImplementation(libs.springBootStarterTest)
}

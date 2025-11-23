plugins {
    id("buildsrc.convention.kotlin-jvm")
    alias(libs.plugins.springBoot)
    alias(libs.plugins.springDependencyManagement)
    alias(libs.plugins.kotlinPluginSpring)
}

dependencies {
    implementation(project(":contracts"))
    implementation(project(":infra:broker"))
    implementation(libs.springBootStarterWeb)
    implementation(libs.springBootStarterActuator)
    implementation(libs.springBootStarterSecurity)
    implementation(libs.springBootStarterOAuth2ResourceServer)
    implementation(libs.jacksonModuleKotlin)
    implementation(libs.springdocOpenapiUi)
    testImplementation(libs.springBootStarterTest)
}

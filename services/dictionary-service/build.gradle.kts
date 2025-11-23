plugins {
    id("buildsrc.convention.kotlin-jvm")
    alias(libs.plugins.springBoot)
    alias(libs.plugins.springDependencyManagement)
    alias(libs.plugins.kotlinPluginSpring)
}

dependencies {
    implementation(project(":contracts"))
    implementation(project(":infra:stores:postgres"))
    implementation(project(":infra:stores:graphdb"))
    implementation(libs.springBootStarterWeb)
    implementation(libs.springBootStarterActuator)
    implementation(libs.springBootStarterDataJpa)
    implementation(libs.liquibaseCore)
    implementation(libs.jacksonModuleKotlin)
    implementation(libs.springBootStarterSecurity)
    implementation(libs.springBootStarterOAuth2ResourceServer)
    runtimeOnly(libs.postgresql)
    testImplementation(libs.springBootStarterTest)
    // RDF4J stack (in-memory by default, replace with remote when needed)
    implementation(libs.rdf4jRepositoryApi)
    implementation(libs.rdf4jRepositorySail)
    implementation(libs.rdf4jSailMemory)
    implementation(libs.rdf4jRioApi)
}

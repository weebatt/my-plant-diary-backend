plugins {
    id("buildsrc.convention.kotlin-jvm")
    application
}

dependencies {
    implementation(project(":contracts"))
    implementation(project(":infra:broker"))
}

application {
    mainClass = "com.myplantdiary.services.adapters.webpush.MainKt"
}


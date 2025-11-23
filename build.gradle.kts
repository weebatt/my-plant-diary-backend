plugins {
    alias(libs.plugins.detekt)
}

val detektVersion = libs.versions.detekt.get()

subprojects {
    pluginManager.apply("io.gitlab.arturbosch.detekt")

    detekt {
        config.from(rootProject.files("config/detekt/detekt.yml"))
        buildUponDefaultConfig = true
    }

    dependencies {
        add(
            "detektPlugins",
            "io.gitlab.arturbosch.detekt:detekt-formatting:$detektVersion"
        )
    }
}

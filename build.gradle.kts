plugins {
    `kotlin-dsl`
    kotlin("jvm") version "2.0.21"
    id("com.gradle.plugin-publish") version "2.0.0"
}

group = "io.github.prasad-babu"
version = "1.0"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
}

gradlePlugin {
    website = "https://github.com/prasad-babu/gradle-plugins"
    vcsUrl = "https://github.com/prasad-babu/gradle-plugins.git"
    plugins {
        register("powermockOpens") {
            id = "io.github.prasad-babu.powermock-opens"
            implementationClass = "com.dpb.gradle.plugin.PowermockOpensPlugin"
            displayName = "Powermock Opens"
            description = "Powermock Opens plugin to relax java restricted modules for powermock unit tests"
            tags = listOf("powermock", "opens", "java", "restricted")
        }
    }
}

kotlin {
    jvmToolchain(21)
}

tasks.test {
    useJUnitPlatform()
}

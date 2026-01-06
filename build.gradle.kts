plugins {
    `kotlin-dsl`
    kotlin("jvm") version "2.0.21"
    `java-gradle-plugin`
    id("com.vanniktech.maven.publish") version "0.35.0"
}

group = "io.github.prasad-babu.gradle.plugin"
version = "1.2"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
}

mavenPublishing {
    publishToMavenCentral()

    signAllPublications()
}

gradlePlugin {
    website = "https://github.com/prasad-babu/gradle-plugins"
    vcsUrl = "https://github.com/prasad-babu/gradle-plugins.git"
    plugins {
        register("powermockOpens") {
            id = "io.github.prasad-babu.powermock-opens"
            implementationClass = "io.github.prasad_babu.gradle.plugin.PowermockOpensPlugin"
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

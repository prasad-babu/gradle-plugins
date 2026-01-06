plugins {
    `kotlin-dsl`
    kotlin("jvm") version "2.0.21"
    id("com.gradle.plugin-publish") version "2.0.0"
    signing
}

group = "io.github.prasad-babu"
version = "1.1"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
}

publishing {
    repositories {
        maven {
            name = "MavenCentral"
            // Use this URL for the modern Sonatype Central Portal
            url = uri("https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/")

            credentials {
                username = System.getenv("MAVEN_USERNAME")
                password = System.getenv("MAVEN_PASSWORD")
            }
        }
    }

    publications.withType<MavenPublication> {
        pom {
            name.set("Powermock Opens")
            description.set("Plugin to relax java restricted modules for powermock unit tests")
            url.set("https://github.com/prasad-babu/gradle-plugins")
            licenses {
                license {
                    name.set("The Apache License, Version 2.0")
                    url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                }
            }
            developers {
                developer {
                    id.set("prasad-babu")
                    name.set("Prasad Babu")
                }
            }
            scm {
                connection.set("scm:git:git://github.com/prasad-babu/gradle-plugins.git")
                developerConnection.set("scm:git:ssh://github.com/prasad-babu/gradle-plugins.git")
                url.set("https://github.com/prasad-babu/gradle-plugins")
            }
        }
    }
}

signing {
    // This tells Gradle to use the GPG keys provided via environment variables
    val signingKey = System.getenv("GPG_SIGNING_KEY")
    val signingPassword = System.getenv("GPG_PASSPHRASE")
    useInMemoryPgpKeys(signingKey, signingPassword)
    sign(publishing.publications)
}

kotlin {
    jvmToolchain(21)
}

tasks.test {
    useJUnitPlatform()
}

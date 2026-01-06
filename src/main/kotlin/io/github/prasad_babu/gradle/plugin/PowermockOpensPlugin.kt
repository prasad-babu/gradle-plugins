package io.github.prasad_babu.gradle.plugin

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.tasks.testing.Test
import java.io.File
import java.util.jar.JarFile

class PowermockOpensPlugin : Plugin<Project> {

    override fun apply(project: Project) {
        val modules: List<String> = loadConfig()
        val javaHome = System.getenv("JAVA_HOME") ?: System.getProperty("java.home")
        val jmods = File(javaHome, "jmods")
        val args = LinkedHashSet<String>()
        for (module in modules) {
            val modFile = File(jmods, "$module.jmod")
            if (!modFile.exists()) continue

            JarFile(modFile).use { jar ->
                val entries = jar.entries()
                while (entries.hasMoreElements()) {
                    val entry = entries.nextElement()
                    val name = entry.name

                    // .jmod entries are prefixed with "classes/"
                    if (name.startsWith("classes/") && name.endsWith(".class")) {
                        val classPath = name.removePrefix("classes/")
                        val lastSlash = classPath.lastIndexOf('/')

                        if (lastSlash > 0) {
                            val pkg = classPath
                                .substring(0, lastSlash)
                                .replace('/', '.')

                            args += "--add-opens=$module/$pkg=ALL-UNNAMED"
                        }
                    }
                }
            }
        }
        val argFile = File(
            project.rootProject.projectDir,
            "gbuild/powermock-open-modules.argfile"
        )

        argFile.parentFile.mkdirs()
        argFile.writeText(args.joinToString("\n"))

        val powermockOpens = PowermockOpens(argFile)
        project.plugins.withType(JavaPlugin::class.java) {
            project.tasks.withType(Test::class.java).configureEach {
                jvmArgs(powermockOpens.getJvmArgs())
            }
        }
    }

    private fun loadConfig(): List<String> =
        PowermockOpensPlugin::class.java
            .getResourceAsStream("/powermock-open-modules.txt")
            ?.bufferedReader()
            ?.readLines()
            ?.map(String::trim)
            ?.filter { it.isNotEmpty() && !it.startsWith("#") }
            ?: emptyList()
}

class PowermockOpens(private val argFile: File) {

    fun getJvmArgs(): List<String> =
        listOf("@${argFile.absolutePath.replace('\\', '/')}")
}

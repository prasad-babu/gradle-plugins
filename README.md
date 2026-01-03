# Gradle Powermock Opens Plugin

A Gradle plugin that automatically configures JVM `--add-opens` arguments for PowerMock unit tests, resolving module access issues in Java 9+.

## Description

When running PowerMock tests on Java 9 and later, you often encounter `IllegalAccessError` exceptions due to the Java Platform Module System (JPMS) restrictions. This plugin automatically:

- Scans specified Java modules from your JDK installation
- Extracts all packages from those modules
- Generates `--add-opens` JVM arguments for each package
- Applies these arguments to all test tasks in your project

The plugin creates an argument file (`gbuild/powermock-open-modules.argfile`) containing all necessary `--add-opens` directives, eliminating the need to manually configure JVM arguments for PowerMock tests.

## Installation

### Using the Gradle Plugin Portal

Add the plugin to your `build.gradle.kts`:

```kotlin
plugins {
    id("com.dpb.plugins.powermock-opens") version "1.0"
}
```

Or in `build.gradle`:

```groovy
plugins {
    id 'com.dpb.plugins.powermock-opens' version '1.0'
}
```

### Manual Installation

If publishing locally, add to your `settings.gradle.kts`:

```kotlin
pluginManagement {
    repositories {
        mavenLocal()
        gradlePluginPortal()
    }
}
```

## Usage

Once applied, the plugin automatically configures all test tasks. No additional configuration is required.

```kotlin
plugins {
    java
    id("com.dpb.plugins.powermock-opens") version "1.0"
}
```

The plugin will:
1. Read the module list from its internal configuration (`powermock-open-modules.txt`)
2. Scan your `JAVA_HOME/jmods` directory for the specified modules
3. Generate `--add-opens` arguments for all packages in those modules
4. Apply these arguments to all `Test` tasks automatically

### Generated Output

The plugin creates a file at `gbuild/powermock-open-modules.argfile` containing lines like:

```
--add-opens=java.base/java.lang=ALL-UNNAMED
--add-opens=java.base/java.util=ALL-UNNAMED
--add-opens=java.base/java.io=ALL-UNNAMED
...
```

This file is automatically referenced by your test tasks via the `@` argument file syntax.

## Requirements

- Gradle 7.0+
- Java 21+ (with `JAVA_HOME` environment variable set)
- PowerMock for unit testing

## How It Works

1. The plugin reads a predefined list of Java modules to process
2. For each module, it opens the corresponding `.jmod` file from your JDK
3. Extracts all package names from the classes within each module
4. Generates `--add-opens=<module>/<package>=ALL-UNNAMED` arguments
5. Writes these to an argument file
6. Configures all test tasks to use this argument file via `jvmArgs`

## Building from Source

```bash
./gradlew build
```

## Publishing

```bash
./gradlew publishPlugins
```

## License

See LICENSE file for details.

## Repository

- **GitHub**: [https://github.com/prasad-babu/gradle-plugins](https://github.com/prasad-babu/gradle-plugins)

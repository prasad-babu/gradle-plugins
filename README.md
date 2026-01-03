# Gradle Powermock Opens Plugin

A Gradle plugin that automatically configures JVM `--add-opens` arguments for PowerMock unit tests, resolving module access issues in Java 9+.

## Description

When running PowerMock tests on Java 9 and later, you often encounter `IllegalAccessError` exceptions due to the Java Platform Module System (JPMS) restrictions. This plugin automatically:

- Scans specified Java modules from your JDK installation
- Extracts all packages from those modules
- Generates `--add-opens` JVM arguments for each package
- Applies these arguments to all test tasks in your project

The plugin creates an argument file (`gbuild/powermock-open-modules.argfile`) containing all necessary `--add-opens` directives, eliminating the need to manually configure JVM arguments for PowerMock tests.

### Why This Plugin?

This plugin is **especially valuable for legacy Java projects** with extensive PowerMock-based unit test suites. When migrating such projects to Java 9+ (particularly Java 21), you would typically need to:

- Manually identify which modules and packages PowerMock needs access to
- Add dozens or hundreds of `--add-opens` arguments to your test configuration
- Maintain this configuration as your codebase evolves

For large codebases with thousands of PowerMock tests, this manual process is error-prone and time-consuming. This plugin **automates the entire process**, allowing you to:

- Migrate legacy projects to modern Java versions without rewriting tests
- Avoid the massive effort of refactoring PowerMock tests to Mockito or other frameworks
- Keep your existing test suite running with minimal configuration changes
- Focus on business logic rather than JVM module system compatibility

## Installation

### Using Gradle Plugin Portal

Add the plugin to your `build.gradle.kts`:

```kotlin
plugins {
    id("io.github.prasad-babu.powermock-opens") version "1.0"
}
```

Or in `build.gradle`:

```groovy
plugins {
    id 'io.github.prasad-babu.powermock-opens' version '1.0'
}
```

The plugin will be automatically resolved from the [Gradle Plugin Portal](https://plugins.gradle.org/).

## Usage

Once applied, the plugin automatically configures all test tasks. No additional configuration is required.

```kotlin
plugins {
    java
    id("io.github.prasad-babu.powermock-opens") version "1.0"
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

## Publishing to Gradle Plugin Portal

To publish a new version of this plugin:

1. Set your Gradle Plugin Portal credentials in `~/.gradle/gradle.properties`:
   ```properties
   gradle.publish.key=YOUR_API_KEY
   gradle.publish.secret=YOUR_API_SECRET
   ```

2. Publish the plugin:
   ```bash
   ./gradlew publishPlugins
   ```

3. The plugin will be available on the [Gradle Plugin Portal](https://plugins.gradle.org/) within a few minutes.

## License

This project is licensed under the **Apache License 2.0** - see the [LICENSE](LICENSE) file for details.

### What This Means

The Apache License 2.0 is a permissive free software license that allows you to:

- ✅ **Use** the plugin freely for personal, commercial, or enterprise projects
- ✅ **Modify** the source code to suit your needs
- ✅ **Distribute** the plugin or modified versions
- ✅ **Sublicense** under compatible terms
- ✅ **Use in proprietary software** without restrictions

**No warranty** is provided, and contributors are not liable for any damages. See the LICENSE file for complete terms and conditions.

## Repository

- **GitHub**: [https://github.com/prasad-babu/gradle-plugins](https://github.com/prasad-babu/gradle-plugins)

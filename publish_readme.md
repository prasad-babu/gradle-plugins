# Publishing Gradle Plugins to Maven Central (Complete Guide)

This document explains **end-to-end** how this repository publishes a **Gradle plugin** to **Maven Central**, including:

- Maven Central / Sonatype setup
- GPG key generation and signing
- Uploading the public key to a public key server
- Storing secrets securely in GitHub
- GitHub Actions configuration
- Versioning and release workflow
- Common pitfalls and verification steps

This guide is written to be **fully reproducible**, with no missing steps.

---

## 1. Prerequisites

Before starting, ensure you have:

- A **GitHub account**
- A **Sonatype (Maven Central) account**
- A **published GitHub repository** (example: https://github.com/prasad-babu/gradle-plugins)
- Java **21+** installed locally
- Gradle **8+** (wrapper preferred)

---

## 2. Maven Central / Sonatype Setup

### 2.1 Create Sonatype Account

1. Go to: https://central.sonatype.com
2. Sign up / Sign in
3. Verify your email

---

### 2.2 Claim Your Namespace (Group ID)

Example group used in this repo:

```
io.github.prasad-babu
```

Steps:

1. Open **Namespaces** in Sonatype Central
2. Click **Create Namespace**
3. Choose:
   - Type: `GitHub`
   - Namespace: `io.github.prasad-babu`
4. Verify ownership (Sonatype will check the GitHub repo)

Once approved, you can publish artifacts under this group.

---

## 3. GPG Key Generation (Required for Maven Central)

Maven Central **requires all artifacts to be signed**.

You can generate GPG keys in **two supported ways**:

- **Option A (Recommended): Local GPG installation**
- **Option B (No local setup): Online key generation**

---

### Option A: Generate GPG Key Locally (Recommended)

Run locally:

```bash
gpg --full-generate-key
```

Choose:
- Type: `RSA and RSA`
- Key size: `4096`
- Expiry: `0` (no expiry) or as desired
- Name / Email: **must match your Sonatype account email**

---

### Option B: Generate GPG Keys Online (No Local Installation)

If you do **not** want to install GPG locally, you can use:

```
https://pgpkeygenerator.com/
```

This method is useful for:
- CI-first setups
- Quick key generation
- Systems without GPG installed

Steps:

1. Open https://pgpkeygenerator.com/
2. Choose:
   - Algorithm: **RSA**
   - Key size: **4096**
3. Enter:
   - Name
   - Email (**must match Sonatype email**)
   - Strong passphrase
4. Generate the key pair
5. Download:
   - **Private key (ASCII armored)**
   - **Public key (ASCII armored)**

⚠️ Security notes:
- Generate keys only on a **trusted machine**
- Never commit or share the private key
- Prefer local GPG for long-term or high-security projects

---


### 3.2 List Your Keys

```bash
gpg --list-secret-keys --keyid-format=long
```

Example output:

```
sec   rsa4096/ABCDEF1234567890
uid   Your Name <you@example.com>
```

The key ID here is:

```
ABCDEF1234567890
```

---

### 3.3 Export Private Key (ASCII-armored)

```bash
gpg --export-secret-keys --armor ABCDEF1234567890 > private.key
```

⚠️ **This file is extremely sensitive. Do NOT commit it.**

---

### 3.4 Export Public Key

```bash
gpg --export --armor ABCDEF1234567890 > public.key
```

---

## 4. Upload Public Key to a Key Server

Maven Central requires the **public key to be discoverable**.

Recommended server:

```
https://keys.openpgp.org
```

Steps:

1. Go to https://keys.openpgp.org
2. Upload `public.key`
3. Verify your email when prompted

Verification command:

```bash
gpg --keyserver keys.openpgp.org --search-keys you@example.com
```

---

## 5. Gradle Project Configuration

This repository uses:

- `java-gradle-plugin`
- `com.vanniktech.maven.publish`

### 5.1 Required Plugins

```kotlin
plugins {
    kotlin("jvm") version "2.0.21"
    `java-gradle-plugin`
    id("com.vanniktech.maven.publish") version "0.35.0"
}
```

⚠️ **Do NOT use `kotlin-dsl` for externally published plugins.**

---

### 5.2 Group and Version

Defined via `gradle.properties`:

```properties
GROUP=io.github.prasad-babu
VERSION_NAME=1.2.4
```

⚠️ Maven versions **must NOT** include a `v` prefix.

---

### 5.3 Gradle Plugin Definition

```kotlin
gradlePlugin {
    website = "https://github.com/prasad-babu/gradle-plugins"
    vcsUrl = "https://github.com/prasad-babu/gradle-plugins.git"

    plugins {
        create("powermockOpens") {
            id = "io.github.prasad-babu.powermock-opens"
            implementationClass =
                "io.github.prasad_babu.gradle.plugin.PowermockOpensPlugin"
            displayName = "Powermock Opens"
            description = "Opens Java modules for Powermock tests"
        }
    }
}
```

Gradle automatically generates **plugin marker artifacts**.

---

### 5.4 Maven Publishing Configuration

```kotlin
mavenPublishing {
    publishToMavenCentral()
    signAllPublications()
}
```

---

## 6. GitHub Secrets Configuration

All sensitive values must be stored as **GitHub Repository Secrets**.

Go to:

```
GitHub → Repository → Settings → Secrets and variables → Actions
```

Add the following secrets:

### 6.1 Required Secrets

| Secret Name | Description               |
|-----------|---------------------------|
| `MAVEN_CENTRAL_USERNAME` | Sonatype username token   |
| `MAVEN_CENTRAL_PASSWORD` | Sonatype token/password   |
| `GPG_PRIVATE_KEY` | Contents of `private.key` |
| `GPG_PASSPHRASE` | GPG key passphrase        |

Paste the **entire ASCII-armored private key** into `GPG_PRIVATE_KEY`.

---

## 7. GitHub Actions Workflow

### 7.1 Trigger Strategy

This project publishes **only on Git tags**:

```
v1.2.4
```

---

### 7.2 GitHub Action (`.github/workflows/release.yml`)

```yaml
name: Release

on:
  push:
    tags:
      - v*
  workflow_dispatch:

jobs:
  release:
    runs-on: ubuntu-latest
    permissions:
      contents: write

    steps:
      - uses: actions/checkout@v4

      - uses: actions/setup-java@v4
        with:
          distribution: temurin
          java-version: 21

      - name: Make gradlew executable
        run: chmod +x gradlew

      - name: Publish to Maven Central
        run: ./gradlew publishToMavenCentral
        env:
          ORG_GRADLE_PROJECT_signingInMemoryKey: ${{ secrets.GPG_PRIVATE_KEY }}
          ORG_GRADLE_PROJECT_signingInMemoryKeyPassword: ${{ secrets.GPG_PASSPHRASE }}
          ORG_GRADLE_PROJECT_mavenCentralUsername: ${{ secrets.MAVEN_CENTRAL_USERNAME }}
          ORG_GRADLE_PROJECT_mavenCentralPassword: ${{ secrets.MAVEN_CENTRAL_PASSWORD }}
```

---

## 8. Release Process (Canonical)

1. Update `VERSION_NAME` in `gradle.properties`
2. Commit changes
3. Create tag:

```bash
git tag v1.2.4
git push origin v1.2.4
```

4. GitHub Action runs automatically
5. Wait for Maven Central sync (5–30 minutes)

---

## 9. Verification Steps

### 9.1 Check Maven Central

```
https://repo1.maven.org/maven2/io/github/prasad-babu/
```

You should see:

- Plugin implementation artifact
- Plugin marker artifact:

```
io.github.prasad-babu.powermock-opens.gradle.plugin
```

---

### 9.2 Verify Plugin Consumption

In a consumer project:

**settings.gradle.kts**
```kotlin
pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
    }
}
```

**build.gradle.kts**
```kotlin
plugins {
    id("io.github.prasad-babu.powermock-opens") version "1.2.4"
}
```

---

## 10. Common Pitfalls (Read This)

| Issue | Cause |
|-----|------|
Plugin not found | Marker artifact missing |
Plugin not found | Version published as `v1.2.x` |
Invalid token | Wrong Sonatype credentials |
PGP key not found | Public key not uploaded |
Signature failure | Wrong passphrase |

---

## 11. Key Takeaways

- **Never prefix Maven versions with `v`**
- **Always publish plugin marker artifacts**
- **Public GPG key must be uploaded**
- **Secrets belong in GitHub Actions secrets**
- **Plugin resolution uses marker artifacts, not JARs**

---

## 12. References

- https://central.sonatype.com
- https://keys.openpgp.org
- https://github.com/vanniktech/gradle-maven-publish-plugin
- https://github.com/prasad-babu/gradle-plugins

---

plugins {
    kotlin("jvm") version "2.1.21"
    id("org.jetbrains.kotlin.plugin.compose") version "2.1.21"
    id("org.jetbrains.compose") version "1.8.1"
    `maven-publish`
}

import org.gradle.jvm.toolchain.JavaLanguageVersion
// use highest JVM target that Kotlin supports
java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(23))
    }
}
kotlin {
    jvmToolchain {
        languageVersion.set(JavaLanguageVersion.of(23))
    }
}
tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    kotlinOptions {
        // explicitly match the toolchain if you like
        jvmTarget = "23"
    }
}

group = "com.hoi4utils"
version = "0.1.0"

repositories {
    // look here first for anything you installed via “mvn install”
    mavenLocal()

    mavenCentral()
    google()            // for Compose artifacts
}

dependencies {
    implementation(compose.desktop.currentOs)
    // if you need to call back into your Scala core
    implementation("com.hoi4utils:HOIIVUtils:14.9.0")

    implementation(compose.material3)
}

compose.desktop {
    application {
        mainClass = "com.hoi4utils.compose.MainKt"
    }
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])
            // these default to project.group/project.name/project.version
            // but you can override artifactId here if you like:
            // artifactId = "compose-ui"
        }
    }
    repositories {
        mavenLocal()
    }
}

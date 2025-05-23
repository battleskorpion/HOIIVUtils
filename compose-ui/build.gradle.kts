plugins {
    kotlin("jvm") version "2.1.0"
    id("org.jetbrains.compose") version "1.8.1"
    `maven-publish`
}

group = "com.hoi4utils"
version = "0.1.0"

repositories {
    mavenCentral()
    google()            // for Compose artifacts
}

dependencies {
    implementation(compose.desktop.currentOs)
    // if you need to call back into your Scala core
    implementation("com.hoi4utils:core:<<your-core-version>>")
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

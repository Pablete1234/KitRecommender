plugins {
    `java-library`
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8

    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

repositories {
    mavenCentral()
    mavenLocal()
    maven("https://repo.pgm.fyi/snapshots")
    maven("https://jitpack.io")
}



dependencies {
    // Setting this to true allows the plugin to use data-collection mode, but also makes
    // the output jar significantly bigger (from ~80kb to ~6MB)
    val enableDataCollection = false

    if (enableDataCollection) {
        api("com.github.pablete1234:parquet-floor:java8-SNAPSHOT") {
            exclude("com.github.luben", "zstd-jni")
        }
    } else {
        compileOnly("com.github.pablete1234:parquet-floor:java8-SNAPSHOT")
    }
    compileOnly("org.jetbrains:annotations:22.0.0")
}

group = "me.pablete1234.kit"
version = "1.7.0"
description = "Plugin to automatically sort kits for the player"

tasks {
    withType<JavaCompile>() {
        options.encoding = "UTF-8"
    }
    withType<Javadoc>() {
        options.encoding = "UTF-8"
    }
}

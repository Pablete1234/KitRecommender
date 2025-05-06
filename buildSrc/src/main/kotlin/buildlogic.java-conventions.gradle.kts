plugins {
    `java-library`
    id("com.diffplug.spotless")
    id("de.skuzzle.restrictimports")
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

repositories {
    mavenCentral()
    mavenLocal()
    maven {
        url = uri("https://repo.pgm.fyi/snapshots")
    }

    maven {
        url = uri("https://s01.oss.sonatype.org/content/repositories/snapshots/")
    }

    maven {
        url = uri("https://jitpack.io")
    }
}

dependencies {
    compileOnly("com.github.pablete1234:parquet-floor:java8-SNAPSHOT")
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

spotless {
    ratchetFrom = "origin/master"
    java {
        removeUnusedImports()
        palantirJavaFormat("2.47.0").style("GOOGLE").formatJavadoc(true)
    }
}

restrictImports {
    group {
        reason = "Use org.jetbrains.annotations to add annotations"
        bannedImports = listOf("javax.annotation.**")
    }
    group {
        reason = "Use tc.oc.pgm.util.Assert to add assertions"
        bannedImports = listOf("com.google.common.base.Preconditions.**", "java.util.Objects.requireNonNull")
    }
}

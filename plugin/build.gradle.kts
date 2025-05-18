import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    id("buildlogic.java-conventions")
    id("com.gradleup.shadow")
}

dependencies {
    implementation(project(":KitUtil"))
    compileOnly(libs.app.ashcon.sportpaper)
    compileOnly(libs.tc.oc.pgm.core)
}

tasks.named<ShadowJar>("shadowJar") {
    archiveFileName = "KitRecommender.jar"
    archiveClassifier.set("")
    destinationDirectory = rootProject.projectDir.resolve("build/libs")

    minimize()
    dependencies {
        exclude(dependency("org.jetbrains:annotations"))
    }

    exclude("META-INF/**")
}

tasks {
    processResources {
        filesMatching(listOf("plugin.yml")) {
            expand(
                "name" to project.name,
                "description" to project.description,
                "mainClass" to "me.pablete1234.kit.recommender.KitRecommender",
                "version" to project.version,
                "commitHash" to project.latestCommitHash(),
                "url" to "https://github.com/Pablete1234/KitRecommender"
            )
        }
    }

    named("jar") {
        enabled = false
    }

    named("build") {
        dependsOn(shadowJar)
    }
}

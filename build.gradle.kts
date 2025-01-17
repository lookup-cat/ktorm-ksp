plugins {
    kotlin("jvm") apply false
}

buildscript {
    dependencies {
        val gradlePluginVersion: String by project
        classpath(kotlin("gradle-plugin", version = gradlePluginVersion))
    }
}

val fileVersion = file("ktorm-ksp.version").readText()

allprojects {
    group = "org.ktorm"
    version = fileVersion
}

subprojects {
    tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
        kotlinOptions.jvmTarget = "1.8"
        kotlinOptions.allWarningsAsErrors = true
        kotlinOptions.freeCompilerArgs += "-Xexplicit-api=strict"
        kotlinOptions.freeCompilerArgs += "-opt-in=kotlin.RequiresOptIn"
    }

    tasks.withType<JavaCompile> {
        options.encoding = "UTF-8"
    }

    tasks.withType<Test>().configureEach {
        maxParallelForks = Runtime.getRuntime().availableProcessors()
    }

    repositories {
        mavenCentral()
        jcenter()
    }

    configureDetekt()
    configureCopyrightCheck()
}

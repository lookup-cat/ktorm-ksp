plugins {
    kotlin("jvm")
}

dependencies {
    api(project(":ktorm-ksp-codegen"))
    api(project(":ktorm-ksp-compiler"))
    api(libs.junit)
    api(libs.assertj.core)
    api(libs.kotlinCompileTesting)
    api(libs.kotlinCompileTesting.ksp)
    api(libs.h2database)
}

for (majorVersion in 8..17) {
    // Adoptium JDK 9 cannot extract on Linux or Mac OS.
    if (majorVersion == 9) continue

    val jdkTest = tasks.register<Test>("testJdk$majorVersion") {
        val javaToolchains = project.extensions.getByType(JavaToolchainService::class)
        javaLauncher.set(javaToolchains.launcherFor {
            languageVersion.set(JavaLanguageVersion.of(majorVersion))
        })

        description = "Runs the test suite on JDK $majorVersion"
        group = LifecycleBasePlugin.VERIFICATION_GROUP

        // Copy inputs from normal Test task.
        val testTask = tasks.getByName<Test>("test")
        classpath = testTask.classpath
        testClassesDirs = testTask.testClassesDirs

    }
    tasks.named<Test>("test").configure {
        dependsOn(jdkTest)
    }
}

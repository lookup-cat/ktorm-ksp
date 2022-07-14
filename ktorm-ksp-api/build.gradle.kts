plugins {
    kotlin("jvm")
}

dependencies {
    api(kotlin("stdlib"))
    api(libs.ktorm.core)
    api(libs.cglib)
    testImplementation(libs.junit)
    testImplementation(libs.assertj.core)
}

configureMavenPublishing()

for (jdkVendor in arrayOf(
    JvmVendorSpec.ADOPTIUM,
    JvmVendorSpec.ADOPTOPENJDK,
    JvmVendorSpec.AMAZON,
    JvmVendorSpec.AZUL,
    JvmVendorSpec.BELLSOFT,
    JvmVendorSpec.HEWLETT_PACKARD,
    JvmVendorSpec.IBM,
    JvmVendorSpec.ORACLE,
    JvmVendorSpec.SAP,
)) {
    for (majorVersion in arrayOf(8, 11, 17)) {
        val jdkTest = tasks.register<Test>("testJdk $jdkVendor$majorVersion") {
            val javaToolchains = project.extensions.getByType(JavaToolchainService::class)
            javaLauncher.set(javaToolchains.launcherFor {
                languageVersion.set(JavaLanguageVersion.of(majorVersion))
                vendor.set(jdkVendor)
            })

            description = "Runs the test suite on JDK $jdkVendor$majorVersion"
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
}


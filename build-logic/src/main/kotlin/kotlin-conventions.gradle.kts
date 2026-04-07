import org.jetbrains.kotlin.gradle.dsl.JvmDefaultMode
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinVersion
import org.jetbrains.kotlin.gradle.dsl.abi.ExperimentalAbiValidation

plugins {
    id("org.jetbrains.kotlin.jvm")
}

private val libs = extensions.getByType<VersionCatalogsExtension>().named("baseLibs")

kotlin {
    val kotlinVersion = libs.getVersionOrFail("kotlin-language")
    val jvmTargetVersion = libs.getVersionOrFail("jvm-target")

    logger.debug("kotlinVersion: {}", kotlinVersion)
    logger.debug("jvmTargetVersion: {}", jvmTargetVersion)
    logger.debug("languageVersion: {}", JavaLanguageVersion.of(jvmTargetVersion))
    logger.debug("KotlinVersion: {}", KotlinVersion.fromVersion(kotlinVersion))

    jvmToolchain {
        languageVersion.set(JavaLanguageVersion.of(jvmTargetVersion))
    }

    explicitApi()

    compilerOptions {
        apiVersion.set(KotlinVersion.fromVersion(kotlinVersion))
        languageVersion.set(KotlinVersion.fromVersion(kotlinVersion))
        jvmTarget.set(JvmTarget.fromTarget(jvmTargetVersion))

        allWarningsAsErrors.set(true)
        progressiveMode.set(true)

        optIn.add("kotlin.RequiresOptIn")
        optIn.add("kotlin.time.ExperimentalTime")
        optIn.add("kotlinx.coroutines.ExperimentalCoroutinesApi")
        optIn.add("kotlinx.coroutines.FlowPreview")

        jvmDefault.set(JvmDefaultMode.ENABLE)

        freeCompilerArgs.addAll(
            listOf(
                "-Xlambdas=indy",
                "-Xjsr305=strict",
                "-Xannotation-default-target=param-property",
            ),
        )

        @OptIn(ExperimentalAbiValidation::class) abiValidation {
            enabled.set(true)
        }
    }
}

dependencies {
    implementation(platform(libs.libraryOrThrow("kotlin-bom")))
    implementation(platform(libs.libraryOrThrow("kotlinx-coroutines-bom")))
    implementation(libs.libraryOrThrow("kotlinx-coroutines-core"))
    compileOnly(libs.libraryOrThrow("jetbrains-annotations"))
}

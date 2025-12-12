import org.jetbrains.kotlin.gradle.dsl.JvmDefaultMode
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinVersion
import org.jetbrains.kotlin.gradle.dsl.abi.ExperimentalAbiValidation

// Apply the Kotlin JVM plugin
plugins {
    id("org.jetbrains.kotlin.jvm")
}

// Retrieve the version catalog for dependency management
private val libs = extensions.getByType<VersionCatalogsExtension>().named("baseLibs")

kotlin {
    // Retrieve Kotlin language version and JVM target version from the version catalog
    val kotlinVersion = libs.getVersionOrFail("kotlin-language")
    val jvmTargetVersion = libs.getVersionOrFail("jvm-target")

    // Log the retrieved versions for debugging purposes
    logger.debug("kotlinVersion: {}", kotlinVersion)
    logger.debug("jvmTargetVersion: {}", jvmTargetVersion)
    logger.debug("languageVersion: {}", JavaLanguageVersion.of(jvmTargetVersion))
    logger.debug("KotlinVersion: {}", KotlinVersion.fromVersion(kotlinVersion))

    // Configure the JVM toolchain with the specified Java language version
    jvmToolchain {
        languageVersion.set(JavaLanguageVersion.of(jvmTargetVersion))
    }

    // Enable explicit API mode to enforce API visibility modifiers
    explicitApi()

    compilerOptions {
        // Set the Kotlin API and language versions
        apiVersion.set(KotlinVersion.fromVersion(kotlinVersion))
        languageVersion.set(KotlinVersion.fromVersion(kotlinVersion))
        jvmTarget.set(JvmTarget.fromTarget(jvmTargetVersion))

        // Treat all warnings as errors
        allWarningsAsErrors.set(true)

        // Enable progressive mode for the compiler
        progressiveMode.set(true)

        // Opt-in to experimental Kotlin features
        optIn.add("kotlin.RequiresOptIn")
        optIn.add("kotlin.time.ExperimentalTime")

        // Opt-in to common coroutine experimental APIs
        optIn.add("kotlinx.coroutines.ExperimentalCoroutinesApi")
        optIn.add("kotlinx.coroutines.FlowPreview")

        // Enable default methods in Kotlin interfaces
        jvmDefault.set(JvmDefaultMode.ENABLE)

        // Add additional compiler arguments
        freeCompilerArgs.addAll(
            listOf(
                "-Xlambdas=indy", // Improve lambda performance on modern JVMs (requires Java 11+)
                "-Xjsr305=strict", // Enable strict nullability checks for Java interop
                "-Xannotation-default-target=param-property", // Set default annotation targets
            ),
        )

        // Enable ABI validation for the project
        @OptIn(ExperimentalAbiValidation::class) abiValidation {
            enabled.set(true)
        }
    }
}

dependencies {
    // Add Gradle API and Kotlin DSL as dependencies
    implementation(gradleApi())
    implementation(gradleKotlinDsl())

    // Add Kotlin Gradle plugin and BOM (Bill of Materials) as dependencies
    implementation(libs.libraryOrThrow("kotlin-gradle-plugin"))
    implementation(platform(libs.libraryOrThrow("kotlin-bom")))
    implementation(platform(libs.libraryOrThrow("kotlinx-coroutines-bom")))
    implementation(libs.libraryOrThrow("kotlinx-coroutines-core"))

    // Add JetBrains annotations as a compile-only dependency
    compileOnly(libs.libraryOrThrow("jetbrains-annotations"))
}

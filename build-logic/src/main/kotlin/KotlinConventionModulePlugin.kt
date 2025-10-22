import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalog
import org.gradle.jvm.toolchain.JavaLanguageVersion
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.jetbrains.kotlin.gradle.dsl.JvmDefaultMode
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinVersion

internal class KotlinConventionModulePlugin : Plugin<Project> {
    override fun apply(target: Project): Unit = with(target) {
        val libs = getLibs()

        val extension = extensions.create("kotlinConventions", KotlinConventionsExtension::class.java)
        sanitizeConfigure(extension, libs)

        pluginManager.apply("org.jetbrains.kotlin.jvm")

        afterEvaluate {
            configureKotlin(extension, libs)
            configureDependencies(libs)
        }

        afterEvaluate {
            println("==================================")
            println("Kotlin Conventions Applied:")
            println(" - JVM Target: ${extension.jvmTarget.get()}")
            println(" - JDK Toolchain: ${extension.jdkToolchain.get()}")
            println(" - Warnings as Errors: ${extension.warningsAsErrors.get()}")
            println(" - Progressive Mode: ${extension.progressive.get()}")
            println("==================================")
        }
    }
}

private fun Project.sanitizeConfigure(extension: KotlinConventionsExtension, libs: VersionCatalog) {
    libs.requiredVersion("kotlinLanguage")
    val jvmTargetVersion = libs.requiredVersion("jvmTarget")
    val jdkToolchainVersion = libs.optionalVersion("jdkToolchain") ?: jvmTargetVersion

    // Toolchain/targets
    extension.jvmTarget.convention(jvmTargetVersion)
    extension.jdkToolchain.convention(jdkToolchainVersion.toInt())

    // Language/compilation flags
    extension.warningsAsErrors.convention(
        providers.gradleProperty("warningsAsErrors").map(String::toBoolean)
            .orElse(providers.environmentVariable("CI").map { true }.orElse(false)),
    )
    extension.progressive.convention(
        providers.gradleProperty("kotlin.progressive").map(String::toBoolean).orElse(false),
    )

    // Dependency management
    extension.addBoms.convention(true)
    extension.useEnforcedPlatforms.convention(true)

    extension.additionalOptIns.convention(emptyList())
}

private fun Project.configureKotlin(extension: KotlinConventionsExtension, libs: VersionCatalog) {
    extensions.configure<KotlinJvmProjectExtension> {
        jvmToolchain {
            languageVersion.set(JavaLanguageVersion.of(extension.jdkToolchain.get()))
        }

        // OpenAPI generated code is not working with explicitApi()
        // explicitApi()
        explicitApiWarning()

        val kotlinLangVersion = libs.requiredVersion("kotlinLanguage")

        compilerOptions {
            apiVersion.set(KotlinVersion.fromVersion(kotlinLangVersion))
            languageVersion.set(KotlinVersion.fromVersion(kotlinLangVersion))
            jvmTarget.set(JvmTarget.fromTarget(extension.jvmTarget.get()))

            allWarningsAsErrors.set(extension.warningsAsErrors)
            progressiveMode.set(extension.progressive)

            optIn.add("kotlin.RequiresOptIn")
            optIn.addAll(extension.additionalOptIns)

            jvmDefault.set(JvmDefaultMode.ENABLE)
            freeCompilerArgs.add("-Xjsr305=strict")
        }
    }
}

private fun Project.configureDependencies(libs: VersionCatalog) {
    dependencies {
        add("implementation", platform(libs.libraryOrThrow("arrowkt-bom")))
        add("implementation", platform(libs.libraryOrThrow("kotlin-bom")))
        add("implementation", platform(libs.libraryOrThrow("kotlinx-coroutines-bom")))
        add("implementation", platform(libs.libraryOrThrow("reactor-bom")))

        add("implementation", libs.libraryOrThrow("reactor-kotlin-extensions"))
        add("implementation", libs.libraryOrThrow("kotlin-reflect"))
        add("implementation", libs.libraryOrThrow("kotlinx-coroutines-core"))
        add("implementation", libs.libraryOrThrow("kotlinx-coroutines-reactor"))
        add("implementation", libs.libraryOrThrow("arrowkt-core"))
        add("implementation", libs.libraryOrThrow("arrowkt-coroutines"))

        add("compileOnly", libs.libraryOrThrow("jetbrains-annotations"))

        add("testImplementation", libs.libraryOrThrow("reactor-test"))
        add("testImplementation", libs.libraryOrThrow("kotlin-test-junit5"))
        add("testImplementation", libs.libraryOrThrow("kotlinx-coroutines-test"))
        add("testRuntimeOnly", libs.libraryOrThrow("junit-platform-launcher"))
    }
}

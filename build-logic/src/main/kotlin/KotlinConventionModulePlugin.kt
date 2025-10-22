import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalog
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.jvm.toolchain.JavaLanguageVersion
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.jetbrains.kotlin.gradle.dsl.JvmDefaultMode
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinVersion
import javax.inject.Inject

internal abstract class KotlinConventionsExtension @Inject constructor(objects: ObjectFactory) {
    // Language and compilation flags
    val warningsAsErrors: Property<Boolean> = objects.property(Boolean::class.java)
    val progressive: Property<Boolean> = objects.property(Boolean::class.java)

    // Toolchain and target bytecode
    val jdkToolchain: Property<Int> = objects.property(Int::class.java)
    val jvmTarget: Property<String> = objects.property(String::class.java)

    // Dependency/BOM management
    val addBoms: Property<Boolean> = objects.property(Boolean::class.java)
    val useEnforcedPlatforms: Property<Boolean> = objects.property(Boolean::class.java)

    // Extra compiler opt-ins
    val additionalOptIns: ListProperty<String> = objects.listProperty(String::class.java)
}

internal class KotlinConventionModulePlugin : Plugin<Project> {
    override fun apply(target: Project): Unit = with(target) {
        val libs = getLibs()

        val extension = extensions.create("kotlinConventions", KotlinConventionsExtension::class.java)
        configureDefaults(extension, libs)

        pluginManager.apply("org.jetbrains.kotlin.jvm")

        pluginManager.withPlugin("org.jetbrains.kotlin.jvm") {
            configureKotlin(extension, libs)
            configureDependencies(extension, libs)
        }
    }
}

private fun Project.configureDefaults(extension: KotlinConventionsExtension, libs: VersionCatalog) {
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

//      OpenAPI generated code is not working with explicitApi()
//        explicitApi()
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

private fun Project.configureDependencies(extension: KotlinConventionsExtension, libs: VersionCatalog) {
    dependencies {
        // Enforce BOMs across common configurations if enabled
        if (extension.addBoms.get()) {
            val kotlinBom = libs.libraryOrThrow("kotlin-bom")
            val coroutinesBom = libs.libraryOrThrow("kotlinx-coroutines-bom")
            val arrowktBom = libs.libraryOrThrow("arrowkt-bom")

            val addBom: (String, Any) -> Unit = { conf, bom ->
                if (extension.useEnforcedPlatforms.get()) {
                    add(conf, enforcedPlatform(bom))
                } else {
                    add(conf, platform(bom))
                }
            }

            listOf("api", "implementation", "testImplementation").forEach { conf ->
                addBom(conf, kotlinBom)
                addBom(conf, coroutinesBom)
                addBom(conf, arrowktBom)
            }
        }

        add("implementation", libs.libraryOrThrow("kotlinx-coroutines-core"))
        add("implementation", libs.libraryOrThrow("arrowkt-core"))
        add("implementation", libs.libraryOrThrow("arrowkt-coroutines"))
        add("compileOnly", libs.libraryOrThrow("jetbrains-annotations"))
    }
}

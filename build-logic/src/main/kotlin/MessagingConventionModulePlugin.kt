import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalog
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.kotlin.dsl.dependencies
import javax.inject.Inject

public abstract class MessagingConventionExtension @Inject constructor(objects: ObjectFactory) {
    public val useKafka: Property<Boolean> = objects.property(Boolean::class.java)
    public val useTestcontainers: Property<Boolean> = objects.property(Boolean::class.java)
}

internal class MessagingConventionModulePlugin : Plugin<Project> {
    override fun apply(target: Project): Unit = with(target) {
        val libs = getLibs()

        val extension = extensions.create("messagingConventions", MessagingConventionExtension::class.java)

        afterEvaluate {
            configureDefaults(extension)

            if (extension.useKafka.get()) {
                configureKafka(extension, libs)
            }
        }
    }
}

private fun Project.configureDefaults(extension: MessagingConventionExtension) {
    extension.useKafka.convention(false)
    extension.useTestcontainers.convention(true)
}

private fun Project.configureKafka(extension: MessagingConventionExtension, libs: VersionCatalog) {
    dependencies {
        add("implementation", libs.libraryOrThrow("spring-kafka"))

        if (extension.useTestcontainers.get()) {
            // Align Testcontainers modules if BOM alias exists
            libs.findLibrary("testcontainers-bom").ifPresent { bom ->
                add("testImplementation", platform(bom.get()))
            }

            add("testImplementation", libs.libraryOrThrow("testcontainers-kafka"))

            libs.findLibrary("testcontainers-junit-jupiter").ifPresent {
                add("testImplementation", it.get())
            }
        }
    }
}

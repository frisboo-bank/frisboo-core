import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalog
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.kotlin.dsl.dependencies
import javax.inject.Inject

public abstract class MessagingConventionExtension @Inject constructor(private val objects: ObjectFactory) {
    private val _useKafka: Property<Boolean> = objects.property(Boolean::class.java).convention(false)
    private val _useTestcontainers: Property<Boolean> = objects.property(Boolean::class.java).convention(true)

    public var useKafka: Boolean
        get() = _useKafka.get()
        set(value) = _useKafka.set(value)

    public var useTestcontainers: Boolean
        get() = _useTestcontainers.get()
        set(value) = _useTestcontainers.set(value)
}

internal class MessagingConventionModulePlugin : Plugin<Project> {
    override fun apply(target: Project): Unit = with(target) {
        val libs = getLibs()

        val extension = extensions.create("messagingConventions", MessagingConventionExtension::class.java)

        afterEvaluate {
            if (extension.useKafka) {
                configureKafka(extension, libs)
            }
        }
    }
}

private fun Project.configureKafka(extension: MessagingConventionExtension, libs: VersionCatalog) {
    dependencies {
        add("implementation", libs.libraryOrThrow("spring-kafka"))

        if (extension.useTestcontainers) {
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

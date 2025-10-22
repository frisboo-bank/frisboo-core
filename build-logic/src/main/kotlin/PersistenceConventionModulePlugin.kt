import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalog
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.kotlin.dsl.dependencies
import javax.inject.Inject

public abstract class PersistenceConventionExtension @Inject constructor(private val objects: ObjectFactory) {
    private val _useMigration: Property<Boolean> = objects.property(Boolean::class.java).convention(false)
    private val _useMongo: Property<Boolean> = objects.property(Boolean::class.java).convention(false)
    private val _usePostgres: Property<Boolean> = objects.property(Boolean::class.java).convention(false)
    private val _useTestcontainers: Property<Boolean> = objects.property(Boolean::class.java).convention(true)

    public var useMigration: Boolean
        get() = _useMigration.get()
        set(value) = _useMigration.set(value)

    public var useMongo: Boolean
        get() = _useMongo.get()
        set(value) = _useMongo.set(value)

    public var usePostgres: Boolean
        get() = _usePostgres.get()
        set(value) = _usePostgres.set(value)

    public var useTestcontainers: Boolean
        get() = _useTestcontainers.get()
        set(value) = _useTestcontainers.set(value)
}

internal class PersistenceConventionModulePlugin : Plugin<Project> {
    override fun apply(target: Project): Unit = with(target) {
        val libs = getLibs()

        val extension = extensions.create("persistenceConventions", PersistenceConventionExtension::class.java)

        afterEvaluate {
            if (extension.useMongo) {
                configureMongo(extension, libs)
            }
            if (extension.usePostgres) {
                configurePostgres(extension, libs)
            }
        }
    }
}

private fun Project.configureMongo(extension: PersistenceConventionExtension, libs: VersionCatalog) {
    dependencies {
        add("implementation", libs.libraryOrThrow("spring-boot-starter-data-mongodb"))
        add("implementation", libs.libraryOrThrow("mongodb"))

        if (extension.useTestcontainers) {
            // Align Testcontainers modules if BOM alias exists
            libs.findLibrary("testcontainers-bom").ifPresent { bom ->
                add("testImplementation", platform(bom.get()))
            }

            add("testImplementation", libs.libraryOrThrow("testcontainers-mongodb"))

            libs.findLibrary("testcontainers-junit-jupiter").ifPresent {
                add("testImplementation", it.get())
            }
        }    }
}

private fun Project.configurePostgres(extension: PersistenceConventionExtension, libs: VersionCatalog) {
    dependencies {
        add("implementation", libs.libraryOrThrow("spring-boot-starter-data-jpa"))
        add("runtimeOnly", libs.libraryOrThrow("postgresql"))

        if (extension.useMigration) {
            add("implementation", libs.libraryOrThrow("flyway-core"))
            add("implementation", libs.libraryOrThrow("flyway-postgresql"))
        }

        if (extension.useTestcontainers) {
            // Align Testcontainers modules if BOM alias exists
            libs.findLibrary("testcontainers-bom").ifPresent { bom ->
                add("testImplementation", platform(bom.get()))
            }

            add("testImplementation", libs.libraryOrThrow("testcontainers-postgresql"))

            libs.findLibrary("testcontainers-junit-jupiter").ifPresent {
                add("testImplementation", it.get())
            }
        }
    }
}

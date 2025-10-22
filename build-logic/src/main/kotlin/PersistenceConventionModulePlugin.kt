import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalog
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.kotlin.dsl.dependencies
import javax.inject.Inject

public abstract class PersistenceConventionExtension @Inject constructor(objects: ObjectFactory) {
    public val useMigration: Property<Boolean> = objects.property(Boolean::class.java)
    public val useMongo: Property<Boolean> = objects.property(Boolean::class.java)
    public val usePostgres: Property<Boolean> = objects.property(Boolean::class.java)
    public val useTestcontainers: Property<Boolean> = objects.property(Boolean::class.java)
}

internal class PersistenceConventionModulePlugin : Plugin<Project> {
    override fun apply(target: Project): Unit = with(target) {
        val libs = getLibs()

        val extension = extensions.create("persistenceConventions", PersistenceConventionExtension::class.java)
        configureDefaults(extension)

        afterEvaluate {
            if (extension.useMongo.get()) {
                configureMongo(extension, libs)
            }
            if (extension.usePostgres.get()) {
                configurePostgres(extension, libs)
            }
        }
    }
}

private fun Project.configureDefaults(extension: PersistenceConventionExtension) {
    extension.useMigration.convention(false)
    extension.useMongo.convention(false)
    extension.usePostgres.convention(false)
    extension.useTestcontainers.convention(true)
}

private fun Project.configureMongo(extension: PersistenceConventionExtension, libs: VersionCatalog) {
    dependencies {
        add("implementation", libs.libraryOrThrow("spring-boot-starter-data-mongodb"))
        add("implementation", libs.libraryOrThrow("mongodb"))

        if (extension.useTestcontainers.get()) {
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

        if (extension.useMigration.get()) {
            add("implementation", libs.libraryOrThrow("fly-code"))
            add("implementation", libs.libraryOrThrow("fly-postgresql"))
        }

        if (extension.useTestcontainers.get()) {
            // Align Testcontainers modules if BOM alias exists
            libs.findLibrary("testcontainers-bom").ifPresent { bom ->
                add("testImplementation", platform(bom.get()))
            }

            add("testImplementation", libs.libraryOrThrow("testcontainers-postgres"))

            libs.findLibrary("testcontainers-junit-jupiter").ifPresent {
                add("testImplementation", it.get())
            }
        }
    }
}

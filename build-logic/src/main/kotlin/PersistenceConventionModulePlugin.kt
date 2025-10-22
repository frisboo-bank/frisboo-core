import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalog
import org.gradle.kotlin.dsl.dependencies

internal class PersistenceConventionModulePlugin : Plugin<Project> {
    override fun apply(target: Project): Unit = with(target) {
        val libs = getLibs()

        val extension = extensions.create("persistenceConventions", PersistenceConventionExtension::class.java)

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
        }
    }
}

private fun Project.configurePostgres(extension: PersistenceConventionExtension, libs: VersionCatalog) {
    dependencies {
        add("implementation", libs.libraryOrThrow("spring-boot-starter-data-jpa"))
        add("runtimeOnly", libs.libraryOrThrow("postgresql"))

        if (extension.useMigration.get()) {
            add("implementation", libs.libraryOrThrow("flyway-core"))
            add("implementation", libs.libraryOrThrow("flyway-postgresql"))
        }

        if (extension.useTestcontainers.get()) {
            add("testImplementation", platform(libs.libraryOrThrow("testcontainers-bom")))
            add("testImplementation", libs.libraryOrThrow("testcontainers-postgresql"))
        }
    }
}

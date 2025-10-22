import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalog
import org.gradle.kotlin.dsl.dependencies

internal class MessagingConventionModulePlugin : Plugin<Project> {
    override fun apply(target: Project): Unit = with(target) {
        val libs = getLibs()
        val extension = extensions.create("messagingConventions", MessagingConventionExtension::class.java)

        afterEvaluate {
            if (extension.useKafka.get()) {
                configureKafka(extension, libs)
            }
        }
    }
}

private fun Project.configureKafka(extension: MessagingConventionExtension, libs: VersionCatalog) {
    dependencies {
        add("implementation", libs.libraryOrThrow("spring-kafka"))

        if (extension.useTestcontainers.get()) {
            add("testImplementation", libs.libraryOrThrow("testcontainers-bom"))
            add("testImplementation", libs.libraryOrThrow("testcontainers-kafka"))
        }
    }
}

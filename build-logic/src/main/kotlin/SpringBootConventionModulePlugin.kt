import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalog
import org.gradle.kotlin.dsl.attributes
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.named
import org.springframework.boot.gradle.dsl.SpringBootExtension
import org.springframework.boot.gradle.tasks.bundling.BootJar

internal class SpringBootConventionModulePlugin : Plugin<Project> {
    override fun apply(target: Project): Unit = with(target) {
        val libs = getLibs()

        pluginManager.apply("org.springframework.boot")
        pluginManager.apply("org.jetbrains.kotlin.plugin.spring")

        pluginManager.withPlugin("org.springframework.boot") {
            configureSpringboot()
            configureDependencies(libs)
            configureTasks()
        }
    }
}

private fun Project.configureSpringboot() {
    extensions.configure<SpringBootExtension> {
        buildInfo()
    }
}

private fun Project.configureDependencies(libs: VersionCatalog) {
    dependencies {
        // Enforce BOMs across common configurations if enabled
        val springBootBom = libs.libraryOrThrow("spring-boot-bom")
        val addBom: (String, Any) -> Unit = { conf, bom ->
            add(conf, platform(bom))
        }
        listOf("api", "implementation", "testImplementation").forEach { conf ->
            addBom(conf, springBootBom)
        }

        add("implementation", libs.libraryOrThrow("spring-boot-starter-webflux"))
        add("implementation", libs.libraryOrThrow("spring-boot-starter-validation"))
        add("implementation", libs.libraryOrThrow("spring-boot-starter-actuator"))
    }
}

private fun Project.configureTasks() {
    tasks.named<BootJar>("bootJar") {
        archiveFileName.set("${project.name}-${project.version}.jar")
        manifest {
            attributes(
                "Implementation-Title" to project.name,
                "Implementation-Version" to project.version,
                "Created-By" to "Gradle ${gradle.gradleVersion}",
            )
        }
    }
}


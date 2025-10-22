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

        afterEvaluate {
            configureSpringboot()
            configureDependencies(libs)
            configureTasks()
        }

        afterEvaluate {
            println("==================================")
            println("Spring Boot Conventions Applied:")
            println(" - Spring Boot Version: ${libs.requiredVersion("spring-boot")}")
            println("==================================")
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
        add("implementation", platform(libs.libraryOrThrow("spring-boot-bom")))

        add("implementation", libs.libraryOrThrow("spring-boot-starter-webflux"))
        add("implementation", libs.libraryOrThrow("spring-boot-starter-validation"))
        add("implementation", libs.libraryOrThrow("spring-boot-starter-actuator"))

        add("testImplementation", libs.libraryOrThrow("spring-boot-starter-test"))
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


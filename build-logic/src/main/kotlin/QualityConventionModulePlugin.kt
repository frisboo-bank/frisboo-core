import com.diffplug.gradle.spotless.SpotlessExtension
import com.diffplug.spotless.LineEnding
import io.gitlab.arturbosch.detekt.Detekt
import io.gitlab.arturbosch.detekt.extensions.DetektExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

internal class QualityConventionModulePlugin : Plugin<Project> {
    override fun apply(target: Project): Unit = with(target) {
        val libs = getLibs()

        plugins.apply(libs.findPlugin("dependency-analysis").get().get().pluginId)
        plugins.apply(libs.findPlugin("detekt").get().get().pluginId)
        plugins.apply(libs.findPlugin("gradle-versions").get().get().pluginId)
        plugins.apply(libs.findPlugin("spotless").get().get().pluginId)

        val headerFile = rootProject.layout.projectDirectory.file("config/license-header.txt")
        val editorConfig = rootProject.layout.projectDirectory.file(".editorconfig")
        val ktlintVersion = libs.findVersion("ktlint").get().requiredVersion
        val delimiter =
            "^\\s*(plugins|pluginManagement|import|buildscript|" +
                    "dependencyResolutionManagement|enableFeaturePreview|include|rootProject)\\b"

        extensions.configure<SpotlessExtension> {
            val commonExcludes =
                listOf(
                    "**/build/**",
                    "**/build-*/**",
                    "**/.gradle/**",
                    "**/.idea/**",
                    "**/.git/**",
                    "**/generated/**",
                    "**/.gradle-test-kit/**",
                )

            kotlin {
                target("**/*.kt")
                targetExclude(commonExcludes)
                ktlint(ktlintVersion).setEditorConfigPath(editorConfig)
                trimTrailingWhitespace()
                endWithNewline()
                licenseHeaderFile(headerFile)
                lineEndings = LineEnding.UNIX
            }

            kotlinGradle {
                target("**/*.gradle.kts")
                targetExclude(commonExcludes)
                ktlint(ktlintVersion).setEditorConfigPath(editorConfig)
                trimTrailingWhitespace()
                endWithNewline()
                licenseHeaderFile(headerFile, delimiter)
                lineEndings = LineEnding.UNIX
            }

            format("misc") {
                target(
                    "**/*.md",
                    "**/*.properties",
                    "**/*.yml",
                    "**/*.yaml",
                    "**/*.xml",
                    "**/.gitignore",
                    "**/*.txt",
                )
                targetExclude(commonExcludes)
                trimTrailingWhitespace()
                endWithNewline()
                lineEndings = LineEnding.UNIX
            }
        }


        val detektConfig = rootProject.layout.projectDirectory.file("config/detekt/detekt.yml")
        val detektBaseline = rootProject.layout.projectDirectory.file("config/detekt/detekt-baseline.xml").asFile

        extensions.configure<DetektExtension> {
            toolVersion = libs.findVersion("detekt").get().requiredVersion
            parallel = true
            buildUponDefaultConfig = true
            config.setFrom(detektConfig)
            baseline = detektBaseline
        }

        tasks.withType<Detekt>().configureEach {
            jvmTarget = JvmTarget.JVM_21.target
            autoCorrect = true

            reports {
                xml.required.set(true)
                html.required.set(true)
                sarif.required.set(true)
                txt.required.set(false)
                md.required.set(false)
            }
        }

        tasks.named("check") {
            dependsOn("spotlessCheck", "detekt")
        }
    }
}

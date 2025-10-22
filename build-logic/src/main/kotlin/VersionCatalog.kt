import org.gradle.api.artifacts.VersionCatalog
import org.gradle.api.artifacts.VersionCatalogsExtension

internal fun VersionCatalog.requiredVersion(alias: String): String =
    findVersion(alias).orElseThrow{
        IllegalStateException("Missing version `$alias` from libs.versions.toml")
    }.requiredVersion

internal fun VersionCatalog.optionalVersion(alias: String): String? =
    findVersion(alias).map { it.requiredVersion }.orElse(null)

internal fun VersionCatalog.libraryOrThrow(alias: String) =
    findLibrary(alias).orElseThrow {
        IllegalStateException("Missing library `$alias` in libs.versions.toml")
    }.get()

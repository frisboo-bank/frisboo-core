plugins {
    base
    id("com.frisboo.corebanking.conventions.kotlin-conventions")
    id("com.frisboo.corebanking.conventions.quality-conventions")
    alias(versionLibs.plugins.maven.gradle.publish) apply false
}

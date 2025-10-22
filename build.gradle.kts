plugins {
//    base
    alias(libs.plugins.frisboo.convention.kotlin)
    alias(libs.plugins.frisboo.convention.spring.boot)
//    alias(libs.plugins.frisboo.convention.openapi)
    alias(libs.plugins.frisboo.convention.quality)
    alias(libs.plugins.maven.gradle.publish) apply false
}

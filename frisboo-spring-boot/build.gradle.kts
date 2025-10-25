plugins {
    kotlin("jvm")

    alias(libs.plugins.dokka)
}

dependencies {
    api(project(":frisboo-core"))
}

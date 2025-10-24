plugins {
    base
    id("quality-conventions")
    alias(libs.plugins.dokka)
    alias(libs.plugins.maven.publish)
}

dokka {
    dokkaPublications.html {
        outputDirectory.set(project.file("docs/api"))
    }
}

dependencies {
//    dokka(projects.frisboo.frisbooCore)
}

allprojects {
    if (this != rootProject) {
        apply(plugin = "com.vanniktech.maven.publish")
    }
}

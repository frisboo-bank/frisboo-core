plugins {
    `version-catalog`
    alias(libs.plugins.dokka)
}

catalog {
    versionCatalog {
        from(files("libs.versions.toml"))
    }
}

//publishing {
//    publications {
//        create<MavenPublication>("versionCatalog") {
//            from(components["versionCatalog"])
//            groupId = project.group.toString()
//            artifactId = project.name
//            version = project.version.toString()
//
//            pom {
//                name.set("frisboo-core-banking-version-catalog")
//                description.set("Shared library versions for Frisboo core banking services")
//            }
//        }
//    }
//    repositories {
//        mavenLocal()
//    }
//}

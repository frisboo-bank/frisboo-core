package com.frisboo.corebanking.conventions

import org.gradle.kotlin.dsl.all
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.plugins

plugins {
    id("com.google.protobuf")
//    id("org.springdoc.openapi-gradle-plugin")
}

private val libs = extensions.getByType<VersionCatalogsExtension>().named("versionLibs")

//protobuf {
//    protoc {
//        artifact = libs.protoc.asProvider().get().toString()
//    }
//    plugins {
//        create("grpc") {
//            artifact = libs.protoc.gen.grpc.java.get().toString()
//        }
//        create("grpckt") {
//            artifact = "${libs.protoc.gen.grpc.kotlin.get()}:jdk8@jar"
//        }
//    }
//    generateProtoTasks {
//        all().forEach {
//            it.plugins {
//                create("grpc")
//                create("grpckt")
//            }
//            it.builtins {
//                create("kotlin")
//            }
//        }
//    }
//}

dependencies {
//    implementation(platform(libs.findLibrary("spring-boot-dependencies"))
}

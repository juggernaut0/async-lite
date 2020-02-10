plugins {
    kotlin("js") version "1.3.61"
    `maven-publish`
    id("com.github.node-gradle.node") version "2.2.1"
    id("org.jetbrains.dokka") version "0.10.0"
}

group = "com.github.juggernaut0"
version = "0.1.0"

repositories {
    mavenCentral()
    jcenter()
}

dependencies {
    implementation(kotlin("stdlib-js"))
    testImplementation(kotlin("test-js"))
}

kotlin {
    target {
        compilations.all {
            kotlinOptions {
                moduleKind = "umd"
                sourceMap = true
                sourceMapEmbedSources = "always"
                freeCompilerArgs = listOf("-Xuse-experimental=kotlin.Experimental")
            }
        }
    }
}

val sourceJar by tasks.creating(Jar::class) {
    from(kotlin.sourceSets.main.map { it.kotlin })
    archiveClassifier.set("sources")
}

publishing {
    publications {
        val maven by creating(MavenPublication::class) {
            from(components["kotlin"])
            artifact(sourceJar)
        }
    }
    repositories {
        maven {
            name = "pages"
            url = uri("pages/m2/repository")
        }
    }
}

node {
    download = true
    version = "12.14.1"
}

tasks {
    val populateNodeModules by registering(Copy::class) {
        configurations.testCompileClasspath {
            this.files.forEach {
                if (it.name.endsWith(".jar")) {
                    from(zipTree(it.absolutePath).matching { include("*.js", "*.js.map") })
                } else {
                    from(fileTree(it.absolutePath).matching { include("*.js", "*.js.map") })
                }
            }
        }

        into("${buildDir}/node_modules")
    }

    val runJest by registering(com.moowork.gradle.node.task.NodeTask::class) {
        dependsOn(compileTestKotlinJs, populateNodeModules, npmInstall)
        script = file("node_modules/jest/bin/jest.js")
        doFirst {
            setArgs(listOf(projectDir.toURI().relativize(compileTestKotlinJs.get().outputFile.toURI())))
        }
    }

    test {
        dependsOn(runJest)
    }
}

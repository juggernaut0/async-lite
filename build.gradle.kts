plugins {
    kotlin("js") version "1.5.0"
    `maven-publish`
}

group = "com.github.juggernaut0"
version = "0.2.0"

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib-js"))
    testImplementation(kotlin("test-js"))
}

kotlin {
    js {
        browser {
            compilations.all {
                kotlinOptions {
                    moduleKind = "umd"
                    sourceMap = true
                    sourceMapEmbedSources = "always"
                    freeCompilerArgs = listOf("-Xuse-experimental=kotlin.Experimental")
                }
            }
            testTask {
                useKarma {
                    useFirefoxHeadless()
                }
            }
        }
    }
}

publishing {
    repositories {
        maven {
            name = "pages"
            url = uri("$rootDir/pages/m2/repository")
        }
    }
    publications {
        create<MavenPublication>("maven") {
            artifact(tasks.named("jsJar"))
            artifact(tasks.named("kotlinSourcesJar"))
        }
    }
}

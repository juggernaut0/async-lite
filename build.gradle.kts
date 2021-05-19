plugins {
    kotlin("js") version "1.5.0"
    `maven-publish`
    id("org.jetbrains.dokka") version "1.4.30"
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
            url = uri("pages/m2/repository")
        }
    }
}

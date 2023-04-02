plugins {
    kotlin("js") version "1.8.20"
    `maven-publish`
    id("dev.twarner.download-firefox") version "0.3.5-SNAPSHOT"
}

group = "com.github.juggernaut0"
version = "0.3.0"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
}

kotlin {
    js(BOTH) {
        browser {
            compilations.all {
                kotlinOptions {
                    moduleKind = "umd"
                    sourceMap = true
                    sourceMapEmbedSources = "always"
                }
            }
            testTask {
                dependsOn(tasks.downloadFirefox)
                doFirst {
                    environment("FIREFOX_BIN", tasks.downloadFirefox.flatMap { it.outputBin }.get().asFile.absolutePath)
                }
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
            from(components.getByName("kotlin"))
        }
    }
}

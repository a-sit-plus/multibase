import org.gradle.kotlin.dsl.support.listFilesOrdered
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl

plugins {
    kotlin("multiplatform") version "2.1.20"
    id("maven-publish")
    id("signing")
    id("io.github.gradle-nexus.publish-plugin") version "1.3.0"
    id("org.jetbrains.dokka") version "1.9.20"
}

val artifactVersion: String by extra
group = "at.asitplus"
version = artifactVersion

repositories {
    mavenCentral()
}

val dokkaOutputDir = "$projectDir/docs"
tasks.dokkaHtml {

    val moduleDesc = File("$rootDir/dokka-tmp.md").also {
        it.createNewFile()

        File("$rootDir/multibase.png").copyTo(File("$rootDir/docs/multibase.png"), overwrite = true)
    }
    val readme =
        File("${rootDir}/README.md").readText().replaceFirst("# ", "")
    val moduleTitle = "multibase"

    moduleDesc.writeText(
        "# Module $moduleTitle\n$readme"
    )
    moduleName.set(moduleTitle)

    dokkaSourceSets {
        named("commonMain") {

            includes.from(moduleDesc)
            sourceLink {
                localDirectory.set(file("src/$name/kotlin"))
                remoteUrl.set(
                    uri("https://github.com/a-sit-plus/multibase/tree/development/src/$name/kotlin").toURL()
                )
                // Suffix which is used to append the line number to the URL. Use #L for GitHub
                remoteLineSuffix.set("#L")
            }
        }
    }
    outputDirectory.set(file("${rootDir}/docs"))
    doLast {
        rootDir.listFilesOrdered { it.extension.lowercase() == "png" || it.extension.lowercase() == "svg" }
            .forEach { it.copyTo(File("$rootDir/docs/${it.name}"), overwrite = true) }
    }
}
val deleteDokkaOutputDir by tasks.register<Delete>("deleteDokkaOutputDirectory") {
    delete(dokkaOutputDir)
}
val javadocJar = tasks.register<Jar>("javadocJar") {
    dependsOn(deleteDokkaOutputDir, tasks.dokkaHtml)
    archiveClassifier.set("javadoc")
    from(dokkaOutputDir)
}


//first sign everything, then publish!
tasks.withType<AbstractPublishToMaven> {
    tasks.withType<Sign>().forEach {
        dependsOn(it)
    }
}

kotlin {
    linuxX64()
    linuxArm64()
    androidNativeX64()
    androidNativeX86()
    androidNativeArm32()
    androidNativeArm64()
    iosX64()
    iosArm64()
    iosSimulatorArm64()
    macosX64()
    macosArm64()
    tvosArm64()
    tvosSimulatorArm64()
    tvosX64()
    watchosArm32()
    watchosArm64()
    watchosX64()
    watchosSimulatorArm64()
    mingwX64()


    jvmToolchain(17)
    jvm {
        compilerOptions {
            freeCompilerArgs = listOf(
                "-Xjsr305=strict"
            )
        }
    }

    listOf(
        js(IR).apply { browser { testTask { enabled = false } } },
        @OptIn(ExperimentalWasmDsl::class)
        wasmJs().apply { browser { testTask { enabled = false } } }
    ).forEach {
        it.nodejs()
    }

    sourceSets {
        commonMain.dependencies {
            implementation("com.ionspin.kotlin:bignum:0.3.10")
            api("io.matthewnelson.encoding:base64:2.4.0")
            api("io.matthewnelson.encoding:base32:2.4.0")
            api("io.matthewnelson.encoding:base16:2.4.0")
        }

        commonTest.dependencies {
            implementation(kotlin("test"))
        }
    }
}

publishing {
    publications {
        withType<MavenPublication> {
            artifact(javadocJar)
            pom {
                name.set("Multibase")
                description.set("KMP Multibase Encoder/Decoder")
                url.set("https://github.com/a-sit-plus/multibase")
                licenses {
                    license {
                        name.set("MIT License")
                        url.set("https://opensource.org/licenses/MIT")
                    }
                }
                developers {
                    developer {
                        id.set("JesusMcCloud")
                        name.set("Bernd Prünster")
                        email.set("bernd.pruenster@a-sit.at")
                    }
                    developer {
                        id.set("n0900")
                        name.set("Simon Müller")
                        email.set("simon.mueller@a-sit.at")
                    }
                }
                scm {
                    connection.set("scm:git:git@github.com:a-sit-plus/multibase.git")
                    developerConnection.set("scm:git:git@github.com:a-sit-plus/multibase.git")
                    url.set("https://github.com/a-sit-plus/multibase")
                }
            }
        }
    }
    repositories {
        mavenLocal {
            signing.isRequired = false
        }
    }
}

signing {
    val signingKeyId: String? by project
    val signingKey: String? by project
    val signingPassword: String? by project
    useInMemoryPgpKeys(signingKeyId, signingKey, signingPassword)
    sign(publishing.publications)
}

nexusPublishing {
    repositories {
        sonatype {
            nexusUrl.set(uri("https://s01.oss.sonatype.org/service/local/"))
            snapshotRepositoryUrl.set(uri("https://s01.oss.sonatype.org/content/repositories/snapshots/"))
        }
    }
}


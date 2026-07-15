import at.asitplus.gradle.dokka
import at.asitplus.gradle.setupDokka
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl

plugins {
    kotlin("multiplatform")
    alias(libs.plugins.testballoon)
    id("signing")
    alias(libs.plugins.asp.conventions)
}

group = rootProject.group
version = rootProject.version

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
    jvm()

    listOf(
        js().apply { browser { testTask { enabled = false } } },
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
    }
}

setupDokka(
    baseUrl = "https://github.com/a-sit-plus/multibase/tree/development"
)
val moduleDesc = rootProject.file("dokka-tmp.md").apply {
    writeText("# Module multibase\n${rootProject.file("README.md").readText().replaceFirst("# ", "")}")
}
dokka {
    moduleName.set("multibase")
    dokkaSourceSets.configureEach {
        includes.from(moduleDesc)
    }
}
val javadocRedirectJar = tasks.register<Jar>("javadocRedirectJar") {
    archiveClassifier.set("javadoc")
    from("javadoc")
}

publishing {
    publications {
        withType<MavenPublication> {
            artifact(javadocRedirectJar)
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
        maven {
            url = uri(rootProject.layout.projectDirectory.dir("repo"))
            this.name = "local"
            signing {
                isRequired = false
            }
        }
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

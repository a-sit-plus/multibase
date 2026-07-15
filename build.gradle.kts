import org.gradle.kotlin.dsl.support.listFilesOrdered

plugins {
    kotlin("multiplatform") version libs.versions.kotlin.get() apply false
    alias(libs.plugins.testballoon) apply false
    alias(libs.plugins.asp.conventions)
}

val artifactVersion: String by extra
group = "at.asitplus"
version = artifactVersion

tasks.register<Sync>("dokkaGenerateSite") {
    dependsOn(":multibase:dokkaGeneratePublicationHtml")
    from(project(":multibase").layout.buildDirectory.dir("dokka/html"))
    from(rootDir.listFilesOrdered { it.extension.lowercase() == "png" || it.extension.lowercase() == "svg" })
    into(rootProject.layout.projectDirectory.dir("docs"))
}

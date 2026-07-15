pluginManagement {
    repositories {
        maven("https://raw.githubusercontent.com/a-sit-plus/gradle-conventions-plugin/mvn/repo")
        mavenCentral()
        gradlePluginPortal()
    }
}

rootProject.name = "multibase-root"
include(":multibase")

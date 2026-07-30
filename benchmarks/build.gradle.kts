plugins {
    java
    id("me.champeau.jmh") version "0.7.3"
}

repositories {
    mavenCentral()
}

dependencies {
    jmh(project(":multibase"))
}

jmh {
    warmupIterations = 5
    iterations = 5
    fork = 3
    timeUnit = "ms"
}

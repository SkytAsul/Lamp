plugins {
    id("java")
}

group = "io.github.revxrsal"

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":common"))
    compileOnly("net.minestom:minestom-snapshots:39d445482f")
}

java.toolchain.languageVersion.set(JavaLanguageVersion.of(21))
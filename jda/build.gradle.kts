plugins {
    id("java")
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":common"))
    compileOnly("net.dv8tion:JDA:6.0.0-rc.1")
}
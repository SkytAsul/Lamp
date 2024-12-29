buildscript {
    repositories {
        maven("https://maven.fabricmc.net/")
    }
}

plugins {
    id("java")
    id("fabric-loom") version "1.8-SNAPSHOT"
}

repositories {
    maven("https://libraries.minecraft.net")
    maven("https://maven.fabricmc.net/")
}

val minecraftVersion = "1.21"
val yarnMappings = "1.21+build.9"
val loaderVersion = "0.16.7"
val fabricVersion = "0.102.0+1.21"
val permissionsVersion = "0.3.1"

dependencies {
    implementation(project(":common"))
    implementation(project(":brigadier"))
    modImplementation(project(":fabric"))
    mappings("net.fabricmc:yarn:${yarnMappings}:v2")
    minecraft("com.mojang:minecraft:${minecraftVersion}")
    modImplementation("net.fabricmc:fabric-loader:${loaderVersion}")
    include("me.lucko:fabric-permissions-api:${permissionsVersion}")?.let { modImplementation(it) }
}

java.toolchain.languageVersion.set(JavaLanguageVersion.of(21))

tasks {
    processResources {
        filesMatching("fabric.mod.json") {
            expand(mapOf(
                "version" to project.version,
                "loader_version" to loaderVersion,
                "minecraft_version" to minecraftVersion
            ))
        }
    }
}

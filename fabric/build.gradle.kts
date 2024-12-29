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
val javaVersion = 21

dependencies {
    implementation(project(":common"))
    implementation(project(":brigadier"))
    minecraft("com.mojang:minecraft:${minecraftVersion}")
    mappings("net.fabricmc:yarn:${yarnMappings}:v2")
    modImplementation("net.fabricmc:fabric-loader:${loaderVersion}")
    modImplementation("net.fabricmc.fabric-api:fabric-api:${fabricVersion}")
    include("me.lucko:fabric-permissions-api:${permissionsVersion}")?.let {
        modImplementation(it)
    }
}

java.toolchain.languageVersion.set(JavaLanguageVersion.of(javaVersion))

tasks {
    processResources {
        filesMatching("fabric.mod.json") {
            expand(
                mapOf(
                    "version" to project.version,
                    "java_version" to javaVersion,
                    "loader_version" to loaderVersion,
                    "fabric_version" to fabricVersion,
                    "minecraft_version" to minecraftVersion
                )
            )
        }
    }
}

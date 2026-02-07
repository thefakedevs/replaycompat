import java.util.Properties

val localProperties = Properties()
val localPropertiesFile = file("local.properties")

if (localPropertiesFile.exists()) {
    localPropertiesFile.inputStream().use {
        localProperties.load(it)
    }
    localProperties.forEach { key, value ->
        gradle.extra[key.toString()] = value
    }
}

pluginManagement {
    repositories {
        gradlePluginPortal()
        maven {
            name = "MinecraftForge"
            url = uri("https://maven.minecraftforge.net/")
        }
        maven {
            name = "ParchmentMC"
            url = uri("https://maven.parchmentmc.org")
        }
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.7.0"
}

rootProject.name = "replaycompat"


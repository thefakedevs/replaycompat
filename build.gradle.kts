import java.text.SimpleDateFormat
import java.util.Date

plugins {
    eclipse
    idea
    id("maven-publish")
    id("net.minecraftforge.gradle") version "[6.0.16,6.2)"
    kotlin("jvm") version "1.9.22"
    kotlin("plugin.serialization") version "1.9.22"
    id("org.parchmentmc.librarian.forgegradle") version "1.+"
    id("org.spongepowered.mixin") version "0.7.+"
}

repositories {
    maven {
        name = "Kotlin for Forge"
        setUrl("https://thedarkcolour.github.io/KotlinForForge/")
    }

    maven {
        name = "Curse Maven"
        url = uri("https://www.cursemaven.com")
        content {
            includeGroup("curse.maven")
        }
    }

    maven {
        name = "svocraftRepository"
        url = uri("https://reposilite.artembay.ru/releases")
    }
    exclusiveContent {
        forRepository {
            maven {
                name = "Modrinth"
                url = uri("https://api.modrinth.com/maven")
            }
        }
        forRepositories(fg.repository) // Only add this if you're using ForgeGradle, otherwise remove this line
        filter {
            includeGroup("maven.modrinth")
        }
    }
    mavenLocal()
}

val mod_group_id: String by project
val mod_version: String by project
val mod_id: String by project
val mapping_channel: String by project
val mapping_version: String by project

fun getGitCommitHash(): String {
    return try {
        val stdout = providers.exec {
            commandLine("git", "rev-parse", "--short", "HEAD")
        }.standardOutput.asText.get()
        stdout.trim()
    } catch (_: Exception) {
        "unknown"
    }
}

group = mod_group_id
version = "$mod_version-${getGitCommitHash()}"

base {
    archivesName.set(mod_id)
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(17))
    withSourcesJar()
}

minecraft {
    mappings(mapping_channel, mapping_version)
    copyIdeResources.set(true)

    runs {
        configureEach {
            workingDirectory(project.file("run"))
            property("forge.logging.markers", "REGISTRIES")
            property("forge.logging.console.level", "debug")
            property("mixin.env.remapRefMap", "true")
            property("mixin.env.refMapRemappingFile", "${project.projectDir}/build/createSrgToMcp/output.srg")

            mods {
                create(mod_id) {
                    source(sourceSets.main.get())
                }
            }
        }

        create("client") {
            property("forge.enabledGameTestNamespaces", mod_id)
            jvmArg("-Dgeckolib.disable_examples=true")
        }

        create("server") {
            property("forge.enabledGameTestNamespaces", mod_id)
            args("--nogui")
            jvmArg("-Dgeckolib.disable_examples=true")
        }
    }
}

mixin {
    add(sourceSets.main.get(), "replaycompat.refmap.json")
    config("replaycompat.mixins.json")
}

dependencies {
    "minecraft"("net.minecraftforge:forge:${project.property("minecraft_version")}-${project.property("forge_version")}")
    implementation("thedarkcolour:kotlinforforge:4.11.0")

    annotationProcessor("org.spongepowered:mixin:0.8.5:processor")

    implementation(fg.deobf("curse.maven:reforgedplay-mod-1018692:5637596"))
    implementation(fg.deobf("curse.maven:forge-client-reset-packet-forward-862147:4808617"))

    implementation(fg.deobf("com.tacz:tacz-1.20.1:1.1.6-svocraft-e0d0c29f"))
    implementation(fg.deobf("com.atsushio.superbwarfare:superbwarfare:svocraft-1.20.1-0.8.8-c7570ac14"))
    implementation(fg.deobf("curse.maven:geckolib-388172:7025129"))
    implementation(fg.deobf("curse.maven:curios-309927:6418456"))

    implementation(fg.deobf("curse.maven:walkie-talkie-825621:5133440"))
    implementation(fg.deobf("curse.maven:architectury-api-419699:5137938"))
    implementation(fg.deobf("curse.maven:playerrevive-266890:6048921"))
    implementation(fg.deobf("curse.maven:creativecore-257814:6383884"))
    implementation(fg.deobf("ru.liko:WRB-Armor:0.4.0"))
    implementation(fg.deobf("curse.maven:sbw-warborn-drones-1383935:7373873"))
    implementation(fg.deobf("curse.maven:simple-voice-chat-416089:7416882"))
    implementation(fg.deobf("tech.vvp:vvp-alpha-1.20.1:0.2.1"))

    implementation(fg.deobf("ru.lavafrai.mcsp:mcsp:1.0.8"))

    implementation(fg.deobf("maven.modrinth:quality-sounds:1.5.0-1.20.1"))
    implementation(fg.deobf("maven.modrinth:cloth-config:11.1.118+forge"))

    implementation(fg.deobf("ru.lavafrai.svogame:svocraft:1.0-SNAPSHOT-da62325"))
    implementation("ru.lavafrai.svogame.runtime:svoruntime:1.0.0-8534f90")
    implementation(fg.deobf("ru.lavafrai.svogame:metrics:1.0-fdbf7b1"))
    implementation(fg.deobf("ru.lavafrai.svogame:score:1.0-SNAPSHOT-3509405"))
}

tasks.withType<ProcessResources> {
    val replaceProperties = mapOf(
            "minecraft_version" to project.property("minecraft_version"),
            "minecraft_version_range" to project.property("minecraft_version_range"),
            "forge_version" to project.property("forge_version"),
            "forge_version_range" to project.property("forge_version_range"),
            "loader_version_range" to project.property("loader_version_range"),
            "mod_id" to mod_id,
            "mod_name" to project.property("mod_name"),
            "mod_license" to project.property("mod_license"),
            "mod_version" to mod_version,
            "mod_authors" to project.property("mod_authors"),
            "mod_description" to project.property("mod_description")
    )
    inputs.properties(replaceProperties)

    filesMatching(listOf("META-INF/mods.toml", "pack.mcmeta")) {
        expand(replaceProperties)
    }
}

tasks.withType<Jar> {
    manifest {
        attributes(mapOf(
                "Specification-Title" to mod_id,
                "Specification-Vendor" to project.property("mod_authors"),
                "Specification-Version" to "1",
                "Implementation-Title" to project.name,
                "Implementation-Version" to project.version,
                "Implementation-Vendor" to project.property("mod_authors"),
                "Implementation-Timestamp" to SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ").format(Date())
        ))
    }
}


afterEvaluate {
    publishing {
        publications {
            create<MavenPublication>("minimalCurseLike") {
                artifact(tasks.named<Jar>("jar")) {
                    builtBy(tasks.named("reobfJar"))
                }

                groupId = project.group.toString()
                artifactId = base.archivesName.get()
                version = project.version.toString()

                pom.withXml {
                    val root = asNode()
                    root.children().clear()

                    root.appendNode("modelVersion", "4.0.0")
                    root.appendNode("groupId", groupId)
                    root.appendNode("artifactId", artifactId)
                    root.appendNode("version", version)
                }
            }
        }

        repositories {
            maven {
                name = "reposilite"
                url = uri("https://reposilite.artembay.ru/releases")

                credentials {
                    username = gradle.extra["reposilite.user"]?.toString() ?: error("reposilite.user is not defined")
                    password = gradle.extra["reposilite.token"]?.toString() ?: error("reposilite.token is not defined")
                }

                authentication {
                    create<BasicAuthentication>("basic")
                }
            }
        }
    }
}
plugins {
    `java-library`
    `maven-publish`
    id("com.github.johnrengelman.shadow") version "7.1.2"
}

group = "com.runicrealms.plugin"
version = "1.0-SNAPSHOT"

dependencies {
    implementation(commonLibs.mongodriversync)
    compileOnly(commonLibs.mythicmobs)
    compileOnly(commonLibs.spigot)
    compileOnly(commonLibs.paper)
    compileOnly(commonLibs.placeholderAPI)
    compileOnly(project(":Projects:Core"))
    compileOnly(project(":Projects:Chat"))
    compileOnly(project(":Projects:Items"))
    compileOnly(project(":Projects:Mounts"))
    compileOnly(project(":Projects:Npcs"))
    compileOnly(project(":Projects:Restart"))
    compileOnly(commonLibs.tabbed)
    implementation(commonLibs.acf)
    compileOnly(commonLibs.holographicdisplays)
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(17))
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = "com.runicrealms.plugin"
            artifactId = "guilds"
            version = "1.0-SNAPSHOT"
            from(components["java"])
        }
    }
}

tasks.withType<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar> {
//    build {
//        dependsOn(shadowJar)
//    }
}

tasks.register("wrapper")
tasks.register("prepareKotlinBuildScriptModel")
plugins {
    `java-library`
    `maven-publish`
}

group = "com.runicrealms.plugin"
version = "1.0-SNAPSHOT"

dependencies {
    compileOnly(commonLibs.mongodbdriversync)
    compileOnly(commonLibs.mythicmobs)
    compileOnly(commonLibs.spigot)
    compileOnly(commonLibs.paper)
    compileOnly(commonLibs.placeholderapi)
    compileOnly(project(":Projects:Core"))
    compileOnly(project(":Projects:Chat"))
    compileOnly(project(":Projects:Items"))
    compileOnly(project(":Projects:Mounts"))
    compileOnly(project(":Projects:Npcs"))
    compileOnly(project(":Projects:Restart"))
    compileOnly(project(":Projects:Common"))
    compileOnly(project(":Projects:Database"))
    compileOnly(commonLibs.tabbed)
    compileOnly(commonLibs.acf)
    compileOnly(commonLibs.taskchain)
    compileOnly(commonLibs.holographicdisplays)
    compileOnly(commonLibs.springdatamongodb)
    compileOnly(commonLibs.mongodbdriversync)
    compileOnly(commonLibs.mongodbdrivercore)
    compileOnly(commonLibs.jedis)
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

tasks.register("wrapper")
tasks.register("prepareKotlinBuildScriptModel")

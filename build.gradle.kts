import org.apache.tools.ant.filters.ReplaceTokens

plugins {
    java
    application
    id("com.github.johnrengelman.shadow") version "7.1.2"
    id("org.ajoberstar.grgit") version "5.0.0"
    idea
}


group = "xyz.htmlcsjs"
version = "0.4.1"
var projectBaseName = "CoffeeFloppa"
var mainClass = "xyz.htmlcsjs.coffeeFloppa.asm.FloppaLauncher"

repositories {
    mavenCentral()
    maven {
        setUrl("https://mvnrepository.com/artifact")
    }
    maven {
        setUrl("https://cursemaven.com")
    }
}

application {
    mainClass.set("xyz.htmlcsjs.coffeeFloppa.asm.FloppaLauncher")
    tasks.run.get().workingDir = File("run/")
}
idea {
    module {
        isDownloadJavadoc = true
        isDownloadJavadoc = true
    }
}

dependencies {
    implementation("net.dv8tion:JDA:5.0.0-beta.3")
    implementation("ch.qos.logback:logback-classic:1.4.4")
    implementation("com.googlecode.json-simple:json-simple:1.1.1")
    implementation("me.xdrop:fuzzywuzzy:1.4.0")
    implementation("org.luaj:luaj-jse:3.0.1")
    implementation("org.jetbrains:annotations:23.0.0")
    implementation("curse.maven:gtceu-557242:4068926")
    implementation("org.reflections:reflections:0.10.2")
    implementation("org.apache.commons:commons-text:1.10.0")
    implementation("org.ow2.asm:asm:9.4") // boy i love asm

    testImplementation("org.junit.jupiter:junit-jupiter:5.9.0")
}

tasks.create<Delete> ("deleteGeneratedSources") {
    delete("$buildDir/sources/src/")
}

tasks.create<Copy> ("generateSource") {
    filteringCharset = "UTF8"
    from("src") {
        filter(ReplaceTokens::class, "tokens" to mapOf("VERSION" to version, "GIT_VER" to grgit.head().id))
    }
    into("$buildDir/sources/src")
    dependsOn("deleteGeneratedSources")
}

tasks {
    jar {
        manifest {
            attributes["Main-Class"] = mainClass
        }
        from(sourceSets.main.get().java)
        archiveBaseName.set(projectBaseName)
    }
    shadowJar {
        archiveBaseName.set("$projectBaseName-Uber")
        dependencies {
            exclude(dependency("org.junit.jupiter:.*"))
        }
    }
    test {
        testLogging {
            events("failed")
            showExceptions = true
            exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
            showStackTraces = true
            showCauses = true
            showStandardStreams = false
        }
        useJUnitPlatform()
    }
    compileJava {
        sourceCompatibility = JavaVersion.VERSION_17.majorVersion
        targetCompatibility = JavaVersion.VERSION_17.majorVersion
        dependsOn("generateSource")
        setSource("$buildDir/sources/src/main")
        options.encoding = "UTF8"
    }
    compileTestJava {
        dependsOn("generateSource")
        setSource("$buildDir/sources/src/test")
    }
}


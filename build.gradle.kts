import org.apache.tools.ant.filters.ReplaceTokens

plugins {
    java
    application
    id("com.github.johnrengelman.shadow") version "7.1.2"
    id("org.ajoberstar.grgit") version "5.0.0"
}

group = "net.htmlcsjs"
version = "0.3.0"
var projectBaseName = "CoffeeFloppa"
var mainClass = "net.htmlcsjs.coffeeFloppa.CoffeeFloppa"

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
    mainClass.set("net.htmlcsjs.coffeeFloppa.CoffeeFloppa")
    tasks.run.get().workingDir = File("run/")
}

dependencies {
    implementation("com.discord4j:discord4j-core:3.2.2")
    implementation("ch.qos.logback:logback-classic:1.2.11")
    implementation("com.googlecode.json-simple:json-simple:1.1.1")
    implementation("me.xdrop:fuzzywuzzy:1.4.0")
    implementation("org.luaj:luaj-jse:3.0.1")
    implementation("org.jetbrains:annotations:23.0.0")
    implementation("curse.maven:gtceu-557242:3808907")
    implementation("org.reflections:reflections:0.10.2")
    implementation("org.apache.commons:commons-text:1.9")

    testImplementation("org.junit.jupiter:junit-jupiter:5.8.2")
}

tasks.create<Delete> ("deleteGeneratedSources") {
    delete("$buildDir/sources/src/")
}

tasks.create<Copy> ("generateSource") {
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
    }
    compileTestJava {
        dependsOn("generateSource")
        setSource("$buildDir/sources/src/test")
    }
}


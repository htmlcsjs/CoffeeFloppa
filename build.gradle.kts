plugins {
    java
    application
}

group = "net.htmlcsjs"
version = "0.1.4"

var d4jVersion = "3.2.2"
var logbackVersion = "1.2.11"
var simpleJSONVersion = "1.1.1"
var fuzzywuzzyVersion = "1.4.0"

repositories {
    mavenCentral()
    maven {
        setUrl("https://mvnrepository.com/artifact")
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

    testImplementation("org.junit.jupiter:junit-jupiter:5.8.2")
}

tasks.withType<Jar> {
    manifest {
        attributes["Main-Class"] = "net.htmlcsjs.coffeeFloppa.CoffeeFloppa"
    }
}

tasks.withType<Test>() {
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



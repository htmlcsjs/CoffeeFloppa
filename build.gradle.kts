plugins {
    java
    application
}

group = "net.htmlcsjs"
version = "0.1"

var d4jVersion = "3.2.1"
var logbackVersion = "1.2.10"
var simpleJSONVersion = "4.0.0"

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
    implementation("com.discord4j:discord4j-core:$d4jVersion")
    implementation("ch.qos.logback:logback-classic:$logbackVersion")
    implementation("com.googlecode.json-simple:json-simple:1.1.1")
}

tasks.withType<Jar> {
    manifest {
        attributes["Main-Class"] = "net.htmlcsjs.coffeeFloppa.CoffeeFloppa"
    }
}

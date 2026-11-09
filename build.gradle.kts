plugins {
    java
}

group = "ir.gharri"
version = "1.0"

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.21.4-R0.1-SNAPSHOT")
    // loaded at runtime through "libraries:" in plugin.yml, so compileOnly is enough
    compileOnly("com.zaxxer:HikariCP:5.1.0")
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
    options.release.set(21)
}
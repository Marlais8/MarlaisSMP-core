plugins {
    java
}

group = "net.marlais"
version = "1.0.0-SNAPSHOT"

repositories {
    mavenCentral()
    // Репозиторий для получения API Minecraft-сервера
    maven("https://repo.papermc.io/repository/maven-public/")
}

dependencies {
    // Подключаем Paper API (версия 1.20.4 — отлично подходит для Java 17)
    compileOnly("io.papermc.paper:paper-api:1.20.4-R0.1-SNAPSHOT")

    // Наша библиотека JSON
    implementation("com.google.code.gson:gson:2.10.1")

    // JUnit 5 для тестов
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.10.2")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.10.2")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher:1.10.2")
}

tasks.test {
    useJUnitPlatform()
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}
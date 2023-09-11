import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("org.springframework.boot") version "3.1.3"
    id("io.spring.dependency-management") version "1.1.3"
    kotlin("jvm") version "1.8.22"
    kotlin("plugin.spring") version "1.8.22"
}

group = "com.example"
version = "0.0.1-SNAPSHOT"

java {
    sourceCompatibility = JavaVersion.VERSION_17
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-data-jdbc")
    implementation("org.springframework.boot:spring-boot-starter-thymeleaf")
    implementation("io.github.wimdeblauwe:htmx-spring-boot-thymeleaf:3.0.0")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.flywaydb:flyway-core")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jsoup:jsoup:1.16.1")
    testImplementation("io.github.ulfs:assertj-jsoup:0.1.4")
    testImplementation("org.junit.jupiter:junit-jupiter:5.9.2")
    testImplementation ("io.rest-assured:kotlin-extensions:5.3.1")
    testImplementation ("io.rest-assured:spring-mock-mvc:5.3.1")
    testImplementation ("io.rest-assured:json-path:5.3.1")
    testImplementation ("io.rest-assured:json-schema-validator:5.3.1")
    runtimeOnly("org.postgresql:postgresql")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.boot:spring-boot-testcontainers")
    testImplementation("org.testcontainers:junit-jupiter")
    testImplementation("org.testcontainers:postgresql")
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs += "-Xjsr305=strict"
        jvmTarget = "17"
    }
}

tasks.withType<Test> {
    testLogging{
        events("passed", "skipped", "failed")
    }
    useJUnitPlatform()
}

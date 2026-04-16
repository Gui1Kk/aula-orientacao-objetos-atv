val ktor_version = "2.3.8"
val kotlin_version = "1.9.22"
val kmongo_version = "4.11.0"
val koin_version = "3.5.3"
val logback_version = "1.4.14"

plugins {
    kotlin("jvm") version "1.9.22"
    kotlin("plugin.serialization") version "1.9.22"
    id("io.ktor.plugin") version "2.3.8"
    application
}

group = "br.com.filacidada"
version = "3.0.0"

application {
    mainClass.set("ApplicationKt")
}

repositories {
    mavenCentral()
}

dependencies {
    // ── Ktor Server ──
    implementation("io.ktor:ktor-server-core:$ktor_version")
    implementation("io.ktor:ktor-server-netty:$ktor_version")
    implementation("io.ktor:ktor-server-content-negotiation:$ktor_version")
    implementation("io.ktor:ktor-server-cors:$ktor_version")
    implementation("io.ktor:ktor-server-status-pages:$ktor_version")
    implementation("io.ktor:ktor-server-auth:$ktor_version")
    implementation("io.ktor:ktor-server-auth-jwt:$ktor_version")
    implementation("io.ktor:ktor-server-websockets:$ktor_version")

    // ── Serialização ──
    implementation("io.ktor:ktor-serialization-kotlinx-json:$ktor_version")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.2")

    // ── MongoDB (KMongo) ──
    implementation("org.litote.kmongo:kmongo:$kmongo_version")
    implementation("org.litote.kmongo:kmongo-serialization:$kmongo_version")

    // ── Koin (DI) ──
    implementation("io.insert-koin:koin-core:$koin_version")
    implementation("io.insert-koin:koin-ktor:$koin_version")
    implementation("io.insert-koin:koin-logger-slf4j:$koin_version")

    // ── JWT (Auth0) ──
    implementation("com.auth0:java-jwt:4.4.0")

    // ── BCrypt ──
    implementation("org.mindrot:jbcrypt:0.4")

    // ── Kotlin-Faker (dados fictícios para seed) ──
    implementation("io.github.serpro69:kotlin-faker:1.16.0")

    // ── Swagger / OpenAPI ──
    implementation("io.ktor:ktor-server-openapi:$ktor_version")
    implementation("io.ktor:ktor-server-swagger:$ktor_version")

    // ── E-mail (Jakarta Mail) ──
    implementation("com.sun.mail:jakarta.mail:2.0.1")

    // ── Logging ──
    implementation("ch.qos.logback:logback-classic:$logback_version")

    // ══════════════ TESTES ══════════════

    // ── Ktor Test ──
    testImplementation("io.ktor:ktor-server-test-host:$ktor_version")
    testImplementation("io.ktor:ktor-client-content-negotiation:$ktor_version")

    // ── Kotlin Test ──
    testImplementation("org.jetbrains.kotlin:kotlin-test:$kotlin_version")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5:$kotlin_version")

    // ── JUnit 5 ──
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.10.1")
    testImplementation("org.junit.jupiter:junit-jupiter-engine:5.10.1")

    // ── Koin Test ──
    testImplementation("io.insert-koin:koin-test:$koin_version")
    testImplementation("io.insert-koin:koin-test-junit5:$koin_version")

    // ── Mockk ──
    testImplementation("io.mockk:mockk:1.13.9")

    // ── Testcontainers (MongoDB para testes de integração) ──
    testImplementation("org.testcontainers:testcontainers:1.19.3")
    testImplementation("org.testcontainers:mongodb:1.19.3")
    testImplementation("org.testcontainers:junit-jupiter:1.19.3")
}

tasks.test {
    useJUnitPlatform()
}

tasks.register<JavaExec>("seed") {
    description = "Popula o banco MongoDB com dados de exemplo"
    group = "application"
    mainClass.set("DatabaseSeedKt")
    classpath = sourceSets["main"].runtimeClasspath
}

kotlin {
    jvmToolchain(17)
}


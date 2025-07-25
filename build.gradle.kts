plugins {
    java
    `jvm-test-suite`
}

repositories {
    mavenCentral()
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

dependencies {
    implementation("org.ow2.asm:asm:9.6")
    testImplementation("org.junit.jupiter:junit-jupiter:5.7.1")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.withType(JavaCompile::class.java).configureEach {
    options.encoding = "UTF-8"
    options.compilerArgs.add("-parameters")
}

tasks.named<Test>("test") {
    useJUnitPlatform()
}
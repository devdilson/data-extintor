plugins {
    application
    java;
    id("com.diffplug.spotless") version "6.22.0" apply true
}

repositories {
    mavenCentral()
}

dependencies {


    implementation("org.hibernate:hibernate-validator:8.0.1.Final")
    implementation("org.glassfish:jakarta.el:4.0.1")

    implementation("mysql:mysql-connector-java:8.0.33")

    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:2.15.3")

    implementation("com.google.guava:guava:32.1.1-jre")

    implementation("org.slf4j:slf4j-api:2.0.9")
    implementation("ch.qos.logback:logback-classic:1.4.11")

    testImplementation("org.junit.jupiter:junit-jupiter:5.9.3")

    testRuntimeOnly("org.junit.platform:junit-platform-launcher")


}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

configure<com.diffplug.gradle.spotless.SpotlessExtension> {
    java {
        googleJavaFormat()
    }
}

tasks.register<Jar>("fatJar") {

    manifest {
        attributes["Main-Class"] = "com.data.extintor.App"
    }
    duplicatesStrategy = DuplicatesStrategy.INCLUDE
    from(configurations.runtimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) })
    with(tasks.jar.get() as CopySpec)
}

tasks.named("build") {
    finalizedBy("fatJar")
}

tasks.named<JavaExec>("run") {
    args = listOf("--file=../application.yaml")
}

application {
    mainClass.set("com.data.extintor.App")
}

tasks.named<Test>("test") {
    useJUnitPlatform()
}


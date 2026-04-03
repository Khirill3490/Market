plugins {
    `java-library`
}

group = "ru.example"
version = "1.0-SNAPSHOT"

dependencies {
    api("jakarta.persistence:jakarta.persistence-api:3.1.0")

    compileOnly("org.projectlombok:lombok:1.18.38")
    annotationProcessor("org.projectlombok:lombok:1.18.38")
}

tasks.test {
    useJUnitPlatform()
}
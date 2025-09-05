plugins {
    id("java")
}

group = "ru.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {

}

tasks.test {
    useJUnitPlatform()
}
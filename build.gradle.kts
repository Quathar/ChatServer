plugins {
    id("java")
    id("io.freefair.lombok") version "8.0.1"
}

group = "com.quathar"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

//val versionJFX = "123.2.2";
dependencies {
    // Lombok
    compileOnly("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")

    // JavaFX
//    implementation("org.openjfx:javafx-controls:${versionJFX}")
//    implementation("org.openjfx:javafx-fxml:${versionJFX}")

    // JUnit
    testImplementation(platform("org.junit:junit-bom:5.9.1"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

tasks.test {
    useJUnitPlatform()
}
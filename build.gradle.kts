plugins {
    application // 'application' extends 'java' plugin
    id("io.freefair.lombok")       version "8.0.1"
    id("org.openjfx.javafxplugin") version "0.0.13"
}

group = "com.quathar"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

javafx {
    // To make the app works you have to go to
    // Run >> Edit Configurations
    // then go to the specific application and
    // click "Modify options" >> "Add VM options"
    // and paste this command in the new field that will appear
    // --module-path "{path to you javafx libraries (change this)}" --add-modules javafx.controls,javafx.fxml
    version = "19"
    modules("javafx.controls")
//    modules("javafx.controls", "javafx.graphics", "javafx.base", "javafx.fxml")
}

//application {
//    // Set the main class for the application
//    mainClass.set("com.quathar.chatserver.Test")
//}

dependencies {
    // Lombok
    compileOnly("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")

    // JUnit
    testImplementation(platform("org.junit:junit-bom:5.9.1"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

tasks.test {
    useJUnitPlatform()
}
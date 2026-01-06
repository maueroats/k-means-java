plugins {
    id("java")
    application
}

group = "info.wy-cs.docmo"
version = "0.5-WIP-SNAPSHOT"

repositories {
    mavenCentral()
}

// Define variables for LWJGL
val lwjglV = "3.3.6"
val os = System.getProperty("os.name").lowercase().let {
    when {
        it.contains("win") -> "windows"
        it.contains("mac") -> "macos"
        else -> "linux"
    }
}

dependencies {
    // Unit Testing
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    // LWJGL Core and Modules
    implementation("org.lwjgl:lwjgl:$lwjglV")
    implementation("org.lwjgl:lwjgl-glfw:$lwjglV")
    implementation("org.lwjgl:lwjgl-opengl:$lwjglV")

    // LWJGL Natives
    runtimeOnly("org.lwjgl:lwjgl:$lwjglV:natives-$os")
    runtimeOnly("org.lwjgl:lwjgl-glfw:$lwjglV:natives-$os")
    runtimeOnly("org.lwjgl:lwjgl-opengl:$lwjglV:natives-$os")
}

tasks.test {
    useJUnitPlatform()
}

application {
    mainClass.set("CentroidAnimation")
}
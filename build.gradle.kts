plugins {
    id("java")
    id("application")
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    // Milo 0.6.x 계열에서 안정적으로 사용되는 조합
    implementation("org.eclipse.milo:sdk-server:0.6.14")
    implementation("org.eclipse.milo:stack-server:0.6.14")

    implementation("org.slf4j:slf4j-simple:2.0.12")

    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

application {
    mainClass.set("org.example.Main")
}

tasks.withType<JavaCompile> {
    options.release.set(17)
}

tasks.test {
    useJUnitPlatform()
}

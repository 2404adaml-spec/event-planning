plugins {
    scala
    `java-library`
}

dependencies {
    implementation("org.scala-lang:scala-library:2.13.12")

    // Testing
    testImplementation("org.scalatest:scalatest_2.13:3.2.17")
    testImplementation("org.scalatestplus:junit-5-10_2.13:3.2.17.0")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.test {
    useJUnitPlatform()
}

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

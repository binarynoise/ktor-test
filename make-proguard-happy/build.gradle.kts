plugins {
    kotlin("jvm")
}

repositories {
    mavenCentral()
    google()
}

val javaVersion = JavaVersion.VERSION_1_8
val javaVersionNumber = javaVersion.name.substringAfter('_').replace('_', '.')
val javaVersionMajor = javaVersion.name.substringAfterLast('_')


tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions.jvmTarget = javaVersionNumber
}

tasks.withType<AbstractCompile> {
    sourceCompatibility = javaVersionNumber
    targetCompatibility = javaVersionNumber
}

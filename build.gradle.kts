import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.7.0"
    id("com.github.johnrengelman.shadow") version "7.1.0"
}

val javaVersion = JavaVersion.VERSION_17
val javaVersionNumber = javaVersion.name.substringAfter('_').replace('_', '.')
val javaVersionMajor = javaVersion.name.substringAfterLast('_')

val main = "de.binarynoise.ktorTest.Main"

repositories {
    mavenCentral()
    google()
}

val enableR8 = true

val r8: Configuration by configurations.creating

dependencies {
    val ktorVersion = "2.0.2"
    implementation("io.ktor:ktor-client-core:$ktorVersion")
    implementation("io.ktor:ktor-client-cio:$ktorVersion")
    implementation("io.ktor:ktor-client-java:$ktorVersion")
    implementation("org.slf4j:slf4j-simple:1.7.36")
    
    if (enableR8) {
        r8("com.android.tools:r8:3.3.28")
        compileOnly(project("make-proguard-happy"))
    }
}

java {
    modularity.inferModulePath.set(true)
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = javaVersionNumber
}

tasks.withType<AbstractCompile> {
    sourceCompatibility = javaVersionNumber
    targetCompatibility = javaVersionNumber
}

tasks.withType<Jar> {
    manifest {
        attributes(mapOf("Main-Class" to main))
    }
}

tasks.withType<ShadowJar> {
    archiveClassifier.set("shadow")
    mergeServiceFiles()
    if (!enableR8) minimize {
    
    }
}

if (enableR8) tasks.register<JavaExec>("shadowJarMinified") {
    dependsOn(configurations.runtimeClasspath)
    
    val proguardRules = file("src/main/proguard-rules.pro")
    inputs.files(tasks.shadowJar.get().outputs.files, proguardRules)
    
    val r8File = File("$buildDir/libs/${base.archivesName.get()}-shadow-minified.jar")
    outputs.file(r8File)
    
    classpath(r8)
    
    mainClass.set("com.android.tools.r8.R8")
    val javaHome = File(ProcessHandle.current().info().command().get()).parentFile.parentFile.canonicalPath
    val javaHomeVersion = Runtime.getRuntime().exec("$javaHome/bin/java -version").run {
        (inputStream.bufferedReader().readText() + errorStream.bufferedReader().readText()).split('"')[1]
    }
    check(JavaVersion.toVersion(javaHomeVersion).isCompatibleWith(javaVersion)) {
        "Incompatible Java Versions: compile-target $javaVersionNumber, r8 runtime $javaHomeVersion (needs to be as new or newer)"
    }
    
    val args = mutableListOf(
        "--debug",
        "--classfile",
        "--output",
        r8File.toString(),
        "--pg-conf",
        proguardRules.toString(),
        "--lib",
        file(File("make-proguard-happy/build/libs/make-proguard-happy.jar")).toString(),
        "--lib",
        javaHome,
    )
    args.add(tasks.shadowJar.get().outputs.files.joinToString(" "))
    
    this.args = args
    
    doFirst {
        check(proguardRules.exists()) { "$proguardRules doesn't exist" }
    }
}

tasks {
    build {
        dependsOn(shadowJar)
    }
}

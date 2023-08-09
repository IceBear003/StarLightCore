plugins {
    `java-library`
    `maven-publish`
    id("io.izzel.taboolib") version "1.56"
    id("org.jetbrains.kotlin.jvm") version "1.9.0"
}

taboolib {
    description {
        contributors {
            name("大白熊_IceBear")
            name("Andy_W7")
        }
        desc("StarLightCore 繁星工坊核心插件")
    }
    install("common")
    install("common-5")
    install("module-configuration")
    install("module-chat")
    install("module-database")
    install("module-ai")
    install("module-navigation")
    install("module-nms")
    install("module-nms-util")
    install("module-ui")
    install("module-effect")
    install("module-kether")
    install("platform-bukkit")
    install("expansion-command-helper")
    install("expansion-persistent-container")
    install("expansion-player-database")
    install("expansion-alkaid-redis")
    classifier = null
    version = "6.0.11-31"

    relocate("org.serverct.parrot.parrotx", "world.icebear03.starlight.taboolib.parrotx")
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("com.google.code.gson:gson:2.8.9")
    implementation(files("/lib/TrChat.jar"))

    compileOnly("ink.ptms:nms-all:1.0.0")
    compileOnly("ink.ptms.core:v12001:12001:mapped")
    compileOnly("ink.ptms.core:v12001:12001:universal")

    taboo("org.tabooproject.taboolib:module-parrotx:1.4.23")
    compileOnly(kotlin("stdlib"))
    compileOnly(fileTree("libs"))
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions {
        jvmTarget = "1.8"
        freeCompilerArgs = listOf("-Xjvm-default=all")
    }
}

configure<JavaPluginConvention> {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

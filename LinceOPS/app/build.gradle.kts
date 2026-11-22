import org.gradle.api.tasks.testing.Test
import org.gradle.testing.jacoco.plugins.JacocoTaskExtension
import org.gradle.testing.jacoco.tasks.JacocoReport
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("com.google.devtools.ksp") version "2.0.21-1.0.27"
    jacoco
}

val secretos = Properties().apply {
    listOf(
        rootProject.file("secrets.properties"),
        rootProject.file("local.properties")
    ).filter { it.exists() }.forEach { archivo ->
        archivo.inputStream().use { load(it) }
    }
}

val maptilerKey = (secretos.getProperty("MAPTILER_KEY")
    ?: System.getenv("MAPTILER_KEY")
    ?: "").trim().trim('"', '\'').trim()

val southboundApiKey = (System.getenv("SOUTHBOUND_API_KEY")
    ?: secretos.getProperty("SOUTHBOUND_API_KEY")
    ?: "").trim()

val releaseStoreFile = (secretos.getProperty("RELEASE_STORE_FILE") ?: "").trim()
val releaseStore = releaseStoreFile
    .takeIf { it.isNotEmpty() }
    ?.let { rootProject.file(it) }
    ?.takeIf { it.exists() }
val releaseStorePassword = secretos.getProperty("RELEASE_STORE_PASSWORD")
val releaseKeyAlias = secretos.getProperty("RELEASE_KEY_ALIAS")
val releaseKeyPassword = secretos.getProperty("RELEASE_KEY_PASSWORD")

android {
    namespace = "com.example.linceops"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.linceops"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        buildConfigField(
            "String",
            "MAPTILER_KEY",
            "\"${maptilerKey.replace("\\", "\\\\").replace("\"", "\\\"")}\""
        )
        buildConfigField(
            "String",
            "SOUTHBOUND_API_KEY",
            "\"${southboundApiKey.replace("\\", "\\\\").replace("\"", "\\\"")}\""
        )
    }

    signingConfigs {
        create("release") {
            releaseStore?.let { store ->
                storeFile = store
                storePassword = releaseStorePassword
                keyAlias = releaseKeyAlias
                keyPassword = releaseKeyPassword
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            signingConfig = releaseStore?.let { signingConfigs.getByName("release") }
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    testOptions {
        unitTests.isReturnDefaultValues = true
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {
    implementation("androidx.work:work-runtime-ktx:2.9.1")
    implementation("com.google.android.gms:play-services-location:21.0.1")
    implementation("org.osmdroid:osmdroid-android:6.1.20")
    implementation("dev.chrisbanes.haze:haze:0.7.3")
    val room_version = "2.6.1"
    implementation("androidx.room:room-runtime:$room_version")
    implementation("androidx.room:room-ktx:$room_version")
    ksp("androidx.room:room-compiler:$room_version")
    implementation("com.google.code.gson:gson:2.10.1")
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("androidx.navigation:navigation-compose:2.8.3")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.6")
    implementation("androidx.datastore:datastore-preferences:1.1.1")
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    testImplementation(libs.junit)
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.8.1")
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}

tasks.withType<Test>().configureEach {
    configure<JacocoTaskExtension> {
        isIncludeNoLocationClasses = true
        excludes = listOf("jdk.internal.*")
    }
}

tasks.register<JacocoReport>("jacocoTestReport") {
    group = "reporting"
    description = "Genera el reporte de cobertura de las pruebas unitarias JVM."
    dependsOn("testDebugUnitTest")

    reports {
        xml.required.set(true)
        html.required.set(true)
        xml.outputLocation.set(
            layout.buildDirectory.file("reports/jacoco/jacocoTestReport/jacocoTestReport.xml")
        )
        html.outputLocation.set(
            layout.buildDirectory.dir("reports/jacoco/jacocoTestReport/html")
        )
    }

    val exclusiones = listOf(
        "**/R.class",
        "**/R$*.class",
        "**/BuildConfig.*",
        "**/*_Impl.*",
        "**/ComposableSingletons*",
        "**/MainActivity*",
        "**/LinceOpsDatabase*",
        "**/data/sync/**",
        "**/utils/**",
        "**/network/**",
        "**/ui/**",
        "**/navigation/**"
    )

    classDirectories.setFrom(
        fileTree(layout.buildDirectory.dir("tmp/kotlin-classes/debug")) {
            exclude(exclusiones)
        },
        fileTree(layout.buildDirectory.dir("intermediates/javac/debug/classes")) {
            exclude(exclusiones)
        }
    )
    sourceDirectories.setFrom(files("src/main/java"))
    executionData.setFrom(
        fileTree(layout.buildDirectory) {
            include("jacoco/testDebugUnitTest.exec")
        }
    )
}

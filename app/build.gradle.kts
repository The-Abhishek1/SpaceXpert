import java.util.Properties

plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.kotlinAndroid)
    alias(libs.plugins.kotlinKapt)
    alias(libs.plugins.kotlinCompose)
    alias(libs.plugins.daggerHilt)
}

android {
    namespace = "com.xcloak.spacexpert"
    compileSdk = 37

    defaultConfig {
        applicationId = "com.xcloak.spacexpert"
        minSdk = 26
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"
    }

    signingConfigs {
        create("release") {
            val keystorePropertiesFile = rootProject.file("keystore.properties")
            if (keystorePropertiesFile.exists()) {
                val properties = Properties()
                properties.load(keystorePropertiesFile.inputStream())
                storeFile = rootProject.file(properties.getProperty("storeFile")!!)
                storePassword = properties.getProperty("storePassword")!!
                keyAlias = properties.getProperty("keyAlias")!!
                keyPassword = properties.getProperty("keyPassword")!!
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("release")
        }
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.lifecycle.runtime.ktx)

    implementation(libs.androidxRoomRuntime)
    implementation(libs.androidxRoomKtx)
    "kapt"(libs.androidxRoomCompiler)

    implementation(libs.hiltAndroid)
    "kapt"(libs.hiltCompiler)
    implementation(libs.hiltNavigationCompose)

    implementation(libs.hiltWork)
    "kapt"(libs.hiltWorkCompiler)
    implementation(libs.kotlinxCoroutinesAndroid)
    implementation(libs.androidxWorkRuntimeKtx)
    implementation(libs.androidxComposeMaterialIconsExtended)

    implementation(libs.googlePlayServicesAds)
    implementation(libs.playBillingKtx)

    implementation("androidx.security:security-crypto:1.1.0-alpha06")
    implementation("androidx.biometric:biometric:1.1.0")
    implementation("androidx.fragment:fragment-ktx:1.8.4")
}
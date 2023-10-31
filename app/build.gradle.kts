@Suppress("DSL_SCOPE_VIOLATION") // TODO: Remove once KTIJ-19369 is fixed
plugins {
    alias(libs.plugins.com.android.application)
    alias(libs.plugins.org.jetbrains.kotlin.android)
    alias(libs.plugins.hiddenApiRefine)
    alias(libs.plugins.ksp)
}

android {
    namespace = "io.sunshine0523.sidebar"
    compileSdk = 33

    buildFeatures {
        dataBinding = true
    }

    defaultConfig {
        applicationId = "io.sunshine0523.sidebar"
        minSdk = 29
        targetSdk = 33
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
}

configurations.configureEach {
    exclude(group = "androidx.appcompat", module = "appcompat")
}

dependencies {
    implementation(libs.core.ktx)
    implementation(libs.lifecycle.viewmodel.ktx)
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.preference.ktx)
    implementation(libs.room.runtime)
    implementation(libs.hiddenapibypass)
    implementation(libs.appiconloader)
    implementation(libs.rikka.recyclerview.adapter)
    implementation(libs.rikka.recyclerview.ktx)
    implementation(libs.rikka.borderview)
    ksp(libs.room.compiler)
    implementation(libs.room.ktx)
    compileOnly(files("libs/XposedBridgeAPI-89.jar"))
    compileOnly(projects.hiddenApi)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.espresso.core)
}
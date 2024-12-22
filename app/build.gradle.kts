import com.android.build.gradle.internal.cxx.configure.gradleLocalProperties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
}

val devKey = gradleLocalProperties(rootDir, providers).getProperty("devKey", "")
val apiUrl = gradleLocalProperties(rootDir, providers).getProperty("apiUrl", "")

android {
    namespace = "com.example.recipegpt"
    compileSdk = 35

    buildFeatures{
        viewBinding=true
        buildConfig=true
    }
    defaultConfig {
        buildConfigField ("String", "API_KEY", "\"${devKey}\"")
        buildConfigField ("String", "BASE_URL", "\"${apiUrl}\"")
        resValue("string", "apiUrl", "\"" + apiUrl + "\"")
        resValue("string", "devKey", "\"" + devKey + "\"")
        applicationId = "com.example.recipegpt"
        minSdk = 31
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
}

dependencies {
    implementation(libs.retrofit)
    implementation(libs.converter.gson)

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.room.common)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}
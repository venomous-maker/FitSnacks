plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.fitsnacks"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.fitsnacks"
        minSdk = 28
        targetSdk = 36
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
}

dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation("androidx.recyclerview:recyclerview:1.4.0")

    // Lifecycle / ViewModel
    implementation("androidx.lifecycle:lifecycle-viewmodel:2.10.0")
    implementation("androidx.lifecycle:lifecycle-livedata:2.10.0")

    // Room
    implementation("androidx.room:room-runtime:2.8.4")
    implementation(libs.room.common.jvm)
    annotationProcessor("androidx.room:room-compiler:2.8.4")
    implementation("androidx.room:room-rxjava2:2.8.4")

    // Ensure Kotlin stdlib is present so annotation processors can read Kotlin metadata from dependencies
    implementation("org.jetbrains.kotlin:kotlin-stdlib:1.9.20")
    // Add kotlin-reflect to improve Kotlin metadata parsing by annotation processors
    implementation("org.jetbrains.kotlin:kotlin-reflect:1.9.20")

    // Kotlin metadata helper for annotation processors (helps read Kotlin @Metadata in dependencies)
    implementation("org.jetbrains.kotlinx:kotlinx-metadata-jvm:0.6.0")

    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}
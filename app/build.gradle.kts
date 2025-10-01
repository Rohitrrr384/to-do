plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.bubbletodo"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.bubbletodo"
        minSdk = 24
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
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    implementation ("androidx.dynamicanimation:dynamicanimation:1.1.0")

    // For better UI components
    implementation (libs.coordinatorlayout)
    implementation ("androidx.viewpager2:viewpager2:1.1.0")
    implementation(libs.gson)


}
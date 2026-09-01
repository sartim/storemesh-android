plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.storemesh.android"
    compileSdk = 35
    buildFeatures { buildConfig = true }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    defaultConfig {
        applicationId = "com.storemesh.android"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "0.1.0"
        // Android emulators reach host localhost through 10.0.2.2.
        buildConfigField("String", "API_BASE_URL", "\"http://10.0.2.2:8080\"")
        buildConfigField("String", "KEYCLOAK_ISSUER", "\"http://10.0.2.2:8081/realms/storemesh\"")
        buildConfigField("String", "KEYCLOAK_CLIENT_ID", "\"storemesh-android\"")
    }
}

kotlin {
    jvmToolchain(17)
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.androidx.compose.material.icons)
    implementation(libs.appauth)
    debugImplementation(libs.androidx.compose.ui.tooling)
}

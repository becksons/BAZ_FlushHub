plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("kotlin-kapt")
    id("realm-android")
}

val tomtomApiKey: String by project

android {
    namespace = "com.example.tomtom"
    compileSdk = 34

    packaging {
        jniLibs.pickFirsts.add("lib/**/libc++_shared.so")
    }
    buildFeatures {
        viewBinding= true
    }
//    viewBinding {
//        enabled = true
//    }

    defaultConfig {
        applicationId = "com.example.flushhubproto"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
        multiDexEnabled = true

        vectorDrawables.useSupportLibrary = true

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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.8"
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }

    realm {
        isSyncEnabled = true
    }

    buildFeatures {
        buildConfig = true
        compose = true
    }
    buildTypes.configureEach {
        buildConfigField("String", "TOMTOM_API_KEY", "\"$tomtomApiKey\"")
    }
}

dependencies {

    implementation ("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
    implementation ("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
    implementation("com.google.android.material:material:1.11.0")

    implementation("com.google.android.gms:play-services-location:21.2.0")
    implementation("com.google.android.gms:play-services-maps:18.2.0")
    implementation ("androidx.fragment:fragment-ktx:1.6.2")
    implementation("androidx.fragment:fragment-ktx:1.6.2")
    implementation("androidx.navigation:navigation-fragment-ktx:2.7.7")
    implementation("androidx.navigation:navigation-ui-ktx:2.7.7")
    implementation ("com.google.android.gms:play-services-maps:18.2.0")
    implementation ("com.google.code.gson:gson:2.10.1")
    implementation("androidx.compose.foundation:foundation-layout-android:1.6.6")
    implementation("androidx.compose.animation:animation-core-android:1.6.6")
    implementation("androidx.compose.foundation:foundation-android:1.6.6")

    val version = "0.50.6"
    implementation("com.tomtom.sdk.routing:route-planner-online:1.0.0")
    implementation("com.tomtom.sdk.routing:range-calculator:1.0.0")
    implementation("com.tomtom.sdk.routing:model:1.0.0")
    implementation("com.tomtom.sdk.location:model:1.0.0")
    implementation("com.tomtom.sdk.vehicle:model:1.0.0")
    implementation("com.tomtom.quantity:quantity:1.0.0")
    implementation("com.tomtom.sdk.maps:map-display:$version")
    implementation("com.tomtom.sdk.location:provider-android:$version")
    implementation ("com.google.android.gms:play-services-maps:18.2.0")
    implementation("androidx.core:core-ktx:1.13.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    testImplementation("junit:junit:4.13.2")
    implementation ("androidx.compose.ui:ui:1.6.6")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    val fragment_version = "1.6.2"

    // Java language implementation
    implementation("androidx.fragment:fragment:$fragment_version")
    implementation("androidx.fragment:fragment-ktx:1.6.2")
    // Kotlin
    implementation("androidx.fragment:fragment-ktx:$fragment_version")
    // Testing Fragments in Isolation
    debugImplementation("androidx.fragment:fragment-testing:$fragment_version")

    //news api dependencies
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.retrofit2:retrofit:2.9.0")

    implementation ("androidx.compose.compiler:compiler:1.5.8")//keep at 1.5.8

    //jetpack compose
    implementation ("androidx.compose.material:material:1.6.6")
    implementation ("androidx.compose.ui:ui-tooling:1.6.6")

}
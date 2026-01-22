plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.devtools.ksp")
    id("com.google.dagger.hilt.android")
    id("kotlin-parcelize")
    // 👇 এই নতুন প্লাগিনটি যুক্ত করা হয়েছে (Kotlin 2.0 এর জন্য বাধ্যতামূলক)
    id("org.jetbrains.kotlin.plugin.compose")
}

android {
    namespace = "com.itpdf.app"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.itpdf.app"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }

        // ⚠️ WARNING: নিরাপত্তার স্বার্থে API KEY এখানে সরাসরি না রাখাই ভালো।
        // আপাতত বিল্ড ঠিক করার জন্য রাখলাম, কিন্তু পরে এটি GitHub Secrets ব্যবহার করে সরাবেন।
        buildConfigField("String", "GEMINI_API_KEY", "\"AIzaSyClB3oy5L_gkjJI0s6_ky12QjDBrnPcCmY\"")
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        debug {
            applicationIdSuffix = ".debug"
            isDebuggable = true
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    // 👇 এই ব্লকটি Kotlin 2.0 তে আর লাগে না, তাই মুছে ফেলা হয়েছে
    // composeOptions {
    //    kotlinCompilerExtensionVersion = "1.5.8"
    // }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
            excludes += "META-INF/DEPENDENCIES"
            excludes += "META-INF/LICENSE"
            excludes += "META-INF/LICENSE.txt"
            excludes += "META-INF/license.txt"
            excludes += "META-INF/NOTICE"
            excludes += "META-INF/NOTICE.txt"
            excludes += "META-INF/notice.txt"
            excludes += "META-INF/ASL2.0"
            excludes += "META-INF/*.kotlin_module"
        }
    }
}

dependencies {
    // Core Android & Kotlin
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.7.0")
    implementation("androidx.activity:activity-compose:1.8.2")

    // Jetpack Compose (Material 3)
    val composeBom = platform("androidx.compose:compose-bom:2024.02.00")
    implementation(composeBom)
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
    
    // Navigation
    implementation("androidx.navigation:navigation-compose:2.7.7")

    // Gemini AI SDK
    implementation("com.google.ai.client.generativeai:generativeai:0.9.0")

    // Dependency Injection - Hilt
    implementation("com.google.dagger:hilt-android:2.50")
    ksp("com.google.dagger:hilt-compiler:2.50")
    implementation("androidx.hilt:hilt-navigation-compose:1.1.0")

    // Room Database
    val roomVersion = "2.6.1"
    implementation("androidx.room:room-runtime:$roomVersion")
    implementation("androidx.room:room-ktx:$roomVersion")
    ksp("androidx.room:room-compiler:$roomVersion")

    // AdMob
    implementation("com.google.android.gms:play-services-ads:23.0.0")

    // Google Play Billing
    implementation("com.android.billingclient:billing-ktx:6.1.0")

    // PDF Generation & Viewing
    implementation("com.itextpdf:itext7-core:7.2.5")
    implementation("com.itextpdf:font-asian:7.2.5") 
    
    // Modern Android PDF Viewer
    implementation("com.github.barteksc:android-pdf-viewer:2.8.2")
    // Image Loading - Coil
    implementation("io.coil-kt:coil-compose:2.5.0")

    // Utilities
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
    implementation("com.google.code.gson:gson:2.10.1")

    // Testing
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation(composeBom)
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}

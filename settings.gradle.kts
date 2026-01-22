pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        
        // 👇 এই লাইনটিই সেই "বিশেষ দোকানের" ঠিকানা
        maven { url = uri("https://jitpack.io") }
    }
}

rootProject.name = "PDF-Project" // আপনার প্রজেক্টের নাম যদি আলাদা হয়, তবে এটি অটোমেটিক ঠিক থাকবে, হাত দেয়ার দরকার নেই।
include(":app")

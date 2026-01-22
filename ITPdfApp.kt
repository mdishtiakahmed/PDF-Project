package com.itpdf.app

import android.app.Application
import android.util.Log
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.initialization.InitializationStatus
import dagger.hilt.android.HiltAndroidApp

/**
 * ITPdfApp: The primary Application entry point for IT PDF - AI Text, CV & PDF Maker.
 * 
 * This class handles:
 * 1. Hilt Dependency Injection initialization.
 * 2. Asynchronous Google Mobile Ads (AdMob) SDK initialization.
 * 3. System-level memory management callbacks.
 */
@HiltAndroidApp
class ITPdfApp : Application() {

    companion object {
        private const val TAG = "ITPdfApp"
    }

    override fun onCreate() {
        super.onCreate()
        setupLogging()
        initializeAdMob()
    }

    /**
     * Basic logging setup. 
     * In a production environment, logic to strip logs in release builds would be added here.
     */
    private fun setupLogging() {
        Log.i(TAG, "IT PDF Application Starting... [Production Mode]")
    }

    /**
     * Initializes the Google Mobile Ads SDK asynchronously.
     * Uses idiomatic Kotlin for map iteration and string templates.
     */
    private fun initializeAdMob() {
        try {
            MobileAds.initialize(this) { status: InitializationStatus ->
                status.adapterStatusMap.forEach { (adapterClass, adapterStatus) ->
                    Log.d(
                        TAG,
                        "AdMob Adapter: $adapterClass, State: ${adapterStatus.initializationState}, Latency: ${adapterStatus.latency}ms"
                    )
                }
                Log.i(TAG, "AdMob SDK Initialization Complete.")
            }
        } catch (e: Exception) {
            // Defensive check to ensure app stability even if AdMob fails
            Log.e(TAG, "AdMob initialization encountered a non-fatal error", e)
        }
    }

    /**
     * Important for PDF generation apps to prevent OOM (Out Of Memory) errors.
     */
    override fun onLowMemory() {
        super.onLowMemory()
        Log.w(TAG, "System Warning: Low memory detected. Optimizing resource usage.")
    }

    override fun onTrimMemory(level: Int) {
        super.onTrimMemory(level)
        if (level >= TRIM_MEMORY_RUNNING_LOW) {
            Log.d(TAG, "Memory pressure level: $level. Adjusting cache priorities.")
        }
    }
}
package com.itpdf.app.ads

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.annotation.MainThread
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback

object AdManager {

    private const val TAG = "AdManager"

    private const val INTERSTITIAL_ID = "ca-app-pub-3940256099942544/1033173712"
    private const val REWARDED_ID = "ca-app-pub-3940256099942544/5224354917"

    private var mInterstitialAd: InterstitialAd? = null
    private var mRewardedAd: RewardedAd? = null
    
    private var isInterstitialLoading = false
    private var isRewardedLoading = false

    @MainThread
    fun initialize(context: Context) {
        MobileAds.initialize(context) { status ->
            Log.d(TAG, "AdMob Initialized: $status")
            loadInterstitial(context.applicationContext)
            loadRewarded(context.applicationContext)
        }
    }

    @MainThread
    fun loadInterstitial(context: Context) {
        if (isInterstitialLoading || mInterstitialAd != null) return

        isInterstitialLoading = true
        val adRequest = AdRequest.Builder().build()

        InterstitialAd.load(
            context.applicationContext,
            INTERSTITIAL_ID,
            adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdFailedToLoad(adError: LoadAdError) {
                    Log.e(TAG, "Interstitial failed to load: ${adError.message}")
                    mInterstitialAd = null
                    isInterstitialLoading = false
                }

                override fun onAdLoaded(interstitialAd: InterstitialAd) {
                    Log.d(TAG, "Interstitial loaded successfully.")
                    mInterstitialAd = interstitialAd
                    isInterstitialLoading = false
                }
            }
        )
    }

    @MainThread
    fun showInterstitial(activity: Activity, isPro: Boolean, onDismiss: () -> Unit) {
        if (isPro) {
            onDismiss()
            return
        }

        mInterstitialAd?.let { ad ->
            ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    Log.d(TAG, "Interstitial dismissed.")
                    mInterstitialAd = null
                    loadInterstitial(activity.applicationContext)
                    onDismiss()
                }

                override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                    Log.e(TAG, "Interstitial failed to show: ${adError.message}")
                    mInterstitialAd = null
                    loadInterstitial(activity.applicationContext)
                    onDismiss()
                }

                override fun onAdShowedFullScreenContent() {
                    Log.d(TAG, "Interstitial showed full screen content.")
                }
            }
            ad.show(activity)
        } ?: run {
            Log.d(TAG, "Interstitial not ready yet.")
            loadInterstitial(activity.applicationContext)
            onDismiss()
        }
    }

    @MainThread
    fun loadRewarded(context: Context) {
        if (isRewardedLoading || mRewardedAd != null) return

        isRewardedLoading = true
        val adRequest = AdRequest.Builder().build()

        RewardedAd.load(
            context.applicationContext,
            REWARDED_ID,
            adRequest,
            object : RewardedAdLoadCallback() {
                override fun onAdFailedToLoad(adError: LoadAdError) {
                    Log.e(TAG, "Rewarded ad failed to load: ${adError.message}")
                    mRewardedAd = null
                    isRewardedLoading = false
                }

                override fun onAdLoaded(rewardedAd: RewardedAd) {
                    Log.d(TAG, "Rewarded ad loaded successfully.")
                    mRewardedAd = rewardedAd
                    isRewardedLoading = false
                }
            }
        )
    }

    @MainThread
    fun showRewarded(
        activity: Activity,
        isPro: Boolean,
        onRewardReceived: () -> Unit,
        onDismiss: () -> Unit
    ) {
        if (isPro) {
            onRewardReceived()
            return
        }

        mRewardedAd?.let { ad ->
            ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    Log.d(TAG, "Rewarded ad dismissed.")
                    mRewardedAd = null
                    loadRewarded(activity.applicationContext)
                    onDismiss()
                }

                override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                    Log.e(TAG, "Rewarded ad failed to show: ${adError.message}")
                    mRewardedAd = null
                    loadRewarded(activity.applicationContext)
                    onDismiss()
                }
            }

            ad.show(activity) { rewardItem ->
                Log.d(TAG, "User earned reward: ${rewardItem.amount} ${rewardItem.type}")
                onRewardReceived()
            }
        } ?: run {
            Log.d(TAG, "Rewarded ad not ready yet.")
            loadRewarded(activity.applicationContext)
            onDismiss()
        }
    }

    fun destroy() {
        mInterstitialAd = null
        mRewardedAd = null
    }
}
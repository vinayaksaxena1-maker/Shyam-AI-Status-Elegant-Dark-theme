package com.example.ads

import android.app.Activity
import android.content.Context
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback

object InterstitialManager {
    private const val TAG = "InterstitialManager"

    private var mInterstitialAd: InterstitialAd? = null
    private var isLoading = false

    // Analytics Callbacks
    var onAdLoaded: (() -> Unit)? = null
    var onAdFailedToLoad: ((LoadAdError) -> Unit)? = null
    var onAdShowed: (() -> Unit)? = null
    var onAdFailedToShow: ((AdError) -> Unit)? = null
    var onAdDismissed: (() -> Unit)? = null
    var onAdClicked: (() -> Unit)? = null

    /**
     * Preloads an Interstitial Ad in the background.
     */
    fun preload(context: Context) {
        if (!AdConfig.adsEnabled) return
        if (mInterstitialAd != null || isLoading) return

        isLoading = true
        android.util.Log.i(TAG, "Preloading Interstitial Ad...")

        val adRequest = AdRequest.Builder().build()
        InterstitialAd.load(
            context.applicationContext,
            AdConfig.interstitialAdUnitId,
            adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(interstitialAd: InterstitialAd) {
                    isLoading = false
                    mInterstitialAd = interstitialAd
                    android.util.Log.i(TAG, "Interstitial Ad Loaded successfully")
                    onAdLoaded?.invoke()
                }

                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    isLoading = false
                    mInterstitialAd = null
                    android.util.Log.e(TAG, "Interstitial Ad Failed to Load: ${loadAdError.message}")
                    onAdFailedToLoad?.invoke(loadAdError)
                }
            }
        )
    }

    /**
     * Checks if an ad is preloaded and ready to be shown.
     */
    fun isAdReady(): Boolean {
        return mInterstitialAd != null
    }

    /**
     * Shows the interstitial ad if ready and compliance parameters allow.
     * @param activity The current visible Activity to launch full screen content.
     * @param forceIgnoreFrequency If true, skips frequency checks (e.g., HD Export is critical and might force ad).
     * @param onAdClosedCallback Invoked when the ad finishes displaying or fails to display so flow continues.
     */
    fun showIfReady(
        activity: Activity,
        forceIgnoreFrequency: Boolean = false,
        onAdClosedCallback: () -> Unit
    ) {
        if (!AdConfig.adsEnabled) {
            onAdClosedCallback()
            return
        }

        // Frequency/Cooldown checks
        val allowedByFrequency = forceIgnoreFrequency || AdConfig.shouldShowInterstitial()

        if (!allowedByFrequency) {
            android.util.Log.i(TAG, "Interstitial show blocked by frequency or cooldown configuration")
            onAdClosedCallback()
            return
        }

        val ad = mInterstitialAd
        if (ad == null) {
            android.util.Log.w(TAG, "Interstitial Ad show requested but ad was not preloaded")
            preload(activity) // Preload for next time
            onAdClosedCallback()
            return
        }

        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdShowedFullScreenContent() {
                android.util.Log.i(TAG, "Interstitial Ad displayed full screen")
                AdConfig.recordInterstitialShown()
                onAdShowed?.invoke()
            }

            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                android.util.Log.e(TAG, "Interstitial Ad failed to display: ${adError.message}")
                mInterstitialAd = null
                preload(activity) // Preload next one
                onAdFailedToShow?.invoke(adError)
                onAdClosedCallback()
            }

            override fun onAdDismissedFullScreenContent() {
                android.util.Log.i(TAG, "Interstitial Ad dismissed by user")
                mInterstitialAd = null
                preload(activity) // Preload next one
                onAdDismissed?.invoke()
                onAdClosedCallback()
            }

            override fun onAdClicked() {
                super.onAdClicked()
                android.util.Log.i(TAG, "Interstitial Ad Clicked")
                onAdClicked?.invoke()
            }
        }

        ad.show(activity)
    }

    /**
     * Clears reference of preloaded ad to prevent context/memory leaks.
     */
    fun destroy() {
        mInterstitialAd = null
        isLoading = false
    }
}

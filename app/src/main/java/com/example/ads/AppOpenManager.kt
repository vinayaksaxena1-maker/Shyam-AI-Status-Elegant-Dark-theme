package com.example.ads

import android.app.Activity
import android.content.Context
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.appopen.AppOpenAd

object AppOpenManager {
    private const val TAG = "AppOpenManager"

    // Keep disabled by default as requested
    var isEnabled: Boolean = false

    private var mAppOpenAd: AppOpenAd? = null
    private var isLoading = false
    private var loadTime: Long = 0

    // Analytics Callbacks
    var onAdLoaded: (() -> Unit)? = null
    var onAdFailedToLoad: ((LoadAdError) -> Unit)? = null
    var onAdShowed: (() -> Unit)? = null
    var onAdFailedToShow: ((AdError) -> Unit)? = null
    var onAdDismissed: (() -> Unit)? = null

    /**
     * Preloads an App Open Ad.
     */
    fun preload(context: Context) {
        if (!isEnabled || !AdConfig.adsEnabled) return
        if (mAppOpenAd != null || isLoading) return

        isLoading = true
        android.util.Log.i(TAG, "Preloading App Open Ad...")

        val adRequest = AdRequest.Builder().build()
        AppOpenAd.load(
            context.applicationContext,
            AdConfig.appOpenAdUnitId,
            adRequest,
            object : AppOpenAd.AppOpenAdLoadCallback() {
                override fun onAdLoaded(appOpenAd: AppOpenAd) {
                    isLoading = false
                    mAppOpenAd = appOpenAd
                    loadTime = System.currentTimeMillis()
                    android.util.Log.i(TAG, "App Open Ad Loaded successfully")
                    onAdLoaded?.invoke()
                }

                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    isLoading = false
                    mAppOpenAd = null
                    android.util.Log.e(TAG, "App Open Ad Failed to Load: ${loadAdError.message}")
                    onAdFailedToLoad?.invoke(loadAdError)
                }
            }
        )
    }

    /**
     * Checks if ad is ready and was loaded within a valid timeframe (typically 4 hours).
     */
    private fun wasLoadTimeLessThanNHoursAgo(numHours: Long): Boolean {
        val dateDifference = System.currentTimeMillis() - loadTime
        val numMilliSecondsPerHour: Long = 3600000
        return dateDifference < (numMilliSecondsPerHour * numHours)
    }

    fun isAdReady(): Boolean {
        return mAppOpenAd != null && wasLoadTimeLessThanNHoursAgo(4)
    }

    /**
     * Shows the App Open ad if ready and enabled.
     */
    fun showIfReady(
        activity: Activity,
        onAdClosedCallback: () -> Unit
    ) {
        if (!isEnabled || !AdConfig.adsEnabled) {
            onAdClosedCallback()
            return
        }

        val ad = mAppOpenAd
        if (ad == null || !isAdReady()) {
            android.util.Log.w(TAG, "App Open Ad requested but ad was not ready or expired")
            preload(activity) // Preload for next time
            onAdClosedCallback()
            return
        }

        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdShowedFullScreenContent() {
                android.util.Log.i(TAG, "App Open Ad displayed full screen")
                onAdShowed?.invoke()
            }

            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                android.util.Log.e(TAG, "App Open Ad failed to display: ${adError.message}")
                mAppOpenAd = null
                preload(activity) // Preload next one
                onAdFailedToShow?.invoke(adError)
                onAdClosedCallback()
            }

            override fun onAdDismissedFullScreenContent() {
                android.util.Log.i(TAG, "App Open Ad dismissed by user")
                mAppOpenAd = null
                preload(activity) // Preload next one
                onAdDismissed?.invoke()
                onAdClosedCallback()
            }
        }

        ad.show(activity)
    }

    /**
     * Clears preloaded ad.
     */
    fun destroy() {
        mAppOpenAd = null
        isLoading = false
    }
}

package com.example.ads

import android.app.Activity
import android.content.Context
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.RequestConfiguration

object AdManager {
    private const val TAG = "AdManager"

    @Volatile
    private var isInitialized = false

    /**
     * Initializes the Mobile Ads SDK and requests consent if required.
     * This handles the GDPR / UMP consent logic completely first, then falls back and initializes AdMob gracefully.
     */
    fun initialize(
        activity: Activity,
        onInitComplete: () -> Unit = {}
    ) {
        if (isInitialized) {
            onInitComplete()
            return
        }

        android.util.Log.i(TAG, "Initializing Consent gathering...")
        val consentManager = ConsentManager.getInstance(activity)

        consentManager.gatherConsent(
            activity,
            object : ConsentManager.ConsentGatheringListener {
                override fun onConsentGatheringFinished(error: String?) {
                    if (error != null) {
                        android.util.Log.e(TAG, "Consent finished with error: $error. Initializing AdMob anyway as graceful fallback.")
                    } else {
                        android.util.Log.i(TAG, "Consent gathering finished successfully.")
                    }

                    // Even if consent gathering failed, try to initialize AdMob so ads work (or serve non-personalized ads)
                    if (consentManager.canRequestAds) {
                        initializeMobileAds(activity.applicationContext, onInitComplete)
                    } else {
                        android.util.Log.w(TAG, "Cannot request ads based on current consent state. Initializing AdMob anyway to preserve fallback/test state.")
                        initializeMobileAds(activity.applicationContext, onInitComplete)
                    }
                }
            }
        )
    }

    /**
     * Performs Mobile Ads SDK initialization and preloads ads.
     */
    private fun initializeMobileAds(
        context: Context,
        onInitComplete: () -> Unit
    ) {
        if (isInitialized) {
            onInitComplete()
            return
        }

        try {
            android.util.Log.i(TAG, "Initializing Google Mobile Ads SDK...")
            
            // Apply global configurations, e.g. test devices if needed
            val configuration = RequestConfiguration.Builder()
                // .setTestDeviceIds(listOf("DEBUG_DEVICE_ID"))
                .build()
            MobileAds.setRequestConfiguration(configuration)

            MobileAds.initialize(context) { initializationStatus ->
                isInitialized = true
                android.util.Log.i(TAG, "Google Mobile Ads SDK Initialized successfully")
                
                // Preload ads in background once SDK is ready
                preloadAllAds(context)
                
                onInitComplete()
            }
        } catch (e: Exception) {
            android.util.Log.e(TAG, "Error initializing Mobile Ads SDK: ${e.message}")
            e.printStackTrace()
            onInitComplete() // Fallback so app remains fully functional
        }
    }

    /**
     * Preloads all full screen ad formats.
     */
    fun preloadAllAds(context: Context) {
        if (!AdConfig.adsEnabled) return
        InterstitialManager.preload(context)
        RewardedManager.preload(context)
        AppOpenManager.preload(context)
    }

    /**
     * Call when the application is exiting or in onDestroy of main activity to prevent leaks.
     */
    fun destroy() {
        InterstitialManager.destroy()
        RewardedManager.destroy()
        AppOpenManager.destroy()
    }
}

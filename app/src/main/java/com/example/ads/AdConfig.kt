package com.example.ads

import com.example.BuildConfig

object AdConfig {
    // Enable or disable all ads globally
    var adsEnabled: Boolean = true

    // Test Ad Unit IDs provided by Google
    private const val TEST_BANNER_ID = "ca-app-pub-3940256099942544/9214920333"
    private const val TEST_INTERSTITIAL_ID = "ca-app-pub-3940256099942544/1033173712"
    private const val TEST_REWARDED_ID = "ca-app-pub-3940256099942544/5224354917"
    private const val TEST_APP_OPEN_ID = "ca-app-pub-3940256099942544/9257395921"

    // Production Ad Unit IDs (Defaults to test, can be configured or updated dynamically)
    var PROD_BANNER_ID = TEST_BANNER_ID
    var PROD_INTERSTITIAL_ID = TEST_INTERSTITIAL_ID
    var PROD_REWARDED_ID = TEST_REWARDED_ID
    var PROD_APP_OPEN_ID = TEST_APP_OPEN_ID

    // Select ID based on Build Type
    val bannerAdUnitId: String
        get() = if (BuildConfig.DEBUG) TEST_BANNER_ID else PROD_BANNER_ID

    val interstitialAdUnitId: String
        get() = if (BuildConfig.DEBUG) TEST_INTERSTITIAL_ID else PROD_INTERSTITIAL_ID

    val rewardedAdUnitId: String
        get() = if (BuildConfig.DEBUG) TEST_REWARDED_ID else PROD_REWARDED_ID

    val appOpenAdUnitId: String
        get() = if (BuildConfig.DEBUG) TEST_APP_OPEN_ID else PROD_APP_OPEN_ID

    // Control parameters for Interstitials
    var interstitialClickInterval = 3 // Show interstitial after every N successful triggers
    private var actionTriggerCount = 0

    // Cooldown in milliseconds to prevent back-to-back intrusive ads
    var interstitialCooldownMillis = 45 * 1000L // 45 seconds
    private var lastInterstitialShowTime: Long = 0

    /**
     * Increments action count and checks if interstitial should be shown.
     */
    fun shouldShowInterstitial(): Boolean {
        if (!adsEnabled) return false
        
        actionTriggerCount++
        val currentTime = System.currentTimeMillis()
        val isCooldownPassed = (currentTime - lastInterstitialShowTime) >= interstitialCooldownMillis
        val isIntervalReached = actionTriggerCount % interstitialClickInterval == 0

        return isIntervalReached && isCooldownPassed
    }

    /**
     * Updates the last shown time of interstitial.
     */
    fun recordInterstitialShown() {
        lastInterstitialShowTime = System.currentTimeMillis()
    }
}

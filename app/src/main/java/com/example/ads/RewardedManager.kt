package com.example.ads

import android.app.Activity
import android.content.Context
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.rewarded.RewardItem
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback

object RewardedManager {
    private const val TAG = "RewardedManager"

    private var mRewardedAd: RewardedAd? = null
    private var isLoading = false

    // Analytics Callbacks
    var onAdLoaded: (() -> Unit)? = null
    var onAdFailedToLoad: ((LoadAdError) -> Unit)? = null
    var onAdShowed: (() -> Unit)? = null
    var onAdFailedToShow: ((AdError) -> Unit)? = null
    var onAdDismissed: (() -> Unit)? = null
    var onAdClicked: (() -> Unit)? = null
    var onRewardEarned: ((RewardItem) -> Unit)? = null

    /**
     * Preloads a Rewarded Ad in the background.
     */
    fun preload(context: Context) {
        if (!AdConfig.adsEnabled) return
        if (mRewardedAd != null || isLoading) return

        isLoading = true
        android.util.Log.i(TAG, "Preloading Rewarded Ad...")

        val adRequest = AdRequest.Builder().build()
        RewardedAd.load(
            context.applicationContext,
            AdConfig.rewardedAdUnitId,
            adRequest,
            object : RewardedAdLoadCallback() {
                override fun onAdLoaded(rewardedAd: RewardedAd) {
                    isLoading = false
                    mRewardedAd = rewardedAd
                    android.util.Log.i(TAG, "Rewarded Ad Loaded successfully")
                    onAdLoaded?.invoke()
                }

                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    isLoading = false
                    mRewardedAd = null
                    android.util.Log.e(TAG, "Rewarded Ad Failed to Load: ${loadAdError.message}")
                    onAdFailedToLoad?.invoke(loadAdError)
                }
            }
        )
    }

    /**
     * Checks if a rewarded ad is loaded and ready to be shown.
     */
    fun isAdReady(): Boolean {
        return mRewardedAd != null
    }

    /**
     * Shows the rewarded ad if loaded and triggers callbacks on success/dismiss/reward earned.
     * @param activity The current visible Activity to launch full screen content.
     * @param onUserEarnedReward Triggered immediately when the user finishes watching and earns the reward item.
     * @param onAdClosedCallback Triggered when the ad is closed or fails to display so flow continues.
     */
    fun showIfReady(
        activity: Activity,
        onUserEarnedReward: (RewardItem) -> Unit,
        onAdClosedCallback: () -> Unit
    ) {
        val ad = mRewardedAd
        if (ad == null) {
            android.util.Log.w(TAG, "Rewarded Ad show requested but ad was not preloaded")
            preload(activity) // Preload for next time
            onAdClosedCallback()
            return
        }

        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdShowedFullScreenContent() {
                android.util.Log.i(TAG, "Rewarded Ad displayed full screen")
                onAdShowed?.invoke()
            }

            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                android.util.Log.e(TAG, "Rewarded Ad failed to display: ${adError.message}")
                mRewardedAd = null
                preload(activity) // Preload next one
                onAdFailedToShow?.invoke(adError)
                onAdClosedCallback()
            }

            override fun onAdDismissedFullScreenContent() {
                android.util.Log.i(TAG, "Rewarded Ad dismissed by user")
                mRewardedAd = null
                preload(activity) // Preload next one
                onAdDismissed?.invoke()
                onAdClosedCallback()
            }

            override fun onAdClicked() {
                super.onAdClicked()
                android.util.Log.i(TAG, "Rewarded Ad Clicked")
                onAdClicked?.invoke()
            }
        }

        ad.show(activity) { rewardItem ->
            android.util.Log.i(TAG, "User earned reward: amount=${rewardItem.amount}, type=${rewardItem.type}")
            onRewardEarned?.invoke(rewardItem)
            onUserEarnedReward(rewardItem)
        }
    }

    /**
     * Clears reference of preloaded ad to prevent context/memory leaks.
     */
    fun destroy() {
        mRewardedAd = null
        isLoading = false
    }
}

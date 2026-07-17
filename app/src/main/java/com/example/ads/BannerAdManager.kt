package com.example.ads

import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.FrameLayout
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.*

object BannerAdManager {
    private const val TAG = "BannerAdManager"

    // Callback listeners for client analytical tracking
    var onAdLoaded: (() -> Unit)? = null
    var onAdFailedToLoad: ((LoadAdError) -> Unit)? = null
    var onAdClicked: (() -> Unit)? = null

    /**
     * Composable function to display an Adaptive Banner Ad.
     */
    @Composable
    fun BannerAdView(
        modifier: Modifier = Modifier,
        adUnitId: String = AdConfig.bannerAdUnitId
    ) {
        val context = LocalContext.current
        val isLocalInspection = LocalInspectionMode.current

        // If in compose preview or ads are disabled, render a dummy view or nothing
        if (isLocalInspection || !AdConfig.adsEnabled) {
            androidx.compose.foundation.layout.Box(
                modifier = modifier
                    .fillMaxWidth()
                    .height(50.dp),
                contentAlignment = androidx.compose.ui.Alignment.Center
            ) {
                androidx.compose.material3.Text(
                    text = "Ad Banner Placeholder",
                    style = androidx.compose.material3.MaterialTheme.typography.labelMedium,
                    color = androidx.compose.ui.graphics.Color.Gray
                )
            }
            return
        }

        val adView = remember {
            AdView(context).apply {
                this.adUnitId = adUnitId
                // Get adaptive ad size
                val adSize = getAdaptiveAdSize(context)
                setAdSize(adSize)
                
                adListener = object : AdListener() {
                    override fun onAdLoaded() {
                        super.onAdLoaded()
                        android.util.Log.i(TAG, "Banner Loaded")
                        onAdLoaded?.invoke()
                    }

                    override fun onAdFailedToLoad(error: LoadAdError) {
                        super.onAdFailedToLoad(error)
                        android.util.Log.e(TAG, "Banner Failed to Load: ${error.message}")
                        onAdFailedToLoad?.invoke(error)
                    }

                    override fun onAdClicked() {
                        super.onAdClicked()
                        android.util.Log.i(TAG, "Banner Clicked")
                        onAdClicked?.invoke()
                    }
                }
            }
        }

        // Trigger load whenever adView is composed
        LaunchedEffect(adView) {
            val adRequest = AdRequest.Builder().build()
            adView.loadAd(adRequest)
        }

        // Properly manage lifecycle and destroy adView when removed from composition
        DisposableEffect(adView) {
            onDispose {
                try {
                    adView.destroy()
                    android.util.Log.i(TAG, "Banner Destroyed")
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }

        // Embed Android View in Jetpack Compose safely
        AndroidView(
            modifier = modifier
                .fillMaxWidth()
                .wrapContentHeight(),
            factory = { context ->
                // Wrap in FrameLayout to handle sizing constraints robustly
                FrameLayout(context).apply {
                    addView(adView)
                }
            }
        )
    }

    /**
     * Calculates the dynamic adaptive banner size anchored to current orientation.
     */
    private fun getAdaptiveAdSize(context: Context): AdSize {
        val displayMetrics = context.resources.displayMetrics
        val adWidthPixels = displayMetrics.widthPixels
        val density = displayMetrics.density
        val adWidthDp = (adWidthPixels / density).toInt()
        return AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(context, adWidthDp)
    }
}

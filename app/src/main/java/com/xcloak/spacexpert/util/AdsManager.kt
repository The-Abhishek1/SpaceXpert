package com.xcloak.spacexpert.util

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.xcloak.spacexpert.BuildConfig

object AdsManager {
    // Production IDs from AdMob Console Screenshot
    private const val PROD_BANNER_ID = "ca-app-pub-5043960817552570/5549390824"
    private const val PROD_INTERSTITIAL_ID = "ca-app-pub-5043960817552570/6726741129"
    private const val PROD_REWARDED_ID = "ca-app-pub-5043960817552570/4450088262"

    // Google Official Test IDs
    private const val TEST_BANNER_ID = "ca-app-pub-3940256099942544/6300978111"
    private const val TEST_INTERSTITIAL_ID = "ca-app-pub-3940256099942544/1033173712"
    private const val TEST_REWARDED_ID = "ca-app-pub-3940256099942544/5224354917"

    val bannerAdUnitId: String
        get() = if (BuildConfig.DEBUG) TEST_BANNER_ID else PROD_BANNER_ID

    val interstitialAdUnitId: String
        get() = if (BuildConfig.DEBUG) TEST_INTERSTITIAL_ID else PROD_INTERSTITIAL_ID

    val rewardedAdUnitId: String
        get() = if (BuildConfig.DEBUG) TEST_REWARDED_ID else PROD_REWARDED_ID
}

@Composable
fun BannerAdView(modifier: Modifier = Modifier) {
    val isPro by BillingManager.isProActive.collectAsState()
    if (isPro) return

    AndroidView(
        modifier = modifier.fillMaxWidth(),
        factory = { context ->
            AdView(context).apply {
                setAdSize(AdSize.BANNER)
                adUnitId = AdsManager.bannerAdUnitId
                loadAd(AdRequest.Builder().build())
            }
        },
        update = { adView ->
            adView.loadAd(AdRequest.Builder().build())
        }
    )
}

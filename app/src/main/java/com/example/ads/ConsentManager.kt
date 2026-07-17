package com.example.ads

import android.app.Activity
import android.content.Context
import com.google.android.ump.ConsentDebugSettings
import com.google.android.ump.ConsentInformation
import com.google.android.ump.ConsentRequestParameters
import com.google.android.ump.UserMessagingPlatform

class ConsentManager private constructor(context: Context) {
    private val consentInformation: ConsentInformation =
        UserMessagingPlatform.getConsentInformation(context.applicationContext)

    /**
     * Helper variable to determine if ads can be initialized.
     */
    val canRequestAds: Boolean
        get() = consentInformation.canRequestAds()

    /**
     * Interface definition for consent gathering callback.
     */
    interface ConsentGatheringListener {
        fun onConsentGatheringFinished(error: String?)
    }

    /**
     * Requests consent information and displays a consent form if required.
     */
    fun gatherConsent(
        activity: Activity,
        listener: ConsentGatheringListener
    ) {
        // For development/debugging, we can specify debug settings if needed.
        val debugSettings = ConsentDebugSettings.Builder(activity)
            // .setDebugGeography(ConsentDebugSettings.DebugGeography.DEBUG_GEOGRAPHY_EEA)
            .build()

        val params = ConsentRequestParameters.Builder()
            .setConsentDebugSettings(debugSettings)
            .build()

        consentInformation.requestConsentInfoUpdate(
            activity,
            params,
            {
                UserMessagingPlatform.loadAndShowConsentFormIfRequired(activity) { formError ->
                    if (formError != null) {
                        android.util.Log.e(TAG, "Consent form show failed: ${formError.message} (code: ${formError.errorCode})")
                        // Fall back gracefully so the user is not blocked
                        listener.onConsentGatheringFinished(formError.message)
                    } else {
                        android.util.Log.i(TAG, "Consent gathered successfully. Can request ads: $canRequestAds")
                        listener.onConsentGatheringFinished(null)
                    }
                }
            },
            { requestConsentError ->
                android.util.Log.e(TAG, "Consent update failed: ${requestConsentError.message}")
                // Fall back gracefully so the user can still use the app
                listener.onConsentGatheringFinished(requestConsentError.message)
            }
        )
    }

    companion object {
        private const val TAG = "ConsentManager"

        @Volatile
        private var INSTANCE: ConsentManager? = null

        fun getInstance(context: Context): ConsentManager {
            return INSTANCE ?: synchronized(this) {
                val instance = ConsentManager(context)
                INSTANCE = instance
                instance
            }
        }
    }
}

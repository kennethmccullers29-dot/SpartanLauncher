package com.ken1dot.spartanlauncher

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.telephony.SubscriptionManager
import android.telephony.TelephonyManager
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

/** Carrier name for the Phone tile, like W10M's ("T-Mobile"). Null = fall back to the label. */
internal object CarrierName {
    var current by mutableStateOf<String?>(null)
        private set
    private var receiver: BroadcastReceiver? = null

    fun refresh(context: Context) {
        current = runCatching { read(context.applicationContext) }.getOrNull()
    }

    private fun read(context: Context): String? {
        val tm = context.getSystemService(TelephonyManager::class.java) ?: return null
        // Dual SIM: use the default voice SIM's carrier when we may list subscriptions.
        val names = runCatching {
            val sm = context.getSystemService(SubscriptionManager::class.java)
            val subs = sm?.activeSubscriptionInfoList.orEmpty()
            val voice = SubscriptionManager.getDefaultVoiceSubscriptionId()
            val ordered = subs.sortedBy { if (it.subscriptionId == voice) 0 else 1 }
            ordered.flatMap { info ->
                val t = tm.createForSubscriptionId(info.subscriptionId)
                listOf(t.simOperatorName, t.networkOperatorName, info.carrierName?.toString())
            }
        }.getOrDefault(emptyList())
        return (names + listOf(tm.simOperatorName, tm.networkOperatorName))
            .map { it?.trim().orEmpty() }
            .firstOrNull { it.isNotEmpty() && !it.equals("null", true) }
    }

    fun register(context: Context) {
        refresh(context)
        if (receiver != null) return
        val r = object : BroadcastReceiver() {
            override fun onReceive(c: Context, i: Intent) = refresh(c)
        }
        val filter = IntentFilter().apply {
            addAction("android.intent.action.SIM_STATE_CHANGED")
            addAction("android.telephony.action.SERVICE_PROVIDERS_UPDATED")
            addAction("android.telephony.action.DEFAULT_SUBSCRIPTION_CHANGED")
            addAction(Intent.ACTION_AIRPLANE_MODE_CHANGED)
        }
        runCatching {
            if (android.os.Build.VERSION.SDK_INT >= 33)
                context.applicationContext.registerReceiver(r, filter, Context.RECEIVER_EXPORTED)
            else context.applicationContext.registerReceiver(r, filter)
            receiver = r
        }
    }
}

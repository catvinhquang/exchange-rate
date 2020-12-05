package com.catvinhquang.exchangerate

import android.annotation.SuppressLint
import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.os.PowerManager
import android.util.Log
import com.catvinhquang.exchangerate.data.DataProvider
import com.catvinhquang.exchangerate.data.sharedmodel.GoldPrice
import com.catvinhquang.exchangerate.managers.NotificationManager

/**
 * Created by QuangCV on 05-Dec-2020
 **/

class GetGoldPriceService : Service() {

    private val notificationId = 1
    private var wakeLock: PowerManager.WakeLock? = null

    override fun onCreate() {
        startForeground(
            notificationId,
            NotificationManager.generateNotification(
                this, getString(R.string.loading), null
            )
        )
        acquireWakeLock()

        val obj = DataProvider.getGoldPrice(1800L).subscribe {
            Log.e("quang", "onCreate: " + it.localSellingPrice)
            pushNotification(it)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.apply {
            if (hasExtra("stop_service")) {
                stopSelf()
            }
        }
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        stopForeground(true)
        releaseWakeLock()
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    @SuppressLint("WakelockTimeout")
    private fun acquireWakeLock() {
        val powerManager = getSystemService(POWER_SERVICE) as? PowerManager
        wakeLock = powerManager?.newWakeLock(
            PowerManager.PARTIAL_WAKE_LOCK,
            "TyGia::GetGoldPriceService"
        )
        wakeLock?.acquire()
    }

    private fun releaseWakeLock() {
        wakeLock?.release()
    }

    private fun pushNotification(p: GoldPrice) {
        val context = applicationContext
        val iconGlobal = if (p.globalSellingPriceUp) "️🔼" else "🔽"
        val iconLocal = if (p.localSellingPriceUp) "🔼" else "🔽"
        val contentText = context.getString(
            R.string.notification_short_content,
            p.globalBuyingPrice.withSeparators(),
            p.globalSellingPrice.withSeparators() + " $iconGlobal",
            p.localBuyingPrice.withSeparators(),
            p.localSellingPrice.withSeparators() + " $iconLocal"
        )
        NotificationManager.push(
            context, notificationId,
            contentText = contentText
        )
    }

}
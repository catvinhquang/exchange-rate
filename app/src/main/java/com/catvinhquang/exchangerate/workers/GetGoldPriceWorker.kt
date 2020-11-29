package com.catvinhquang.exchangerate.workers

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.*
import com.catvinhquang.exchangerate.MainActivity
import com.catvinhquang.exchangerate.R
import com.catvinhquang.exchangerate.data.DataProvider
import com.catvinhquang.exchangerate.data.network.NetworkManager
import com.catvinhquang.exchangerate.data.sharedmodel.GoldPrice
import com.catvinhquang.exchangerate.withSeparators
import java.util.concurrent.TimeUnit

class GetGoldPriceWorker(context: Context, workerParams: WorkerParameters) :
    Worker(context, workerParams) {

    companion object {

        private const val TAG = "GetGoldPriceWorker"

        fun schedule(context: Context) {
            val request = OneTimeWorkRequestBuilder<GetGoldPriceWorker>()
                .setInitialDelay(30, TimeUnit.MINUTES)
                .build()

            WorkManager.getInstance(context)
                .enqueueUniqueWork(
                    "ExchangeRate-GetGoldPrice",
                    ExistingWorkPolicy.REPLACE,
                    request
                )

            Log.e(TAG, "schedule")
        }

    }

    private val locker = Object()

    override fun doWork(): Result {
        val obj = NetworkManager.getGoldPrice()
            .subscribe({
                Log.e(TAG, "doWork: running")
                DataProvider.saveNewGoldPrice(it)
                pushNotification(it)
            }, {
                Log.e(TAG, "doWork: error")
                onComplete()
            }, {
                Log.e(TAG, "doWork: complete")
                onComplete()
            })

        pause()

        return Result.success()
    }

    private fun onComplete() {
        schedule(applicationContext)
        resume()
    }

    private fun pause() {
        synchronized(locker) {
            locker.wait()
        }
    }

    private fun resume() {
        synchronized(locker) {
            locker.notify()
        }
    }

    private fun pushNotification(p: GoldPrice) {
        val context = applicationContext
        val channelId = context.getString(R.string.app_name)

        // create channel
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            with(context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager) {
                createNotificationChannel(
                    NotificationChannel(
                        channelId, channelId,
                        NotificationManager.IMPORTANCE_DEFAULT
                    )
                )
            }
        }

        val intent = PendingIntent.getActivity(
            context, 0,
            Intent(context, MainActivity::class.java),
            0
        )

        val shortText = context.getString(
            R.string.notification_short_content,
            p.globalBuyingPrice.withSeparators(),
            p.globalSellingPrice.withSeparators(),
            p.localBuyingPrice.withSeparators(),
            p.localSellingPrice.withSeparators()
        )

        val longText = context.getString(
            R.string.notification_full_content,
            p.globalBuyingPrice.withSeparators(),
            p.globalSellingPrice.withSeparators(),
            p.localBuyingPrice.withSeparators(),
            p.localSellingPrice.withSeparators()
        )

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setColor(Color.parseColor("#fece01"))
            .setContentTitle(context.getString(R.string.notification_title))
            .setContentText(shortText)
            .setStyle(NotificationCompat.BigTextStyle().bigText(longText))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(intent)
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(context)
            .notify((1..Int.MAX_VALUE).random(), notification)
    }

}
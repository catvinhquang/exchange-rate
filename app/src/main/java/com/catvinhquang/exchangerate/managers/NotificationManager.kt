package com.catvinhquang.exchangerate.managers

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.catvinhquang.exchangerate.GetGoldPriceService
import com.catvinhquang.exchangerate.MainActivity
import com.catvinhquang.exchangerate.R

/**
 * Created by QuangCV on 05-Dec-2020
 **/

object NotificationManager {

    private fun createChannel(context: Context): String {
        val channelId = context.getString(R.string.app_name)
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
        return channelId
    }

    fun push(context: Context, id: Int, contentText: String, longContentText: String? = null) {
        NotificationManagerCompat.from(context).notify(
            id, generateNotification(context, contentText, longContentText)
        )
    }

    fun generateNotification(
        context: Context,
        contentText: String,
        longContentText: String?
    ): Notification {
        val channelId = createChannel(context)

        val intent = PendingIntent.getActivity(
            context, 0,
            Intent(context, MainActivity::class.java),
            0
        )

        val builder = NotificationCompat.Builder(context, channelId)
            .setColor(Color.parseColor("#fece01"))
            .setContentTitle(context.getString(R.string.update_gold_price))
            .setContentText(contentText)
            .setContentIntent(intent)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setSmallIcon(R.drawable.ic_notification)
            .addAction(
                0, context.getString(R.string.turn_off_notification),
                PendingIntent.getService(
                    context, 0,
                    Intent(context, GetGoldPriceService::class.java).apply {
                        putExtra("stop_service", "")
                    }, 0
                )
            )

        if (!longContentText.isNullOrEmpty()) {
            builder.setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText(longContentText)
            )
        }

        return builder.build()
    }

}
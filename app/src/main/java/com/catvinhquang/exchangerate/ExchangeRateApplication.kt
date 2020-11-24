package com.catvinhquang.exchangerate

import android.app.Application
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequest
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.catvinhquang.exchangerate.data.DataProvider
import com.catvinhquang.exchangerate.workers.GetGoldPriceWorker
import java.util.concurrent.TimeUnit

/**
 * Created by QuangCV on 13-Aug-2020
 **/

class ExchangeRateApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        DataProvider.init(this)
        initGetGoldPriceWorker()
    }

    private fun initGetGoldPriceWorker() {
        val request = PeriodicWorkRequestBuilder<GetGoldPriceWorker>(
            PeriodicWorkRequest.MIN_PERIODIC_INTERVAL_MILLIS,
            TimeUnit.MILLISECONDS
        ).setInitialDelay(
            PeriodicWorkRequest.MIN_PERIODIC_INTERVAL_MILLIS,
            TimeUnit.MILLISECONDS
        ).build()

        WorkManager.getInstance(this)
            .enqueueUniquePeriodicWork(
                "ExchangeRate-GetGoldPrice",
                ExistingPeriodicWorkPolicy.KEEP,
                request
            )
    }

}
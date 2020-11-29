package com.catvinhquang.exchangerate

import android.app.Application
import com.catvinhquang.exchangerate.data.DataProvider
import com.catvinhquang.exchangerate.workers.GetGoldPriceWorker

/**
 * Created by QuangCV on 13-Aug-2020
 **/

class ExchangeRateApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        DataProvider.init(this)
        GetGoldPriceWorker.schedule(this)
    }

}
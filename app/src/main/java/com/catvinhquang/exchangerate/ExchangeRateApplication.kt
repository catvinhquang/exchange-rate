package com.catvinhquang.exchangerate

import android.app.Application
import android.content.Intent
import com.catvinhquang.exchangerate.data.DataProvider

/**
 * Created by QuangCV on 13-Aug-2020
 **/

class ExchangeRateApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        DataProvider.init(this)
    }

}
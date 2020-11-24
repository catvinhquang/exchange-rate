package com.catvinhquang.exchangerate.workers

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.catvinhquang.exchangerate.data.DataProvider
import com.catvinhquang.exchangerate.data.network.NetworkManager

class GetGoldPriceWorker(context: Context, workerParams: WorkerParameters) :
    Worker(context, workerParams) {

    private val locker = Object()

    override fun doWork(): Result {
        val obj = NetworkManager.getGoldPrice()
            .subscribe {
                DataProvider.saveNewGoldPrice(it)
                synchronized(locker) {
                    locker.notify()
                }
            }

        synchronized(locker) {
            locker.wait()
        }

        return Result.success()
    }

}
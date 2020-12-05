package com.catvinhquang.exchangerate.data

import android.content.Context
import android.util.Log
import com.catvinhquang.exchangerate.data.cache.CacheManager
import com.catvinhquang.exchangerate.data.network.NetworkManager
import com.catvinhquang.exchangerate.data.sharedmodel.GoldPrice
import com.catvinhquang.exchangerate.data.sharedmodel.UsdPrice
import com.catvinhquang.exchangerate.data.sharedmodel.UserAssets
import com.catvinhquang.exchangerate.now
import com.catvinhquang.exchangerate.toTimeString
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import org.json.JSONObject
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * Created by QuangCV on 13-Aug-2020
 **/

object DataProvider {

    private const val updateIntervalInSeconds = 15L
    private val TAG = DataProvider.javaClass.simpleName

    fun init(context: Context) {
        CacheManager.init(context)
        NetworkManager.init()
    }

    fun getGoldPrice(intervalInSeconds: Long = updateIntervalInSeconds): Observable<GoldPrice> {
        return Observable.create<GoldPrice> { emitter ->
            val cache = GoldPrice(
                CacheManager.goldGlobalBuyingPrice,
                CacheManager.goldGlobalSellingPrice,
                CacheManager.goldLocalBuyingPrice,
                CacheManager.goldLocalSellingPrice
            ).apply {
                globalBuyingPriceUp = CacheManager.goldGlobalBuyingPriceUp
                globalSellingPriceUp = CacheManager.goldGlobalSellingPriceUp
                localBuyingPriceUp = CacheManager.goldLocalBuyingPriceUp
                localSellingPriceUp = CacheManager.goldLocalSellingPriceUp
            }
            emitter.onNext(cache)
            Log.d(TAG, "getGoldPrice: [cache] $cache")

            Observable.interval(0, intervalInSeconds, TimeUnit.SECONDS)
                .takeUntil { emitter.isDisposed }
                .flatMap { NetworkManager.getGoldPrice() }
                .subscribe {
                    it.fromNetwork = true
                    saveNewGoldPrice(it)
                    emitter.onNext(it)
                    Log.d(TAG, "getGoldPrice: [network] $it")
                }
        }
            .subscribeOn(Schedulers.computation())
            .observeOn(AndroidSchedulers.mainThread())
    }

    fun getUsdPrice(): Observable<UsdPrice> {
        return Observable.create<UsdPrice> { emitter ->
            val cache = UsdPrice(
                CacheManager.usdBuyingPrice,
                CacheManager.usdSellingPrice
            ).apply {
                buyingPriceUp = CacheManager.usdBuyingPriceUp
                sellingPriceUp = CacheManager.usdSellingPriceUp
            }
            emitter.onNext(cache)
            Log.d(TAG, "getUsdPrice: [cache] $cache")

            Observable.interval(0, updateIntervalInSeconds, TimeUnit.SECONDS)
                .takeUntil { emitter.isDisposed }
                .flatMap { NetworkManager.getUsdPrice() }
                .subscribe {
                    it.buyingPriceUp = when {
                        it.buyingPrice > CacheManager.usdBuyingPrice -> true
                        it.buyingPrice == CacheManager.usdBuyingPrice -> CacheManager.usdBuyingPriceUp
                        else -> false
                    }
                    CacheManager.usdBuyingPriceUp = it.buyingPriceUp
                    CacheManager.usdBuyingPrice = it.buyingPrice

                    it.sellingPriceUp = when {
                        it.sellingPrice > CacheManager.usdSellingPrice -> true
                        it.sellingPrice == CacheManager.usdSellingPrice -> CacheManager.usdSellingPriceUp
                        else -> false
                    }
                    CacheManager.usdSellingPriceUp = it.sellingPriceUp
                    CacheManager.usdSellingPrice = it.sellingPrice

                    emitter.onNext(it)
                    Log.d(TAG, "getUsdPrice: [network] $it")
                }
        }
            .subscribeOn(Schedulers.computation())
            .observeOn(AndroidSchedulers.mainThread())
    }

    fun getUserAssets(): Observable<UserAssets> {
        return Observable.create<UserAssets> { emitter ->
            CacheManager.userAssets?.apply {
                emitter.onNext(this)
            }
        }
            .subscribeOn(Schedulers.computation())
            .observeOn(AndroidSchedulers.mainThread())
    }

    fun getGoldPriceHistory(): Observable<JSONObject> {
        return Observable.create<JSONObject> { emitter ->
            emitter.onNext(CacheManager.goldPriceHistory ?: JSONObject())
        }
            .subscribeOn(Schedulers.computation())
            .observeOn(AndroidSchedulers.mainThread())
    }

    fun setUserAssets(value: UserAssets?) {
        CacheManager.userAssets = value
    }

    private fun saveNewGoldPrice(newPrice: GoldPrice) {
        newPrice.globalBuyingPriceUp = when {
            newPrice.globalBuyingPrice > CacheManager.goldGlobalBuyingPrice -> true
            newPrice.globalBuyingPrice == CacheManager.goldGlobalBuyingPrice -> CacheManager.goldGlobalBuyingPriceUp
            else -> false
        }
        CacheManager.goldGlobalBuyingPriceUp = newPrice.globalBuyingPriceUp
        CacheManager.goldGlobalBuyingPrice = newPrice.globalBuyingPrice

        newPrice.globalSellingPriceUp = when {
            newPrice.globalSellingPrice > CacheManager.goldGlobalSellingPrice -> true
            newPrice.globalSellingPrice == CacheManager.goldGlobalSellingPrice -> CacheManager.goldGlobalSellingPriceUp
            else -> false
        }
        CacheManager.goldGlobalSellingPriceUp = newPrice.globalSellingPriceUp
        CacheManager.goldGlobalSellingPrice = newPrice.globalSellingPrice

        newPrice.localBuyingPriceUp = when {
            newPrice.localBuyingPrice > CacheManager.goldLocalBuyingPrice -> true
            newPrice.localBuyingPrice == CacheManager.goldLocalBuyingPrice -> CacheManager.goldLocalBuyingPriceUp
            else -> false
        }
        CacheManager.goldLocalBuyingPriceUp = newPrice.localBuyingPriceUp
        CacheManager.goldLocalBuyingPrice = newPrice.localBuyingPrice

        newPrice.localSellingPriceUp = when {
            newPrice.localSellingPrice > CacheManager.goldLocalSellingPrice -> true
            newPrice.localSellingPrice == CacheManager.goldLocalSellingPrice -> CacheManager.goldLocalSellingPriceUp
            else -> false
        }
        CacheManager.goldLocalSellingPriceUp = newPrice.localSellingPriceUp
        CacheManager.goldLocalSellingPrice = newPrice.localSellingPrice

        val history = CacheManager.goldPriceHistory ?: JSONObject()
        var key = now().toTimeString("dd-MM-yyyy")
        val date = history.optJSONObject(key) ?: JSONObject()
        date.put("gbp", newPrice.globalBuyingPrice)
        date.put("gsp", newPrice.globalSellingPrice)
        date.put("lbp", newPrice.localBuyingPrice)
        date.put("lsp", newPrice.localSellingPrice)
        history.put(key, date)
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DATE, -90)
        key = calendar.time.time.toTimeString("dd-MM-yyyy")
        history.remove(key)
        CacheManager.goldPriceHistory = history
    }

}
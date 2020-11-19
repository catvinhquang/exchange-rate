package com.catvinhquang.exchangerate.data

import android.content.Context
import android.util.Log
import com.catvinhquang.exchangerate.data.cache.CacheManager
import com.catvinhquang.exchangerate.data.network.NetworkManager
import com.catvinhquang.exchangerate.data.sharedmodel.GoldPrice
import com.catvinhquang.exchangerate.data.sharedmodel.UsdPrice
import com.catvinhquang.exchangerate.data.sharedmodel.UserAssets
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
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

    fun getGoldPrice(): Observable<GoldPrice> {
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

            Observable.interval(0, updateIntervalInSeconds, TimeUnit.SECONDS)
                .takeUntil { emitter.isDisposed }
                .flatMap { NetworkManager.getGoldPrice() }
                .subscribe {
                    CacheManager.goldGlobalBuyingPrice = it.globalBuyingPrice
                    CacheManager.goldGlobalSellingPrice = it.globalSellingPrice
                    CacheManager.goldLocalBuyingPrice = it.localBuyingPrice
                    CacheManager.goldLocalSellingPrice = it.localSellingPrice
                    it.globalBuyingPriceUp = CacheManager.goldGlobalBuyingPriceUp
                    it.globalSellingPriceUp = CacheManager.goldGlobalSellingPriceUp
                    it.localBuyingPriceUp = CacheManager.goldLocalBuyingPriceUp
                    it.localSellingPriceUp = CacheManager.goldLocalSellingPriceUp
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
                    CacheManager.usdBuyingPrice = it.buyingPrice
                    CacheManager.usdSellingPrice = it.sellingPrice
                    it.buyingPriceUp = CacheManager.usdBuyingPriceUp
                    it.sellingPriceUp = CacheManager.usdSellingPriceUp
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

    fun setUserAssets(value: UserAssets?) {
        CacheManager.userAssets = value
    }

}
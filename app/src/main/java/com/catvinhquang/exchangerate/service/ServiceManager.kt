package com.catvinhquang.exchangerate.service

import android.annotation.SuppressLint
import android.content.Context
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import okhttp3.Cache
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import java.security.SecureRandom
import java.security.cert.X509Certificate
import java.util.regex.Pattern
import javax.net.ssl.HostnameVerifier
import javax.net.ssl.SSLContext
import javax.net.ssl.X509TrustManager

/**
 * Created by QuangCV on 14-Jul-2020
 **/

class ServiceManager(
    context: Context,
    private val listener: OnPricesLoadedListener
) {

    private val goldService: GoldService
    private val bankService: BankService

    init {
        goldService = initGoldService(context)
        bankService = initBankService(context)
    }

    private fun initGoldService(context: Context): GoldService {
        val loggingInterceptor = HttpLoggingInterceptor()
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY)
        val client: OkHttpClient = OkHttpClient.Builder()
            .addInterceptor(CacheInterceptor(context))
            .addInterceptor(loggingInterceptor)
            .cache(Cache(context.cacheDir, Long.MAX_VALUE))
            .build()
        val retrofit = Retrofit.Builder()
            .client(client)
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .baseUrl("http://www.sjc.com.vn/")
            .build()
        return retrofit.create<GoldService>(GoldService::class.java)
    }

    private fun initBankService(context: Context): BankService {
        fun initBuilder(): OkHttpClient.Builder {
            val builder = OkHttpClient.Builder()
            try {
                // Create a trust manager that does not validate certificate chains
                val manager = object : X509TrustManager {
                    override fun checkClientTrusted(
                        chain: Array<X509Certificate>,
                        authType: String
                    ) {
                    }

                    override fun checkServerTrusted(
                        chain: Array<X509Certificate>,
                        authType: String
                    ) {
                    }

                    override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf()
                }

                // Install the all-trusting trust manager
                val sslContext = SSLContext.getInstance("SSL")
                sslContext.init(null, arrayOf(manager), SecureRandom())

                // Create an ssl socket factory with our all-trusting manager
                val sslSocketFactory = sslContext.socketFactory

                builder.sslSocketFactory(sslSocketFactory, manager)
                builder.hostnameVerifier(HostnameVerifier { _, _ -> true })
            } catch (t: Throwable) {
                t.printStackTrace()
            }
            return builder
        }

        val loggingInterceptor = HttpLoggingInterceptor()
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY)
        val client: OkHttpClient = initBuilder()
            .addInterceptor(CacheInterceptor(context))
            .addInterceptor(loggingInterceptor)
            .cache(Cache(context.cacheDir, Long.MAX_VALUE))
            .build()
        val retrofit = Retrofit.Builder()
            .client(client)
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .baseUrl("https://portal.vietcombank.com.vn/")
            .build()
        return retrofit.create<BankService>(BankService::class.java)
    }

    fun loadPrices() {
        loadGoldPrices()
        loadUsdPrices()
    }

    @SuppressLint("CheckResult")
    private fun loadGoldPrices() {
        goldService.getGoldPrices()
            .subscribeOn(Schedulers.computation())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                val response = it.string()
                var globalBuyingPrice = 0F
                var globalSellingPrice = 0F
                var localBuyingPrice = 0
                var localSellingPrice = 0

                Pattern.compile("<span style=\"color:#FFF;font-size:28px\">(.*)</span>")
                    .matcher(response)
                    .apply {
                        if (find()) {
                            val prices = group(1).trim().split("/")
                            globalBuyingPrice = prices[0].toFloat()
                            globalSellingPrice = prices[1].toFloat()
                        }
                    }

                Pattern.compile("<span style=\"font-size:larger\">(.*)</span>")
                    .matcher(response)
                    .apply {
                        if (find()) {
                            localBuyingPrice = group(1).trim()
                                .replace(",", "")
                                .toInt()
                        }

                        if (find()) {
                            localSellingPrice = group(1).trim()
                                .replace(",", "")
                                .toInt()
                        }
                    }

                listener.onGoldPricesUpdated(
                    globalBuyingPrice,
                    globalSellingPrice,
                    localBuyingPrice,
                    localSellingPrice
                )
            }, {
                it.printStackTrace()
                listener.onGoldPricesUpdated(0f, 0f, 0, 0)
            })
    }

    @SuppressLint("CheckResult")
    private fun loadUsdPrices() {
        bankService.getExchangeRates()
            .subscribeOn(Schedulers.computation())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                val response = it.string()
                var buyingPrice = 0
                var sellingPrice = 0

                Pattern.compile(
                    "<td style=\"text-align:center;\">USD</td>(.*)</tr>",
                    Pattern.DOTALL
                )
                    .matcher(response)
                    .apply {
                        if (find()) {
                            val prices = group(1).trim()
                            val pricePattern = Pattern.compile("<td>(.*)</td>")
                            pricePattern.matcher(prices).apply {
                                if (find()) {
                                    buyingPrice = group(1).trim().run {
                                        substring(0, indexOf("."))
                                            .replace(",", "")
                                            .toInt()
                                    }
                                }

                                // ignore transfer price
                                find()

                                if (find()) {
                                    sellingPrice = group(1).trim().run {
                                        substring(0, indexOf("."))
                                            .replace(",", "")
                                            .toInt()
                                    }
                                }
                            }
                        }
                    }

                listener.onUsdPricesUpdated(buyingPrice, sellingPrice)
            }, {
                it.printStackTrace()
                listener.onUsdPricesUpdated(0, 0)
            })
    }

    interface OnPricesLoadedListener {

        fun onGoldPricesUpdated(
            globalBuyingPrice: Float, globalSellingPrice: Float,
            localBuyingPrice: Int, localSellingPrice: Int
        )

        fun onUsdPricesUpdated(
            buyingPrice: Int,
            sellingPrice: Int
        )

    }

}
package com.catvinhquang.exchangerate.data.network

import android.util.Log
import com.catvinhquang.exchangerate.data.sharedmodel.GoldPrice
import com.catvinhquang.exchangerate.data.sharedmodel.UsdPrice
import io.reactivex.Observable
import io.reactivex.ObservableSource
import io.reactivex.functions.Function
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.security.SecureRandom
import java.security.cert.X509Certificate
import java.util.regex.Pattern
import javax.net.ssl.SSLContext
import javax.net.ssl.X509TrustManager

/**
 * Created by QuangCV on 14-Jul-2020
 **/

object NetworkManager {

    private const val showLog = false
    private val TAG = NetworkManager.javaClass.simpleName

    private lateinit var goldService: GoldService
    private lateinit var bankService: BankService

    fun init() {
        goldService = initGoldService()
        bankService = initBankService()
    }

    private fun initBuilder(): OkHttpClient.Builder {
        val builder = OkHttpClient.Builder()

        // Create a trust manager that does not validate certificate chains
        val manager = object : X509TrustManager {
            override fun checkClientTrusted(chain: Array<X509Certificate>, authType: String) {}

            override fun checkServerTrusted(chain: Array<X509Certificate>, authType: String) {}

            override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf()
        }

        // Install the all-trusting trust manager
        val sslContext = SSLContext.getInstance("SSL")
        sslContext.init(null, arrayOf(manager), SecureRandom())

        // Create an ssl socket factory with our all-trusting manager
        val sslSocketFactory = sslContext.socketFactory

        builder.sslSocketFactory(sslSocketFactory, manager)
        builder.hostnameVerifier { _, _ -> true }
        return builder
    }

    private fun initGoldService(): GoldService {
        val client = if (showLog) {
            val loggingInterceptor = HttpLoggingInterceptor()
            loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY)
            initBuilder().addInterceptor(loggingInterceptor).build()
        } else {
            initBuilder().build()
        }
        val retrofit = Retrofit.Builder()
            .client(client)
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .baseUrl("http://www.sjc.com.vn/")
            .build()
        return retrofit.create(GoldService::class.java)
    }

    private fun initBankService(): BankService {
        val client = if (showLog) {
            val loggingInterceptor = HttpLoggingInterceptor()
            loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY)
            initBuilder().addInterceptor(loggingInterceptor).build()
        } else {
            initBuilder().build()
        }
        val retrofit = Retrofit.Builder()
            .client(client)
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .baseUrl("https://portal.vietcombank.com.vn/")
            .build()
        return retrofit.create(BankService::class.java)
    }

    fun getGoldPrice(): Observable<GoldPrice> {
        return goldService.getGoldPrices()
            .map {
                val response = it.string()
                var globalBuyingPrice = 0F
                var globalSellingPrice = 0F
                var localBuyingPrice = 0
                var localSellingPrice = 0

                Pattern.compile("<span style=\"color:#FFF;font-size:28px\">(.*)</span>")
                    .matcher(response)
                    .apply {
                        if (find()) {
                            val prices = group(1)!!.trim().split("/")
                            globalBuyingPrice = prices[0].toFloat()
                            globalSellingPrice = prices[1].toFloat()
                        }
                    }

                Pattern.compile("<span style=\"font-size:larger\">(.*)</span>")
                    .matcher(response)
                    .apply {
                        if (find()) {
                            localBuyingPrice = group(1)!!.trim()
                                .replace(",", "")
                                .toInt()
                        }

                        if (find()) {
                            localSellingPrice = group(1)!!.trim()
                                .replace(",", "")
                                .toInt()
                        }
                    }

                GoldPrice(
                    globalBuyingPrice, globalSellingPrice,
                    localBuyingPrice, localSellingPrice
                )
            }
            .onErrorResumeNext(Function<Throwable, ObservableSource<GoldPrice>> {
                handleError(it)
                Observable.empty()
            })
    }

    fun getUsdPrice(): Observable<UsdPrice> {
        return bankService.getExchangeRates()
            .map {
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
                            val prices = group(1)!!.trim()
                            val pricePattern = Pattern.compile("<td>(.*)</td>")
                            pricePattern.matcher(prices).apply {
                                if (find()) {
                                    buyingPrice = group(1)!!.trim().run {
                                        substring(0, indexOf("."))
                                            .replace(",", "")
                                            .toInt()
                                    }
                                }

                                // ignore transfer price
                                find()

                                if (find()) {
                                    sellingPrice = group(1)!!.trim().run {
                                        substring(0, indexOf("."))
                                            .replace(",", "")
                                            .toInt()
                                    }
                                }
                            }
                        }
                    }

                UsdPrice(buyingPrice, sellingPrice)
            }
            .onErrorResumeNext(Function<Throwable, ObservableSource<UsdPrice>> {
                handleError(it)
                Observable.empty()
            })
    }

    private fun handleError(t: Throwable) {
        when (t) {
            is UnknownHostException, is SocketTimeoutException -> {
                Log.e(TAG, "handleError: no internet")
            }
            else -> t.printStackTrace()
        }
    }

}
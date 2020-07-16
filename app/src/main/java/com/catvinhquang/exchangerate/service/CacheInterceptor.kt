package com.catvinhquang.exchangerate.service

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkInfo
import okhttp3.CacheControl
import okhttp3.Interceptor
import okhttp3.Response

/**
 * Created by QuangCV on 14-Jul-2020
 */

class CacheInterceptor(private val context: Context) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        var request = chain.request()
        if (!isNetworkAvailable()) {
            request = request.newBuilder()
                .cacheControl(CacheControl.FORCE_CACHE)
                .build()
        }
        return chain.proceed(request)
    }

    private fun isNetworkAvailable(): Boolean {
        var result = false
        val connectivity = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val info = connectivity.allNetworkInfo
        for (networkInfo in info) {
            if (networkInfo.state == NetworkInfo.State.CONNECTED) {
                result = true
                break
            }
        }
        return result
    }

}
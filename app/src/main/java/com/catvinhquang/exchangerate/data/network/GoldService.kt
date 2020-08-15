package com.catvinhquang.exchangerate.data.network

import io.reactivex.Observable
import okhttp3.ResponseBody
import retrofit2.http.GET

/**
 * Created by QuangCV on 14-Jul-2020
 */

interface GoldService {

    @GET("giavang/textContent.php")
    fun getGoldPrices(): Observable<ResponseBody>

}
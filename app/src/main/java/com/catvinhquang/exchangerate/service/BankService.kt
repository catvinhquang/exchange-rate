package com.catvinhquang.exchangerate.service

import io.reactivex.Observable
import okhttp3.ResponseBody
import retrofit2.http.GET

/**
 * Created by QuangCV on 14-Jul-2020
 */

interface BankService {

    @GET("UserControls/TVPortal.TyGia/pListTyGia.aspx")
    fun getExchangeRates(): Observable<ResponseBody>

}
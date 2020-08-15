package com.catvinhquang.exchangerate.data.sharedmodel

import com.google.gson.annotations.SerializedName

/**
 * Created by QuangCV on 15-Aug-2020
 **/

class UserAssets(
    @SerializedName("tael_of_gold")
    var taelOfGold: Double = 0.0,

    @SerializedName("usd")
    var usd: Int = 0,

    @SerializedName("saving_money")
    var savingMoney: Int = 0
)
package com.catvinhquang.exchangerate.data.sharedmodel

import com.google.gson.annotations.SerializedName

/**
 * Created by QuangCV on 15-Aug-2020
 **/

class UserAssets(
    @SerializedName("tael_of_gold")
    var taelOfGold: String,

    @SerializedName("usd")
    var usd: String,

    @SerializedName("saving_money")
    var savingMoney: String,

    @SerializedName("visible")
    var visible: Boolean = true
)
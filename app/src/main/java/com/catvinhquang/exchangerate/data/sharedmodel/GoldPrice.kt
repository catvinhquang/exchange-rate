package com.catvinhquang.exchangerate.data.sharedmodel

/**
 * Created by QuangCV on 13-Aug-2020
 **/

class GoldPrice(
    var internationalBuyingPrice: Float,
    var internationalSellingPrice: Float,
    var vietnamBuyingPrice: Int,
    var vietnamSellingPrice: Int
) {

    override fun toString(): String {
        return "internationalBuyingPrice = $internationalBuyingPrice, " +
                "internationalSellingPrice = $internationalSellingPrice, " +
                "vietnamBuyingPrice = $vietnamBuyingPrice, " +
                "vietnamSellingPrice = $vietnamSellingPrice"
    }

}
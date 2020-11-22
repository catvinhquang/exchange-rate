package com.catvinhquang.exchangerate.data.sharedmodel

/**
 * Created by QuangCV on 13-Aug-2020
 **/

class UsdPrice(
    var buyingPrice: Int,
    var sellingPrice: Int
) {

    var buyingPriceUp = true
    var sellingPriceUp = true

    override fun toString(): String {
        return "buyingPrice = $buyingPrice, " +
                "buyingPriceUp = $buyingPriceUp, " +
                "sellingPrice = $sellingPrice, " +
                "sellingPriceUp = $sellingPriceUp"
    }

}
package com.catvinhquang.exchangerate.data.sharedmodel

/**
 * Created by QuangCV on 13-Aug-2020
 **/

class GoldPrice(
    var globalBuyingPrice: Float,
    var globalSellingPrice: Float,
    var localBuyingPrice: Int,
    var localSellingPrice: Int
) {

    var globalBuyingPriceUp = true
    var globalSellingPriceUp = true
    var localBuyingPriceUp = true
    var localSellingPriceUp = true

    var fromNetwork = false

    override fun toString(): String {
        return "globalBuyingPrice = $globalBuyingPrice, " +
                "globalBuyingPriceUp = $globalBuyingPriceUp, " +
                "globalSellingPrice = $globalSellingPrice, " +
                "globalSellingPriceUp = $globalSellingPriceUp, " +
                "localBuyingPrice = $localBuyingPrice, " +
                "localBuyingPriceUp = $localBuyingPriceUp, " +
                "localSellingPrice = $localSellingPrice, " +
                "localSellingPriceUp = $localSellingPriceUp"
    }

}
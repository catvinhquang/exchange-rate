package com.catvinhquang.exchangerate.data.sharedmodel

/**
 * Created by QuangCV on 13-Aug-2020
 **/

class UsdPrice(var buyingPrice: Int, var sellingPrice: Int) {

    override fun toString(): String {
        return "buyingPrice = $buyingPrice, sellingPrice = $sellingPrice"
    }

}
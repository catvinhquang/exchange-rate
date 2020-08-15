package com.catvinhquang.exchangerate

import com.google.gson.Gson

/**
 * Created by QuangCV on 15-Aug-2020
 **/

object Utils {

    val gson = Gson()

}

fun Number.toNumberString(): String {
    return if (this is Double && this != toLong().toDouble()) {
        "%,.1f".format(this)
    } else {
        "%,d".format(toLong())
    }
}
package com.catvinhquang.exchangerate

import com.google.gson.Gson

/**
 * Created by QuangCV on 15-Aug-2020
 **/

private val gson = Gson()

fun Any.toJson(): String {
    return gson.toJson(this)
}

fun <T> String?.toObject(cls: Class<T>): T? {
    return gson.fromJson(this, cls)
}

fun Number.toNumberString(): String {
    return if (this is Double && this != toLong().toDouble()) {
        "%,.1f".format(this)
    } else {
        "%,d".format(toLong())
    }
}
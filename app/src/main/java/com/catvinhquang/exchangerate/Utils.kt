package com.catvinhquang.exchangerate

import android.graphics.Bitmap
import android.graphics.Canvas
import android.view.View
import com.google.gson.Gson
import java.text.SimpleDateFormat
import java.util.*

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

fun View.getBitmap(): Bitmap {
    if (visibility == View.GONE) {
        throw RuntimeException("Can not capture view when it has visibility as GONE")
    }

    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    draw(canvas)
    return bitmap
}

fun Long.toTimeString(format: String): String {
    val formatter = SimpleDateFormat(format, Locale.getDefault())
    return formatter.format(this)
}
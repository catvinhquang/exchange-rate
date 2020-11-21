package com.catvinhquang.exchangerate

import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Canvas
import android.view.View
import com.google.gson.Gson
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*

/**
 * Created by QuangCV on 15-Aug-2020
 **/

private const val decimalSeparator = "."

private val gson = Gson()
private val formatter = DecimalFormat("#,###")

fun Any.toJson(): String {
    return gson.toJson(this)
}

fun <T> String?.toObject(cls: Class<T>): T? {
    return gson.fromJson(this, cls)
}

fun Number.withSeparators(): String {
    val d = toString().toBigDecimal()
    val s = d.toPlainString()
    val isDecimal = s.contains(decimalSeparator)
    return if (isDecimal) {
        val left = s.substringBefore(decimalSeparator).toBigDecimal()
        var right = s.substringAfter(decimalSeparator)

        while (right.endsWith("0")) {
            right = right.dropLast(1)
        }

        if (right.isEmpty()) {
            formatter.format(left)
        } else {
            formatter.format(left) + decimalSeparator + right
        }
    } else {
        formatter.format(d)
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

fun toPx(dp: Float): Int {
    return (Resources.getSystem().displayMetrics.density * dp).toInt()
}
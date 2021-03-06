package com.catvinhquang.exchangerate.data.cache

import android.content.Context
import android.content.SharedPreferences
import com.catvinhquang.exchangerate.data.sharedmodel.UserAssets
import com.catvinhquang.exchangerate.toJson
import com.catvinhquang.exchangerate.toObject
import org.json.JSONObject

/**
 * Created by QuangCV on 13-Aug-2020
 **/

object CacheManager {

    private const val KEY_GOLD_GLOBAL_BUYING_PRICE = "KEY_GOLD_GLOBAL_BUYING_PRICE"
    private const val KEY_GOLD_GLOBAL_BUYING_PRICE_UP = "KEY_GOLD_GLOBAL_BUYING_PRICE_UP"
    private const val KEY_GOLD_GLOBAL_SELLING_PRICE = "KEY_GOLD_GLOBAL_SELLING_PRICE"
    private const val KEY_GOLD_GLOBAL_SELLING_PRICE_UP = "KEY_GOLD_GLOBAL_SELLING_PRICE_UP"
    private const val KEY_GOLD_LOCAL_BUYING_PRICE = "KEY_GOLD_LOCAL_BUYING_PRICE"
    private const val KEY_GOLD_LOCAL_BUYING_PRICE_UP = "KEY_GOLD_LOCAL_BUYING_PRICE_UP"
    private const val KEY_GOLD_LOCAL_SELLING_PRICE = "KEY_GOLD_LOCAL_SELLING_PRICE"
    private const val KEY_GOLD_LOCAL_SELLING_PRICE_UP = "KEY_GOLD_LOCAL_SELLING_PRICE_UP"
    private const val KEY_USD_BUYING_PRICE = "KEY_USD_BUYING_PRICE"
    private const val KEY_USD_BUYING_PRICE_UP = "KEY_USD_BUYING_PRICE_UP"
    private const val KEY_USD_SELLING_PRICE = "KEY_USD_SELLING_PRICE"
    private const val KEY_USD_SELLING_PRICE_UP = "KEY_USD_SELLING_PRICE_UP"
    private const val KEY_USER_ASSETS = "KEY_USER_ASSETS"
    private const val KEY_GOLD_PRICE_HISTORY = "KEY_GOLD_PRICE_HISTORY"

    private lateinit var sp: SharedPreferences

    fun init(context: Context) {
        if (!::sp.isInitialized) sp = context.getSharedPreferences(
            context.packageName, Context.MODE_PRIVATE
        )
    }

    private inline fun <reified T> get(key: String, defValue: T): T {
        return when (defValue) {
            is String -> sp.getString(key, defValue)
            is Int -> sp.getInt(key, defValue)
            is Long -> sp.getLong(key, defValue)
            is Float -> sp.getFloat(key, defValue)
            is Boolean -> sp.getBoolean(key, defValue)
            else -> Any()
        } as T
    }

    private fun put(key: String, value: Any?) {
        val editor = sp.edit()
        if (value == null) {
            editor.remove(key)
        } else {
            when (value) {
                is String -> editor.putString(key, value)
                is Int -> editor.putInt(key, value)
                is Long -> editor.putLong(key, value)
                is Float -> editor.putFloat(key, value)
                is Boolean -> editor.putBoolean(key, value)
            }
        }
        editor.apply()
    }

    var goldGlobalBuyingPrice
        get() = get(KEY_GOLD_GLOBAL_BUYING_PRICE, 0F)
        set(value) = put(KEY_GOLD_GLOBAL_BUYING_PRICE, value)

    var goldGlobalBuyingPriceUp
        get() = get(KEY_GOLD_GLOBAL_BUYING_PRICE_UP, true)
        set(value) = put(KEY_GOLD_GLOBAL_BUYING_PRICE_UP, value)

    var goldGlobalSellingPrice
        get() = get(KEY_GOLD_GLOBAL_SELLING_PRICE, 0F)
        set(value) = put(KEY_GOLD_GLOBAL_SELLING_PRICE, value)

    var goldGlobalSellingPriceUp
        get() = get(KEY_GOLD_GLOBAL_SELLING_PRICE_UP, true)
        set(value) = put(KEY_GOLD_GLOBAL_SELLING_PRICE_UP, value)

    var goldLocalBuyingPrice
        get() = get(KEY_GOLD_LOCAL_BUYING_PRICE, 0)
        set(value) = put(KEY_GOLD_LOCAL_BUYING_PRICE, value)

    var goldLocalBuyingPriceUp
        get() = get(KEY_GOLD_LOCAL_BUYING_PRICE_UP, true)
        set(value) = put(KEY_GOLD_LOCAL_BUYING_PRICE_UP, value)

    var goldLocalSellingPrice
        get() = get(KEY_GOLD_LOCAL_SELLING_PRICE, 0)
        set(value) = put(KEY_GOLD_LOCAL_SELLING_PRICE, value)

    var goldLocalSellingPriceUp
        get() = get(KEY_GOLD_LOCAL_SELLING_PRICE_UP, true)
        set(value) = put(KEY_GOLD_LOCAL_SELLING_PRICE_UP, value)

    var usdBuyingPrice
        get() = get(KEY_USD_BUYING_PRICE, 0)
        set(value) = put(KEY_USD_BUYING_PRICE, value)

    var usdBuyingPriceUp
        get() = get(KEY_USD_BUYING_PRICE_UP, true)
        set(value) = put(KEY_USD_BUYING_PRICE_UP, value)

    var usdSellingPrice
        get() = get(KEY_USD_SELLING_PRICE, 0)
        set(value) = put(KEY_USD_SELLING_PRICE, value)

    var usdSellingPriceUp
        get() = get(KEY_USD_SELLING_PRICE_UP, true)
        set(value) = put(KEY_USD_SELLING_PRICE_UP, value)

    var userAssets
        get() = run {
            val json = get(KEY_USER_ASSETS, "")
            if (json.isBlank()) {
                null
            } else {
                json.toObject(UserAssets::class.java)
            }
        }
        set(value) = run {
            val json = value?.toJson()
            put(KEY_USER_ASSETS, json)
        }

    var goldPriceHistory
        get() = run {
            val json = get(KEY_GOLD_PRICE_HISTORY, "")
            if (json.isBlank()) {
                null
            } else {
                JSONObject(json)
            }
        }
        set(value) = run {
            val json = value?.toString()
            put(KEY_GOLD_PRICE_HISTORY, json)
        }

}
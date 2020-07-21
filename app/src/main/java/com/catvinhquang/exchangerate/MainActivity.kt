package com.catvinhquang.exchangerate

import android.animation.LayoutTransition
import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.catvinhquang.exchangerate.service.ServiceManager
import kotlinx.android.synthetic.main.activity_main.*
import java.text.SimpleDateFormat
import java.util.*

/**
 * Created by QuangCV on 14-Jul-2020
 **/

private val TAG = MainActivity::class.java.simpleName

class MainActivity : AppCompatActivity(), ServiceManager.OnPricesLoadedListener {

    private lateinit var service: ServiceManager
    private val clickListener = View.OnClickListener {
        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText(getString(R.string.app_name), it.tag.toString())
        clipboard.setPrimaryClip(clip)
        Toast.makeText(this, R.string.copied, Toast.LENGTH_SHORT).show()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        container.layoutTransition.enableTransitionType(LayoutTransition.CHANGING)
        btn_share.setOnClickListener { share() }
        tv_global_buying_price.setOnClickListener(clickListener)
        tv_global_selling_price.setOnClickListener(clickListener)
        tv_local_buying_price.setOnClickListener(clickListener)
        tv_local_selling_price.setOnClickListener(clickListener)
        tv_buying_price.setOnClickListener(clickListener)
        tv_selling_price.setOnClickListener(clickListener)

        service = ServiceManager(this, this)
        service.loadPrices()
    }

    override fun onGoldPricesUpdated(
        globalBuyingPrice: Float, globalSellingPrice: Float,
        localBuyingPrice: Int, localSellingPrice: Int
    ) {
        Log.d(
            TAG,
            "onGoldPricesUpdated: $globalBuyingPrice $globalSellingPrice" +
                    " $localBuyingPrice $localSellingPrice"
        )
        tv_global_buying_price.text = globalBuyingPrice.toNumberString()
        tv_global_buying_price.tag = globalBuyingPrice
        tv_global_selling_price.text = globalSellingPrice.toNumberString()
        tv_global_selling_price.tag = globalSellingPrice
        tv_local_buying_price.text = localBuyingPrice.toNumberString()
        tv_local_buying_price.tag = localBuyingPrice
        tv_local_selling_price.text = localSellingPrice.toNumberString()
        tv_local_selling_price.tag = localSellingPrice
    }

    override fun onUsdPricesUpdated(
        buyingPrice: Int,
        sellingPrice: Int
    ) {
        Log.d(TAG, "onUsdPricesUpdated: $buyingPrice $sellingPrice")
        tv_buying_price.text = buyingPrice.toNumberString()
        tv_buying_price.tag = buyingPrice
        tv_selling_price.text = sellingPrice.toNumberString()
        tv_selling_price.tag = sellingPrice
    }

    private fun Number.toNumberString(): String {
        var result: String
        result = if (this is Float && this != this.toLong().toFloat()) {
            "%,.1f".format(this)
        } else {
            "%,d".format(this.toLong())
        }
        if (result == "0") result = "#unknown"
        return result
    }

    @Suppress("DEPRECATION")
    @SuppressLint("SimpleDateFormat")
    private fun share() {
        // export bitmap from view
        val bitmap = Bitmap.createBitmap(
            container.width, container.height,
            Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)
        container.draw(canvas)

        // save bitmap to media store and share it
        val time = SimpleDateFormat("YYYYMMdd_HHmmss").format(Date())
        val path = MediaStore.Images.Media.insertImage(
            contentResolver, bitmap, "ER_$time", null
        )
        val intent = Intent(Intent.ACTION_SEND)
        intent.type = "image/jpg"
        intent.putExtra(Intent.EXTRA_STREAM, Uri.parse(path))
        startActivity(Intent.createChooser(intent, "Share"))
    }

}
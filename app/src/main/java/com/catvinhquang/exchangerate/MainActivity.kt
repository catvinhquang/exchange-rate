package com.catvinhquang.exchangerate

import android.animation.LayoutTransition
import android.app.Dialog
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Bundle
import android.view.HapticFeedbackConstants
import android.view.View
import android.view.Window
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import com.catvinhquang.exchangerate.data.DataProvider
import com.catvinhquang.exchangerate.data.sharedmodel.GoldPrice
import com.catvinhquang.exchangerate.data.sharedmodel.UsdPrice
import com.catvinhquang.exchangerate.data.sharedmodel.UserAssets
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.dialog_assets.*
import java.io.File
import java.io.FileOutputStream

/**
 * Created by QuangCV on 14-Jul-2020
 **/

class MainActivity : AppCompatActivity() {

    private val compositeDisposable = CompositeDisposable()

    private var goldPrice: GoldPrice? = null
    private var usdPrice: UsdPrice? = null
    private var userAssets: UserAssets? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        layout_table.layoutTransition.enableTransitionType(LayoutTransition.CHANGING)
        layout_price.setOnLongClickListener {
            share(it)
            it.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
            true
        }
        layout_user_assets.setOnLongClickListener {
            share(layout_table)
            it.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
            true
        }
        btn_chest.setOnClickListener { collectUserAssets() }

        loadData()
    }

    override fun onDestroy() {
        super.onDestroy()
        compositeDisposable.clear()
    }

    private fun loadData() {
        val getGoldPrice = DataProvider.getGoldPrice().subscribe {
            it.apply {
                tv_global_buying_price.text = internationalBuyingPrice.toNumberString()
                tv_global_buying_price.tag = internationalBuyingPrice
                tv_global_selling_price.text = internationalSellingPrice.toNumberString()
                tv_global_selling_price.tag = internationalSellingPrice
                tv_local_buying_price.text = vietnamBuyingPrice.toNumberString()
                tv_local_buying_price.tag = vietnamBuyingPrice
                tv_local_selling_price.text = vietnamSellingPrice.toNumberString()
                tv_local_selling_price.tag = vietnamSellingPrice
                goldPrice = it
                updateUserAssets()
            }
        }
        compositeDisposable.add(getGoldPrice)

        val getUsdPrice = DataProvider.getUsdPrice().subscribe {
            it.apply {
                tv_buying_price.text = buyingPrice.toNumberString()
                tv_buying_price.tag = buyingPrice
                tv_selling_price.text = sellingPrice.toNumberString()
                tv_selling_price.tag = sellingPrice
                usdPrice = it
                updateUserAssets()
            }
        }
        compositeDisposable.add(getUsdPrice)

        val getUserAssets = DataProvider.getUserAssets().subscribe {
            it.apply {
                userAssets = it
                updateUserAssets()
            }
        }
        compositeDisposable.add(getUserAssets)
    }

    private fun updateUserAssets() {
        if (userAssets != null && goldPrice != null && usdPrice != null) {
            val x = goldPrice!!.vietnamBuyingPrice
            val y = usdPrice!!.buyingPrice
            val value = userAssets!!.run {
                x * taelOfGold + y * usd + savingMoney
            }.toNumberString()

            tv_user_assets.text = value
            layout_user_assets.visibility = View.VISIBLE
        } else {
            layout_user_assets.visibility = View.GONE
        }
    }

    private fun share(v: View) {
        val disposable = Observable.fromCallable {
            // export bitmap from view
            val bitmap = Bitmap.createBitmap(v.width, v.height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            v.draw(canvas)

            // save bitmap to cache directory
            val folder = File(cacheDir, "images")
            folder.mkdirs()
            val file = File(folder, "image.png")
            val stream = FileOutputStream("$file")
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
            stream.close()
            file
        }
            .subscribeOn(Schedulers.computation())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ file ->
                // share image
                FileProvider.getUriForFile(
                    this,
                    "com.catvinhquang.exchangerate.fileprovider",
                    file
                )?.apply {
                    val i = Intent(Intent.ACTION_SEND)
                    i.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    i.setDataAndType(this, contentResolver.getType(this))
                    i.putExtra(Intent.EXTRA_STREAM, this)
                    startActivity(Intent.createChooser(i, getString(R.string.share)))
                }
            }, { it.printStackTrace() })
        compositeDisposable.add(disposable)
    }

    private fun collectUserAssets() {
        val dialog = Dialog(this, R.style.AppTheme_Dialog)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCanceledOnTouchOutside(false)
        dialog.setContentView(R.layout.dialog_assets)

        userAssets?.apply {
            var text = taelOfGold.toString()
            text = if (text.endsWith(".0")) text.replace(".0", "") else text
            dialog.et_tael_of_gold.setText(text)
            dialog.et_usd.setText(usd.toString())
            dialog.et_saving_money.setText(savingMoney.toString())
        } ?: dialog.et_tael_of_gold.requestFocus()

        dialog.btn_clear.setOnClickListener {
            DataProvider.setUserAssets(null)
            userAssets = null
            updateUserAssets()
            dialog.dismiss()
        }
        dialog.btn_complete.setOnClickListener {
            val taelOfGold = dialog.et_tael_of_gold.text.toString()
                .toDoubleOrNull() ?: 0.0
            val usd = dialog.et_usd.text.toString()
                .toIntOrNull() ?: 0
            val savingMoney = dialog.et_saving_money.text.toString()
                .toIntOrNull() ?: 0
            val userAssets = UserAssets(taelOfGold, usd, savingMoney)
            DataProvider.setUserAssets(userAssets)
            MainActivity@ this.userAssets = userAssets
            updateUserAssets()
            dialog.dismiss()
        }
        dialog.show()
    }

}
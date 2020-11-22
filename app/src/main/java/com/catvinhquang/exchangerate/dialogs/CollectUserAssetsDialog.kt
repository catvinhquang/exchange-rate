package com.catvinhquang.exchangerate.dialogs

import android.app.Dialog
import android.content.Context
import android.graphics.Rect
import android.os.Bundle
import android.view.KeyEvent
import android.view.MotionEvent
import androidx.appcompat.app.AlertDialog
import com.catvinhquang.exchangerate.R
import com.catvinhquang.exchangerate.data.DataProvider
import com.catvinhquang.exchangerate.data.sharedmodel.UserAssets
import kotlinx.android.synthetic.main.dialog_assets.*

/**
 * Created by QuangCV on 20-Nov-2020
 **/

class CollectUserAssetsDialog(
    context: Context,
    private var userAssets: UserAssets?,
    listener: OnSaveListener
) : Dialog(context, R.style.AppTheme_Dialog) {

    private var listener: OnSaveListener? = listener

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setCancelable(false)
        setContentView(R.layout.dialog_assets)

        btn_clear.setOnClickListener {
            userAssets = null
            DataProvider.setUserAssets(userAssets)
            dismiss()

            listener?.onSave(userAssets)
        }
        btn_complete.setOnClickListener {
            val newTaelOfGold = et_tael_of_gold.getNumberString()
            val newUsd = et_usd.getNumberString()
            val newSavingMoney = et_saving_money.getNumberString()

            userAssets = userAssets?.apply {
                taelOfGold = newTaelOfGold
                usd = newUsd
                savingMoney = newSavingMoney
            } ?: UserAssets(newTaelOfGold, newUsd, newSavingMoney)
            DataProvider.setUserAssets(userAssets)
            dismiss()

            listener?.onSave(userAssets)
        }

        updateUi()
    }

    override fun onKeyUp(keyCode: Int, event: KeyEvent): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            verifyDataChanges()
            return true
        }
        return super.onKeyUp(keyCode, event)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_UP) {
            val location = IntArray(2)
            window?.decorView?.getLocationOnScreen(location)
            val width = window?.decorView?.width ?: 0
            val height = window?.decorView?.height ?: 0
            val rect = Rect(location[0], location[1], width, height)
            if (!rect.contains(event.rawX.toInt(), event.rawY.toInt())) {
                verifyDataChanges()
                return true
            }
        }
        return super.onTouchEvent(event)
    }

    private fun updateUi() {
        userAssets?.apply {
            et_tael_of_gold.setText(taelOfGold)
            et_usd.setText(usd)
            et_saving_money.setText(savingMoney)
        } ?: et_tael_of_gold.requestFocus()
    }

    private fun verifyDataChanges() {
        val s1 = userAssets?.run { taelOfGold + usd + savingMoney } ?: "000"
        val s2 = et_tael_of_gold.getNumberString() +
                et_usd.getNumberString() +
                et_saving_money.getNumberString()

        if (s1 != s2) {
            AlertDialog.Builder(context)
                .setMessage(R.string.discard_changes)
                .setPositiveButton(R.string.yes) { _, _ -> dismiss() }
                .setNegativeButton(R.string.no, null)
                .show()
        } else {
            dismiss()
        }
    }

    interface OnSaveListener {
        fun onSave(userAssets: UserAssets?)
    }

}
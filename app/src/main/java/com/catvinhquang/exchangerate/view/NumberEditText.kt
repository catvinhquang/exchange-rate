package com.catvinhquang.exchangerate.view

import android.content.Context
import android.graphics.Rect
import android.text.method.DigitsKeyListener
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatEditText
import java.text.DecimalFormat

/**
 * Created by QuangCV on 17-Nov-2020
 */

class NumberEditText : AppCompatEditText {

    companion object {
        private const val thousandSeparator = ","
        private const val decimalSeparator = "."
        private const val accepted = "0123456789$decimalSeparator"
        private val formatter = DecimalFormat("#,###")
    }

    constructor(context: Context) : this(context, null)

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        keyListener = DigitsKeyListener.getInstance(accepted)
    }

    override fun onTextChanged(
        text: CharSequence, start: Int,
        lengthBefore: Int, lengthAfter: Int
    ) {
        if (validate()) {
            format(false)
        }
    }

    override fun onFocusChanged(focused: Boolean, direction: Int, previouslyFocusedRect: Rect?) {
        super.onFocusChanged(focused, direction, previouslyFocusedRect)
        format(true)
    }

    override fun onSelectionChanged(selStart: Int, selEnd: Int) {
        super.onSelectionChanged(selStart, selEnd)
        setSelection(length())
    }

    private fun validate(): Boolean {
        val input = text.toString()

        if (input.isEmpty()) {
            return false
        }

        if (input.startsWith("0") && input.length > 1 && input[1].toString() != decimalSeparator) {
            setText(input.drop(1))
            return false
        }

        if (input == decimalSeparator) {
            setText("")
            return false
        }

        if (input.endsWith(decimalSeparator)) {
            if (input.count { it == decimalSeparator.single() } > 1) {
                setText(input.dropLast(1))
            }
            return false
        }

        return true
    }

    private fun format(removeTrailingZeros: Boolean) {
        val input = text.toString()

        if (input.isEmpty()) {
            return
        }

        val d = input.replace(thousandSeparator, "").toBigDecimal()
        var output = d.toPlainString()

        val isDecimal = output.contains(decimalSeparator)
        output = if (isDecimal) {
            val left = output.substringBefore(decimalSeparator).toBigDecimal()
            var right = output.substringAfter(decimalSeparator)

            if (removeTrailingZeros) {
                while (right.endsWith("0")) {
                    right = right.dropLast(1)
                }
            }

            if (right.isEmpty()) {
                formatter.format(left)
            } else {
                formatter.format(left) + decimalSeparator + right
            }
        } else {
            formatter.format(d)
        }

        if (input != output) {
            setText(output)
        }
    }

    fun getNumberString(): String {
        val s = text.toString()

        if (s.isEmpty()) {
            return "0"
        }

        val d = s.replace(thousandSeparator, "").toBigDecimal()
        var output = d.toPlainString()

        val isDecimal = output.contains(decimalSeparator)
        if (isDecimal) {
            val left = output.substringBefore(decimalSeparator).toBigDecimal()
            var right = output.substringAfter(decimalSeparator)

            while (right.endsWith("0")) {
                right = right.dropLast(1)
            }

            output = if (right.isEmpty()) {
                left.toPlainString()
            } else {
                left.toPlainString() + decimalSeparator + right
            }
        }

        return output
    }

}
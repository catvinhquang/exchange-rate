package com.catvinhquang.exchangerate.view

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.widget.LinearLayout
import com.catvinhquang.exchangerate.R
import com.catvinhquang.exchangerate.data.sharedmodel.GoldPrice
import com.catvinhquang.exchangerate.now
import com.catvinhquang.exchangerate.toTimeString
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import kotlinx.android.synthetic.main.view_gold_price_chart.view.*
import org.json.JSONObject
import java.util.*

/**
 * Created by QuangCV on 24-Nov-2020
 **/

class GoldPriceChartView(context: Context, attrs: AttributeSet?) : LinearLayout(context, attrs) {

    private var data: JSONObject? = null

    private var dsGbp: LineDataSet? = null
    private var dsGsp: LineDataSet? = null
    private var dsLbp: LineDataSet? = null
    private var dsLsp: LineDataSet? = null

    private var isLocalSelected = true

    override fun onFinishInflate() {
        super.onFinishInflate()

        line_chart.apply {
            legend.isEnabled = false
            description.isEnabled = false
            xAxis.setDrawGridLines(false)
            xAxis.position = XAxis.XAxisPosition.BOTTOM
            xAxis.granularity = 1F
            xAxis.valueFormatter = object : ValueFormatter() {
                override fun getFormattedValue(value: Float): String {
                    return value.toLong().toTimeString("dd-MM")
                }
            }
            axisLeft.setDrawGridLines(false)
            axisRight.isEnabled = false
            marker = ChartMarkerView(context).also {
                it.chartView = this
            }
            setScaleEnabled(false)
        }

        iv_global.setOnClickListener {
            if (isLocalSelected) {
                isLocalSelected = false
                iv_global.setBackgroundResource(R.drawable.ripple_yellow)
                iv_local.setBackgroundResource(R.drawable.ripple_gray)
                refresh()
            }
        }
        iv_local.setOnClickListener {
            if (!isLocalSelected) {
                isLocalSelected = true
                iv_local.setBackgroundResource(R.drawable.ripple_yellow)
                iv_global.setBackgroundResource(R.drawable.ripple_gray)
                refresh()
            }
        }
    }

    private fun refresh() {
        // Bug fix: add left padding when set new data
        // Reference: https://stackoverflow.com/questions/43192135/left-padding-when-set-new-data-on-barchart-view-from-mpandroidchart-library
        line_chart.axisLeft.apply {
            mEntries = FloatArray(0)
            mEntryCount = 0
        }

        line_chart.clear()
        val lineData = LineData()
        if (isLocalSelected) {
            lineData.addDataSet(dsLsp)
            lineData.addDataSet(dsLbp)
        } else {
            lineData.addDataSet(dsGsp)
            lineData.addDataSet(dsGbp)
        }
        line_chart.data = lineData
    }

    fun setData(data: JSONObject, withAnimation: Boolean = true) {
        this.data = data
        data.apply {
            val calendar = Calendar.getInstance()

            val vGbp = ArrayList<Entry>()
            val vGsp = ArrayList<Entry>()
            val vLbp = ArrayList<Entry>()
            val vLsp = ArrayList<Entry>()
            for (i in 1..90) {
                val time = calendar.time.time
                val key = time.toTimeString("dd-MM-yyyy")
                val item = optJSONObject(key)

                item?.apply {
                    val x = time.toFloat()
                    val yGbp = optString("gbp").toFloat()
                    val yGsp = optString("gsp").toFloat()
                    val yLbp = optString("lbp").toFloat()
                    val yLsp = optString("lsp").toFloat()
                    if (yGbp != 0F && yGsp != 0F && yLbp != 0F && yLsp != 0F) {
                        vGbp.add(0, Entry(x, yGbp))
                        vGsp.add(0, Entry(x, yGsp))
                        vLbp.add(0, Entry(x, yLbp))
                        vLsp.add(0, Entry(x, yLsp))
                    }
                }

                calendar.add(Calendar.DATE, -1)
            }

            val sColor = Color.parseColor("#e34334")
            val sColor2 = Color.parseColor("#e4837a")
            val bColor = Color.parseColor("#2bc566")
            val bColor2 = Color.parseColor("#97e1b3")
            val hColor = Color.rgb(244, 117, 117)

            dsLsp = LineDataSet(vLsp, null).apply {
                color = sColor
                highLightColor = hColor
                fillColor = sColor2
                fillAlpha = 255
                setDrawFilled(true)
                setDrawValues(false)
                setDrawCircles(false)
            }

            dsLbp = LineDataSet(vLbp, null).apply {
                color = bColor
                highLightColor = hColor
                fillColor = bColor2
                fillAlpha = 255
                setDrawFilled(true)
                setDrawValues(false)
                setDrawCircles(false)
            }

            dsGsp = LineDataSet(vGsp, null).apply {
                color = sColor
                highLightColor = hColor
                fillColor = sColor2
                fillAlpha = 255
                setDrawFilled(true)
                setDrawValues(false)
                setDrawCircles(false)
            }

            dsGbp = LineDataSet(vGbp, null).apply {
                color = bColor
                highLightColor = hColor
                fillColor = bColor2
                fillAlpha = 255
                setDrawFilled(true)
                setDrawValues(false)
                setDrawCircles(false)
            }
        }

        refresh()

        if (withAnimation) {
            line_chart.animateX(1000)
        }
    }

    fun updateCurrentEntry(p: GoldPrice) {
        data?.also {
            val key = now().toTimeString("dd-MM-yyyy")
            val item = it.optJSONObject(key) ?: JSONObject()
            item.apply {
                val b1 = optString("gbp") != p.globalBuyingPrice.toString()
                val b2 = optString("gsp") != p.globalSellingPrice.toString()
                val b3 = optString("lbp") != p.localBuyingPrice.toString()
                val b4 = optString("lsp") != p.localSellingPrice.toString()
                val changed = b1.or(b2).or(b3).or(b4)
                if (changed) {
                    put("gbp", p.globalBuyingPrice)
                    put("gsp", p.globalSellingPrice)
                    put("lbp", p.localBuyingPrice)
                    put("lsp", p.localSellingPrice)
                    it.put(key, item)
                    setData(it, false)
                }
            }
        }
    }

}
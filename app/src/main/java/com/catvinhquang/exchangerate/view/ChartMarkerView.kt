package com.catvinhquang.exchangerate.view

import android.content.Context
import com.catvinhquang.exchangerate.R
import com.catvinhquang.exchangerate.toTimeString
import com.catvinhquang.exchangerate.withSeparators
import com.github.mikephil.charting.components.MarkerView
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.utils.MPPointF
import kotlinx.android.synthetic.main.chart_marker.view.*

/**
 * Created by QuangCV on 24-Nov-2020
 **/

class ChartMarkerView(context: Context) : MarkerView(context, R.layout.chart_marker) {

    override fun refreshContent(e: Entry, highlight: Highlight) {
        val action = context.getString(
            if (highlight.dataSetIndex == 0) R.string.sell else R.string.buy
        )
        val label = e.x.toLong().toTimeString("dd-MM")
        val value = e.y.withSeparators()
        tv_label.text = String.format("%s: [%s] %s", label, action, value)
        super.refreshContent(e, highlight)
    }

    override fun getOffset(): MPPointF {
        return MPPointF(-width.toFloat() / 2, -height.toFloat())
    }

}
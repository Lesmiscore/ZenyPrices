package com.nao20010128nao.zenyprices

import android.databinding.BindingAdapter
import android.graphics.Color
import android.widget.TextView

@BindingAdapter("android:text")
fun TextView.setConversionOrder(order: PriceConverter) {
    val subm = order.conversionProgress.submissions
    text = order.conversionOrder.map { it.name.toUpperCase() }.joinWithStylesWithCustomSeparator { it ->
        val job = order.jobs[it]
        if (subm.isFailed(job)) {
            " > ".colored(Color.RED)
        } else {
            " > "
        }
    }
}

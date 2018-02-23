package com.nao20010128nao.zenyprices

import android.databinding.BindingAdapter
import android.widget.TextView

@BindingAdapter("android:text")
fun TextView.setConversionOrder(order: List<SupportedCurrency>){
    text = order.map { it.name.toUpperCase() }.joinToString(" > ")
}

package com.nao20010128nao.zenyprices

import android.databinding.ViewDataBinding
import android.support.v7.widget.RecyclerView
import android.text.SpannableStringBuilder
import com.nao20010128nao.zenyprices.databinding.PriceConversionBinding
import okhttp3.OkHttpClient
import okhttp3.Request
import java.net.URL

fun withRequest(url: String) = Request.Builder().url(url).build()

fun okClient() = OkHttpClient()

inline fun measure(f: () -> Unit): Long {
    val start = System.nanoTime()
    val end: Long
    try {
        f()
    } finally {
        end = System.nanoTime()
    }
    return end - start
}

fun Any.javaWait() = (this as java.lang.Object).wait()
fun Any.javaNotifyAll() = (this as java.lang.Object).notifyAll()

fun String.toURL(): URL = URL(this)

typealias VH = RecyclerView.ViewHolder

class BindingViewHolder<out T : ViewDataBinding>(val binding: T) : VH(binding.root)
typealias PriceConversionVH = BindingViewHolder<PriceConversionBinding>

inline fun <T> Any.yay(a: T): T = a
inline fun <T> yay(a: T, f: () -> Unit): T = a.also {
    f()
}

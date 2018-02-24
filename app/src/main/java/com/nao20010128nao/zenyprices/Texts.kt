package com.nao20010128nao.zenyprices

import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.ForegroundColorSpan

infix fun CharSequence.then(cs: CharSequence): CharSequence = SpannableStringBuilder().append(this).append(cs)

fun CharSequence.spannable(vararg spans:Any): CharSequence =
        SpannableStringBuilder().append(this).also { ssb->
            spans.forEach {
                ssb.setSpan(it,0,length,Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            }
        }

fun CharSequence.colored(color:Int) = spannable(ForegroundColorSpan(color))


fun <T> Iterable<T>.joinWithStyles(separator: CharSequence = ", ", prefix: CharSequence = "", postfix: CharSequence = "", limit: Int = -1, truncated: CharSequence = "...", transform: ((T) -> CharSequence)? = null): CharSequence =
        joinTo(SpannableStringBuilder(), separator, prefix, postfix, limit, truncated, transform)

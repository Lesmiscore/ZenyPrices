package com.nao20010128nao.zenyprices

import java.math.BigDecimal

inline infix fun BigDecimal?.times(other: BigDecimal?): BigDecimal? =
        other?.let { this?.multiply(it) }

fun min(vararg decimals: BigDecimal): BigDecimal = when (decimals.size) {
    0 -> throw IllegalArgumentException("No BigDecimal present")
    1 -> decimals[0]
    else -> decimals.reduce { a, b ->
        if (a < b) a else b
    }
}
fun max(vararg decimals: BigDecimal): BigDecimal = when (decimals.size) {
    0 -> throw IllegalArgumentException("No BigDecimal present")
    1 -> decimals[0]
    else -> decimals.reduce { a, b ->
        if (a > b) a else b
    }
}

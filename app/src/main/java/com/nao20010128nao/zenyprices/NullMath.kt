package com.nao20010128nao.zenyprices

import java.math.BigDecimal

inline infix fun BigDecimal?.times(other: BigDecimal?): BigDecimal? =
        other?.let { this?.multiply(it) }

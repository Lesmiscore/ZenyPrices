package com.nao20010128nao.zenyprices

import de.bitsharesmunich.graphenej.Converter
import de.bitsharesmunich.graphenej.Price
import de.bitsharesmunich.graphenej.errors.IncompleteAssetError
import java.math.BigDecimal
import java.math.MathContext
import kotlin.math.max


enum class SupportedCurrency {
    BTC, MONA, ZNY, JPY, USD, ETH, BCH
}

typealias TradingPair = Pair<SupportedCurrency, SupportedCurrency>

fun TradingPair.reverse(): TradingPair = second to first
val TradingPair.displayString: String get() = "$second / $first"


val TEN = BigDecimal.TEN!!

fun getConversionRate(price: Price, direction: Int): BigDecimal {
    val base = price.base.asset
    val quote = price.quote.asset
    if (base.precision == -1 || quote.precision == -1) {
        throw IncompleteAssetError("The given asset instance must provide precision information")
    }
    val conversionRate: BigDecimal
    val precisionFactor: BigDecimal
    val mathContext = MathContext(max(base.precision, quote.precision) * 2)
    val baseValue = BigDecimal(price.base.amount.bigIntegerValue())
    val quoteValue = BigDecimal(price.quote.amount.bigIntegerValue())
    //        System.out.println(String.format("base: %d, quote: %d", baseValue.longValue(), quoteValue.longValue()));
    if (direction == Converter.BASE_TO_QUOTE) {
        conversionRate = quoteValue.divide(baseValue, mathContext)
        precisionFactor = TEN.pow(base.precision) / TEN.pow(quote.precision)
    } else {
        conversionRate = baseValue.divide(quoteValue, mathContext)
        precisionFactor = TEN.pow(quote.precision) / TEN.pow(base.precision)
    }
    //        System.out.println(String.format("conversion rate: %.4f, precision factor: %.2f", conversionRate, precisionFactor));
    return conversionRate * precisionFactor
}

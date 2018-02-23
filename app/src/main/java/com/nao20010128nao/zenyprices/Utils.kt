package com.nao20010128nao.zenyprices

import de.bitsharesmunich.graphenej.Asset
import de.bitsharesmunich.graphenej.Converter.BASE_TO_QUOTE
import de.bitsharesmunich.graphenej.Price
import de.bitsharesmunich.graphenej.errors.IncompleteAssetError
import okhttp3.OkHttpClient
import okhttp3.Request
import java.math.BigDecimal
import java.math.MathContext
import java.net.URL
import kotlin.math.max

sealed class CoinsNeeded(assetId: String, symbol: String, precision: Int) : Asset(assetId, symbol, precision) {
    object BTC : CoinsNeeded("1.3.1570", "BTC", 8)
    object MONA : CoinsNeeded("1.3.2570", "MONA", 6)
    object ZNY : CoinsNeeded("1.3.2481", "ZNY", 6)
}

// List of BitShares full nodes, from OpenLedger and CryptoBridge
val bitSharesFullNodes = listOf(
        "wss://proj.tokyo:8090",
        "wss://bts.ai.la/ws",
        "wss://openledger.hk/ws",
        "wss://bitshares.openledger.info/ws"
)

const val zaifLastPriceEndpoint = "https://api.zaif.jp/api/1/last_price"

enum class ZaifLastPrice(val pair: String) {
    BTC_JPY("btc_jpy"), MONA_JPY("mona_jpy");

    fun toUrlString() = "$zaifLastPriceEndpoint/$pair"
}

const val gaitameOnlineEndpoint = "https://www.gaitameonline.com/rateaj/getrate"

enum class GaitameOnlineLastPrice(val pair: String) {
    USD_JPY("USDJPY")
}

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

val TEN = BigDecimal.TEN!!

fun getConversionRate(price: Price, direction: Int): BigDecimal {
    val base = price.base.asset
    val quote = price.quote.asset
    if (base.precision == -1 || quote.precision == -1) {
        throw IncompleteAssetError("The given asset instance must provide precision information")
    }
    val conversionRate: BigDecimal
    val precisionFactor: BigDecimal
    val mathContext = MathContext(max(base.precision, quote.precision))
    val baseValue = BigDecimal(price.base.amount.bigIntegerValue())
    val quoteValue = BigDecimal(price.quote.amount.bigIntegerValue())
    //        System.out.println(String.format("base: %d, quote: %d", baseValue.longValue(), quoteValue.longValue()));
    if (direction == BASE_TO_QUOTE) {
        conversionRate = quoteValue.divide(baseValue, mathContext)
        precisionFactor = TEN.pow(base.precision) / TEN.pow(quote.precision)
    } else {
        conversionRate = baseValue.divide(quoteValue, mathContext)
        precisionFactor = TEN.pow(quote.precision) / TEN.pow(base.precision)
    }
    //        System.out.println(String.format("conversion rate: %.4f, precision factor: %.2f", conversionRate, precisionFactor));
    return conversionRate * precisionFactor
}

fun String.toURL(): URL = URL(this)

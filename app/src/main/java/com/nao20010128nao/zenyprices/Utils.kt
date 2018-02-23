package com.nao20010128nao.zenyprices

import android.databinding.ViewDataBinding
import android.support.v7.widget.RecyclerView
import com.nao20010128nao.zenyprices.databinding.PriceConversionBinding
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

enum class SupportedCurrency {
    BTC, MONA, ZNY, JPY, USD, ETH, BCH
}

typealias TradingPair = Pair<SupportedCurrency, SupportedCurrency>

fun TradingPair.reverse(): TradingPair = second to first
val TradingPair.displayString: String get() = "$second / $first"

sealed class BitSharesAssets(
        assetId: String,
        symbol: String,
        precision: Int,
        val currency: SupportedCurrency
) : Asset(assetId, symbol, precision) {
    object BTC : BitSharesAssets("1.3.1570", "BTC", 8, SupportedCurrency.BTC)
    object MONA : BitSharesAssets("1.3.2570", "MONA", 6, SupportedCurrency.MONA)
    object ZNY : BitSharesAssets("1.3.2481", "ZNY", 6, SupportedCurrency.ZNY)
}

// List of BitShares full nodes, from OpenLedger and CryptoBridge
val bitSharesFullNodes = listOf(
        "wss://proj.tokyo:8090",
        "wss://bts.ai.la/ws",
        "wss://openledger.hk/ws",
        "wss://bitshares.openledger.info/ws"
)

const val zaifLastPriceEndpoint = "https://api.zaif.jp/api/1/last_price"

enum class ZaifLastPrice(val type: String, val tradingPair: TradingPair) {
    BTC_JPY("btc_jpy", SupportedCurrency.BTC to SupportedCurrency.JPY),
    MONA_JPY("mona_jpy", SupportedCurrency.MONA to SupportedCurrency.JPY);

    fun toUrlString() = "$zaifLastPriceEndpoint/$type"
}

const val gaitameOnlineEndpoint = "https://www.gaitameonline.com/rateaj/getrate"

enum class GaitameOnlineLastPrice(val id: String, val tradingPair: TradingPair) {
    USD_JPY("USDJPY", SupportedCurrency.USD to SupportedCurrency.JPY)
}

const val coinCheckLastPriceEndpoint = "https://coincheck.com/api/rate"

enum class CoinCheckLastPrice(val type: String, val tradingPair: TradingPair) {
    BTC_JPY("btc_jpy", SupportedCurrency.BTC to SupportedCurrency.JPY);

    fun toUrlString() = "$coinCheckLastPriceEndpoint/$type"
}

const val bitFlyerLastPriceEndpoint = "https://api.bitflyer.jp/v1/board"

enum class BitFlyerLastPrice(val type: String, val tradingPair: TradingPair) {
    BTC_JPY("BTC_JPY", SupportedCurrency.BTC to SupportedCurrency.JPY),
    BTC_JPY_FX("FX_BTC_JPY", SupportedCurrency.BTC to SupportedCurrency.JPY),
    ETH_BTC("ETH_BTC", SupportedCurrency.ETH to SupportedCurrency.BTC),
    BCH_BTC("BCH_BTC", SupportedCurrency.BCH to SupportedCurrency.BTC);

    fun toUrlString() = "$bitFlyerLastPriceEndpoint/$type"
}

const val coinDeskLastPriceEndpoint = "https://api.coindesk.com/v1/bpi/currentprice.json"

enum class CoinDeskLastPrice(val type: String, val tradingPair: TradingPair) {
    BTC_USD("USD", SupportedCurrency.BTC to SupportedCurrency.USD);//,
    //BTC_GBP("GBP", SupportedCurrency.BTC to SupportedCurrency.JPY),
    //BTC_EUR("EUR", SupportedCurrency.BTC to SupportedCurrency.BTC);
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
    val mathContext = MathContext(max(base.precision, quote.precision) * 2)
    val baseValue = BigDecimal(price.base.amount.bigIntegerValue())
    val quoteValue = BigDecimal(price.quote.amount.bigIntegerValue())
    //        System.out.println(String.format("base: %d, quote: %d", baseValue.longValue(), quoteValue.longValue()));
    if (direction == BASE_TO_QUOTE) {
        conversionRate = quoteValue.divide(baseValue, mathContext)
        precisionFactor = TEN.pow(base.precision).divide(TEN.pow(quote.precision), mathContext)
    } else {
        conversionRate = baseValue.divide(quoteValue, mathContext)
        precisionFactor = TEN.pow(quote.precision).divide(TEN.pow(base.precision), mathContext)
    }
    //        System.out.println(String.format("conversion rate: %.4f, precision factor: %.2f", conversionRate, precisionFactor));
    return conversionRate * precisionFactor
}

fun String.toURL(): URL = URL(this)

typealias VH = RecyclerView.ViewHolder

class BindingViewHolder<out T : ViewDataBinding>(val binding: T) : VH(binding.root)
typealias PriceConversionVH = BindingViewHolder<PriceConversionBinding>

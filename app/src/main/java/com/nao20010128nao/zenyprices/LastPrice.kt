package com.nao20010128nao.zenyprices

import de.bitsharesmunich.graphenej.Asset

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
        "wss://bitshares.openledger.info/ws",
        "wss://bitshares-api.wancloud.io/ws",
        "wss://bit.btsabc.org/ws"
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
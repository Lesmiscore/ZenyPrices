package com.nao20010128nao.zenyprices

import org.junit.Test

class Tests {
    @Test
    fun testTradingPairValidation() {
        val conv = PriceConverter(
                BitSharesJob(BitSharesAssets.ZNY, BitSharesAssets.BTC),
                ZaifJob(ZaifLastPrice.BTC_JPY, false),
                GaitameOnlineJob(GaitameOnlineLastPrice.USD_JPY, true)
        )
        val pair = conv.tradingPair
        assert(pair == SupportedCurrency.ZNY to SupportedCurrency.USD)
    }
}
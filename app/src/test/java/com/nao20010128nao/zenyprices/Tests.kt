package com.nao20010128nao.zenyprices

import org.junit.Test
import java.util.concurrent.Executors

class Tests {
    val exec = Executors.newSingleThreadExecutor()

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

    @Test
    fun testBitSharesRate() {
        val future = exec.getBitSharesPair(BitSharesAssets.BTC, BitSharesAssets.ZNY)
        println(future.get())
    }

    @Test
    fun testBitSharesRate2() {
        val future = exec.getBitSharesPair(BitSharesAssets.ZNY, BitSharesAssets.BTC)
        println(future.get())
    }
}
package com.nao20010128nao.zenyprices


val allPossibleJobs: List<PriceJob>
    get() = (ZaifLastPrice.values().map {
        ZaifJob(it, false)
    } + CoinCheckLastPrice.values().map {
        CoinCheckJob(it, false)
    } + BitFlyerLastPrice.values().map {
        BitFlyerJob(it, false)
    } + BitbankLastPrice.values().map {
        BitbankJob(it, false)
    } + CoinDeskLastPrice.values().map {
        CoinDeskJob(it, false)
    } + GaitameOnlineLastPrice.values().map {
        GaitameOnlineJob(it, false)
    }).run {
        this + map { it.inverse() }
    } + bitSharesAssets.map { a ->
        (bitSharesAssets - a).map { b ->
            BitSharesJob(a, b)
        }
    }.flatMap { it }

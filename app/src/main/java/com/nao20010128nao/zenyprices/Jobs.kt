package com.nao20010128nao.zenyprices

import android.graphics.Color
import com.google.gson.Gson
import com.google.gson.JsonObject
import java.math.BigDecimal
import java.math.MathContext
import java.util.*
import java.util.concurrent.Callable
import java.util.concurrent.ExecutorService
import java.util.concurrent.Future

interface PriceJob {
    fun enqueue(exec: ExecutorService): Future<BigDecimal?>
    fun inverse(): PriceJob
    val tradingPair: TradingPair
}

data class ZaifJob(val pair: ZaifLastPrice, val inverse: Boolean) : PriceJob {
    override fun enqueue(exec: ExecutorService): Future<BigDecimal?> {
        return exec.submit(Callable {
            val url = pair.toUrlString()
            try {
                val body = okClient().newCall(withRequest(url)).execute().body()?.string()!!
                //val body = url.toURL().openStream().bufferedReader()
                val json = Gson().fromJson(body, JsonObject::class.java)
                json["last_price"]?.asBigDecimal
            } catch (e: Throwable) {
                null
            }?.run {
                if (inverse) {
                    BigDecimal.ONE.divide(this, MathContext(15))
                } else {
                    this
                }
            }
        })
    }

    override fun inverse(): PriceJob = ZaifJob(pair, !inverse)

    override val tradingPair: TradingPair = if (inverse) pair.tradingPair.reverse() else pair.tradingPair
}

data class BitSharesJob(val base: BitSharesAssets, val quote: BitSharesAssets) : PriceJob {
    override fun enqueue(exec: ExecutorService): Future<BigDecimal?> =
            exec.getBitSharesPair(base, quote)

    override fun inverse(): PriceJob = BitSharesJob(quote, base)

    override val tradingPair: TradingPair = base.currency to quote.currency
}

data class GaitameOnlineJob(val pair: GaitameOnlineLastPrice, val inverse: Boolean) : PriceJob {
    override fun enqueue(exec: ExecutorService): Future<BigDecimal?> {
        return exec.submit(Callable {
            try {
                val body = okClient()
                        .newCall(withRequest(gaitameOnlineEndpoint))
                        .execute().body()?.string()!!
                val json = Gson().fromJson(body, JsonObject::class.java)
                json["quotes"].asJsonArray
                        .firstOrNull { it.asJsonObject["currencyPairCode"]?.asString == pair.id }
                        ?.asJsonObject?.get("open")?.asBigDecimal
            } catch (e: Throwable) {
                null
            }?.run {
                if (inverse) {
                    BigDecimal.ONE.divide(this, MathContext(15))
                } else {
                    this
                }
            }
        })
    }

    override fun inverse(): PriceJob = GaitameOnlineJob(pair, !inverse)

    override val tradingPair: TradingPair = if (inverse) pair.tradingPair.reverse() else pair.tradingPair
}


data class CoinCheckJob(val pair: CoinCheckLastPrice, val inverse: Boolean) : PriceJob {
    override fun enqueue(exec: ExecutorService): Future<BigDecimal?> {
        return exec.submit(Callable {
            val url = pair.toUrlString()
            try {
                val body = okClient().newCall(withRequest(url)).execute().body()?.string()!!
                val json = Gson().fromJson(body, JsonObject::class.java)
                json["rate"].asBigDecimal
            } catch (e: Throwable) {
                null
            }?.run {
                if (inverse) {
                    BigDecimal.ONE.divide(this, MathContext(15))
                } else {
                    this
                }
            }
        })
    }

    override fun inverse(): PriceJob = CoinCheckJob(pair, !inverse)

    override val tradingPair: TradingPair = if (inverse) pair.tradingPair.reverse() else pair.tradingPair
}


data class BitFlyerJob(val pair: BitFlyerLastPrice, val inverse: Boolean) : PriceJob {
    override fun enqueue(exec: ExecutorService): Future<BigDecimal?> {
        return exec.submit(Callable {
            val url = pair.toUrlString()
            try {
                val body = okClient().newCall(withRequest(url)).execute().body()?.string()!!
                val json = Gson().fromJson(body, JsonObject::class.java)
                json["mid_price"].asBigDecimal
            } catch (e: Throwable) {
                null
            }?.run {
                if (inverse) {
                    BigDecimal.ONE.divide(this, MathContext(15))
                } else {
                    this
                }
            }
        })
    }

    override fun inverse(): PriceJob = BitFlyerJob(pair, !inverse)

    override val tradingPair: TradingPair = if (inverse) pair.tradingPair.reverse() else pair.tradingPair
}

data class CoinDeskJob(val pair: CoinDeskLastPrice, val inverse: Boolean) : PriceJob {
    override fun enqueue(exec: ExecutorService): Future<BigDecimal?> {
        return exec.submit(Callable {
            try {
                val body = okClient()
                        .newCall(withRequest(coinDeskLastPriceEndpoint))
                        .execute().body()?.string()!!
                val json = Gson().fromJson(body, JsonObject::class.java)
                json["bpi"]?.asJsonObject
                        ?.get(pair.type)?.asJsonObject
                        ?.get("rate_float")?.asBigDecimal
            } catch (e: Throwable) {
                null
            }?.run {
                if (inverse) {
                    BigDecimal.ONE.divide(this, MathContext(15))
                } else {
                    this
                }
            }
        })
    }

    override fun inverse(): PriceJob = CoinDeskJob(pair, !inverse)

    override val tradingPair: TradingPair = if (inverse) pair.tradingPair.reverse() else pair.tradingPair
}


class PriceConverter(val jobs: List<PriceJob>) {
    constructor(vararg jobs: PriceJob) : this(Arrays.asList(*jobs))

    // validate that all jobs can be combined
    val tradingPair: TradingPair = jobs.map { it.tradingPair }
            .reduce { a, b ->
                if (a.second == b.first)
                    a.first to b.second
                else
                    throw IllegalArgumentException("$a and $b can't be combined")
            }

    val conversionOrder = jobs.map { it.tradingPair.first } + listOf(jobs.last().tradingPair.second)

    val conversionProgress: PriceConversionProgress = PriceConversionProgress(jobs.toList())

    override fun toString(): String =
            jobs.joinToString("\n") { "${it.tradingPair.displayString}: ${conversionProgress[it]}" }

    fun toStatusText(): CharSequence = conversionProgress.submissions.let { subm ->
        jobs.joinWithStyles("\n") {
            "${it.tradingPair.displayString}: ${conversionProgress[it]}".let { line ->
                if (subm.containsKey(it) && subm[it] == null) {
                    line.colored(Color.RED)
                } else {
                    line
                }
            }
        }
    }

    class PriceConversionProgress internal constructor(val jobs: List<PriceJob>) {
        private val jobsSet = jobs.toSet()

        private val finished: MutableMap<PriceJob, BigDecimal?> = mutableMapOf()

        val submissions get() = Collections.unmodifiableMap(finished)

        @JvmName("submit")
        operator fun set(job: PriceJob, value: BigDecimal?) {
            if (job in jobsSet && job !in finished.keys) {
                finished[job] = value
            }
        }

        operator fun get(job: PriceJob): BigDecimal? = finished[job]

        fun clear() {
            finished.clear()
        }

        fun remainingJobs() = jobsSet - finished.keys

        fun remainingJobsCount() = remainingJobs().size

        fun calculate(): BigDecimal? = when (remainingJobsCount()) {
            0 -> jobs.map { finished[it] }.reduce { a, b -> a times b }
            else -> null
        }
    }
}

fun List<PriceJob>.reverseJobs(): List<PriceJob> = map { it.inverse() }.reversed()
fun PriceConverter.reverseJobs(): PriceConverter = PriceConverter(jobs.reverseJobs())

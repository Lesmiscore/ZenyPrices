package com.nao20010128nao.zenyprices

import com.google.gson.Gson
import com.google.gson.JsonObject
import de.bitsharesmunich.graphenej.Asset
import java.math.BigDecimal
import java.math.MathContext
import java.util.concurrent.Callable
import java.util.concurrent.ExecutorService
import java.util.concurrent.Future

interface PriceJob {
    fun enqueue(exec: ExecutorService): Future<BigDecimal?>
    fun inverse(): PriceJob
}

data class ZaifJob(val pair: ZaifLastPrice, val inverse: Boolean) : PriceJob {
    override fun enqueue(exec: ExecutorService): Future<BigDecimal?> {
        return exec.submit(Callable {
            val url = pair.toUrlString()
            try {
                val body = okClient().newCall(withRequest(url)).execute().body()?.string()!!
                //val body = url.toURL().openStream().bufferedReader()
                val json = Gson().fromJson(body, JsonObject::class.java)
                json["last_price"].asBigDecimal
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
}

data class BitSharesJob(val base: Asset, val quote: Asset) : PriceJob {
    override fun enqueue(exec: ExecutorService): Future<BigDecimal?> =
            exec.getBitSharesPair(base, quote)

    override fun inverse(): PriceJob = BitSharesJob(quote, base)
}

class PriceConverter(vararg val jobs: PriceJob) {
    fun startConversion(): PriceConversionProgress = PriceConversionProgress(jobs.toSet())
}

class PriceConversionProgress internal constructor(val jobs: Set<PriceJob>) {
    private val finished: MutableMap<PriceJob, BigDecimal?> = mutableMapOf()
    operator fun set(job: PriceJob, value: BigDecimal?) {
        if (job in jobs && job !in finished.keys) {
            finished[job] = value
        }
    }

    fun remainingJobs() = jobs - finished.keys

    fun remainingJobsCount() = remainingJobs().size

    fun calculate(): BigDecimal? = finished.values.reduce { a, b -> a times b }
}

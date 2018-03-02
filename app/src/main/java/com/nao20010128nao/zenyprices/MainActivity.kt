package com.nao20010128nao.zenyprices

import android.annotation.SuppressLint
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.Menu
import android.view.MenuItem
import android.view.ViewGroup
import com.google.common.util.concurrent.FutureCallback
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.MoreExecutors
import com.nao20010128nao.zenyprices.databinding.PriceConversionBinding
import java.math.BigDecimal
import java.util.concurrent.Executors
import kotlin.concurrent.thread

class MainActivity : AppCompatActivity() {

    lateinit var list: RecyclerView
    private val convertions: MutableList<PriceConverter> = mutableListOf()
    private val executor = MoreExecutors.listeningDecorator(Executors.newFixedThreadPool(5))

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        list = findViewById(R.id.list)
        list.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)

        listOf(
                ZaifJob(ZaifLastPrice.BTC_JPY, false),
                CoinCheckJob(CoinCheckLastPrice.BTC_JPY, false),
                BitFlyerJob(BitFlyerLastPrice.BTC_JPY, false),
                BitFlyerJob(BitFlyerLastPrice.BTC_JPY_FX, false)
        ).forEach {
            convertions.add(
                    PriceConverter(
                            BitSharesJob(BitSharesAssets.ZNY, BitSharesAssets.BTC),
                            it
                    )
            )
        }

        convertions.add(
                PriceConverter(
                        BitSharesJob(BitSharesAssets.ZNY, BitSharesAssets.MONA),
                        ZaifJob(ZaifLastPrice.MONA_JPY, false)
                )
        )

        convertions.add(
                PriceConverter(
                        BitSharesJob(BitSharesAssets.ZNY, BitSharesAssets.MONA),
                        BitSharesJob(BitSharesAssets.MONA, BitSharesAssets.BTC),
                        ZaifJob(ZaifLastPrice.BTC_JPY, false)
                )
        )

        convertions.add(
                PriceConverter(
                        BitSharesJob(BitSharesAssets.ZNY, BitSharesAssets.MONA),
                        BitSharesJob(BitSharesAssets.MONA, BitSharesAssets.BTC),
                        CoinDeskJob(CoinDeskLastPrice.BTC_USD, false),
                        GaitameOnlineJob(GaitameOnlineLastPrice.USD_JPY, false)
                )
        )

        convertions.add(
                PriceConverter(
                        BitSharesJob(BitSharesAssets.ZNY, BitSharesAssets.MONA),
                        BitSharesJob(BitSharesAssets.MONA, BitSharesAssets.BTC),
                        BitSharesJob(BitSharesAssets.BTC, BitSharesAssets.ZNY)
                )
        )

        convertions.add(
                PriceConverter(
                        BitSharesJob(BitSharesAssets.ZNY, BitSharesAssets.MONA),
                        BitSharesJob(BitSharesAssets.MONA, BitSharesAssets.BTC),
                        BitSharesJob(BitSharesAssets.BTC, BitSharesAssets.ZNY),
                        BitSharesJob(BitSharesAssets.ZNY, BitSharesAssets.MONA),
                        BitSharesJob(BitSharesAssets.MONA, BitSharesAssets.BTC),
                        BitSharesJob(BitSharesAssets.BTC, BitSharesAssets.ZNY)
                )
        )

        convertions.add(
                PriceConverter(
                        BitSharesJob(BitSharesAssets.ZNY, BitSharesAssets.MONA),
                        BitSharesJob(BitSharesAssets.MONA, BitSharesAssets.BTC),
                        BitSharesJob(BitSharesAssets.BTC, BitSharesAssets.ZNY)
                ).reverseJobs()
        )

        convertions.add(
                PriceConverter(
                        BitSharesJob(BitSharesAssets.ZNY, BitSharesAssets.MONA),
                        BitSharesJob(BitSharesAssets.MONA, BitSharesAssets.BTC),
                        BitSharesJob(BitSharesAssets.BTC, BitSharesAssets.ZNY),
                        BitSharesJob(BitSharesAssets.ZNY, BitSharesAssets.MONA),
                        BitSharesJob(BitSharesAssets.MONA, BitSharesAssets.BTC),
                        BitSharesJob(BitSharesAssets.BTC, BitSharesAssets.ZNY)
                ).reverseJobs()
        )

        convertions.add(
                PriceConverter(
                        BitSharesJob(BitSharesAssets.BTC, BitSharesAssets.OpenBTC)
                )
        )

        convertions.add(
                PriceConverter(
                        BitSharesJob(BitSharesAssets.OpenBTC, BitSharesAssets.BTC)
                )
        )


        list.adapter = ConvAdapter(this)

        startCheck()
    }

    fun resetPrices() {
        convertions.forEach {
            it.conversionProgress.clear()
        }
        list.adapter.notifyDataSetChanged()
    }

    fun startCheck() {
        val jobs = convertions.flatMap { it.jobs.toList() }.distinct()
        jobs.forEach { job ->
            Futures.addCallback(job.enqueue(executor) as ListenableFuture<BigDecimal?>, object : FutureCallback<BigDecimal?> {
                override fun onSuccess(result: BigDecimal?) {
                    runOnUiThread {
                        convertions.forEach {
                            it.conversionProgress[job] = result
                        }
                        list.adapter.notifyDataSetChanged()
                    }
                }

                override fun onFailure(err: Throwable?) {
                    runOnUiThread {
                        err?.printStackTrace()
                        convertions.forEach {
                            it.conversionProgress[job] = null
                        }
                        list.adapter.notifyDataSetChanged()
                    }
                }
            })
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean = menu?.run {
        add(0, 0, 0, R.string.update)
        true
    } == true

    override fun onOptionsItemSelected(item: MenuItem?): Boolean = item?.run {
        when (groupId) {
            0 -> when (itemId) {
                0 -> yay(true) {
                    resetPrices()
                    startCheck()
                }
                else -> false
            }
            else -> false
        }
    } != false

    class ConvAdapter(val activity: MainActivity) : RecyclerView.Adapter<PriceConversionVH>() {
        override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): PriceConversionVH =
                PriceConversionVH(PriceConversionBinding.inflate(activity.layoutInflater, parent, false))

        override fun getItemCount(): Int = activity.convertions.size

        override fun onBindViewHolder(holder: PriceConversionVH?, position: Int) {
            holder?.also {
                val conv = activity.convertions[position]
                it.binding.jobs = conv
                it.binding.intermediate = conv.conversionProgress
            }
        }
    }
}

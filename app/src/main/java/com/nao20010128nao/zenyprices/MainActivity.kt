package com.nao20010128nao.zenyprices

import android.annotation.SuppressLint
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.ViewGroup
import com.nao20010128nao.zenyprices.databinding.PriceConversionBinding
import java.util.concurrent.Executors

class MainActivity : AppCompatActivity() {

    lateinit var list: RecyclerView
    private val convertions: MutableList<PriceConverter> = mutableListOf()
    private val executor = Executors.newFixedThreadPool(5)

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        list = findViewById(R.id.list)
        list.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)

        convertions.add(
                PriceConverter(
                        BitSharesJob(BitSharesAssets.ZNY, BitSharesAssets.BTC),
                        ZaifJob(ZaifLastPrice.BTC_JPY, false)
                )
        )
        convertions.add(
                PriceConverter(
                        BitSharesJob(BitSharesAssets.ZNY, BitSharesAssets.MONA),
                        ZaifJob(ZaifLastPrice.MONA_JPY, false)
                )
        )

        list.adapter = ConvAdapter(this)
    }

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

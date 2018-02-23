package com.nao20010128nao.zenyprices

import android.annotation.SuppressLint
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import java.util.concurrent.Executors
import kotlin.concurrent.thread

class MainActivity : AppCompatActivity() {

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val text: TextView = findViewById(R.id.textInit)

        val exec = Executors.newSingleThreadExecutor()
        thread {
            try {
                val monaJpyJob = ZaifJob(ZaifLastPrice.MONA_JPY, false)
                val znyMonaJob = BitSharesJob(CoinsNeeded.ZNY, CoinsNeeded.MONA)
                val monaJpy = monaJpyJob.enqueue(exec).get()
                val znyMona = znyMonaJob.enqueue(exec).get()
                val znyJpy = znyMona times monaJpy
                runOnUiThread {
                    text.text = "JPY > MONA > ZNY: $znyJpy, JPY > MONA: $monaJpy, MONA > ZNY: $znyMona"
                }
            } catch (e: Throwable) {
                runOnUiThread {
                    text.text = Log.getStackTraceString(e)
                }
            }
        }
    }
}

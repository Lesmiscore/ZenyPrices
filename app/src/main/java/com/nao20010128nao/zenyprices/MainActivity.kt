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
                val btcZny = exec.getBitSharesPair(CoinsNeeded.MONA, CoinsNeeded.ZNY).get()
                runOnUiThread {
                    text.text = "ZNY/MONA: $btcZny"
                }
            } catch (e: Throwable) {
                runOnUiThread {
                    text.text = Log.getStackTraceString(e)
                }
            }
        }
    }
}

package com.example.mpp.android

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.mpp.SampleCommonPresenter
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.*

class MainActivity : AppCompatActivity() {
    private val presenter = SampleCommonPresenter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    @SuppressLint("SetTextI18n")
    @Suppress("EXPERIMENTAL_API_USAGE")
    @OptIn(ExperimentalCoroutinesApi::class)
    override fun onResume() {
        super.onResume()

        GlobalScope.launch(Dispatchers.IO) {
            val pashkaNameOrError = try {
                presenter.getPashkaProfile()?.firstName
            } catch (e: Throwable) {
                e.message
            }

            withContext(Dispatchers.Main) {
                someText.text = pashkaNameOrError
            }
        }
    }
}
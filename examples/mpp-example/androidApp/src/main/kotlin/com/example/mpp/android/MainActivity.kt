package com.example.mpp.android

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.mpp.SampleCommonPresenter
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

class MainActivity : AppCompatActivity() {
    private val presenter = SampleCommonPresenter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    @SuppressLint("SetTextI18n")
    @OptIn(ExperimentalCoroutinesApi::class)
    override fun onResume() {
        super.onResume()

        // Use flows, for example, and the full other functionality.
        presenter.getPashkaProfileFlow()
            .onEach { pashka -> someText.text = pashka.firstName }
            .catch { error -> someText.text = error.message ?: "Error!" }
            .launchIn(lifecycleScope)
    }
}
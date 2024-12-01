package com.cs407.spotistats

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class FullStatsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Link the placeholder layout
        setContentView(R.layout.full_stats)
    }
}
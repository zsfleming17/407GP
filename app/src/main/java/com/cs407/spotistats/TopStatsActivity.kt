package com.cs407.spotistats

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity

class TopStatsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.top_stats)


        val accessToken = intent.getStringExtra("ACCESS_TOKEN")
        if (accessToken != null) {
            Log.d("TopStatsActivity", "Access Token: $accessToken")
            // todo: fetch and display user stats from Spotify using this token
        } else {
            Log.e("TopStatsActivity", "Access token is missing!")
        }


    }
}
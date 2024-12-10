package com.cs407.spotistats

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.cs407.spotistats.models.Artist
import com.cs407.spotistats.models.TopArtistsResponse
import com.cs407.spotistats.models.TopTracksResponse
import com.cs407.spotistats.models.Track
import com.cs407.spotistats.network.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class TopStatsActivity : AppCompatActivity() {
    private var timeRange = "short_term"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.top_stats)

        val sharedPreferences = getSharedPreferences("SpotistatsPrefs", MODE_PRIVATE)
        timeRange = sharedPreferences.getString("TopStatsTimeRange", "short_term") ?: "short_term"

        val accessToken = intent.getStringExtra("ACCESS_TOKEN")
        if (accessToken != null) {
            Log.d("TopStatsActivity", "Got access token: ${accessToken.take(20)}...")

            val toggleGroup = findViewById<RadioGroup>(R.id.toggleGroup)
            when (timeRange) {
                "short_term" -> toggleGroup.check(R.id.radio_month)
                "medium_term" -> toggleGroup.check(R.id.radio_6months)
                "long_term" -> toggleGroup.check(R.id.radio_year)
            }

            setupTimeRangeToggle()
            fetchAndDisplayStats(accessToken)

            val fullStatsButton = findViewById<Button>(R.id.logoutButton)
            fullStatsButton.setOnClickListener {
                Log.d("TopStatsActivity", "Passing token to FullStats: ${accessToken.take(20)}...")
                val intent = Intent(this, FullStatsActivity::class.java)
                intent.putExtra("ACCESS_TOKEN", accessToken)
                startActivity(intent)
            }
        } else {
            Log.d("TopStatsActivity", "Access token is missing!")
        }
    }

    private fun setupTimeRangeToggle() {
        val toggleGroup = findViewById<RadioGroup>(R.id.toggleGroup)
        val sharedPreferences = getSharedPreferences("SpotistatsPrefs", MODE_PRIVATE)

        toggleGroup.setOnCheckedChangeListener { _, checkedId ->
            timeRange = when (checkedId) {
                R.id.radio_month -> "short_term"
                R.id.radio_6months -> "medium_term"
                R.id.radio_year -> "long_term"
                else -> "short_term"
            }
            sharedPreferences.edit().putString("TopStatsTimeRange", timeRange).apply()

            val accessToken = intent.getStringExtra("ACCESS_TOKEN") ?: return@setOnCheckedChangeListener
            fetchAndDisplayStats(accessToken)
        }
    }

    private fun fetchAndDisplayStats(accessToken: String) {
        getTopTracks(accessToken) { tracks ->
            val songsContainer = findViewById<LinearLayout>(R.id.songsContainer)
            songsContainer.removeAllViews()

            tracks.take(5).forEach { track ->
                val textView = TextView(this)
                textView.text = track.name
                textView.setTextColor(resources.getColor(R.color.white))
                textView.textSize = 14f
                songsContainer.addView(textView)
            }
        }

        getTopArtists(accessToken) { artists ->
            val artistsContainer = findViewById<LinearLayout>(R.id.artistsContainer)
            artistsContainer.removeAllViews()

            artists.take(5).forEach { artist ->
                val textView = TextView(this)
                textView.text = artist.name
                textView.setTextColor(resources.getColor(R.color.white))
                textView.textSize = 14f
                artistsContainer.addView(textView)
            }
        }

        getTopGenres(accessToken) { genres ->
            val genresContainer = findViewById<LinearLayout>(R.id.genresContainer)
            genresContainer.removeAllViews()

            genres.take(5).forEach { genre ->
                val textView = TextView(this)
                textView.text = genre
                textView.setTextColor(resources.getColor(R.color.white))
                textView.textSize = 14f
                genresContainer.addView(textView)
            }
        }
    }

    fun getTopTracks(accessToken: String, callback: (List<Track>) -> Unit) {
        RetrofitClient.instance.getTopTracks("Bearer $accessToken", timeRange, limit = 5)
            .enqueue(object : Callback<TopTracksResponse> {
                override fun onResponse(call: Call<TopTracksResponse>, response: Response<TopTracksResponse>) {
                    if (response.isSuccessful) {
                        val tracks = response.body()?.items ?: emptyList()
                        callback(tracks)
                    } else {
                        callback(emptyList())
                    }
                }

                override fun onFailure(call: Call<TopTracksResponse>, t: Throwable) {
                    Toast.makeText(this@TopStatsActivity, "Error retrieving data. Check internet connection",
                        Toast.LENGTH_SHORT).show()
                    callback(emptyList())
                }
            })
    }

    fun getTopArtists(accessToken: String, callback: (List<Artist>) -> Unit) {
        RetrofitClient.instance.getTopArtists("Bearer $accessToken", timeRange, limit = 5)
            .enqueue(object : Callback<TopArtistsResponse> {
                override fun onResponse(call: Call<TopArtistsResponse>, response: Response<TopArtistsResponse>) {
                    if (response.isSuccessful) {
                        val artists = response.body()?.items ?: emptyList()
                        callback(artists)
                    } else {
                        callback(emptyList())
                    }
                }

                override fun onFailure(call: Call<TopArtistsResponse>, t: Throwable) {
                    Toast.makeText(this@TopStatsActivity, "Error retrieving data. Check internet connection",
                        Toast.LENGTH_SHORT).show()
                    callback(emptyList())
                }
            })
    }

    fun getTopGenres(accessToken: String, callback: (List<String>) -> Unit) {
        RetrofitClient.instance.getTopArtists("Bearer $accessToken", timeRange, limit = 5)
            .enqueue(object : Callback<TopArtistsResponse> {
                override fun onResponse(call: Call<TopArtistsResponse>, response: Response<TopArtistsResponse>) {
                    if (response.isSuccessful) {
                        val artists = response.body()?.items ?: emptyList()
                        val genres = artists.flatMap { it.genres }
                            .groupingBy { it }
                            .eachCount()
                            .toList()
                            .sortedByDescending { (_, count) -> count }
                            .take(5)
                            .map { it.first.capitalizeWords() }
                        callback(genres)
                    } else {
                        callback(emptyList())
                    }
                }
                override fun onFailure(call: Call<TopArtistsResponse>, t: Throwable) {
                    Toast.makeText(this@TopStatsActivity, "Error retrieving data. Check internet connection",
                        Toast.LENGTH_SHORT).show()
                    callback(emptyList())
                }
            })
    }


    // Helper function for capitalization (genres)
    private fun String.capitalizeWords(): String =
        split(" ").joinToString(" ") { it.replaceFirstChar { char -> char.uppercaseChar() } }
}
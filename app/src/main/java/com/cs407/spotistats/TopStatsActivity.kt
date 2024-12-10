package com.cs407.spotistats

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.LinearLayout
import android.widget.RadioGroup
import android.widget.TextView
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

        val accessToken = intent.getStringExtra("ACCESS_TOKEN")
        if (accessToken != null) {
            Log.d("TopStatsActivity", "Got access token: ${accessToken.take(20)}...")

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
            Log.e("TopStatsActivity", "Access token is missing!")
        }
    }

    private fun setupTimeRangeToggle() {
        val toggleGroup = findViewById<RadioGroup>(R.id.toggleGroup)
        toggleGroup.setOnCheckedChangeListener { _, checkedId ->
            timeRange = when (checkedId) {
                R.id.radio_month -> "short_term"
                R.id.radio_6months -> "medium_term"
                R.id.radio_year -> "long_term"
                else -> "short_term"
            }
            Log.d("TopStatsActivity", "Time range changed to: $timeRange")
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

        // Update top artists
        getTopArtists(accessToken) { artists ->
            val artistsContainer = findViewById<LinearLayout>(R.id.artistsContainer)
            artistsContainer.removeAllViews() // clear any previous entries

            artists.take(5).forEach { artist ->
                val textView = TextView(this)
                textView.text = artist.name
                textView.setTextColor(resources.getColor(R.color.white))
                textView.textSize = 14f
                artistsContainer.addView(textView)
            }
        }

        // Update top genres
        getTopGenres(accessToken) { genres ->
            val genresContainer = findViewById<LinearLayout>(R.id.genresContainer)
            genresContainer.removeAllViews() // Clear any previous entries

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
        RetrofitClient.instance.getTopTracks("Bearer $accessToken", timeRange, limit = 5)  // Added limit=5
            .enqueue(object : Callback<TopTracksResponse> {
                override fun onResponse(call: Call<TopTracksResponse>, response: Response<TopTracksResponse>) {
                    if (response.isSuccessful) {
                        val tracks = response.body()?.items ?: emptyList()
                        callback(tracks)
                    } else {
                        Log.e("TopStatsActivity", "Failed to fetch top tracks: ${response.errorBody()?.string()}")
                        callback(emptyList())
                    }
                }

                override fun onFailure(call: Call<TopTracksResponse>, t: Throwable) {
                    Log.e("TopStatsActivity", "Error fetching top tracks", t)
                    callback(emptyList())
                }
            })
    }

    fun getTopArtists(accessToken: String, callback: (List<Artist>) -> Unit) {
        RetrofitClient.instance.getTopArtists("Bearer $accessToken", timeRange, limit = 5)  // Added limit=5
            .enqueue(object : Callback<TopArtistsResponse> {
                override fun onResponse(call: Call<TopArtistsResponse>, response: Response<TopArtistsResponse>) {
                    if (response.isSuccessful) {
                        val artists = response.body()?.items ?: emptyList()
                        callback(artists)
                    } else {
                        Log.e("TopStatsActivity", "Failed to fetch top artists: ${response.errorBody()?.string()}")
                        callback(emptyList())
                    }
                }

                override fun onFailure(call: Call<TopArtistsResponse>, t: Throwable) {
                    Log.e("TopStatsActivity", "Error fetching top artists", t)
                    callback(emptyList())
                }
            })
    }

    fun getTopGenres(accessToken: String, callback: (List<String>) -> Unit) {
        RetrofitClient.instance.getTopArtists("Bearer $accessToken", timeRange, limit = 5)  // Added limit=5
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
                        Log.e("TopStatsActivity", "Failed to fetch genres: ${response.errorBody()?.string()}")
                        callback(emptyList())
                    }
                }

                override fun onFailure(call: Call<TopArtistsResponse>, t: Throwable) {
                    Log.e("TopStatsActivity", "Error fetching genres", t)
                    callback(emptyList())
                }
            })
    }


    // Helper function for capitalization (genres)
    private fun String.capitalizeWords(): String =
        split(" ").joinToString(" ") { it.replaceFirstChar { char -> char.uppercaseChar() } }
}
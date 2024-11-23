package com.cs407.spotistats

import android.os.Bundle
import android.util.Log
import android.widget.RadioGroup
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
            setupTimeRangeToggle()
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
        }
    }

    fun getTopTracks(accessToken: String, callback: (List<Track>) -> Unit) {
        RetrofitClient.instance.getTopTracks("Bearer $accessToken", timeRange)
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
        RetrofitClient.instance.getTopArtists("Bearer $accessToken", timeRange)
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
        RetrofitClient.instance.getTopArtists("Bearer $accessToken", timeRange)
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

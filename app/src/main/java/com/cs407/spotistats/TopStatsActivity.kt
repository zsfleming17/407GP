package com.cs407.spotistats

import android.os.Bundle
import android.util.Log
import android.widget.LinearLayout
import android.widget.RadioGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
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
            setupTimeRangeToggle(accessToken)
            fetchTopStats(accessToken)
            fetchTopGenres(accessToken)
        } else {
            Log.e("TopStatsActivity", "Access token is missing!")
        }
    }

    private fun setupTimeRangeToggle(accessToken: String) {
        val toggleGroup = findViewById<RadioGroup>(R.id.toggleGroup)
        toggleGroup.setOnCheckedChangeListener { _, checkedId ->
            timeRange = when (checkedId) {
                R.id.radio_month -> "short_term"  // 1 month
                R.id.radio_6months -> "medium_term" // 6 months
                R.id.radio_year -> "long_term"     // 1 year
                else -> "short_term"
            }
            fetchTopStats(accessToken)
        }
    }

    private fun fetchTopStats(accessToken: String) {
        fetchTopTracks(accessToken)
        fetchTopArtists(accessToken)
        fetchTopGenres(accessToken)
    }

    private fun fetchTopTracks(accessToken: String) {
        RetrofitClient.instance.getTopTracks("Bearer $accessToken", timeRange)
            .enqueue(object : Callback<TopTracksResponse> {
                override fun onResponse(
                    call: Call<TopTracksResponse>,
                    response: Response<TopTracksResponse>
                ) {
                    if (response.isSuccessful) {
                        val tracks = response.body()?.items
                        Log.d("TopStatsActivity", "Top Tracks: $tracks")
                        if (tracks != null && tracks.isNotEmpty()) {
                            updateSongsUI(tracks)
                        } else {
                            showNoDataMessage(R.id.songsContainer, "No top tracks available")
                        }
                    } else {
                        Log.e(
                            "TopStatsActivity",
                            "Failed to fetch top tracks: ${response.errorBody()?.string()}"
                        )
                    }
                }

                override fun onFailure(call: Call<TopTracksResponse>, t: Throwable) {
                    Log.e("TopStatsActivity", "Error fetching top tracks", t)
                }
            })
    }

    private fun fetchTopArtists(accessToken: String) {
        RetrofitClient.instance.getTopArtists("Bearer $accessToken", timeRange)
            .enqueue(object : Callback<TopArtistsResponse> {
                override fun onResponse(
                    call: Call<TopArtistsResponse>,
                    response: Response<TopArtistsResponse>
                ) {
                    if (response.isSuccessful) {
                        val artists = response.body()?.items
                        Log.d("TopStatsActivity", "Top Artists: $artists")
                        if (artists != null && artists.isNotEmpty()) {
                            updateArtistsUI(artists.map { it.name })
                        } else {
                            showNoDataMessage(R.id.artistsContainer, "No top artists available")
                        }
                    } else {
                        Log.e(
                            "TopStatsActivity",
                            "Failed to fetch top artists: ${response.errorBody()?.string()}"
                        )
                    }
                }

                override fun onFailure(call: Call<TopArtistsResponse>, t: Throwable) {
                    Log.e("TopStatsActivity", "Error fetching top artists", t)
                }
            })
    }

    private fun fetchTopGenres(accessToken: String) {
        RetrofitClient.instance.getTopArtists("Bearer $accessToken", timeRange).enqueue(object : Callback<TopArtistsResponse> {
            override fun onResponse(call: Call<TopArtistsResponse>, response: Response<TopArtistsResponse>) {
                if (response.isSuccessful) {
                    val artists = response.body()?.items
                    if (artists != null) {
                        val genres = artists.flatMap { it.genres } // Use artists to generate genres
                            .groupingBy { it } // Group by genre
                            .eachCount() // Count num of genres
                            .toList()
                            .sortedByDescending { (_, count) -> count } // Sort in descending order
                            .take(5) // Top 5 genres
                            .map { it.first.capitalizeWords() } // Capitalize genres
                        updateGenresUI(genres)
                    }
                } else {
                    Log.e("TopStatsActivity", "Failed to fetch top artists for genres: ${response.errorBody()?.string()}")
                }
            }

            override fun onFailure(call: Call<TopArtistsResponse>, t: Throwable) {
                Log.e("TopStatsActivity", "Error fetching top artists for genres", t)
            }
        })
    }

    private fun updateSongsUI(tracks: List<Track>) {
        val container = findViewById<LinearLayout>(R.id.songsContainer)
        container.removeAllViews()
        tracks.forEach { track ->
            val textView = TextView(this).apply {
                text = "${track.name} by ${track.artists.joinToString { it.name }}"
            }
            container.addView(textView)
        }
    }

    private fun updateArtistsUI(artists: List<String>) {
        val container = findViewById<LinearLayout>(R.id.artistsContainer)
        container.removeAllViews()
        artists.forEach { artistName ->
            val textView = TextView(this).apply {
                text = artistName
            }
            container.addView(textView)
        }
    }

    private fun updateGenresUI(genres: List<String>) {
        val container = findViewById<LinearLayout>(R.id.genresContainer)
        container.removeAllViews()
        genres.forEach { genre ->
            val textView = TextView(this).apply {
                text = genre
            }
            container.addView(textView)
        }
    }

    private fun String.capitalizeWords(): String = split(" ").joinToString(" ")
    { it.replaceFirstChar { char -> char.uppercase() } }



    private fun showNoDataMessage(containerId: Int, message: String) {
        val container = findViewById<LinearLayout>(containerId)
        container.removeAllViews()
        val textView = TextView(this).apply {
            text = message
        }
        container.addView(textView)
    }
}

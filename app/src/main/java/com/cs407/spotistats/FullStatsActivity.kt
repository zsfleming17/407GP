package com.cs407.spotistats

import android.os.Bundle
import android.util.Log
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.setMargins
import com.cs407.spotistats.models.Artist
import com.cs407.spotistats.models.TopArtistsResponse
import com.cs407.spotistats.models.TopTracksResponse
import com.cs407.spotistats.models.Track
import com.cs407.spotistats.network.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class FullStatsActivity : AppCompatActivity() {
    private var timeRange = "short_term"
    private var currentCategory = "songs"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.full_stats)

        val accessToken = intent.getStringExtra("ACCESS_TOKEN")
        if (accessToken != null) {
            Log.d("FullStatsActivity", "Got access token: ${accessToken.take(5)}...")
            findViewById<RadioButton>(R.id.radio_songs).isChecked = true
            findViewById<RadioButton>(R.id.radio_month).isChecked = true

            setupTimeRangeToggle(accessToken)
            setupCategoryToggle(accessToken)
            fetchFullStats(accessToken)
        } else {
            Log.e("FullStatsActivity", "No valid access token")
        }
    }

    private fun setupTimeRangeToggle(accessToken: String) {
        val timeRangeGroup = findViewById<RadioGroup>(R.id.timeRangeGroup)
        timeRangeGroup.setOnCheckedChangeListener { _, checkedId ->
            timeRange = when (checkedId) {
                R.id.radio_month -> "short_term"
                R.id.radio_6months -> "medium_term"
                R.id.radio_year -> "long_term"
                else -> "short_term"
            }
            fetchFullStats(accessToken)
        }
    }

    private fun setupCategoryToggle(accessToken: String) {
        val categoryToggleGroup = findViewById<RadioGroup>(R.id.categoryToggleGroup)
        categoryToggleGroup.setOnCheckedChangeListener { _, checkedId ->
            currentCategory = when (checkedId) {
                R.id.radio_songs -> "songs"
                R.id.radio_artists -> "artists"
                R.id.radio_genres -> "genres"
                else -> "songs"
            }
            fetchFullStats(accessToken)
        }
    }

    private fun createStyledTextView(text: String, index: Int): TextView {
        return TextView(this).apply {
            this.text = "${index + 1}. $text"
            setTextColor(resources.getColor(R.color.white))
            textSize = 16f
            setPadding(16, 12, 16, 12)
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(8)
            }
        }
    }

    private fun fetchFullStats(accessToken: String) {
        val contentContainer = findViewById<LinearLayout>(R.id.contentContainer)

        contentContainer.removeAllViews()

        when (currentCategory) {
            "songs" -> {
                Log.d("FullStatsActivity", "Fetching songs...")
                getFullTracks(accessToken) { tracks ->
                    Log.d("FullStatsActivity", "Received ${tracks.size} tracks")
                    runOnUiThread {
                        if (tracks.isEmpty()) {
                            Log.d("FullStatsActivity", "No tracks received")
                            val textView = TextView(this)
                            textView.text = "No tracks found"
                            textView.setTextColor(resources.getColor(R.color.white))
                            contentContainer.addView(textView)
                        } else {
                            tracks.take(50).forEachIndexed { index, track ->
                                val artistNames = track.artists.joinToString(", ") { it.name }
                                val displayText = "${track.name} - $artistNames"
                                Log.d("FullStatsActivity", "Adding track $index: $displayText")
                                contentContainer.addView(createStyledTextView(displayText, index))
                            }
                        }
                    }
                }
            }

            "artists" -> {
                Log.d("FullStatsActivity", "Fetching artists...")
                getFullArtists(accessToken) { artists ->
                    Log.d("FullStatsActivity", "Received ${artists.size} artists")
                    runOnUiThread {
                        if (artists.isEmpty()) {
                            val textView = TextView(this)
                            textView.text = "No artists found"
                            textView.setTextColor(resources.getColor(R.color.white))
                            contentContainer.addView(textView)
                        } else {
                            artists.take(50).forEachIndexed { index, artist ->
                                contentContainer.addView(createStyledTextView(artist.name, index))
                            }
                        }
                    }
                }
            }

            "genres" -> {
                Log.d("FullStatsActivity", "Fetching genres...")
                getFullGenres(accessToken) { genres ->
                    Log.d("FullStatsActivity", "Received ${genres.size} genres")
                    runOnUiThread {
                        if (genres.isEmpty()) {
                            val textView = TextView(this)
                            textView.text = "No genres found"
                            textView.setTextColor(resources.getColor(R.color.white))
                            contentContainer.addView(textView)
                        } else {
                            genres.take(25).forEachIndexed { index, genre ->
                                contentContainer.addView(createStyledTextView(genre, index))
                            }
                        }
                    }
                }
            }
        }
    }

    private fun getFullTracks(accessToken: String, callback: (List<Track>) -> Unit) {
        RetrofitClient.instance.getTopTracks("Bearer $accessToken", timeRange, limit = 50)
            .enqueue(object : Callback<TopTracksResponse> {
                override fun onResponse(
                    call: Call<TopTracksResponse>,
                    response: Response<TopTracksResponse>
                ) {
                    if (response.isSuccessful) {
                        val tracks = response.body()?.items ?: emptyList()
                        Log.d("FullStatsActivity", "Tracks received: ${tracks.size}")
                        callback(tracks)
                    } else {
                        callback(emptyList())
                    }
                }

                override fun onFailure(call: Call<TopTracksResponse>, t: Throwable) {
                    callback(emptyList())
                }
            })
    }

    private fun getFullArtists(accessToken: String, callback: (List<Artist>) -> Unit) {
        RetrofitClient.instance.getTopArtists("Bearer $accessToken", timeRange, limit = 50)
            .enqueue(object : Callback<TopArtistsResponse> {
                override fun onResponse(
                    call: Call<TopArtistsResponse>,
                    response: Response<TopArtistsResponse>
                ) {
                    if (response.isSuccessful) {
                        val artists = response.body()?.items ?: emptyList()
                        Log.d("FullStatsActivity", "Artists received: ${artists.size}")
                        callback(artists)
                    } else {
                        callback(emptyList())
                    }
                }

                override fun onFailure(call: Call<TopArtistsResponse>, t: Throwable) {
                    callback(emptyList())
                }
            })
    }

    private fun getFullGenres(accessToken: String, callback: (List<String>) -> Unit) {
        RetrofitClient.instance.getTopArtists("Bearer $accessToken", timeRange, limit = 25)
            .enqueue(object : Callback<TopArtistsResponse> {
                override fun onResponse(
                    call: Call<TopArtistsResponse>,
                    response: Response<TopArtistsResponse>
                ) {
                    if (response.isSuccessful) {
                        val artists = response.body()?.items ?: emptyList()
                        val genres = artists.flatMap { it.genres }
                            .groupingBy { it }
                            .eachCount()
                            .toList()
                            .sortedByDescending { it.second }
                            .map { (genre) ->
                                genre.split(" ")
                                    .joinToString(" ") { word ->
                                        word.replaceFirstChar { char ->
                                            char.uppercase()
                                        }
                                    }
                            }
                        callback(genres)
                    } else {
                        callback(emptyList())
                    }
                }

                override fun onFailure(call: Call<TopArtistsResponse>, t: Throwable) {
                    callback(emptyList())
                }
            })
    }
}



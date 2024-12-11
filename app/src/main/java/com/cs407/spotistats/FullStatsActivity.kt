package com.cs407.spotistats

import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
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

        val sharedPreferences = getSharedPreferences("SpotistatsPrefs", MODE_PRIVATE)
        timeRange = sharedPreferences.getString("FullStatsTimeRange", "short_term") ?: "short_term"
        currentCategory = sharedPreferences.getString("FullStatsCategory", "songs") ?: "songs"

        val accessToken = intent.getStringExtra("ACCESS_TOKEN")
        if (accessToken != null) {
            Log.d("FullStatsActivity", "Got access token: ${accessToken.take(5)}...")
            val timeRangeGroup = findViewById<RadioGroup>(R.id.timeRangeGroup)
            when (timeRange) {
                "short_term" -> timeRangeGroup.check(R.id.radio_month)
                "medium_term" -> timeRangeGroup.check(R.id.radio_6months)
                "long_term" -> timeRangeGroup.check(R.id.radio_year)
            }

            val categoryToggleGroup = findViewById<RadioGroup>(R.id.categoryToggleGroup)
            when (currentCategory) {
                "songs" -> categoryToggleGroup.check(R.id.radio_songs)
                "artists" -> categoryToggleGroup.check(R.id.radio_artists)
                "genres" -> categoryToggleGroup.check(R.id.radio_genres)
            }

            setupTimeRangeToggle(accessToken)
            setupCategoryToggle(accessToken)
            fetchFullStats(accessToken)
        } else {
            Log.e("FullStatsActivity", "No valid access token")
        }
    }


    private fun setupTimeRangeToggle(accessToken: String) {
        val sharedPreferences = getSharedPreferences("SpotistatsPrefs", MODE_PRIVATE)
        val timeRangeGroup = findViewById<RadioGroup>(R.id.timeRangeGroup)

        fun updateTabStyles() {
            for (i in 0 until timeRangeGroup.childCount) {
                val radioButton = timeRangeGroup.getChildAt(i) as RadioButton
                if (radioButton.isChecked) {
                    radioButton.setBackgroundResource(R.drawable.radio_button_selected)
                    radioButton.setTextColor(resources.getColor(R.color.white))
                } else {
                    radioButton.setBackgroundResource(R.drawable.radio_button_unselected)
                    radioButton.setTextColor(resources.getColor(R.color.black))
                }
            }
        }

        timeRangeGroup.setOnCheckedChangeListener { _, checkedId ->
            timeRange = when (checkedId) {
                R.id.radio_month -> "short_term"
                R.id.radio_6months -> "medium_term"
                R.id.radio_year -> "long_term"
                else -> "short_term"
            }
            sharedPreferences.edit().putString("FullStatsTimeRange", timeRange).apply()
            fetchFullStats(accessToken)
            updateTabStyles()
        }
        updateTabStyles()
    }

    private fun setupCategoryToggle(accessToken: String) {
        val sharedPreferences = getSharedPreferences("SpotistatsPrefs", MODE_PRIVATE)
        val categoryToggleGroup = findViewById<RadioGroup>(R.id.categoryToggleGroup)

        fun updateTabStyles() {
            for (i in 0 until categoryToggleGroup.childCount) {
                val radioButton = categoryToggleGroup.getChildAt(i) as RadioButton
                if (radioButton.isChecked) {
                    radioButton.setBackgroundResource(R.drawable.radio_button_selected)
                    radioButton.setTextColor(resources.getColor(R.color.white))
                } else {
                    radioButton.setBackgroundResource(R.drawable.radio_button_unselected)
                    radioButton.setTextColor(resources.getColor(R.color.black))
                }
            }
        }

        categoryToggleGroup.setOnCheckedChangeListener { _, checkedId ->
            currentCategory = when (checkedId) {
                R.id.radio_songs -> "songs"
                R.id.radio_artists -> "artists"
                R.id.radio_genres -> "genres"
                else -> "songs"
            }
            sharedPreferences.edit().putString("FullStatsCategory", currentCategory).apply()
            fetchFullStats(accessToken)
            updateTabStyles()
        }
        updateTabStyles()
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
        val loadingOverlay = findViewById<FrameLayout>(R.id.loadingOverlay)

        loadingOverlay.visibility = View.VISIBLE
        loadingOverlay.bringToFront()

        when (currentCategory) {
            "songs" -> getFullTracks(accessToken) { tracks ->
                runOnUiThread {
                    loadingOverlay.visibility = View.GONE
                    contentContainer.visibility = View.VISIBLE
                    contentContainer.removeAllViews()
                    if (tracks.isEmpty()) {
                        contentContainer.addView(createStyledTextView("No tracks found", 0))
                    } else {
                        tracks.take(50).forEachIndexed { index, track ->
                            val artistNames = track.artists.joinToString(", ") { it.name }
                            val displayText = "${track.name} - $artistNames"
                            contentContainer.addView(createStyledTextView(displayText, index))
                        }
                    }
                }
            }

            "artists" -> getFullArtists(accessToken) { artists ->
                runOnUiThread {
                    loadingOverlay.visibility = View.GONE
                    contentContainer.visibility = View.VISIBLE
                    contentContainer.removeAllViews()
                    if (artists.isEmpty()) {
                        contentContainer.addView(createStyledTextView("No artists found", 0))
                    } else {
                        artists.take(50).forEachIndexed { index, artist ->
                            contentContainer.addView(createStyledTextView(artist.name, index))
                        }
                    }
                }
            }

            "genres" -> getFullGenres(accessToken) { genres ->
                runOnUiThread {
                    loadingOverlay.visibility = View.GONE
                    contentContainer.visibility = View.VISIBLE
                    contentContainer.removeAllViews()
                    if (genres.isEmpty()) {
                        contentContainer.addView(createStyledTextView("No genres found", 0))
                    } else {
                        genres.take(25).forEachIndexed { index, genre ->
                            contentContainer.addView(createStyledTextView(genre, index))
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
                    Toast.makeText(this@FullStatsActivity, "Error retrieving data. Check internet connection",
                        Toast.LENGTH_SHORT).show()
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
                    Toast.makeText(this@FullStatsActivity, "Error retrieving data. Check internet connection",
                        Toast.LENGTH_SHORT).show()
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
                    Toast.makeText(this@FullStatsActivity, "Error retrieving data. Check internet connection",
                        Toast.LENGTH_SHORT).show()
                    callback(emptyList())
                }
            })
    }
}



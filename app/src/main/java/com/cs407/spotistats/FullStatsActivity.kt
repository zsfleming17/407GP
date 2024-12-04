package com.cs407.spotistats

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.full_stats)

        val accessToken = intent.getStringExtra("ACCESS_TOKEN")
        if (accessToken != null) {
        } else {
            Log.e("FullStatsActivity", "Access token is missing!")
        }
    }

    fun getFullTracks(accessToken: String, callback: (List<Track>) -> Unit) {
        RetrofitClient.instance.getTopTracks("Bearer $accessToken", timeRange, limit = 100)
            .enqueue(object : Callback<TopTracksResponse> {
                override fun onResponse(call: Call<TopTracksResponse>, response: Response<TopTracksResponse>) {
                    if (response.isSuccessful) {
                        val tracks = response.body()?.items ?: emptyList()
                        callback(tracks)
                    } else {
                        Log.e("FullStatsActivity", "Failed to fetch full tracks: ${response.errorBody()?.string()}")
                        callback(emptyList())
                    }
                }

                override fun onFailure(call: Call<TopTracksResponse>, t: Throwable) {
                    Log.e("FullStatsActivity", "Error fetching full tracks", t)
                    callback(emptyList())
                }
            })
    }

    fun getFullArtists(accessToken: String, callback: (List<Artist>) -> Unit) {
        RetrofitClient.instance.getTopArtists("Bearer $accessToken", timeRange, limit = 100)
            .enqueue(object : Callback<TopArtistsResponse> {
                override fun onResponse(call: Call<TopArtistsResponse>, response: Response<TopArtistsResponse>) {
                    if (response.isSuccessful) {
                        val artists = response.body()?.items ?: emptyList()
                        callback(artists)
                    } else {
                        Log.e("FullStatsActivity", "Failed to fetch full artists: ${response.errorBody()?.string()}")
                        callback(emptyList())
                    }
                }

                override fun onFailure(call: Call<TopArtistsResponse>, t: Throwable) {
                    Log.e("FullStatsActivity", "Error fetching full artists", t)
                    callback(emptyList())
                }
            })
    }

    fun getFullGenres(accessToken: String, callback: (List<String>) -> Unit) {
        RetrofitClient.instance.getTopArtists("Bearer $accessToken", timeRange, limit = 100)
            .enqueue(object : Callback<TopArtistsResponse> {
                override fun onResponse(call: Call<TopArtistsResponse>, response: Response<TopArtistsResponse>) {
                    if (response.isSuccessful) {
                        val artists = response.body()?.items ?: emptyList()
                        val genres = artists.flatMap { it.genres }
                            .groupingBy { it }
                            .eachCount()
                            .toList()
                            .sortedByDescending { (_, count) -> count }
                            .map { it.first.capitalizeWords() }
                        callback(genres)
                    } else {
                        Log.e("FullStatsActivity", "Failed to fetch genres: ${response.errorBody()?.string()}")
                        callback(emptyList())
                    }
                }

                override fun onFailure(call: Call<TopArtistsResponse>, t: Throwable) {
                    Log.e("FullStatsActivity", "Error fetching genres", t)
                    callback(emptyList())
                }
            })
    }

    private fun String.capitalizeWords(): String =
        split(" ").joinToString(" ") { it.replaceFirstChar { char -> char.uppercaseChar() } }
}
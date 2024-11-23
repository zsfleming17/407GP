package com.cs407.spotistats.network

import com.cs407.spotistats.models.TopArtistsResponse
import com.cs407.spotistats.models.TopTracksResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

interface SpotifyService {
    @GET("v1/me/top/tracks")
    fun getTopTracks(
        @Header("Authorization") token: String,
        @Query("time_range") timeRange: String = "short_term",
        @Query("limit") limit: Int = 5
    ): Call<TopTracksResponse>

    @GET("v1/me/top/artists")
    fun getTopArtists(
        @Header("Authorization") token: String,
        @Query("time_range") timeRange: String = "short_term",
        @Query("limit") limit: Int = 5
    ): Call<TopArtistsResponse>
}
package com.cs407.spotistats.models

data class TopTracksResponse (
    val items: List<Track>
    )

    data class Track(
        val name: String,
        val artists: List<Artist>,
        val album: Album
    )

    data class Artist(
        val name: String,
        val genres: List<String>
    )

    data class Album(
        val name: String
    )
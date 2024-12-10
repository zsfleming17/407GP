package com.cs407.spotistats

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.spotify.sdk.android.auth.AuthorizationClient
import com.spotify.sdk.android.auth.AuthorizationRequest
import com.spotify.sdk.android.auth.AuthorizationResponse

class MainActivity : AppCompatActivity() {
    private val CLIENT_ID = BuildConfig.SPOTIFY_CLIENT_ID
    private val REDIRECT_URI = "spotistats://callback"
    private val REQUEST_CODE = 1337

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val loginButton = findViewById<Button>(R.id.button)
        loginButton.setOnClickListener {
            startSpotifyAuthentication()
        }
    }

    private fun startSpotifyAuthentication() {
        val builder = AuthorizationRequest.Builder(
            CLIENT_ID,
            AuthorizationResponse.Type.TOKEN,
            REDIRECT_URI
        )
            .setScopes(arrayOf("streaming", "user-library-read", "user-top-read"))
        val request = builder.build()
        AuthorizationClient.openLoginActivity(this, REQUEST_CODE, request)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE) {
            val response = AuthorizationClient.getResponse(resultCode, data)
            Log.d("MainActivity", "Received response type: ${response.type}")
            when (response.type) {
                AuthorizationResponse.Type.TOKEN -> {
                    val accessToken = response.accessToken
                    if (!accessToken.isNullOrEmpty()) {
                        Log.d("MainActivity", "Access Token: $accessToken")
                        val intent = Intent(this, TopStatsActivity::class.java)
                        intent.putExtra("ACCESS_TOKEN", accessToken)
                        startActivity(intent)
                        finish()
                    } else {
                        Log.d("MainActivity", "Access token is null or empty!")
                    }
                }
                AuthorizationResponse.Type.ERROR -> {
                    val error = response.error
                    Log.d("MainActivity", "Error during Spotify Authentication: $error")
                }

                AuthorizationResponse.Type.EMPTY -> {
                    Log.d("MainActivity", "Authentication was canceled")
                }

                else -> {
                    Log.d("MainActivity", "Unexpected response type: ${response.type}")
                }
            }
        } else {
            Log.d("MainActivity", "Request code does not match: $requestCode")
        }
    }
}
package com.example.eventra.untils

import android.content.Context
import android.util.Log
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit

class ApiClient(private val context: Context) {

    private val sessionManager = SessionManager(context)
    private val baseUrl = context.getString(com.example.eventra.R.string.server)

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .addInterceptor { chain ->
            val originalRequest = chain.request()
            val requestBuilder = originalRequest.newBuilder()

            // Aggiungi automaticamente l'Authorization header se l'utente è loggato
            sessionManager.getAuthorizationHeader()?.let { authHeader ->
                requestBuilder.addHeader("Authorization", authHeader)
            }

            requestBuilder.addHeader("Content-Type", "application/json")
            requestBuilder.addHeader("Accept", "application/json")

            val request = requestBuilder.build()
            Log.d("ApiClient", "Request: ${request.method} ${request.url}")

            val response = chain.proceed(request)

            // Se ricevi 401, prova a fare refresh del token
            if (response.code == 401 && sessionManager.isLoggedIn()) {
                response.close()

                // Prova refresh token
                val refreshed = refreshTokenSync()
                if (refreshed) {
                    // Rifai la richiesta con il nuovo token
                    val newRequest = originalRequest.newBuilder()
                        .addHeader("Authorization", sessionManager.getAuthorizationHeader() ?: "")
                        .addHeader("Content-Type", "application/json")
                        .addHeader("Accept", "application/json")
                        .build()

                    return@addInterceptor chain.proceed(newRequest)
                } else {
                    sessionManager.clearSession()
                }
            }

            response
        }
        .build()

    private fun refreshTokenSync(): Boolean {
        return try {
            val refreshToken = sessionManager.getRefreshToken() ?: return false

            val url = "$baseUrl/api/utente/refresh?refreshtoken=$refreshToken"
            val request = Request.Builder()
                .url(url)
                .post("".toRequestBody())
                .build()

            val response = client.newCall(request).execute()
            val responseBody = response.body?.string()

            if (response.isSuccessful && responseBody != null) {
                val jsonResponse = JSONObject(responseBody)
                val newToken = jsonResponse.optString("token", "")
                val newRefreshToken = jsonResponse.optString("refreshToken", refreshToken)
                val userId = sessionManager.getUserId()

                if (newToken.isNotEmpty()) {
                    sessionManager.saveUserSession(newToken, newRefreshToken, userId)
                    true
                } else {
                    false
                }
            } else {
                false
            }
        } catch (e: Exception) {
            Log.e("ApiClient", "Error refreshing token", e)
            false
        }
    }

    fun makeRequest(
        method: String,
        endpoint: String,
        body: String? = null,
        callback: (success: Boolean, response: String?) -> Unit
    ) {
        val url = "$baseUrl$endpoint"

        val requestBuilder = Request.Builder().url(url)

        when (method.uppercase()) {
            "GET" -> requestBuilder.get()
            "POST" -> {
                val requestBody = body?.toRequestBody("application/json".toMediaTypeOrNull())
                    ?: "".toRequestBody()
                requestBuilder.post(requestBody)
            }
            "PUT" -> {
                val requestBody = body?.toRequestBody("application/json".toMediaTypeOrNull())
                    ?: "".toRequestBody()
                requestBuilder.put(requestBody)
            }
            "DELETE" -> requestBuilder.delete()
        }

        val request = requestBuilder.build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("ApiClient", "Request failed", e)
                callback(false, e.message)
            }

            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body?.string()
                Log.d("ApiClient", "Response: ${response.code} - $responseBody")
                callback(response.isSuccessful, responseBody)
            }
        })
    }

    fun isUserLoggedIn(): Boolean = sessionManager.isLoggedIn()

    fun logout() = sessionManager.clearSession()
}
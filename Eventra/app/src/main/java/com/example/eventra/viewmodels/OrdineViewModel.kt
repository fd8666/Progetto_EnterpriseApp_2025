package com.example.eventra.viewmodels

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import com.example.eventra.R
import com.example.eventra.untils.SessionManager
import com.example.eventra.viewmodels.data.ErrorData
import com.example.eventra.viewmodels.data.OrdineData
import java.io.EOFException
import java.io.IOException
import java.net.URL
import java.util.concurrent.TimeUnit

class OrdineViewModel(application: Application) : AndroidViewModel(application) {

    private val _application = application
    private val gson: Gson = GsonBuilder()
        .setDateFormat("yyyy-MM-dd'T'HH:mm:ss")
        .create()
    private val server = application.getString(R.string.server)
    private val backendUrl = URL("$server/api/ordine")
    private val sessionManager = SessionManager(application)
    // OkHttpClient configurato con timeout
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _ordineCreated = MutableStateFlow<OrdineData?>(null)
    val ordineCreated: StateFlow<OrdineData?> = _ordineCreated.asStateFlow()

    private val _error = MutableStateFlow<ErrorData?>(null)
    val error: StateFlow<ErrorData?> = _error.asStateFlow()

    fun aggiungiOrdine(ordineData: OrdineData, idProprietario: Long, onSuccess: ((OrdineData) -> Unit)? = null) {
        CoroutineScope(Dispatchers.IO).launch {
            _error.value = null
            _isLoading.value = true

            val token = sessionManager.getJwtToken()

            if (token.isNullOrEmpty()) {
                _error.value = ErrorData(401, "Token JWT non disponibile.")
                Log.e("OrdineViewModel", "Token JWT mancante.")
                _isLoading.value = false
                return@launch
            }

            val urlString = "$backendUrl/aggiungi/$idProprietario"
            Log.d("OrdineViewModel", "Creating ordine at $urlString")

            val requestJson = gson.toJson(ordineData)
            val requestBody = requestJson.toRequestBody("application/json".toMediaType())

            val request = Request.Builder()
                .url(urlString)
                .post(requestBody)
                .addHeader("Content-Type", "application/json")
                .addHeader("Authorization", "Bearer $token")
                .build()

            try {
                val response = client.newCall(request).execute()

                if (!response.isSuccessful) {
                    val errorBody = response.body?.string() ?: "Unknown error"
                    _error.value = ErrorData(response.code, "Errore nella creazione dell'ordine: $errorBody")
                    Log.e("OrdineViewModel", "HTTP error: ${response.code}, Body: $errorBody")
                    return@launch
                }

                val responseJson = response.body?.string() ?: ""
                val ordineCreatedData = gson.fromJson(responseJson, OrdineData::class.java)
                _ordineCreated.value = ordineCreatedData
                onSuccess?.invoke(ordineCreatedData)

            } catch (e: Exception) {
                _error.value = ErrorData(0, _application.getString(R.string.unexpected_error))
                Log.e("OrdineViewModel", "Exception: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }


    // Aggiorna un ordine esistente
    fun updateOrdine(ordineId: Long, ordineData: OrdineData, onSuccess: (() -> Unit)? = null) {
        CoroutineScope(Dispatchers.IO).launch {
            _error.value = null
            _isLoading.value = true

            val urlString = "$backendUrl/aggiorna/$ordineId"
            Log.d("OrdineViewModel", "Updating ordine at $urlString")

            val requestJson = gson.toJson(ordineData)
            Log.d("OrdineViewModel", "Request: $requestJson")

            val requestBody = requestJson.toRequestBody("application/json".toMediaType())
            val request = Request.Builder()
                .url(urlString)
                .put(requestBody)
                .addHeader("Content-Type", "application/json")
                .build()

            try {
                val response = client.newCall(request).execute()

                if (!response.isSuccessful) {
                    val errorBody = response.body?.string() ?: "Unknown error"
                    _error.value = ErrorData(response.code, "Errore nell'aggiornamento dell'ordine: $errorBody")
                    Log.e("OrdineViewModel", "HTTP error: ${response.code}, Body: $errorBody")
                    return@launch
                }

                val responseText = response.body?.string() ?: ""
                Log.d("OrdineViewModel", "Response update ordine: $responseText")

                onSuccess?.invoke()

            } catch (e: IOException) {
                _error.value = ErrorData(0, _application.getString(R.string.network_error))
                Log.e("OrdineViewModel", "Network error: ${e.message}")
            } catch (e: Exception) {
                _error.value = ErrorData(0, _application.getString(R.string.unexpected_error))
                Log.e("OrdineViewModel", "Unexpected error: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }

    // Elimina un ordine
    fun deleteOrdine(ordineId: Long, onSuccess: (() -> Unit)? = null) {
        CoroutineScope(Dispatchers.IO).launch {
            _error.value = null
            _isLoading.value = true

            val urlString = "$backendUrl/elimina/$ordineId"
            Log.d("OrdineViewModel", "Deleting ordine at $urlString")

            val request = Request.Builder()
                .url(urlString)
                .delete()
                .build()

            try {
                val response = client.newCall(request).execute()

                if (!response.isSuccessful) {
                    val errorBody = response.body?.string() ?: "Unknown error"
                    _error.value = ErrorData(response.code, "Errore nell'eliminazione dell'ordine: $errorBody")
                    Log.e("OrdineViewModel", "HTTP error: ${response.code}, Body: $errorBody")
                    return@launch
                }

                val responseText = response.body?.string() ?: ""
                Log.d("OrdineViewModel", "Response delete ordine: $responseText")

                onSuccess?.invoke()

            } catch (e: IOException) {
                _error.value = ErrorData(0, _application.getString(R.string.network_error))
                Log.e("OrdineViewModel", "Network error: ${e.message}")
            } catch (e: Exception) {
                _error.value = ErrorData(0, _application.getString(R.string.unexpected_error))
                Log.e("OrdineViewModel", "Unexpected error: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }

    // Reset dello stato dell'ordine
    fun resetOrdineState() {
        _ordineCreated.value = null
        _error.value = null
    }
}
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
import com.example.eventra.viewmodels.data.BigliettoData
import com.example.eventra.viewmodels.data.ErrorData
import java.io.EOFException
import java.io.IOException
import java.net.URL
import java.util.concurrent.TimeUnit

class BigliettoViewModel(application: Application) : AndroidViewModel(application) {

    private val _application = application
    private val gson: Gson = GsonBuilder()
        .setDateFormat("yyyy-MM-dd'T'HH:mm:ss")
        .create()
    private val server = application.getString(R.string.server)
    private val backendUrl = URL("$server/api/biglietto")
    private val sessionManager = SessionManager(application)
    // OkHttpClient configurato con timeout
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _biglietti = MutableStateFlow<List<BigliettoData>?>(emptyList())
    val biglietti: StateFlow<List<BigliettoData>?> = _biglietti.asStateFlow()

    private val _bigliettoCreated = MutableStateFlow<BigliettoData?>(null)
    val bigliettoCreated: StateFlow<BigliettoData?> = _bigliettoCreated.asStateFlow()

    private val _error = MutableStateFlow<ErrorData?>(null)
    val error: StateFlow<ErrorData?> = _error.asStateFlow()

    fun createBiglietto(
        bigliettoCreateData: BigliettoData,
        onSuccess: ((BigliettoData) -> Unit)? = null,
        onError: ((String) -> Unit)? = null
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            _error.value = null
            _isLoading.value = true

            val token = sessionManager.getJwtToken()
            if (token.isNullOrEmpty()) {
                Log.e("BigliettoViewModel", "Token JWT non disponibile")
                _error.value = ErrorData(401, "Token JWT mancante o non valido")
                _isLoading.value = false
                return@launch
            }

            val urlString = "$backendUrl/create"
            Log.d("BigliettoViewModel", "Creating biglietto at $urlString")

            val requestJson = gson.toJson(bigliettoCreateData)
            Log.d("BigliettoViewModel", "Request: $requestJson")

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
                    _error.value = ErrorData(response.code, "Errore nella creazione del biglietto: $errorBody")
                    Log.e("BigliettoViewModel", "HTTP error: ${response.code}, Body: $errorBody")
                    return@launch
                }

                val responseJson = response.body?.string() ?: ""
                Log.d("BigliettoViewModel", "Response create biglietto: $responseJson")

                val bigliettoCreatedData: BigliettoData = gson.fromJson(responseJson, BigliettoData::class.java)
                _bigliettoCreated.value = bigliettoCreatedData

                onSuccess?.invoke(bigliettoCreatedData)

            } catch (e: IOException) {
                _error.value = ErrorData(0, _application.getString(R.string.network_error))
                Log.e("BigliettoViewModel", "Network error: ${e.message}")
            } catch (e: EOFException) {
                _error.value = ErrorData(0, _application.getString(R.string.end_of_stream_error))
                Log.e("BigliettoViewModel", "End of stream error: ${e.message}")
            } catch (e: Exception) {
                _error.value = ErrorData(0, _application.getString(R.string.unexpected_error))
                Log.e("BigliettoViewModel", "Unexpected error: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }


    // Ottieni biglietti per evento
    fun getBigliettiByEvento(eventoId: Long) {
        CoroutineScope(Dispatchers.IO).launch {
            _error.value = null
            _isLoading.value = true

            val urlString = "$backendUrl/evento/$eventoId"
            Log.d("BigliettoViewModel", "Getting biglietti by evento: $urlString")

            val request = Request.Builder()
                .url(urlString)
                .get()
                .build()

            try {
                val response = client.newCall(request).execute()

                if (!response.isSuccessful) {
                    val errorBody = response.body?.string() ?: "Unknown error"
                    _error.value = ErrorData(response.code, "Errore nel recupero dei biglietti: $errorBody")
                    Log.e("BigliettoViewModel", "HTTP error: ${response.code}, Body: $errorBody")
                    return@launch
                }

                val responseJson = response.body?.string() ?: ""
                Log.d("BigliettoViewModel", "Response biglietti: $responseJson")

                val bigliettiList: List<BigliettoData> = gson.fromJson(responseJson, Array<BigliettoData>::class.java).toList()
                _biglietti.value = bigliettiList

            } catch (e: IOException) {
                _error.value = ErrorData(0, _application.getString(R.string.network_error))
                Log.e("BigliettoViewModel", "Network error: ${e.message}")
            } catch (e: Exception) {
                _error.value = ErrorData(0, _application.getString(R.string.unexpected_error))
                Log.e("BigliettoViewModel", "Unexpected error: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }

    // Ottieni biglietti per utente
    fun getBigliettiByUtente(utenteId: Long) {
        CoroutineScope(Dispatchers.IO).launch {
            _error.value = null
            _isLoading.value = true

            val urlString = "$backendUrl/utente/$utenteId"
            Log.d("BigliettoViewModel", "Getting biglietti by utente: $urlString")

            val request = Request.Builder()
                .url(urlString)
                .get()
                .build()

            try {
                val response = client.newCall(request).execute()

                if (!response.isSuccessful) {
                    val errorBody = response.body?.string() ?: "Unknown error"
                    _error.value = ErrorData(response.code, "Errore nel recupero dei biglietti utente: $errorBody")
                    Log.e("BigliettoViewModel", "HTTP error: ${response.code}, Body: $errorBody")
                    return@launch
                }

                val responseJson = response.body?.string() ?: ""
                Log.d("BigliettoViewModel", "Response biglietti utente: $responseJson")

                val bigliettiList: List<BigliettoData> = gson.fromJson(responseJson, Array<BigliettoData>::class.java).toList()
                _biglietti.value = bigliettiList

            } catch (e: IOException) {
                _error.value = ErrorData(0, _application.getString(R.string.network_error))
                Log.e("BigliettoViewModel", "Network error: ${e.message}")
            } catch (e: Exception) {
                _error.value = ErrorData(0, _application.getString(R.string.unexpected_error))
                Log.e("BigliettoViewModel", "Unexpected error: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }

    // Reset dello stato
    fun resetState() {
        _biglietti.value = emptyList()
        _bigliettoCreated.value = null
        _error.value = null
    }
}
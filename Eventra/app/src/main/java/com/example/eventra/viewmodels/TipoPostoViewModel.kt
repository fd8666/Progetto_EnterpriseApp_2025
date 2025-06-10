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
import okhttp3.OkHttpClient
import okhttp3.Request
import com.example.eventra.R
import com.example.eventra.viewmodels.data.ErrorData
import com.example.eventra.viewmodels.data.TipoPostoData
import java.io.EOFException
import java.io.IOException
import java.net.URL
import java.util.concurrent.TimeUnit

class TipoPostoViewModel(application: Application) : AndroidViewModel(application) {

    private val _application = application
    private val gson: Gson = GsonBuilder()
        .setDateFormat("yyyy-MM-dd'T'HH:mm:ss")
        .create()
    private val server = application.getString(R.string.server)
    private val backendUrl = URL("$server/api/tipi-posto")

    // OkHttpClient configurato con timeout
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _tipiPosto = MutableStateFlow<List<TipoPostoData>?>(emptyList())
    val tipiPosto: StateFlow<List<TipoPostoData>?> = _tipiPosto.asStateFlow()

    private val _tipoPostoDetail = MutableStateFlow<TipoPostoData?>(null)
    val tipoPostoDetail: StateFlow<TipoPostoData?> = _tipoPostoDetail.asStateFlow()

    private val _totalPosti = MutableStateFlow<Int?>(0)
    val totalPosti: StateFlow<Int?> = _totalPosti.asStateFlow()

    private val _error = MutableStateFlow<ErrorData?>(null)
    val error: StateFlow<ErrorData?> = _error.asStateFlow()


    // Ottieni tipo posto per ID
    fun getTipoPostoById(id: Long) {
        CoroutineScope(Dispatchers.IO).launch {
            _error.value = null
            _isLoading.value = true

            val urlString = "$backendUrl/$id"
            Log.d("TipoPostoViewModel", "Getting tipo posto by ID: $urlString")

            val request = Request.Builder()
                .url(urlString)
                .get()
                .build()

            try {
                val response = client.newCall(request).execute()

                if (!response.isSuccessful) {
                    val errorBody = response.body?.string() ?: "Unknown error"
                    _error.value = ErrorData(response.code, "Errore nel recuperare il tipo di posto: $errorBody")
                    Log.e("TipoPostoViewModel", "HTTP error: ${response.code}, Body: $errorBody")
                    return@launch
                }

                val responseJson = response.body?.string() ?: ""
                Log.d("TipoPostoViewModel", "Response tipo posto detail: $responseJson")

                val tipoPostoData: TipoPostoData = gson.fromJson(responseJson, TipoPostoData::class.java)
                _tipoPostoDetail.value = tipoPostoData

            } catch (e: IOException) {
                _error.value = ErrorData(0, _application.getString(R.string.network_error))
                Log.e("TipoPostoViewModel", "Network error: ${e.message}")
            } catch (e: Exception) {
                _error.value = ErrorData(0, _application.getString(R.string.unexpected_error))
                Log.e("TipoPostoViewModel", "Unexpected error: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }

    // Ottieni totale posti per evento
    fun getTotalPostiByEvento(eventoId: Long) {
        CoroutineScope(Dispatchers.IO).launch {
            _error.value = null
            _isLoading.value = true

            val urlString = "$backendUrl/total-posti/$eventoId"
            Log.d("TipoPostoViewModel", "Getting total posti: $urlString")

            val request = Request.Builder()
                .url(urlString)
                .get()
                .build()

            try {
                val response = client.newCall(request).execute()

                if (!response.isSuccessful) {
                    val errorBody = response.body?.string() ?: "Unknown error"
                    _error.value = ErrorData(response.code, "Errore nel recuperare il totale posti: $errorBody")
                    Log.e("TipoPostoViewModel", "HTTP error: ${response.code}, Body: $errorBody")
                    return@launch
                }

                val responseText = response.body?.string() ?: "0"
                Log.d("TipoPostoViewModel", "Response total posti: $responseText")

                val totalPostiValue = responseText.toIntOrNull() ?: 0
                _totalPosti.value = totalPostiValue

            } catch (e: IOException) {
                _error.value = ErrorData(0, _application.getString(R.string.network_error))
                Log.e("TipoPostoViewModel", "Network error: ${e.message}")
            } catch (e: Exception) {
                _error.value = ErrorData(0, _application.getString(R.string.unexpected_error))
                Log.e("TipoPostoViewModel", "Unexpected error: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }
    fun getTipiPostoByEvento(eventoId: Long) {
        CoroutineScope(Dispatchers.IO).launch {
            _error.value = null
            _isLoading.value = true

            try {
                val urlString = "$backendUrl/evento/$eventoId"
                Log.d("TipoPostoViewModel", "Getting tipi posto for evento: $urlString")

                val request = Request.Builder()
                    .url(urlString)
                    .get()
                    .build()

                val response = client.newCall(request).execute()

                if (!response.isSuccessful) {


                    return@launch
                }

                val responseJson = response.body?.string() ?: ""
                Log.d("TipoPostoViewModel", "Response tipi posto: $responseJson")

                val tipiPostoList: List<TipoPostoData> = gson.fromJson(responseJson, Array<TipoPostoData>::class.java).toList()
                _tipiPosto.value = tipiPostoList

            } catch (e: Exception) {


            } finally {
                _isLoading.value = false
            }
        }
    }


}
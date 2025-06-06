package com.example.eventra.viewmodels

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonParser
import com.google.gson.JsonSyntaxException
import com.google.gson.reflect.TypeToken
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
import com.example.eventra.viewmodels.data.EventoData
import java.io.EOFException
import java.io.IOException
import java.net.URL

class EventiViewModel(application: Application) : AndroidViewModel(application) {

    private val _application = application
    private val gson: Gson = GsonBuilder()
        .setDateFormat("yyyy-MM-dd'T'HH:mm:ss")
        .setLenient()
        .create()
    private val server = application.getString(R.string.server)
    private val backendUrl = URL("$server/api/evento")

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _eventi = MutableStateFlow<List<EventoData>?>(emptyList())
    val eventi: StateFlow<List<EventoData>?> = _eventi.asStateFlow()

    private val _eventiByCategoria = MutableStateFlow<List<EventoData>?>(emptyList())
    val eventiByCategoria: StateFlow<List<EventoData>?> = _eventiByCategoria.asStateFlow()

    private val _eventoDetail = MutableStateFlow<EventoData?>(null)
    val eventoDetail: StateFlow<EventoData?> = _eventoDetail.asStateFlow()

    private val _error = MutableStateFlow<ErrorData?>(null)
    val error: StateFlow<ErrorData?> = _error.asStateFlow()

    // Funzioni per ottenere eventi
    fun getAllEventi() = getEventi("", _eventi, "allEventi")

    fun getEventiByCategoria(categoriaId: Long) = getEventi("/categoria/$categoriaId", _eventiByCategoria, "categoria")

    fun getEventoById(id: Long) {
        CoroutineScope(Dispatchers.IO).launch {
            _error.value = null
            _isLoading.value = true

            val urlString = "$backendUrl/$id"
            Log.d("EventiViewModel", "Retrieving evento from $urlString")

            val client = OkHttpClient()
            val request = Request.Builder()
                .url(urlString)
                .get()
                .build()

            try {
                val response = client.newCall(request).execute()

                if (!response.isSuccessful) {
                    _error.value = ErrorData(response.code, _application.getString(R.string.http_error))
                    Log.e("EventiViewModel", "HTTP error: ${response.code}")
                    return@launch
                }

                val responseBody = response.body?.string() ?: ""
                Log.d("EventiViewModel", "Response evento detail: $responseBody")

                if (responseBody.isBlank()) {
                    _error.value = ErrorData(0, "Risposta vuota dal server")
                    return@launch
                }

                // Controlla se è HTML (pagina di login)
                if (responseBody.trimStart().startsWith("<!DOCTYPE html>") ||
                    responseBody.trimStart().startsWith("<html")) {
                    _error.value = ErrorData(401, "Autenticazione richiesta")
                    Log.e("EventiViewModel", "Server returned HTML login page")
                    return@launch
                }

                try {
                    val eventoData: EventoData = gson.fromJson(responseBody, EventoData::class.java)
                    _eventoDetail.value = eventoData
                } catch (e: JsonSyntaxException) {
                    Log.e("EventiViewModel", "Errore nel parsing del dettaglio evento: ${e.message}")
                    _error.value = ErrorData(0, "Errore nel formato dei dati: ${e.message}")
                }

            } catch (e: IOException) {
                _error.value = ErrorData(0, _application.getString(R.string.network_error))
                Log.e("EventiViewModel", "Network error: ${e.message}")
            } catch (e: EOFException) {
                _error.value = ErrorData(0, _application.getString(R.string.end_of_stream_error))
                Log.e("EventiViewModel", "End of stream error: ${e.message}")
            } catch (e: Exception) {
                _error.value = ErrorData(0, _application.getString(R.string.unexpected_error))
                Log.e("EventiViewModel", "Unexpected error: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun searchEventiByNome(nome: String) = getEventi("/search?nome=$nome", _eventi, "search")

    fun searchEventiByLuogo(luogo: String) = getEventi("/luogo?luogo=$luogo", _eventi, "luogo")

    private fun getEventi(urlSuffix: String, targetFlow: MutableStateFlow<List<EventoData>?>, category: String) {
        CoroutineScope(Dispatchers.IO).launch {
            targetFlow.value = emptyList()
            _error.value = null
            _isLoading.value = true

            val urlString = "$backendUrl$urlSuffix"
            Log.d("EventiViewModel", "Fetching eventi from: $urlString")

            val client = OkHttpClient()
            val request = Request.Builder()
                .url(urlString)
                .get()
                .build()

            try {
                val response = client.newCall(request).execute()

                if (!response.isSuccessful) {
                    _error.value = ErrorData(response.code, _application.getString(R.string.http_error))
                    Log.e("EventiViewModel", "HTTP error: ${response.code} for category: $category")
                    return@launch
                }

                val responseBody = response.body?.string() ?: ""
                Log.d("EventiViewModel", "Raw Response Body for $category: ${responseBody.take(200)}...")

                if (responseBody.isBlank()) {
                    Log.d("EventiViewModel", "Risposta vuota per $category")
                    targetFlow.value = emptyList()
                    return@launch
                }

                // Controlla se è HTML (pagina di login)
                if (responseBody.trimStart().startsWith("<!DOCTYPE html>") ||
                    responseBody.trimStart().startsWith("<html")) {
                    _error.value = ErrorData(401, "Autenticazione richiesta - Il server richiede login OAuth2")
                    Log.e("EventiViewModel", "Server returned HTML login page for $category")
                    targetFlow.value = emptyList()
                    return@launch
                }

                try {
                    // Verifica se la risposta è una stringa semplice (messaggio di errore)
                    if (responseBody.startsWith("\"") && responseBody.endsWith("\"")) {
                        val errorMessage = responseBody.replace("\"", "")
                        Log.e("EventiViewModel", "Server returned error message: $errorMessage")
                        _error.value = ErrorData(0, errorMessage)
                        targetFlow.value = emptyList()
                        return@launch
                    }

                    // Verifica se è un JSON valido
                    val jsonElement = JsonParser.parseString(responseBody)

                    val eventiList = if (jsonElement.isJsonArray) {
                        val listType = object : TypeToken<List<EventoData>>() {}.type
                        gson.fromJson<List<EventoData>>(responseBody, listType)
                    } else if (jsonElement.isJsonObject) {
                        val singleEvento = gson.fromJson(responseBody, EventoData::class.java)
                        listOf(singleEvento)
                    } else {
                        Log.e("EventiViewModel", "Formato di risposta non valido per $category")
                        _error.value = ErrorData(0, "Formato di risposta non valido")
                        emptyList()
                    }

                    Log.d("EventiViewModel", "Parsed ${eventiList.size} eventi for $category")
                    targetFlow.value = eventiList

                } catch (e: JsonSyntaxException) {
                    Log.e("EventiViewModel", "Errore nel parsing del JSON per $category: ${e.message}")
                    _error.value = ErrorData(0, "Errore di autenticazione o formato dati non valido")
                    targetFlow.value = emptyList()
                } catch (e: IllegalStateException) {
                    Log.e("EventiViewModel", "Stato illegale nel parsing per $category: ${e.message}")
                    _error.value = ErrorData(0, "Errore di autenticazione - Login richiesto")
                    targetFlow.value = emptyList()
                }

            } catch (e: IOException) {
                _error.value = ErrorData(0, _application.getString(R.string.network_error))
                Log.e("EventiViewModel", "Network error for $category: ${e.message}")
            } catch (e: EOFException) {
                _error.value = ErrorData(0, _application.getString(R.string.end_of_stream_error))
                Log.e("EventiViewModel", "End of stream error for $category: ${e.message}")
            } catch (e: Exception) {
                _error.value = ErrorData(0, _application.getString(R.string.unexpected_error))
                Log.e("EventiViewModel", "Unexpected error for $category: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }
}

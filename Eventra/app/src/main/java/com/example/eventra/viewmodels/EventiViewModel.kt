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
import com.example.eventra.viewmodels.data.EventoData
import java.io.EOFException
import java.io.IOException
import java.net.URL
import java.net.URLEncoder

class EventiViewModel(application: Application) : AndroidViewModel(application) {

    private val _application = application
    private val gson: Gson = GsonBuilder()
        .setDateFormat("yyyy-MM-dd'T'HH:mm:ss")
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

                val responseJson = response.body?.string() ?: ""
                Log.d("EventiViewModel", "Response evento detail: $responseJson")

                val eventoData: EventoData = gson.fromJson(responseJson, EventoData::class.java)

                _eventoDetail.value = eventoData
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

    fun searchEventiByNome(nome: String) = getEventi("/search?nome=${URLEncoder.encode(nome, "UTF-8")}", _eventi, "search")

    fun searchEventiByLuogo(luogo: String) = getEventi("/luogo?luogo=${URLEncoder.encode(luogo, "UTF-8")}", _eventi, "luogo")

    private fun getEventi(urlSuffix: String, targetFlow: MutableStateFlow<List<EventoData>?>, category: String) {
        CoroutineScope(Dispatchers.IO).launch {
            targetFlow.value = emptyList()
            _error.value = null
            _isLoading.value = true

            val urlString = "$backendUrl$urlSuffix"

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

                val responseJson = response.body?.string() ?: ""
                Log.d("EventiViewModel", "Response $category: $responseJson")

                val eventiList: List<EventoData> = gson.fromJson(responseJson, Array<EventoData>::class.java).toList()

                targetFlow.value = eventiList
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

    fun searchEventiByDateRange(startDate: String?, endDate: String?) {
        CoroutineScope(Dispatchers.IO).launch {
            _eventi.value = emptyList()
            _error.value = null
            _isLoading.value = true

            val urlString = buildString {
                append("$backendUrl/filtra")
                val params = mutableListOf<String>()

                startDate?.let { params.add("dataInizio=${URLEncoder.encode(it, "UTF-8")}") }
                endDate?.let { params.add("dataFine=${URLEncoder.encode(it, "UTF-8")}") }

                if (params.isNotEmpty()) {
                    append("?${params.joinToString("&")}")
                }
            }

            Log.d("EventiViewModel", "Searching eventi by date range: $urlString")

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

                val responseJson = response.body?.string() ?: ""
                Log.d("EventiViewModel", "Response date search: $responseJson")

                val eventiList: List<EventoData> = gson.fromJson(responseJson, Array<EventoData>::class.java).toList()
                _eventi.value = eventiList

            } catch (e: IOException) {
                _error.value = ErrorData(0, _application.getString(R.string.network_error))
                Log.e("EventiViewModel", "Network error: ${e.message}")
            } catch (e: Exception) {
                _error.value = ErrorData(0, _application.getString(R.string.unexpected_error))
                Log.e("EventiViewModel", "Unexpected error: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun searchEventiAfterDate(date: String) {
        CoroutineScope(Dispatchers.IO).launch {
            _eventi.value = emptyList()
            _error.value = null
            _isLoading.value = true

            val urlString = "$backendUrl/data-after?data=${URLEncoder.encode(date, "UTF-8")}"
            Log.d("EventiViewModel", "Searching eventi after date: $urlString")

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

                val responseJson = response.body?.string() ?: ""
                Log.d("EventiViewModel", "Response after date search: $responseJson")

                val eventiList: List<EventoData> = gson.fromJson(responseJson, Array<EventoData>::class.java).toList()
                _eventi.value = eventiList

            } catch (e: IOException) {
                _error.value = ErrorData(0, _application.getString(R.string.network_error))
                Log.e("EventiViewModel", "Network error: ${e.message}")
            } catch (e: Exception) {
                _error.value = ErrorData(0, _application.getString(R.string.unexpected_error))
                Log.e("EventiViewModel", "Unexpected error: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }

    // Metodo per ricerca combinata
    fun searchEventiCombined(
        nome: String? = null,
        luogo: String? = null,
        categoriaId: Long? = null,
        startDate: String? = null,
        endDate: String? = null
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            _eventi.value = emptyList()
            _error.value = null
            _isLoading.value = true

            try {
                getAllEventiAndFilter(nome, luogo, categoriaId, startDate, endDate)
            } catch (e: Exception) {
                _error.value = ErrorData(0, _application.getString(R.string.unexpected_error))
                Log.e("EventiViewModel", "Error in combined search: ${e.message}")
                _isLoading.value = false
            }
        }
    }

    // Metodo che implementa il filtro lato client
    private suspend fun getAllEventiAndFilter(
        nome: String?,
        luogo: String?,
        categoriaId: Long?,
        startDate: String?,
        endDate: String?
    ) {
        try {
            // Prima ottieni tutti gli eventi
            val urlString = "$backendUrl"
            Log.d("EventiViewModel", "Getting all events for filtering: $urlString")

            val client = OkHttpClient()
            val request = Request.Builder()
                .url(urlString)
                .get()
                .build()

            val response = client.newCall(request).execute()

            if (!response.isSuccessful) {
                _error.value = ErrorData(response.code, _application.getString(R.string.http_error))
                Log.e("EventiViewModel", "HTTP error: ${response.code}")
                return
            }

            val responseJson = response.body?.string() ?: ""
            Log.d("EventiViewModel", "All events response: $responseJson")

            val allEventi: List<EventoData> = gson.fromJson(responseJson, Array<EventoData>::class.java).toList()

            // Ora applica i filtri lato client
            val filteredEventi = allEventi.filter { evento ->
                var matchesFilters = true

                // Filtro per nome
                nome?.takeIf { it.isNotBlank() }?.let { searchNome ->
                    matchesFilters = matchesFilters &&
                            (evento.nome?.contains(searchNome, ignoreCase = true) == true)
                }

                // Filtro per luogo
                luogo?.takeIf { it.isNotBlank() }?.let { searchLuogo ->
                    matchesFilters = matchesFilters &&
                            (evento.luogo.contains(searchLuogo, ignoreCase = true))
                }

                // Filtro per categoria
                categoriaId?.let { searchCategoriaId ->
                    matchesFilters = matchesFilters &&
                            (evento.categoriaId == searchCategoriaId)
                }

                // Filtro per data di inizio
                startDate?.takeIf { it.isNotBlank() }?.let { searchStartDate ->
                    try {
                        val eventDate = evento.dataOraEvento
                        matchesFilters = matchesFilters &&
                                (eventDate >= searchStartDate)
                    } catch (e: Exception) {
                        Log.e("EventiViewModel", "Error parsing start date: ${e.message}")
                    }
                }

                // Filtro per data di fine
                endDate?.takeIf { it.isNotBlank() }?.let { searchEndDate ->
                    try {
                        val eventDate = evento.dataOraEvento
                        matchesFilters = matchesFilters &&
                                (eventDate <= searchEndDate)
                    } catch (e: Exception) {
                        Log.e("EventiViewModel", "Error parsing end date: ${e.message}")
                    }
                }

                matchesFilters
            }

            Log.d("EventiViewModel", "Filtered ${filteredEventi.size} events from ${allEventi.size} total")
            _eventi.value = filteredEventi

        } catch (e: IOException) {
            _error.value = ErrorData(0, _application.getString(R.string.network_error))
            Log.e("EventiViewModel", "Network error in filtering: ${e.message}")
        } catch (e: Exception) {
            _error.value = ErrorData(0, _application.getString(R.string.unexpected_error))
            Log.e("EventiViewModel", "Unexpected error in filtering: ${e.message}")
        } finally {
            _isLoading.value = false
        }
    }
}

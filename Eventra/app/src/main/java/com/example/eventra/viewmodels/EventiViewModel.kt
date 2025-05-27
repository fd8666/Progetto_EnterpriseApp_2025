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

    fun searchEventiByNome(nome: String) = getEventi("/search?nome=$nome", _eventi, "search")

    fun searchEventiByLuogo(luogo: String) = getEventi("/luogo?luogo=$luogo", _eventi, "luogo")

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


}

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
import com.example.eventra.viewmodels.data.StrutturaInfoUtenteData
import com.example.eventra.viewmodels.data.StrutturaMapInfoData
import java.io.EOFException
import java.io.IOException
import java.net.URL

class StrutturaViewModel(application: Application) : AndroidViewModel(application) {

    private val _application = application
    private val gson: Gson = GsonBuilder()
        .setDateFormat("yyyy-MM-dd'T'HH:mm:ss")
        .create()
    private val server = application.getString(R.string.server)
    private val backendUrl = URL("$server/api/strutture")

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _strutturaInfo = MutableStateFlow<StrutturaInfoUtenteData?>(null)
    val strutturaInfo: StateFlow<StrutturaInfoUtenteData?> = _strutturaInfo.asStateFlow()

    private val _strutturaMapInfo = MutableStateFlow<StrutturaMapInfoData?>(null)
    val strutturaMapInfo: StateFlow<StrutturaMapInfoData?> = _strutturaMapInfo.asStateFlow()

    private val _error = MutableStateFlow<ErrorData?>(null)
    val error: StateFlow<ErrorData?> = _error.asStateFlow()

   //informazioni strttura dal dto info utenti
    fun getStrutturaByEventoId(eventoId: Long) {
        CoroutineScope(Dispatchers.IO).launch {
            _error.value = null
            _isLoading.value = true

            val urlString = "$backendUrl/evento/$eventoId"
            Log.d("StrutturaViewModel", "Retrieving struttura from $urlString")

            val client = OkHttpClient()
            val request = Request.Builder()
                .url(urlString)
                .get()
                .build()

            try {
                val response = client.newCall(request).execute()

                if (!response.isSuccessful) {
                    _error.value = ErrorData(response.code, _application.getString(R.string.http_error))
                    Log.e("StrutturaViewModel", "HTTP error: ${response.code}")
                    return@launch
                }

                val responseJson = response.body?.string() ?: ""
                Log.d("StrutturaViewModel", "Response struttura info: $responseJson")

                val strutturaData: StrutturaInfoUtenteData = gson.fromJson(responseJson, StrutturaInfoUtenteData::class.java)
                _strutturaInfo.value = strutturaData

            } catch (e: IOException) {
                _error.value = ErrorData(0, _application.getString(R.string.network_error))
                Log.e("StrutturaViewModel", "Network error: ${e.message}")
            } catch (e: EOFException) {
                _error.value = ErrorData(0, _application.getString(R.string.end_of_stream_error))
                Log.e("StrutturaViewModel", "End of stream error: ${e.message}")
            } catch (e: Exception) {
                _error.value = ErrorData(0, _application.getString(R.string.unexpected_error))
                Log.e("StrutturaViewModel", "Unexpected error: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }
    //per la creazione mappa
    fun getStrutturaMapInfo(strutturaId: Long) {
        CoroutineScope(Dispatchers.IO).launch {
            _error.value = null

            val urlString = "$backendUrl/$strutturaId/map"
            Log.d("StrutturaViewModel", "Retrieving map info from $urlString")

            val client = OkHttpClient()
            val request = Request.Builder()
                .url(urlString)
                .get()
                .build()

            try {
                val response = client.newCall(request).execute()

                if (!response.isSuccessful) {
                    _error.value = ErrorData(response.code, _application.getString(R.string.http_error))
                    Log.e("StrutturaViewModel", "HTTP error for map info: ${response.code}")
                    return@launch
                }

                val responseJson = response.body?.string() ?: ""
                Log.d("StrutturaViewModel", "Response map info: $responseJson")

                val mapData: StrutturaMapInfoData = gson.fromJson(responseJson, StrutturaMapInfoData::class.java)
                _strutturaMapInfo.value = mapData

            } catch (e: IOException) {
                _error.value = ErrorData(0, _application.getString(R.string.network_error))
                Log.e("StrutturaViewModel", "Network error for map: ${e.message}")
            } catch (e: EOFException) {
                _error.value = ErrorData(0, _application.getString(R.string.end_of_stream_error))
                Log.e("StrutturaViewModel", "End of stream error for map: ${e.message}")
            } catch (e: Exception) {
                _error.value = ErrorData(0, _application.getString(R.string.unexpected_error))
                Log.e("StrutturaViewModel", "Unexpected error for map: ${e.message}")
            }
        }
    }

}
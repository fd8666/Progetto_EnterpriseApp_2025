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
import com.example.eventra.viewmodels.data.PagamentoData
import com.example.eventra.viewmodels.data.PagamentoRequest
import java.io.EOFException
import java.io.IOException
import java.net.URL
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.concurrent.TimeUnit

class PagamentoViewModel(application: Application) : AndroidViewModel(application) {

    private val _application = application
    private val gson: Gson = GsonBuilder()
        .setDateFormat("yyyy-MM-dd'T'HH:mm:ss")
        .create()
    private val server = application.getString(R.string.server)
    private val backendUrl = URL("$server/api/pagamenti")

    // OkHttpClient configurato con timeout
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _pagamenti = MutableStateFlow<List<PagamentoData>?>(emptyList())
    val pagamenti: StateFlow<List<PagamentoData>?> = _pagamenti.asStateFlow()

    private val _pagamentoCreated = MutableStateFlow<PagamentoData?>(null)
    val pagamentoCreated: StateFlow<PagamentoData?> = _pagamentoCreated.asStateFlow()

    private val _error = MutableStateFlow<ErrorData?>(null)
    val error: StateFlow<ErrorData?> = _error.asStateFlow()

    private val _paymentSuccess = MutableStateFlow(false)
    val paymentSuccess: StateFlow<Boolean> = _paymentSuccess.asStateFlow()

    private val sessionManager = SessionManager(application)

    // Ottieni pagamenti per utente
    fun getPagamentiByUtente(utenteId: Long) {
        CoroutineScope(Dispatchers.IO).launch {
            _error.value = null
            _isLoading.value = true

            val urlString = "$backendUrl/$utenteId"
            Log.d("PagamentoViewModel", "Retrieving pagamenti from $urlString")

            val request = Request.Builder()
                .url(urlString)
                .get()
                .build()

            try {
                val response = client.newCall(request).execute()

                if (!response.isSuccessful) {
                    val errorBody = response.body?.string() ?: "Unknown error"
                    _error.value = ErrorData(response.code, "Errore nel recupero dei pagamenti: $errorBody")
                    Log.e("PagamentoViewModel", "HTTP error: ${response.code}, Body: $errorBody")
                    return@launch
                }

                val responseJson = response.body?.string() ?: ""
                Log.d("PagamentoViewModel", "Response pagamenti: $responseJson")

                val pagamentiList: List<PagamentoData> = gson.fromJson(responseJson, Array<PagamentoData>::class.java).toList()
                _pagamenti.value = pagamentiList

            } catch (e: IOException) {
                _error.value = ErrorData(0, _application.getString(R.string.network_error))
                Log.e("PagamentoViewModel", "Network error: ${e.message}")
            } catch (e: EOFException) {
                _error.value = ErrorData(0, _application.getString(R.string.end_of_stream_error))
                Log.e("PagamentoViewModel", "End of stream error: ${e.message}")
            } catch (e: Exception) {
                _error.value = ErrorData(0, _application.getString(R.string.unexpected_error))
                Log.e("PagamentoViewModel", "Unexpected error: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun createPagamento(
        ordineId: Long,
        pagamentoRequest: PagamentoRequest,
        onSuccess: ((PagamentoData) -> Unit)? = null
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            _error.value = null
            _isLoading.value = true
            _paymentSuccess.value = false

            try {
                val token = sessionManager.getJwtToken()
                if (token.isNullOrEmpty()) {
                    Log.e("PagamentoViewModel", "Token JWT non disponibile")
                    _error.value = ErrorData(401, "Token JWT mancante o non valido")
                    _isLoading.value = false
                    return@launch
                }

                // Validazione carta
                val isValidCard = validateCreditCard(pagamentoRequest)
                if (!isValidCard) {
                    _error.value = ErrorData(400, "Dati della carta non validi")
                    return@launch
                }

                val urlString = "$backendUrl/createPagamento/$ordineId"
                Log.d("PagamentoViewModel", "Creating pagamento at $urlString")

                val pagamentoData = PagamentoData(
                    nomeTitolare = pagamentoRequest.nomeTitolare,
                    cognomeTitolare = pagamentoRequest.cognomeTitolare,
                    numeroCarta = pagamentoRequest.numeroCarta,
                    scadenza = pagamentoRequest.scadenza,
                    cvv = pagamentoRequest.cvv,
                    importo = pagamentoRequest.importo.toBigDecimal(),
                    ordineId = ordineId,
                )

                val requestJson = gson.toJson(pagamentoData)
                Log.d("PagamentoViewModel", "Request: $requestJson")

                val requestBody = requestJson.toRequestBody("application/json".toMediaType())
                val request = Request.Builder()
                    .url(urlString)
                    .post(requestBody)
                    .addHeader("Content-Type", "application/json")
                    .addHeader("Authorization", "Bearer $token")
                    .build()

                val response = client.newCall(request).execute()

                if (!response.isSuccessful) {
                    val errorBody = response.body?.string() ?: "Unknown error"
                    _error.value = ErrorData(response.code, "Errore nella creazione del pagamento: $errorBody")
                    Log.e("PagamentoViewModel", "HTTP error: ${response.code}, Body: $errorBody")
                    return@launch
                }

                val responseJson = response.body?.string() ?: ""
                Log.d("PagamentoViewModel", "Response create pagamento: $responseJson")

                val pagamentoCreato: PagamentoData = gson.fromJson(responseJson, PagamentoData::class.java)
                _pagamentoCreated.value = pagamentoCreato

                // Simulazione del processamento del pagamento (2 secondi)
                kotlinx.coroutines.delay(2000)

                onSuccess?.invoke(pagamentoCreato)

            } catch (e: IOException) {
                _error.value = ErrorData(0, _application.getString(R.string.network_error))
                Log.e("PagamentoViewModel", "Network error: ${e.message}")
            } catch (e: Exception) {
                _error.value = ErrorData(0, _application.getString(R.string.unexpected_error))
                Log.e("PagamentoViewModel", "Unexpected error: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }



    private fun validateCreditCard(pagamentoRequest: PagamentoRequest): Boolean {
        // Validazioni base per la simulazione
        return pagamentoRequest.nomeTitolare.isNotBlank() &&
                pagamentoRequest.cognomeTitolare.isNotBlank() &&
                pagamentoRequest.numeroCarta.length >= 13 &&
                pagamentoRequest.cvv.length >= 3 &&
                pagamentoRequest.scadenza.matches(Regex("\\d{2}/\\d{2}"))
    }

    // Reset dello stato del pagamento
    fun resetPaymentState() {
        _paymentSuccess.value = false
        _pagamentoCreated.value = null
        _error.value = null

    }




}

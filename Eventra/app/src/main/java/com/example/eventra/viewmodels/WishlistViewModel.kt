package com.example.eventra.viewmodels

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.eventra.R
import com.example.eventra.Visibilita
import com.example.eventra.untils.SessionManager
import com.example.eventra.viewmodels.data.ErrorData
import com.example.eventra.viewmodels.data.WishlistCondivisaRequest
import com.example.eventra.viewmodels.data.WishlistData
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
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.EOFException
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.util.concurrent.TimeUnit

class WishlistViewModel(application: Application) : AndroidViewModel(application) {

    private val _application = application
    private val sessionManager = SessionManager(application)
    private val gson: Gson = GsonBuilder()
        .setDateFormat("yyyy-MM-dd'T'HH:mm:ss")
        .setLenient()
        .create()
    private val server = application.getString(R.string.server)
    private val backendUrl = URL("$server/api/wishlist")

    private val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .writeTimeout(10, TimeUnit.SECONDS)
        .build()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _wishlists = MutableStateFlow<List<WishlistData>?>(emptyList())
    val wishlists: StateFlow<List<WishlistData>?> = _wishlists.asStateFlow()

    private val _wishlistsByVisibilita = MutableStateFlow<List<WishlistData>?>(emptyList())
    val wishlistsByVisibilita: StateFlow<List<WishlistData>?> = _wishlistsByVisibilita.asStateFlow()

    private val _wishlistCondivise = MutableStateFlow<List<WishlistData>?>(emptyList())
    val wishlistCondivise: StateFlow<List<WishlistData>?> = _wishlistCondivise.asStateFlow()

    private val _wishlistDetail = MutableStateFlow<WishlistData?>(null)
    val wishlistDetail: StateFlow<WishlistData?> = _wishlistDetail.asStateFlow()

    private val _error = MutableStateFlow<ErrorData?>(null)
    val error: StateFlow<ErrorData?> = _error.asStateFlow()

    private val _isWishlistCondivisa = MutableStateFlow(false)
    val isWishlistCondivisa: StateFlow<Boolean> = _isWishlistCondivisa.asStateFlow()

    // Definisci gli stati possibili della condivisione
    sealed class CondivisioneState {
        object Idle : CondivisioneState()
        object Loading : CondivisioneState()
        data class Success(val message: String) : CondivisioneState()
        data class Error(val message: String) : CondivisioneState()
    }

    private val _condivisoneState = MutableStateFlow<CondivisioneState>(CondivisioneState.Idle)
    val condivisioneState = _condivisoneState.asStateFlow()

    fun resetCondivisioneState() {
        _condivisoneState.value = CondivisioneState.Idle
    }

    private fun getAuthenticatedRequest(urlString: String): Request.Builder {
        val token = sessionManager.getJwtToken()
        Log.d("WishlistViewModel", "Using token for request: ${if (token.isNullOrEmpty()) "MISSING" else "PRESENT"}")

        if (token.isNullOrEmpty()) {
            Log.e("WishlistViewModel", "Token JWT non disponibile")
            throw IllegalStateException("Token JWT non disponibile")
        }

        return Request.Builder()
            .url(urlString)
            .addHeader("Authorization", "Bearer $token")
            .addHeader("Accept", "application/json")
            .addHeader("Content-Type", "application/json")
    }

    fun verificaSeWishlistECondivisa(wishlistId: Long) {
        viewModelScope.launch (Dispatchers.IO){
            try {
                val urlString = "$server/api/wishlist/condivisa/check/$wishlistId"
                Log.d("WishlistViewModel", "Checking if wishlist is shared: $urlString")

                val request = getAuthenticatedRequest(urlString).get().build()
                val response = client.newCall(request).execute()

                if (response.isSuccessful) {
                    val responseBody = response.body?.string() ?: "false"
                    val isShared = responseBody.toBoolean()
                    _isWishlistCondivisa.value = isShared
                    Log.d("WishlistViewModel", "Wishlist $wishlistId is shared: $isShared")
                } else {
                    handleHttpError(response.code, "verificaSeWishlistECondivisa")
                    _isWishlistCondivisa.value = false
                }
            } catch (e: Exception) {
                _isWishlistCondivisa.value = false
                Log.e("WishlistViewModel", "Error checking wishlist share status", e)
            }
        }
    }

    fun rimuoviTutteCondivisioni(wishlistId: Long, onSuccess: () -> Unit = {}) {
        viewModelScope.launch(Dispatchers.IO) {
            _condivisoneState.value = CondivisioneState.Loading

            try {
                val urlString = "$server/api/wishlist/condivisa/remove-by-wishlist/$wishlistId"
                Log.d("WishlistViewModel", "Removing all shares for wishlist: $urlString")

                val request = getAuthenticatedRequest(urlString).delete().build()
                val response = client.newCall(request).execute()

                if (response.isSuccessful) {
                    _condivisoneState.value = CondivisioneState.Success("Condivisioni rimosse con successo!")
                    _isWishlistCondivisa.value = false

                    withContext(Dispatchers.Main) {
                        onSuccess()
                    }

                    Log.d("WishlistViewModel", "All shares removed successfully for wishlist $wishlistId")
                } else {
                    val errorMsg = when (response.code) {
                        404 -> "Wishlist non trovata"
                        else -> "Errore nella rimozione delle condivisioni (${response.code})"
                    }
                    _condivisoneState.value = CondivisioneState.Error(errorMsg)
                    Log.e("WishlistViewModel", "Error removing shares: ${response.code}")
                }
            } catch (e: Exception) {
                _condivisoneState.value = CondivisioneState.Error("Errore di connessione: ${e.message}")
                Log.e("WishlistViewModel", "Error removing shares", e)
            }
        }
    }

    fun removeEventoFromWishlist(wishlistId: Long, eventoId: Long, onSuccess: () -> Unit = {}) {
        viewModelScope.launch(Dispatchers.IO) {
            _error.value = null
            Log.d("WishlistViewModel", "Attempting to remove evento $eventoId from wishlist $wishlistId")

            val urlString = "$backendUrl/$wishlistId/evento/$eventoId"
            Log.d("WishlistViewModel", "Removing evento URL: $urlString")

            try {
                val request = getAuthenticatedRequest(urlString).delete().build()
                val response = client.newCall(request).execute()

                Log.d("WishlistViewModel", "Remove response code: ${response.code}")

                if (!response.isSuccessful) {
                    _error.value = ErrorData(response.code, "Errore HTTP: ${response.code}")
                    Log.e("WishlistViewModel", "HTTP error removing evento: ${response.code}")
                    return@launch
                }

                Log.d("WishlistViewModel", "Evento rimosso con successo dalla wishlist")

                // Aggiorna lo stato locale immediatamente
                updateWishlistsAfterEventRemoval(wishlistId, eventoId)

                withContext(Dispatchers.Main) {
                    onSuccess()
                }
            } catch (e: Exception) {
                _error.value = ErrorData(0, "Errore di rete: ${e.message}")
                Log.e("WishlistViewModel", "Exception removing evento", e)
            }
        }
    }

    private fun updateWishlistsAfterEventRemoval(wishlistId: Long, eventoId: Long) {
        _wishlistsByVisibilita.value?.let { currentWishlists ->
            val updatedWishlists = currentWishlists.map { wishlist ->
                if (wishlist.id == wishlistId) {
                    wishlist.copy(eventi = wishlist.eventi.filter { it != eventoId })
                } else {
                    wishlist
                }
            }
            _wishlistsByVisibilita.value = updatedWishlists
            Log.d("WishlistViewModel", "Updated wishlist after removal. New size: ${updatedWishlists.find { it.id == wishlistId }?.eventi?.size}")
        }

        _wishlistCondivise.value?.let { currentWishlistCondivise ->
            val updatedWishlistCondivise = currentWishlistCondivise.map { wishlist ->
                if (wishlist.id == wishlistId) {
                    wishlist.copy(eventi = wishlist.eventi.filter { it != eventoId })
                } else {
                    wishlist
                }
            }
            _wishlistCondivise.value = updatedWishlistCondivise
        }
    }

    fun getWishlistsByUtenteAndVisibilita(utenteId: Long, visibilita: Visibilita) {
        Log.d("WishlistViewModel", "Getting wishlists for user $utenteId with visibility $visibilita")
        getWishlists("/utente/$utenteId/visibilita/${visibilita.name}", _wishlistsByVisibilita, "byUtenteAndVisibilita")
    }

    fun addEventoToWishlist(wishlistId: Long, eventoId: Long, onSuccess: () -> Unit = {}) {
        viewModelScope.launch(Dispatchers.IO) {
            _error.value = null
            Log.d("WishlistViewModel", "Attempting to add evento $eventoId to wishlist $wishlistId")

            val urlString = "$backendUrl/$wishlistId/evento/$eventoId"
            Log.d("WishlistViewModel", "Add evento URL: $urlString")

            try {
                val request = getAuthenticatedRequest(urlString)
                    .post("".toRequestBody())
                    .build()

                val response = client.newCall(request).execute()

                Log.d("WishlistViewModel", "Add response code: ${response.code}")

                if (!response.isSuccessful) {
                    _error.value = ErrorData(response.code, "Errore HTTP: ${response.code}")
                    Log.e("WishlistViewModel", "HTTP error adding evento: ${response.code}")
                    return@launch
                }

                Log.d("WishlistViewModel", "Evento aggiunto con successo alla wishlist")

                // Aggiorna lo stato locale immediatamente
                updateWishlistsAfterEventAddition(wishlistId, eventoId)

                withContext(Dispatchers.Main) {
                    onSuccess()
                }
            } catch (e: Exception) {
                _error.value = ErrorData(0, "Errore di rete: ${e.message}")
                Log.e("WishlistViewModel", "Exception adding evento", e)
            }
        }
    }

    private fun updateWishlistsAfterEventAddition(wishlistId: Long, eventoId: Long) {
        _wishlistsByVisibilita.value?.let { currentWishlists ->
            val updatedWishlists = currentWishlists.map { wishlist ->
                if (wishlist.id == wishlistId) {
                    wishlist.copy(eventi = wishlist.eventi + eventoId)
                } else {
                    wishlist
                }
            }
            _wishlistsByVisibilita.value = updatedWishlists
            Log.d("WishlistViewModel", "Updated wishlist after addition. New size: ${updatedWishlists.find { it.id == wishlistId }?.eventi?.size}")
        }
    }



    fun getFirstPrivateWishlistId(utenteId: Long, callback: (Long?) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) { // AGGIUNTO Dispatchers.IO
            try {
                val urlString = "$backendUrl/utente/$utenteId"
                Log.d("WishlistViewModel", "Fetching user wishlists from: $urlString")

                val request = getAuthenticatedRequest(urlString).get().build()
                val response = client.newCall(request).execute()

                Log.d("WishlistViewModel", "Response code: ${response.code}")

                if (!response.isSuccessful) {
                    Log.e("WishlistViewModel", "Error fetching user wishlists: ${response.code}")
                    withContext(Dispatchers.Main) {
                        callback(null)
                    }
                    return@launch
                }

                val responseBody = response.body?.string() ?: ""
                Log.d("WishlistViewModel", "Response body: ${responseBody.take(200)}...")

                if (responseBody.isBlank()) {
                    Log.e("WishlistViewModel", "Empty response body")
                    withContext(Dispatchers.Main) {
                        callback(null)
                    }
                    return@launch
                }

                // Controlla se è HTML (pagina di login)
                if (responseBody.trimStart().startsWith("<!DOCTYPE html>") ||
                    responseBody.trimStart().startsWith("<html")) {
                    Log.e("WishlistViewModel", "Server returned HTML login page")
                    withContext(Dispatchers.Main) {
                        callback(null)
                    }
                    return@launch
                }

                // Parsa la risposta
                val jsonElement = JsonParser.parseString(responseBody)
                val wishlistList = if (jsonElement.isJsonArray) {
                    val listType = object : TypeToken<List<WishlistData>>() {}.type
                    gson.fromJson<List<WishlistData>>(responseBody, listType)
                } else {
                    emptyList<WishlistData>()
                }

                Log.d("WishlistViewModel", "Parsed ${wishlistList.size} wishlists")

                // Trova la wishlist privata
                val privateWishlist = wishlistList.firstOrNull { it.visibilita == Visibilita.PRIVATA }

                Log.d("WishlistViewModel", "Found private wishlist: ${privateWishlist?.id}")

                // Torna al main thread per il callback
                withContext(Dispatchers.Main) {
                    callback(privateWishlist?.id)
                }

            } catch (e: Exception) {
                Log.e("WishlistViewModel", "Exception getting private wishlist ID", e)
                withContext(Dispatchers.Main) {
                    callback(null)
                }
            }
        }
    }


    // Versione sincrona per uso immediato quando i dati sono già caricati
    fun getFirstPrivateWishlistIdSync(): Long? {
        return _wishlistsByVisibilita.value?.firstOrNull { it.visibilita == Visibilita.PRIVATA }?.id
    }



    fun getWishlistCondiviseConUtente(userId: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            _wishlistCondivise.value = emptyList()
            _error.value = null
            _isLoading.value = true

            val urlString = "$backendUrl/condiviseCon/$userId"
            Log.d("WishlistViewModel", "Fetching wishlist condivise con utente $userId from: $urlString")

            try {
                val request = getAuthenticatedRequest(urlString).get().build()
                val response = client.newCall(request).execute()

                Log.d("WishlistViewModel", "Condivise response code: ${response.code}")

                if (!response.isSuccessful) {
                    handleHttpError(response.code, "getWishlistCondiviseConUtente")
                    return@launch
                }

                val responseBody = response.body?.string().orEmpty()
                Log.d("WishlistViewModel", "Condivise response body: ${responseBody.take(200)}...")

                if (responseBody.isBlank() || responseBody.startsWith("<!DOCTYPE html>")) {
                    _error.value = ErrorData(401, "Non autorizzato o risposta vuota")
                    _wishlistCondivise.value = emptyList()
                    return@launch
                }

                parseWishlistResponse(responseBody, _wishlistCondivise)

            } catch (e: Exception) {
                Log.e("WishlistViewModel", "Error fetching condivise", e)
                _error.value = ErrorData(0, "Errore: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun getWishlists(
        urlSuffix: String,
        targetFlow: MutableStateFlow<List<WishlistData>?>,
        tag: String
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            targetFlow.value = emptyList()
            _error.value = null
            _isLoading.value = true

            val urlString = "$backendUrl$urlSuffix"
            Log.d("WishlistViewModel", "Fetching wishlists from: $urlString")

            try {
                val request = getAuthenticatedRequest(urlString).get().build()
                val response = client.newCall(request).execute()

                Log.d("WishlistViewModel", "Response code for $tag: ${response.code}")

                if (!response.isSuccessful) {
                    handleHttpError(response.code, tag)
                    return@launch
                }

                val responseBody = response.body?.string() ?: ""
                Log.d("WishlistViewModel", "Raw Response Body for $tag: ${responseBody.take(200)}...")

                if (responseBody.isBlank()) {
                    targetFlow.value = emptyList()
                    return@launch
                }

                // Controlla se è HTML (pagina di login)
                if (responseBody.trimStart().startsWith("<!DOCTYPE html>") ||
                    responseBody.trimStart().startsWith("<html")) {
                    _error.value = ErrorData(401, "Autenticazione richiesta")
                    Log.e("WishlistViewModel", "Server returned HTML login page for $tag")
                    targetFlow.value = emptyList()
                    return@launch
                }

                parseWishlistResponse(responseBody, targetFlow)

            } catch (e: Exception) {
                Log.e("WishlistViewModel", "Error in getWishlists for $tag", e)
                _error.value = ErrorData(0, "Errore: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun parseWishlistResponse(responseBody: String, targetFlow: MutableStateFlow<List<WishlistData>?>) {
        try {
            val jsonElement = JsonParser.parseString(responseBody)

            val wishlistList = if (jsonElement.isJsonArray) {
                val listType = object : TypeToken<List<WishlistData>>() {}.type
                gson.fromJson<List<WishlistData>>(responseBody, listType)
            } else if (jsonElement.isJsonObject) {
                val singleWishlist = gson.fromJson(responseBody, WishlistData::class.java)
                listOf(singleWishlist)
            } else {
                Log.e("WishlistViewModel", "Formato di risposta non valido")
                _error.value = ErrorData(0, "Formato di risposta non valido")
                emptyList()
            }

            targetFlow.value = wishlistList
            Log.d("WishlistViewModel", "Parsed ${wishlistList.size} wishlists successfully")
        } catch (e: JsonSyntaxException) {
            Log.e("WishlistViewModel", "Errore nel parsing del JSON", e)
            _error.value = ErrorData(0, "Errore di parsing JSON: ${e.message}")
            targetFlow.value = emptyList()
        } catch (e: Exception) {
            Log.e("WishlistViewModel", "Errore generico nel parsing", e)
            _error.value = ErrorData(0, "Errore generico: ${e.message}")
            targetFlow.value = emptyList()
        }
    }

    fun condividiWishlistConEmail(wishlistId: Long, email: String) {
        viewModelScope.launch(Dispatchers.IO) {
            withContext(Dispatchers.Main) {
                _condivisoneState.value = CondivisioneState.Loading
            }

            try {
                // Step 1: Cerca l'utente tramite email
                val emailUrl = "$server/api/utente/cerca-email?email=${URLEncoder.encode(email, "UTF-8")}"
                Log.d("WishlistViewModel", "Searching user by email: $emailUrl")

                val emailRequest = getAuthenticatedRequest(emailUrl).get().build()
                val emailResponse = client.newCall(emailRequest).execute()

                Log.d("WishlistViewModel", "Email search response code: ${emailResponse.code}")

                if (!emailResponse.isSuccessful) {
                    val errorMsg = when (emailResponse.code) {
                        404 -> "Email non trovata nel sistema"
                        401 -> "Non autorizzato - controlla il token"
                        500 -> "Errore interno del server"
                        else -> "Errore nella ricerca dell'utente (${emailResponse.code})"
                    }
                    Log.e("WishlistViewModel", "Error searching email: $errorMsg")
                    withContext(Dispatchers.Main) {
                        _condivisoneState.value = CondivisioneState.Error(errorMsg)
                    }
                    return@launch
                }

                val responseBody = emailResponse.body?.string() ?: ""
                Log.d("WishlistViewModel", "Email search response body: $responseBody")

                if (responseBody.isBlank()) {
                    Log.e("WishlistViewModel", "Empty response body from email search")
                    withContext(Dispatchers.Main) {
                        _condivisoneState.value = CondivisioneState.Error("Risposta vuota dal server")
                    }
                    return@launch
                }

                // Controlla se è HTML (pagina di errore)
                if (responseBody.trimStart().startsWith("<!DOCTYPE html>") ||
                    responseBody.trimStart().startsWith("<html")) {
                    Log.e("WishlistViewModel", "Server returned HTML page instead of JSON")
                    withContext(Dispatchers.Main) {
                        _condivisoneState.value = CondivisioneState.Error("Errore di autenticazione")
                    }
                    return@launch
                }

                // CORREZIONE PRINCIPALE: Il server restituisce solo l'ID come numero
                val userId = try {
                    responseBody.trim().toLong()
                } catch (e: NumberFormatException) {
                    Log.e("WishlistViewModel", "Errore nel parsing dell'ID utente da response: $responseBody", e)
                    withContext(Dispatchers.Main) {
                        _condivisoneState.value = CondivisioneState.Error("Formato ID non valido")
                    }
                    return@launch
                }

                Log.d("WishlistViewModel", "Found user ID: $userId for email: $email")

                // Step 2: Condividi la wishlist
                val condivisione = WishlistCondivisaRequest(
                    wishlistId = wishlistId,
                    userId = userId
                )
                val jsonBody = gson.toJson(condivisione)
                val requestBody = jsonBody.toRequestBody("application/json".toMediaTypeOrNull())

                val shareUrl = "$server/api/wishlist/condivisa/create"
                Log.d("WishlistViewModel", "Creating share: $shareUrl with body: $jsonBody")

                val postRequest = getAuthenticatedRequest(shareUrl)
                    .post(requestBody)
                    .build()

                val postResponse = client.newCall(postRequest).execute()
                Log.d("WishlistViewModel", "Share response code: ${postResponse.code}")

                if (!postResponse.isSuccessful) {
                    val shareResponseBody = postResponse.body?.string() ?: ""
                    Log.e("WishlistViewModel", "Share failed with response: $shareResponseBody")

                    val errorMsg = when (postResponse.code) {
                        400 -> "Wishlist già condivisa con questo utente"
                        404 -> "Wishlist non trovata"
                        401 -> "Non autorizzato"
                        500 -> "Errore interno del server"
                        else -> "Errore nella condivisione (${postResponse.code})"
                    }
                    withContext(Dispatchers.Main) {
                        _condivisoneState.value = CondivisioneState.Error(errorMsg)
                    }
                    return@launch
                }

                Log.d("WishlistViewModel", "Wishlist shared successfully with user $userId")
                withContext(Dispatchers.Main) {
                    _condivisoneState.value = CondivisioneState.Success("Wishlist condivisa con successo con $email!")
                    _isWishlistCondivisa.value = true // Aggiorna lo stato di condivisione
                }

                // Ricarica le condivisioni per la wishlist corrente
                verificaSeWishlistECondivisa(wishlistId)

            } catch (e: IOException) {
                Log.e("WishlistViewModel", "Network error sharing wishlist", e)
                withContext(Dispatchers.Main) {
                    _condivisoneState.value = CondivisioneState.Error("Errore di connessione: Verifica la tua connessione internet")
                }
            } catch (e: Exception) {
                Log.e("WishlistViewModel", "Exception sharing wishlist", e)
                withContext(Dispatchers.Main) {
                    _condivisoneState.value = CondivisioneState.Error("Errore imprevisto: ${e.message}")
                }
            }
        }
    }


    private fun handleHttpError(responseCode: Int, operation: String) {
        when (responseCode) {
            HttpURLConnection.HTTP_UNAUTHORIZED -> {
                Log.e("WishlistViewModel", "Token scaduto o non valido per $operation")
                sessionManager.clearSession()
                _error.value = ErrorData(responseCode, "Sessione scaduta, rifare il login")
            }
            HttpURLConnection.HTTP_FORBIDDEN -> {
                Log.e("WishlistViewModel", "Accesso negato per $operation")
                _error.value = ErrorData(responseCode, "Accesso negato")
            }
            HttpURLConnection.HTTP_NOT_FOUND -> {
                Log.e("WishlistViewModel", "Risorsa non trovata per $operation")
                _error.value = ErrorData(responseCode, "Risorsa non trovata")
            }
            else -> {
                Log.e("WishlistViewModel", "Errore HTTP $responseCode per $operation")
                _error.value = ErrorData(responseCode, "Errore HTTP: $responseCode")
            }
        }
    }
}
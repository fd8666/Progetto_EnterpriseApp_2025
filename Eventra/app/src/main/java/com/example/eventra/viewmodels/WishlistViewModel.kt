package com.example.eventra.viewmodels

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.eventra.R
import com.example.eventra.Visibilita
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
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.EOFException
import java.io.IOException
import java.net.URL
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import java.net.URLEncoder

class WishlistViewModel(application: Application) : AndroidViewModel(application) {

    private val _application = application
    private val gson: Gson = GsonBuilder()
        .setDateFormat("yyyy-MM-dd'T'HH:mm:ss")
        .setLenient()
        .create()
    private val server = application.getString(R.string.server)
    private val backendUrl = URL("$server/api/wishlist")

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _wishlists = MutableStateFlow<List<WishlistData>?>(emptyList())
    val wishlists: StateFlow<List<WishlistData>?> = _wishlists.asStateFlow()

    private val _wishlistsByVisibilita = MutableStateFlow<List<WishlistData>?>(emptyList())
    val wishlistsByVisibilita: StateFlow<List<WishlistData>?> = _wishlistsByVisibilita.asStateFlow()

    // Nuovo StateFlow per le wishlist condivise da me
    private val _wishlistCondivise = MutableStateFlow<List<WishlistData>?>(emptyList())
    val wishlistCondivise: StateFlow<List<WishlistData>?> = _wishlistCondivise.asStateFlow()

    private val _wishlistDetail = MutableStateFlow<WishlistData?>(null)
    val wishlistDetail: StateFlow<WishlistData?> = _wishlistDetail.asStateFlow()

    private val _error = MutableStateFlow<ErrorData?>(null)
    val error: StateFlow<ErrorData?> = _error.asStateFlow()


    fun removeEventoFromWishlist(wishlistId: Long, eventoId: Long, onSuccess: () -> Unit = {}) {
        CoroutineScope(Dispatchers.IO).launch {
            _error.value = null

            val urlString = "$backendUrl/$wishlistId/evento/$eventoId"
            Log.d("WishlistViewModel", "Removing evento $eventoId from wishlist $wishlistId: $urlString")

            val client = OkHttpClient()
            val request = Request.Builder()
                .url(urlString)
                .delete()
                .build()

            try {
                val response = client.newCall(request).execute()

                if (!response.isSuccessful) {
                    _error.value = ErrorData(response.code, _application.getString(R.string.http_error))
                    Log.e("WishlistViewModel", "HTTP error removing evento: ${response.code}")
                    return@launch
                }

                Log.d("WishlistViewModel", "Evento rimosso con successo dalla wishlist")

                // Ricarica le wishlist dopo la rimozione
                _wishlistsByVisibilita.value?.let { currentWishlists ->
                    val updatedWishlists = currentWishlists.map { wishlist ->
                        if (wishlist.id == wishlistId) {
                            wishlist.copy(eventi = wishlist.eventi.filter { it != eventoId })
                        } else {
                            wishlist
                        }
                    }
                    _wishlistsByVisibilita.value = updatedWishlists
                }

                // Aggiorna anche le wishlist condivise se necessario
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

                onSuccess()
            } catch (e: IOException) {
                _error.value = ErrorData(0, _application.getString(R.string.network_error))
                Log.e("WishlistViewModel", "Network error removing evento: ${e.message}")
            } catch (e: Exception) {
                _error.value = ErrorData(0, _application.getString(R.string.unexpected_error))
                Log.e("WishlistViewModel", "Unexpected error removing evento: ${e.message}")
            }
        }
    }

    fun getAllWishlists() = getWishlists("", _wishlists, "allWishlists")

    fun getWishlistsByUtente(utenteId: Long) =
        getWishlists("/utente/$utenteId", _wishlists, "byUtente")

    fun getWishlistsByVisibilita(visibilita: Visibilita) =
        getWishlists("/visibilita/${visibilita.name}", _wishlistsByVisibilita, "byVisibilita")

    fun getWishlistsByUtenteAndVisibilita(utenteId: Long, visibilita: Visibilita) =
        getWishlists("/utente/$utenteId/visibilita/${visibilita.name}", _wishlistsByVisibilita, "byUtenteAndVisibilita")

    fun addEventoToWishlist(wishlistId: Long, eventoId: Long, onSuccess: () -> Unit = {}) {
        CoroutineScope(Dispatchers.IO).launch {
            _error.value = null

            val urlString = "$backendUrl/$wishlistId/evento/$eventoId"
            Log.d("WishlistViewModel", "Adding evento $eventoId to wishlist $wishlistId: $urlString")

            val client = OkHttpClient()
            val request = Request.Builder()
                .url(urlString)
                .post("".toRequestBody()) // POST vuoto
                .build()

            try {
                val response = client.newCall(request).execute()

                if (!response.isSuccessful) {
                    _error.value = ErrorData(response.code, _application.getString(R.string.http_error))
                    Log.e("WishlistViewModel", "HTTP error adding evento: ${response.code}")
                    return@launch
                }

                Log.d("WishlistViewModel", "Evento aggiunto con successo alla wishlist")

                // Aggiorna le wishlist dopo l'aggiunta
                _wishlistsByVisibilita.value?.let { currentWishlists ->
                    val updatedWishlists = currentWishlists.map { wishlist ->
                        if (wishlist.id == wishlistId) {
                            wishlist.copy(eventi = wishlist.eventi + eventoId)
                        } else {
                            wishlist
                        }
                    }
                    _wishlistsByVisibilita.value = updatedWishlists
                }

                onSuccess()
            } catch (e: IOException) {
                _error.value = ErrorData(0, _application.getString(R.string.network_error))
                Log.e("WishlistViewModel", "Network error adding evento: ${e.message}")
            } catch (e: Exception) {
                _error.value = ErrorData(0, _application.getString(R.string.unexpected_error))
                Log.e("WishlistViewModel", "Unexpected error adding evento: ${e.message}")
            }
        }
    }

    // Metodo per verificare se un evento è nella wishlist
    fun isEventoInWishlist(eventoId: Long): Boolean {
        return _wishlistsByVisibilita.value?.any { wishlist ->
            wishlist.eventi.contains(eventoId)
        } ?: false
    }

    // Metodo per ottenere l'ID della prima wishlist privata
    fun getFirstPrivateWishlistId(): Long? {
        return _wishlistsByVisibilita.value?.firstOrNull { wishlist ->
            wishlist.visibilita == Visibilita.PRIVATA
        }?.id
    }

    fun getWishlistCondiviseConUtente(utenteId: Long) {
        CoroutineScope(Dispatchers.IO).launch {
            _wishlistCondivise.value = emptyList()
            _error.value = null
            _isLoading.value = true

            val urlString = "$server/api/wishlist/condiviseCon/$utenteId"
            Log.d("WishlistViewModel", "Fetching wishlist condivise con utente from: $urlString")

            val client = OkHttpClient()
            val request = Request.Builder()
                .url(urlString)
                .get()
                .build()

            try {
                val response = client.newCall(request).execute()

                if (!response.isSuccessful) {
                    _error.value = ErrorData(response.code, _application.getString(R.string.http_error))
                    Log.e("WishlistViewModel", "HTTP error: ${response.code} for wishlist condivise con utente")
                    return@launch
                }

                val responseBody = response.body?.string() ?: ""
                Log.d("WishlistViewModel", "Raw Response Body for condivise con utente: ${responseBody.take(200)}...")

                if (responseBody.isBlank()) {
                    _wishlistCondivise.value = emptyList()
                    return@launch
                }

                // Controlla se è HTML (pagina di login)
                if (responseBody.trimStart().startsWith("<!DOCTYPE html>") ||
                    responseBody.trimStart().startsWith("<html")) {
                    _error.value = ErrorData(401, "Autenticazione richiesta per wishlist condivise con utente")
                    Log.e("WishlistViewModel", "Server returned HTML login page for wishlist condivise con utente")
                    _wishlistCondivise.value = emptyList()
                    return@launch
                }

                try {
                    val jsonElement = JsonParser.parseString(responseBody)

                    val wishlistList = if (jsonElement.isJsonArray) {
                        val listType = object : TypeToken<List<WishlistData>>() {}.type
                        gson.fromJson<List<WishlistData>>(responseBody, listType)
                    } else if (jsonElement.isJsonObject) {
                        val singleWishlist = gson.fromJson(responseBody, WishlistData::class.java)
                        listOf(singleWishlist)
                    } else {
                        Log.e("WishlistViewModel", "Formato di risposta non valido per wishlist condivise con utente")
                        _error.value = ErrorData(0, "Formato di risposta non valido")
                        emptyList()
                    }

                    _wishlistCondivise.value = wishlistList
                    Log.d("WishlistViewModel", "Caricate ${wishlistList.size} wishlist condivise con utente")
                } catch (e: JsonSyntaxException) {
                    Log.e("WishlistViewModel", "Errore nel parsing del JSON per wishlist condivise con utente: ${e.message}")
                    _error.value = ErrorData(0, "Errore di autenticazione o formato dati non valido")
                    _wishlistCondivise.value = emptyList()
                }
            } catch (e: IOException) {
                _error.value = ErrorData(0, _application.getString(R.string.network_error))
                Log.e("WishlistViewModel", "Network error per wishlist condivise con utente: ${e.message}")
            } catch (e: Exception) {
                _error.value = ErrorData(0, _application.getString(R.string.unexpected_error))
                Log.e("WishlistViewModel", "Unexpected error per wishlist condivise con utente: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }


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

    private fun getWishlists(
        urlSuffix: String,
        targetFlow: MutableStateFlow<List<WishlistData>?>,
        tag: String
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            targetFlow.value = emptyList()
            _error.value = null
            _isLoading.value = true

            val urlString = "$backendUrl$urlSuffix"
            Log.d("WishlistViewModel", "Fetching wishlists from: $urlString")

            val client = OkHttpClient()
            val request = Request.Builder()
                .url(urlString)
                .get()
                .build()

            try {
                val response = client.newCall(request).execute()

                if (!response.isSuccessful) {
                    _error.value = ErrorData(response.code, _application.getString(R.string.http_error))
                    Log.e("WishlistViewModel", "HTTP error: ${response.code} for tag: $tag")
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
                } catch (e: JsonSyntaxException) {
                    Log.e("WishlistViewModel", "Errore nel parsing del JSON: ${e.message}")
                    _error.value = ErrorData(0, "Errore di autenticazione o formato dati non valido")
                    targetFlow.value = emptyList()
                }
            } catch (e: IOException) {
                _error.value = ErrorData(0, _application.getString(R.string.network_error))
                Log.e("WishlistViewModel", "Network error: ${e.message}")
            } catch (e: Exception) {
                _error.value = ErrorData(0, _application.getString(R.string.unexpected_error))
                Log.e("WishlistViewModel", "Unexpected error: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun condividiWishlistConEmail(wishlistId: Long, email: String) {
        CoroutineScope(Dispatchers.IO).launch {
            _condivisoneState.value = CondivisioneState.Loading

            try {
                // Step 1: Cerca l'utente per email
                val emailUrl = "$server/api/utente/cerca-email?email=${URLEncoder.encode(email, "UTF-8")}"
                Log.d("WishlistViewModel", "Searching user by email: $emailUrl")

                val emailRequest = Request.Builder().url(emailUrl).get().build()
                val emailResponse = OkHttpClient().newCall(emailRequest).execute()

                if (!emailResponse.isSuccessful) {
                    val errorMsg = when (emailResponse.code) {
                        404 -> "Email non trovata nel sistema"
                        else -> "Errore nella ricerca dell'utente (${emailResponse.code})"
                    }
                    _condivisoneState.value = CondivisioneState.Error(errorMsg)
                    return@launch
                }

                val responseBody = emailResponse.body?.string() ?: ""
                Log.d("WishlistViewModel", "Email search response: $responseBody")

                val jsonObject = JsonParser.parseString(responseBody).asJsonObject
                val userId = jsonObject["data"].asJsonObject["id"].asLong

                val condivisione = WishlistCondivisaRequest(wishlistId, userId)
                val jsonBody = gson.toJson(condivisione)
                val requestBody = jsonBody.toRequestBody("application/json".toMediaTypeOrNull())

                val shareUrl = "$server/api/wishlist/condivisa/create" // Verifica questo endpoint
                Log.d("WishlistViewModel", "Creating share: $shareUrl with body: $jsonBody")

                val postRequest = Request.Builder()
                    .url(shareUrl)
                    .post(requestBody)
                    .build()

                val postResponse = OkHttpClient().newCall(postRequest).execute()

                if (!postResponse.isSuccessful) {
                    val errorMsg = when (postResponse.code) {
                        400 -> "Wishlist già condivisa con questo utente"
                        404 -> "Wishlist non trovata"
                        else -> "Errore nella condivisione (${postResponse.code})"
                    }
                    _condivisoneState.value = CondivisioneState.Error(errorMsg)
                    return@launch
                }

                _condivisoneState.value = CondivisioneState.Success("Wishlist condivisa con successo con $email!")
                getWishlistCondiviseConUtente(1)

            } catch (e: Exception) {
                Log.e("WishlistViewModel", "Error sharing wishlist: ${e.message}")
                _condivisoneState.value = CondivisioneState.Error("Errore di connessione: ${e.message}")
            }
        }
    }


}

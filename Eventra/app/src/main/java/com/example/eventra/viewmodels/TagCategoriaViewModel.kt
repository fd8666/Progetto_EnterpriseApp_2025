package com.example.eventra.viewmodels
import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.example.eventra.R
import com.example.eventra.viewmodels.data.ErrorData
import com.example.eventra.viewmodels.data.TagCategoriaData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException
import java.net.URL

class TagCategoriaViewModel(application: Application) : AndroidViewModel(application) {
    private val _application = application
    private val gson = Gson()
    private val server = application.getString(R.string.server)
    private val backendUrl = URL("$server/api/tag-categoria")

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _categorie = MutableStateFlow<List<TagCategoriaData>?>(emptyList())
    val categorie: StateFlow<List<TagCategoriaData>?> = _categorie.asStateFlow()

    private val _error = MutableStateFlow<ErrorData?>(null)
    val error: StateFlow<ErrorData?> = _error.asStateFlow()

    fun getAllCategorie() = getCategorie("", _categorie)

    private fun getCategorie(urlSuffix: String, targetFlow: MutableStateFlow<List<TagCategoriaData>?>) {
        viewModelScope.launch(Dispatchers.IO) {
            targetFlow.value = emptyList()
            _error.value = null
            _isLoading.value = true

            val urlString = "$backendUrl$urlSuffix"
            Log.d("TagCategoriaViewModel", "Retrieving categorie from $urlString")

            val client = OkHttpClient()
            val request = Request.Builder()
                .url(urlString)
                .get()
                .build()

            try {
                val response = client.newCall(request).execute()

                if (!response.isSuccessful) {
                    _error.value = ErrorData(response.code, _application.getString(R.string.http_error))
                    Log.e("TagCategoriaViewModel", "HTTP error: ${response.code}")

                    return@launch
                }

                val responseJson = response.body?.string() ?: ""
                Log.d("TagCategoriaViewModel", "Response categorie: $responseJson")

                val categorieList: List<TagCategoriaData> = gson.fromJson(responseJson, Array<TagCategoriaData>::class.java).toList()

                targetFlow.value = categorieList
            } catch (e: IOException) {
                _error.value = ErrorData(0, _application.getString(R.string.network_error))
                Log.e("TagCategoriaViewModel", "Network error: ${e.message}")

            } catch (e: Exception) {
                _error.value = ErrorData(0, _application.getString(R.string.unexpected_error))
                Log.e("TagCategoriaViewModel", "Unexpected error: ${e.message}")

            } finally {
                _isLoading.value = false
            }
        }
    }

}

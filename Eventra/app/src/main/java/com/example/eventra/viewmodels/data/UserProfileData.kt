package com.example.eventra.viewmodels.data

data class OrdineData(
    val id: Long,
    val dataOrdine: String,
    val importoTotale: Double,
    val stato: String,
    val biglietti: List<BigliettoData> = emptyList()
)


data class ApiResponseData<T>(
    val success: Boolean,
    val message: String,
    val data: T?
)

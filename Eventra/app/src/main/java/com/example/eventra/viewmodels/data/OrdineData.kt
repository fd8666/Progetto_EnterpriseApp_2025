package com.example.eventra.viewmodels.data


data class OrdineData(
    val id: Long? = null,
    val dataCreazione: String? = null,
    val emailProprietario: String,
    val prezzoTotale: Double,
    val proprietarioId: Long? = null
)
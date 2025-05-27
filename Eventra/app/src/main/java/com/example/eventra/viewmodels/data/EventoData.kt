package com.example.eventra.viewmodels.data


data class EventoData(
    val id: Long,
    val nome: String?,
    val descrizione: String?,
    val immagine: String?,
    val categoriaId: Long?,
    val dataOraEvento: String,
    val dataOraAperturaCancelli: String,
    val postiDisponibili: Int,
    val luogo: String
)


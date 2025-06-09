package com.example.eventra.viewmodels.data

data class TipoPostoData(
    val id: Long? = null,
    val nome: String = "",
    val prezzo: Double = 0.0,
    val postiDisponibili: Int = 0,
    val eventoId: Long? = null
)
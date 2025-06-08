package com.example.eventra.viewmodels.data

data class BigliettoData(
    val id: Long,
    val nomeSpettatore: String?,
    val cognomeSpettatore: String?,
    val prezzo: Double,
    val eventoNome: String,
    val tipoPostoNome: String,
    val dataEvento: String
)
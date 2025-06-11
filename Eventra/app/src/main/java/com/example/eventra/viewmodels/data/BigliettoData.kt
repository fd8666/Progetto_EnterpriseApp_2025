package com.example.eventra.viewmodels.data

data class BigliettoData(
    val id: Long? = null,
    val nomeSpettatore: String = "",
    val cognomeSpettatore: String = "",
    val emailSpettatore: String = "",
    val eventoId: Long,
    val tipoPostoId: Long,
    val pagamentoId: Long? = null,
    val isExpanded: Boolean = true,
    val dataCreazione: String
)


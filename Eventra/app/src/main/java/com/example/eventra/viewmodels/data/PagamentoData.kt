package com.example.eventra.viewmodels.data

import java.math.BigDecimal


data class PagamentoData(
    val id: Long? = null,
    val nomeTitolare: String,
    val cognomeTitolare: String,
    val numeroCarta: String,
    val scadenza: String,
    val cvv: String,
    val importo: BigDecimal,
    val dataPagamento: String? = null,
    val ordineId: Long? = null
)

data class PagamentoRequest(
    val nomeTitolare: String,
    val cognomeTitolare: String,
    val numeroCarta: String,
    val scadenza: String,
    val cvv: String,
    val importo: String
)
package com.example.eventra.viewmodels.data

import com.example.eventra.Visibilita

data class WishlistData(
    val id: Long,
    val utenteId: Long,
    val visibilita: Visibilita,
    val dataCreazione: String,
    val eventi: List<Long>
)


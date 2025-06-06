package com.example.eventra.viewmodels.data

import java.time.LocalDateTime
import com.example.eventra.Visibilita

data class WishlistData(
    val id: Long,
    val utenteId: Long = 1,
    val visibilita: Visibilita,
    val dataCreazione: String,
    val eventi: List<Long>
)


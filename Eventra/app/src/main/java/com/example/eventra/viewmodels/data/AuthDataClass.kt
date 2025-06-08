package com.example.eventra.viewmodels.data

data class LoginRequest(
    val email: String,
    val password: String
)

data class LoginResponse(
    val token: String,
    val refreshToken: String,
    val userId: Long
)

data class ApiResponseAuth<T>(
    val success: Boolean,
    val message: String,
    val data: T?
)

data class RegistrationRequest(
    val nome: String,
    val cognome: String,
    val email: String,
    val password: String,
    val numerotelefono: String? = null
)

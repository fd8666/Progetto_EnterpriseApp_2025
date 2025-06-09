package com.example.eventra.viewmodels.data

data class ApiResponseData<T>(
    val success: Boolean,
    val message: String,
    val data: T?
)

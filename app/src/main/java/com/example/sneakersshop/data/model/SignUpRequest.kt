package com.example.sneakersshop.data.model

data class SignUpRequest(
    val email: String,
    val password: String,
    val data: Map<String, String>? = null
)

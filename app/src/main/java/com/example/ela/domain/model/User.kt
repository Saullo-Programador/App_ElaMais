package com.example.ela.domain.model

data class User(
    val uid: String,
    val email: String? = null,
    val displayName: String? = null
)
package com.example.ela.data.remote.dto

data class CareActionDto(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val phase: String = "",
    val isCompleted: Boolean = false
)
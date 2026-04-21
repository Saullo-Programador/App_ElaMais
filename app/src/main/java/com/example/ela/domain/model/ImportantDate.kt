package com.example.ela.domain.model

data class ImportantDate(
    val id: Long,
    val title: String,
    val date: Long,
    val isRecurring: Boolean
)
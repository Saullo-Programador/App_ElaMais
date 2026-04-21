package com.example.ela.data.remote.dto

data class ImportantDateDto(
    val id: String = "",
    val title: String = "",
    val date: Long = 0L,
    val isRecurring: Boolean = false
)
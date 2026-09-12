package com.example.ela.data.remote.dto

data class ReminderDto(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val date: Long = 0L,
    val type: String = ""
)
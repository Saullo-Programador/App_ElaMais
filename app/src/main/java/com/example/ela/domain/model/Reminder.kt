package com.example.ela.domain.model

data class Reminder(
    val id: Long,
    val title: String,
    val description: String,
    val date: Long,
    val type: String
)
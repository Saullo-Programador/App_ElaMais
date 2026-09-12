package com.example.ela.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "care_actions")
data class CareActionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val description: String,
    val phase: String,
    val isCompleted: Boolean
)
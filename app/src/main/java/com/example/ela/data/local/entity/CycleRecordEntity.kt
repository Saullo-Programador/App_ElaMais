package com.example.ela.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cycle_records")
data class CycleRecordEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val startDate: Long,
    val endDate: Long
)
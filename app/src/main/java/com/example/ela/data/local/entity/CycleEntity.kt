package com.example.ela.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "cycle")
data class CycleEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val cycleLength: Int,
    val periodLength: Int,
    val lastPeriodStart: Long
)
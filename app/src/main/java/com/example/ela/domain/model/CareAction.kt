package com.example.ela.domain.model

import com.example.ela.domain.model.CyclePhase

data class CareAction(
    val id: Long,
    val title: String,
    val description: String,
    val phase: CyclePhase,
    val isCompleted: Boolean
)
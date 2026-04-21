package com.example.ela.domain.model

data class CycleInfo(
    val currentPhase: CyclePhase,
    val daysUntilNextPeriod: Int,
    val isFertileWindow: Boolean,
    val isPms: Boolean,
    val suggestions: List<String>,
    val hasData: Boolean
) {

    companion object {
        fun empty() = CycleInfo(
            currentPhase = CyclePhase.FOLLICULAR,
            daysUntilNextPeriod = 0,
            isFertileWindow = false,
            isPms = false,
            suggestions = emptyList(),
            hasData = false
        )
    }
}
package com.example.ela.domain.model

data class CycleInfo(
    val currentPhase: CyclePhase,
    val daysUntilNextPeriod: Int,
    val daysRemainingInPhase: Int = 0,
    val daysUntilFertileWindow: Int = 0,
    val daysUntilPms: Int = 0,
    val isFertileWindow: Boolean,
    val isPms: Boolean,
    val suggestions: List<String>,
    val hasData: Boolean
) {

    companion object {
        fun empty() = CycleInfo(
            currentPhase = CyclePhase.FOLLICULAR,
            daysUntilNextPeriod = 0,
            daysRemainingInPhase = 0,
            daysUntilFertileWindow = 0,
            daysUntilPms = 0,
            isFertileWindow = false,
            isPms = false,
            suggestions = emptyList(),
            hasData = false
        )
    }
}
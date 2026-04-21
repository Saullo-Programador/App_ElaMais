package com.example.ela.core.utils

import com.example.ela.domain.model.CyclePhase

fun getPhaseTitle(phase: CyclePhase): String {
    return when (phase) {
        CyclePhase.MENSTRUAL -> "Menstrual 🩸"
        CyclePhase.FOLLICULAR -> "Folicular 🌱"
        CyclePhase.OVULATION -> "Ovulação 💕"
        CyclePhase.LUTEAL -> "Lútea 🌙"
        CyclePhase.TPM -> "TPM 😅"
    }
}
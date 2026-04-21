package com.example.ela.data.repository

import com.example.ela.domain.model.CareAction
import com.example.ela.domain.model.CyclePhase
import com.example.ela.domain.repository.CareActionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class CareActionRepositoryImpl : CareActionRepository {

    // Dados mockados para teste
    private val defaultActions = mapOf(
        CyclePhase.MENSTRUAL to listOf(
            CareAction(1, "Beber água quente", "Ajuda com as cólicas", CyclePhase.MENSTRUAL, false),
            CareAction(2, "Descansar", "Seu corpo precisa de energia", CyclePhase.MENSTRUAL, false),
            CareAction(3, "Comer chocolate", "Você merece 😊", CyclePhase.MENSTRUAL, false),
            CareAction(4, "Fazer compressa quente", "Alivia as dores", CyclePhase.MENSTRUAL, false)
        ),
        CyclePhase.FOLLICULAR to listOf(
            CareAction(5, "Exercício leve", "Aproveite a energia renovada", CyclePhase.FOLLICULAR, false),
            CareAction(6, "Planejar a semana", "Momento de organização", CyclePhase.FOLLICULAR, false),
            CareAction(7, "Comer alimentos frescos", "Nutrientes para o corpo", CyclePhase.FOLLICULAR, false)
        ),
        CyclePhase.OVULATION to listOf(
            CareAction(8, "Socializar", "Você está no auge da energia!", CyclePhase.OVULATION, false),
            CareAction(9, "Atividade física", "Aproveite a força", CyclePhase.OVULATION, false),
            CareAction(10, "Cuidar da pele", "A pele está mais bonita", CyclePhase.OVULATION, false)
        ),
        CyclePhase.LUTEAL to listOf(
            CareAction(11, "Meditar", "Momento de introspecção", CyclePhase.LUTEAL, false),
            CareAction(12, "Organizar espaço", "Prepare o ambiente", CyclePhase.LUTEAL, false),
            CareAction(13, "Reduzir cafeína", "Ajuda na qualidade do sono", CyclePhase.LUTEAL, false)
        ),
        CyclePhase.TPM to listOf(
            CareAction(14, "Evitar gatilhos", "Cuide do seu emocional", CyclePhase.TPM, false),
            CareAction(15, "Tomar chá de camomila", "Ajuda a relaxar", CyclePhase.TPM, false),
            CareAction(16, "Escrever um diário", "Expresse seus sentimentos", CyclePhase.TPM, false),
            CareAction(17, "Ficar em casa", "Momento de autocuidado", CyclePhase.TPM, false)
        )
    )

    private val userActions = mutableMapOf<Long, CareAction>()

    override fun getByPhase(phase: CyclePhase): Flow<List<CareAction>> = flow {
        val actions = defaultActions[phase]?.map { action ->
            userActions[action.id] ?: action
        } ?: emptyList()
        emit(actions)
    }

    override suspend fun update(action: CareAction) {
        userActions[action.id] = action
    }

    override suspend fun save(action: CareAction) {
        userActions[action.id] = action
    }
}

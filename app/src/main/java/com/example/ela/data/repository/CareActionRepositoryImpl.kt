package com.example.ela.data.repository

import android.util.Log
import com.example.ela.data.local.dao.CareActionDao
import com.example.ela.data.mapper.toDomain
import com.example.ela.data.mapper.toEntity
import com.example.ela.domain.model.CareAction
import com.example.ela.domain.model.CyclePhase
import com.example.ela.domain.repository.CareActionRepository
import kotlinx.coroutines.flow.*

class CareActionRepositoryImpl(
    private val dao: CareActionDao
) : CareActionRepository {

    // Dados mockados para teste (Dicas padrão)
    private val defaultActions = mapOf(
        CyclePhase.MENSTRUAL to listOf(
            CareAction(1, "Beber água quente", "Ajuda com as cólicas", CyclePhase.MENSTRUAL, false),
            CareAction(2, "Descansar", "Seu corpo precisa de energia", CyclePhase.MENSTRUAL, false),
            CareAction(3, "Comer chocolate", "Você merece 😊", CyclePhase.MENSTRUAL, false),
            CareAction(4, "Fazer compressa quente", "Alivia as dores", CyclePhase.MENSTRUAL, false),
            CareAction(5, "Consumir alimentos ricos em ferro", "Repõe as perdas sanguíneas", CyclePhase.MENSTRUAL, false),
            CareAction(6, "Alongamentos suaves", "Reduz a tensão muscular", CyclePhase.MENSTRUAL, false)
        ),
        CyclePhase.FOLLICULAR to listOf(
            CareAction(7, "Exercício leve", "Aproveite a energia renovada", CyclePhase.FOLLICULAR, false),
            CareAction(8, "Planejar a semana", "Momento de organização", CyclePhase.FOLLICULAR, false),
            CareAction(9, "Comer alimentos frescos", "Nutrientes para o corpo", CyclePhase.FOLLICULAR, false),
            CareAction(10, "Atividades criativas", "Sua mente está mais aberta a novas ideias", CyclePhase.FOLLICULAR, false),
            CareAction(11, "Treino de força", "Aproveite o aumento de energia", CyclePhase.FOLLICULAR, false)
        ),
        CyclePhase.OVULATION to listOf(
            CareAction(12, "Socializar", "Você está no auge da energia!", CyclePhase.OVULATION, false),
            CareAction(13, "Atividade física", "Aproveite a força", CyclePhase.OVULATION, false),
            CareAction(14, "Cuidar da pele", "A pele está mais bonita", CyclePhase.OVULATION, false),
            CareAction(15, "Treinos de alta intensidade", "Seu corpo está no pico de performance", CyclePhase.OVULATION, false),
            CareAction(16, "Vitamina C e antioxidantes", "Suporte ao sistema imunológico", CyclePhase.OVULATION, false)
        ),
        CyclePhase.LUTEAL to listOf(
            CareAction(17, "Meditar", "Momento de introspecção", CyclePhase.LUTEAL, false),
            CareAction(18, "Organizar espaço", "Prepare o ambiente", CyclePhase.LUTEAL, false),
            CareAction(19, "Reduzir cafeína", "Ajuda na qualidade do sono", CyclePhase.LUTEAL, false),
            CareAction(20, "Carboidratos complexos", "Estabiliza o humor e a energia", CyclePhase.LUTEAL, false),
            CareAction(21, "Reduzir o consumo de sal", "Diminui a retenção de líquidos", CyclePhase.LUTEAL, false)
        ),
        CyclePhase.TPM to listOf(
            CareAction(22, "Evitar gatilhos", "Cuide do seu emocional", CyclePhase.TPM, false),
            CareAction(23, "Tomar chá de camomila", "Ajuda a relaxar", CyclePhase.TPM, false),
            CareAction(24, "Escrever um diário", "Expresse seus sentimentos", CyclePhase.TPM, false),
            CareAction(25, "Ficar em casa", "Momento de autocuidado", CyclePhase.TPM, false),
            CareAction(26, "Banhos mornos", "Relaxa a musculatura e a mente", CyclePhase.TPM, false),
            CareAction(27, "Consumir Ômega-3 e Cálcio", "Ajuda a reduzir sintomas de irritabilidade", CyclePhase.TPM, false)
        )
    )

    override fun getByPhase(
        phase: CyclePhase
    ): Flow<List<CareAction>> =
        dao.getByPhase(phase.name)
            .map { entities ->
                entities.map { it.toDomain() }
            }

    override suspend fun initializeDefaults(phase: CyclePhase) {
        val defaults = defaultActions[phase] ?: emptyList()

        dao.insertAll(
            defaults.map { it.toEntity() }
        )
    }

    override suspend fun update(action: CareAction) {
        val entity = action.toEntity()

        val rowsUpdated = dao.update(entity)

        Log.d(
            "CareAction",
            "Update: id=${entity.id}, " +
                    "title=${entity.title}, " +
                    "isCompleted=${entity.isCompleted}, " +
                    "rowsUpdated=$rowsUpdated"
        )
    }

    override suspend fun save(action: CareAction) {
        dao.insert(action.toEntity())
    }

    override suspend fun resetCompletedActions() {
        dao.resetAllCompletions()
    }
}

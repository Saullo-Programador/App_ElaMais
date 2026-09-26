package com.example.ela.data.repository

import android.util.Log
import com.example.ela.data.local.dao.CareActionDao
import com.example.ela.data.mapper.toDomain
import com.example.ela.data.mapper.toEntity
import com.example.ela.data.mapper.toDto
import com.example.ela.domain.model.CareAction
import com.example.ela.domain.model.CyclePhase
import com.example.ela.domain.repository.AuthRepository
import com.example.ela.domain.repository.CareActionRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.tasks.await

class CareActionRepositoryImpl(
    private val dao: CareActionDao,
    private val firestore: FirebaseFirestore,
    private val authRepository: AuthRepository
) : CareActionRepository {

    private fun getCareActionsCollection() =
        authRepository.getCurrentUser()?.uid?.let { uid ->
            firestore.collection("users").document(uid).collection("care_actions")
        }

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
                val dbActions = entities.map { it.toDomain() }
                val defaults = defaultActions[phase] ?: emptyList()

                val result = defaults.toMutableList()
                dbActions.forEach { dbAction ->
                    val index = result.indexOfFirst { it.id == dbAction.id }
                    if (index != -1) {
                        result[index] = dbAction
                    } else {
                        result.add(dbAction)
                    }
                }
                result
            }

    override suspend fun initializeDefaults(phase: CyclePhase) {
        val defaults = defaultActions[phase] ?: emptyList()

        dao.insertAll(
            defaults.map { it.toEntity() }
        )
    }

    override suspend fun update(action: CareAction) {
        // 🔹 Salva local primeiro
        dao.update(action.toEntity())
        // 🔹 Tenta atualizar no Firebase
        try {
            getCareActionsCollection()?.document(action.id.toString())
                ?.set(action.toDto())
                ?.await()
        } catch (e: Exception) {
            Log.e("CareActionRepository", "Erro ao atualizar cuidado no Firebase", e)
        }
    }

    override suspend fun save(action: CareAction) {
        // 🔹 Salva local primeiro
        dao.insert(action.toEntity())
        // 🔹 Tenta salvar no Firebase
        try {
            getCareActionsCollection()?.document(action.id.toString())
                ?.set(action.toDto())
                ?.await()
        } catch (e: Exception) {
            Log.e("CareActionRepository", "Erro ao salvar cuidado no Firebase", e)
        }
    }

    override suspend fun deleteById(id: Long){
        // 🔹 Deleta local primeiro
        dao.deleteById(id)
        // 🔹 Tenta deletar no Firebase
        try {
            getCareActionsCollection()?.document(id.toString())
                ?.delete()
                ?.await()
        } catch (e: Exception) {
            Log.e("CareActionRepository", "Erro ao deletar cuidado no Firebase", e)
        }
    }

    override suspend fun deleteAll() {
        dao.deleteAll()
        // Since we can't easily delete all documents in Firestore without a batch
        // or querying them all, we'll fetch and delete.
        try {
            val snapshot = getCareActionsCollection()?.get()?.await()
            snapshot?.documents?.forEach { doc ->
                getCareActionsCollection()?.document(doc.id)?.delete()?.await()
            }
        } catch (e: Exception) {
            Log.e("CareActionRepository", "Erro ao deletar todos os cuidados no Firebase", e)
        }
    }

    override suspend fun resetCompletedActions() {
        dao.resetAllCompletions()
    }

    override suspend fun syncCareActions() {
        try {
            val snapshot = getCareActionsCollection()?.get()?.await()
            val list = snapshot?.documents?.mapNotNull {
                it.toObject(com.example.ela.data.remote.dto.CareActionDto::class.java)
            } ?: emptyList()

            list.forEach { dto ->
                dao.insert(dto.toDomain().toEntity())
            }
        } catch (e: Exception) {
            Log.e("CareActionRepository", "Erro ao sincronizar cuidados do Firebase", e)
        }
    }
}

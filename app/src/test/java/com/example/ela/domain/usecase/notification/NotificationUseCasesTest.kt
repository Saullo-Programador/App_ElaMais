package com.example.ela.domain.usecase.notification

import com.example.ela.domain.model.Preferences
import com.example.ela.domain.repository.PreferencesRepository
import com.example.ela.notification.scheduler.NotificationScheduler
import io.mockk.Called
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Test

class NotificationUseCasesTest {

    private val scheduler = mockk<NotificationScheduler>(relaxed = true)
    private val preferencesRepository = mockk<PreferencesRepository>()

    private val preferences = Preferences(notificationsEnabled = true, notificationsTime = "09:00", timesPerDay = 2)

    // ------------------------------------------------------------------
    // ScheduleCycleNotificationsUseCase
    // ------------------------------------------------------------------

    @Test
    fun `ScheduleCycle deve agendar fertilidade e menstruacao usando as preferencias`() = runTest {
        every { preferencesRepository.getPreferences() } returns flowOf(preferences)

        ScheduleCycleNotificationsUseCase(scheduler, preferencesRepository)(
            cycleId = 10L,
            lastPeriodStart = 5_000L,
            cycleLength = 28
        )

        verify(exactly = 1) {
            scheduler.scheduleFertileWindowNotifications(
                cycleId = 10L,
                lastPeriodStart = 5_000L,
                cycleLength = 28,
                preferences = preferences
            )
        }
        verify(exactly = 1) {
            scheduler.scheduleMenstruationNotifications(
                cycleId = 10L,
                lastPeriodStart = 5_000L,
                cycleLength = 28,
                preferences = preferences
            )
        }
    }

    @Test
    fun `ScheduleCycle nao deve agendar nada quando nao ha preferencias salvas`() = runTest {
        every { preferencesRepository.getPreferences() } returns flowOf(null)

        ScheduleCycleNotificationsUseCase(scheduler, preferencesRepository)(10L, 5_000L, 28)

        verify { scheduler wasNot Called }
    }

    // ------------------------------------------------------------------
    // ScheduleImportantDateNotificationUseCase
    // ------------------------------------------------------------------

    @Test
    fun `ScheduleImportantDate deve agendar a notificacao da data`() = runTest {
        every { preferencesRepository.getPreferences() } returns flowOf(preferences)

        ScheduleImportantDateNotificationUseCase(scheduler, preferencesRepository)(
            dateId = 3L,
            title = "Aniversário",
            dateMillis = 9_000L
        )

        verify(exactly = 1) {
            scheduler.scheduleImportantDateNotification(
                dateId = 3L,
                title = "Aniversário",
                dateMillis = 9_000L,
                preferences = preferences
            )
        }
    }

    @Test
    fun `ScheduleImportantDate nao deve agendar quando nao ha preferencias`() = runTest {
        every { preferencesRepository.getPreferences() } returns flowOf(null)

        ScheduleImportantDateNotificationUseCase(scheduler, preferencesRepository)(3L, "Aniversário", 9_000L)

        verify { scheduler wasNot Called }
    }

    // ------------------------------------------------------------------
    // CancelNotificationsUseCase
    // ------------------------------------------------------------------

    @Test
    fun `Cancel deve cancelar as notificacoes de uma data importante`() {
        CancelNotificationsUseCase(scheduler).cancelImportantDateNotifications(4L)

        verify(exactly = 1) { scheduler.cancelImportantDateNotifications(4L) }
    }

    @Test
    fun `Cancel deve cancelar fertilidade e menstruacao de um ciclo`() {
        CancelNotificationsUseCase(scheduler).cancelCycleNotifications(8L)

        verify(exactly = 1) { scheduler.cancelFertileWindowNotifications(8L) }
        verify(exactly = 1) { scheduler.cancelMenstruationNotifications(8L) }
    }

    @Test
    fun `Cancel deve cancelar todas as notificacoes`() {
        CancelNotificationsUseCase(scheduler).cancelAllNotifications()

        verify(exactly = 1) { scheduler.cancelAllNotifications() }
    }
}

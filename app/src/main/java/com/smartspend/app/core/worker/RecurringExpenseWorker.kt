package com.smartspend.app.core.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.smartspend.app.core.notification.NotificationHelper
import com.smartspend.app.domain.usecase.recurring.ProcessDueRecurringExpensesUseCase
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class RecurringExpenseWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val processDueRecurringExpensesUseCase: ProcessDueRecurringExpensesUseCase,
    private val notificationHelper: NotificationHelper
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            val processed = processDueRecurringExpensesUseCase()
            if (processed > 0) {
                notificationHelper.showRecurringExpenseProcessedNotification(processed)
            }
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }
}

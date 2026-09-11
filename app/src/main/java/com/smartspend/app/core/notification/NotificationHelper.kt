package com.smartspend.app.core.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationHelper @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        const val CHANNEL_BUDGET = "smartspend_budget_alerts"
        const val CHANNEL_RECURRING = "smartspend_recurring"
        const val CHANNEL_GOALS = "smartspend_goals"
    }

    init {
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            val budgetChannel = NotificationChannel(
                CHANNEL_BUDGET,
                "Budget Alerts",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Alerts when spending approaches or exceeds budget limits"
            }

            val recurringChannel = NotificationChannel(
                CHANNEL_RECURRING,
                "Subscriptions & Bills",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Reminders for due and recurring expenses"
            }

            val goalsChannel = NotificationChannel(
                CHANNEL_GOALS,
                "Savings Goals",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Milestone updates for your savings targets"
            }

            notificationManager.createNotificationChannels(listOf(budgetChannel, recurringChannel, goalsChannel))
        }
    }

    fun showRecurringExpenseProcessedNotification(count: Int) {
        if (count <= 0) return

        val notification = NotificationCompat.Builder(context, CHANNEL_RECURRING)
            .setSmallIcon(android.R.drawable.ic_menu_agenda)
            .setContentTitle("SmartSpend Subscriptions")
            .setContentText("Processed $count recurring expense${if (count > 1) "s" else ""}.")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(1001, notification)
    }

    fun showBudgetWarningNotification(title: String, message: String) {
        val notification = NotificationCompat.Builder(context, CHANNEL_BUDGET)
            .setSmallIcon(android.R.drawable.stat_notify_error)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(2001, notification)
    }
}

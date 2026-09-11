package com.smartspend.app

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.smartspend.app.core.worker.RecurringExpenseWorker
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltAndroidApp
class SmartSpendApplication : Application(), Configuration.Provider {

    companion object {
        init {
            try {
                System.loadLibrary("sqlcipher")
            } catch (t: Throwable) {
                // Ignore
            }
        }
    }

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override fun onCreate() {
        super.onCreate()
        try {
            System.loadLibrary("sqlcipher")
        } catch (e: Exception) {
            Timber.e(e, "Failed to load sqlcipher native library")
        }

        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
        setupPeriodicWorkers()
    }

    private fun setupPeriodicWorkers() {
        try {
            val recurringWorkRequest = PeriodicWorkRequestBuilder<RecurringExpenseWorker>(
                1, TimeUnit.DAYS
            ).build()

            WorkManager.getInstance(this).enqueueUniquePeriodicWork(
                "SmartSpendRecurringWorker",
                ExistingPeriodicWorkPolicy.KEEP,
                recurringWorkRequest
            )
        } catch (e: Exception) {
            Timber.e(e, "Failed to schedule periodic workers")
        }
    }
}

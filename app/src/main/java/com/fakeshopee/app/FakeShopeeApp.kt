package com.fakeshopee.app

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.fakeshopee.app.data.local.AppDatabase
import com.fakeshopee.app.data.local.SeedData
import com.fakeshopee.app.data.worker.SyncWorker
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltAndroidApp
class FakeShopeeApp : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    @Inject
    lateinit var appDatabase: AppDatabase

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override fun onCreate() {
        super.onCreate()
        // Pre-populate Room DB with initial catalog and wallet balance if new install
        CoroutineScope(Dispatchers.IO).launch {
            SeedData.seedDatabaseIfEmpty(appDatabase)
        }
        setupBackgroundSync()
    }

    private fun setupBackgroundSync() {
        // Scheduled background synchronization when charging and connected to Wi-Fi (Unmetered)
        val syncConstraints = Constraints.Builder()
            .setRequiresCharging(true)
            .setRequiredNetworkType(NetworkType.UNMETERED) // Wi-Fi constraint
            .setRequiresBatteryNotLow(true)
            .build()

        val syncWorkRequest = PeriodicWorkRequestBuilder<SyncWorker>(15, TimeUnit.MINUTES)
            .setConstraints(syncConstraints)
            .build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "FakeShopeeCatalogSyncWork",
            ExistingPeriodicWorkPolicy.KEEP,
            syncWorkRequest
        )
    }
}

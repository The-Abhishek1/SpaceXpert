package com.xcloak.spacexpert

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.xcloak.spacexpert.engine.TrashCleanupWorker
import com.xcloak.spacexpert.engine.ScheduledCleanupWorker
import dagger.hilt.android.HiltAndroidApp
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltAndroidApp
class SpaceXpertApp : Application(), Configuration.Provider {

    @Inject lateinit var workerFactory: HiltWorkerFactory

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override fun onCreate() {
        super.onCreate()
        com.google.android.gms.ads.MobileAds.initialize(this) {}
        com.xcloak.spacexpert.util.BillingManager.initialize(this)
        scheduleTrashCleanup()
    }

    private fun scheduleTrashCleanup() {
        val request = PeriodicWorkRequestBuilder<TrashCleanupWorker>(24, TimeUnit.HOURS)
            .build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "trash_cleanup",
            ExistingPeriodicWorkPolicy.KEEP,
            request
        )

        val scheduledRequest = PeriodicWorkRequestBuilder<ScheduledCleanupWorker>(7, TimeUnit.DAYS)
            .build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "warp_speed_cleanup",
            ExistingPeriodicWorkPolicy.KEEP,
            scheduledRequest
        )
    }
}
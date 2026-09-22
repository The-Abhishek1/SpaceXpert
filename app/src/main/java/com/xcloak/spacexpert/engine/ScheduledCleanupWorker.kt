package com.xcloak.spacexpert.engine

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class ScheduledCleanupWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted params: WorkerParameters,
    private val storageAnalyzer: StorageAnalyzer,
    private val trashRepository: TrashRepository
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            // 1. Auto-cleanup: clear internal trash files/expired items
            trashRepository.purgeExpired()

            // 2. Background Alert check: estimate potential cleanup bytes
            val potentialBytes = storageAnalyzer.estimatePotentialCleanupBytes()
            val thresholdBytes = 1024L * 1024L * 1024L // 1GB

            if (potentialBytes > thresholdBytes) {
                showJunkAlertNotification(potentialBytes)
            }

            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }

    private fun showJunkAlertNotification(bytes: Long) {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "scheduled_clean_alerts"
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Junk Alerts",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            manager.createNotificationChannel(channel)
        }

        val mb = bytes / (1024 * 1024)
        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.stat_notify_chat)
            .setContentTitle("Warp Speed Cleanup Alert")
            .setContentText("You have over ${mb} MB of redundant junk files waiting to be cleaned!")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        manager.notify(976, notification)
    }
}
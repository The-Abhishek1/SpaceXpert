package com.xcloak.spacexpert.engine

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class TrashCleanupWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val trashRepository: TrashRepository
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            trashRepository.purgeExpired()
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }
}
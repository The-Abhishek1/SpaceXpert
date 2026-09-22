package com.xcloak.spacexpert.engine

import android.content.Context
import android.net.Uri
import com.xcloak.spacexpert.data.TrashDao
import com.xcloak.spacexpert.data.TrashEntity
import kotlinx.coroutines.flow.Flow
import java.io.File
import java.util.UUID
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class TrashRepository @Inject constructor(
    private val dao: TrashDao,
    @ApplicationContext private val context: Context
) {
    private val retentionDays = 7 // bump to 30/90 for Pro tier later

    fun observeTrash(): Flow<List<TrashEntity>> = dao.observeAll()

    suspend fun moveToTrash(sourceUri: Uri, originalPath: String) {
        val trashDir = File(context.filesDir, "trash").apply { mkdirs() }
        val trashFile = File(trashDir, UUID.randomUUID().toString())
        context.contentResolver.openInputStream(sourceUri)?.use { input ->
            trashFile.outputStream().use { output -> input.copyTo(output) }
        }
        val now = System.currentTimeMillis()
        dao.insert(
            TrashEntity(
                id = trashFile.name,
                originalPath = originalPath,
                trashPath = trashFile.path,
                deletedAt = now,
                sizeBytes = trashFile.length(),
                expiresAt = now + retentionDays * 86_400_000L
            )
        )
    }

    /**
     * Moves a file straight from its live location into trash, then deletes the original.
     * Used by the duplicate cleaner — it already has a File path, not a MediaStore Uri.
     */
    suspend fun trashFileAt(path: String) {
        val original = File(path)
        if (!original.exists()) return
        val trashDir = File(context.filesDir, "trash").apply { mkdirs() }
        val trashFile = File(trashDir, UUID.randomUUID().toString())
        original.copyTo(trashFile, overwrite = true)
        val now = System.currentTimeMillis()
        dao.insert(
            TrashEntity(
                id = trashFile.name,
                originalPath = path,
                trashPath = trashFile.path,
                deletedAt = now,
                sizeBytes = trashFile.length(),
                expiresAt = now + retentionDays * 86_400_000L
            )
        )
        original.delete()
    }

    /**
     * Restores a trashed file back to its original path.
     * Note: writing back to arbitrary external paths on API 29+ requires
     * MANAGE_EXTERNAL_STORAGE — fine for now since trashFileAt() above only
     * touches files your Files/Clean tabs already had access to via SAF/MediaStore.
     */
    suspend fun restore(item: TrashEntity) {
        val trashFile = File(item.trashPath)
        if (!trashFile.exists()) return
        val destination = File(item.originalPath)
        destination.parentFile?.mkdirs()
        trashFile.copyTo(destination, overwrite = true)
        trashFile.delete()
        dao.delete(item.id)
    }

    suspend fun purgeExpired() {
        dao.getExpired(System.currentTimeMillis()).forEach {
            File(it.trashPath).delete()
            dao.delete(it.id)
        }
    }
}
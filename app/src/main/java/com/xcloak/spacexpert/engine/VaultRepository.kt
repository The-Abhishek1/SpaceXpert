package com.xcloak.spacexpert.engine

import android.content.Context
import android.net.Uri
import androidx.security.crypto.EncryptedFile
import androidx.security.crypto.MasterKey
import com.xcloak.spacexpert.data.VaultDao
import com.xcloak.spacexpert.data.DecoyVaultDao
import com.xcloak.spacexpert.data.DecoyVaultEntity
import com.xcloak.spacexpert.data.VaultEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.util.UUID
import javax.inject.Inject

class VaultRepository @Inject constructor(
    private val dao: VaultDao,
    private val decoyDao: DecoyVaultDao,
    @ApplicationContext private val context: Context
) {
    private val masterKey by lazy {
        MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
    }

    private val vaultDir: File
        get() = File(context.filesDir, "vault").apply { mkdirs() }

    fun observeVaultItems(isDecoy: Boolean = false): Flow<List<VaultEntity>> {
        return if (isDecoy) {
            decoyDao.observeAll().map { list ->
                list.map { VaultEntity(it.id, it.originalName, it.encryptedPath, it.addedAt, it.sizeBytes) }
            }
        } else {
            dao.observeAll()
        }
    }

    /** Encrypts and moves a file into the vault, then deletes the original plaintext copy. */
    suspend fun addToVault(sourceUri: Uri, originalName: String, originalPath: String?, isDecoy: Boolean = false) =
        withContext(Dispatchers.IO) {
            val encryptedFile = File(vaultDir, UUID.randomUUID().toString())

            val encrypted = EncryptedFile.Builder(
                context,
                encryptedFile,
                masterKey,
                EncryptedFile.FileEncryptionScheme.AES256_GCM_HKDF_4KB
            ).build()

            var totalBytes = 0L
            context.contentResolver.openInputStream(sourceUri)?.use { input ->
                encrypted.openFileOutput().use { output ->
                    val buffer = ByteArray(8192)
                    var read: Int
                    while (input.read(buffer).also { read = it } != -1) {
                        output.write(buffer, 0, read)
                        totalBytes += read
                    }
                }
            }

            if (isDecoy) {
                decoyDao.insert(
                    DecoyVaultEntity(
                        id = encryptedFile.name,
                        originalName = originalName,
                        encryptedPath = encryptedFile.path,
                        addedAt = System.currentTimeMillis(),
                        sizeBytes = totalBytes
                    )
                )
            } else {
                dao.insert(
                    VaultEntity(
                        id = encryptedFile.name,
                        originalName = originalName,
                        encryptedPath = encryptedFile.path,
                        addedAt = System.currentTimeMillis(),
                        sizeBytes = totalBytes
                    )
                )
            }

            // Remove the plaintext original now that it's safely encrypted in the vault
            originalPath?.let { File(it).delete() }
        }

    /** Decrypts a vault item out to a temp cache file for viewing/sharing, returns the temp file path. */
    suspend fun decryptToTemp(item: VaultEntity): File = withContext(Dispatchers.IO) {
        val encryptedFile = EncryptedFile.Builder(
            context,
            File(item.encryptedPath),
            masterKey,
            EncryptedFile.FileEncryptionScheme.AES256_GCM_HKDF_4KB
        ).build()

        val tempFile = File(context.cacheDir, item.originalName)
        encryptedFile.openFileInput().use { input ->
            tempFile.outputStream().use { output -> input.copyTo(output) }
        }
        tempFile
    }

    suspend fun removeFromVault(item: VaultEntity, isDecoy: Boolean = false) = withContext(Dispatchers.IO) {
        File(item.encryptedPath).delete()
        if (isDecoy) {
            decoyDao.delete(DecoyVaultEntity(item.id, item.originalName, item.encryptedPath, item.addedAt, item.sizeBytes))
        } else {
            dao.delete(item)
        }
    }
}
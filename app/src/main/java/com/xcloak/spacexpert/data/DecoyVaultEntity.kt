package com.xcloak.spacexpert.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "decoy_vault_items")
data class DecoyVaultEntity(
    @PrimaryKey val id: String,
    val originalName: String,
    val encryptedPath: String,
    val addedAt: Long,
    val sizeBytes: Long
)

@Dao
interface DecoyVaultDao {
    @Query("SELECT * FROM decoy_vault_items ORDER BY addedAt DESC")
    fun observeAll(): Flow<List<DecoyVaultEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: DecoyVaultEntity)

    @Delete
    suspend fun delete(item: DecoyVaultEntity)
}

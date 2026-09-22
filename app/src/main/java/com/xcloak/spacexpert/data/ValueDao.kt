package com.xcloak.spacexpert.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface VaultDao {
    @Insert suspend fun insert(item: VaultEntity)

    @Delete suspend fun delete(item: VaultEntity)

    @Query("SELECT * FROM vault_items ORDER BY addedAt DESC")
    fun observeAll(): Flow<List<VaultEntity>>
}
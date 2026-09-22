package com.xcloak.spacexpert.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface TrashDao {
    @Insert suspend fun insert(item: TrashEntity)

    @Query("SELECT * FROM trash_items WHERE expiresAt < :now")
    suspend fun getExpired(now: Long): List<TrashEntity>

    @Query("DELETE FROM trash_items WHERE id = :id")
    suspend fun delete(id: String)

    @Query("SELECT * FROM trash_items ORDER BY deletedAt DESC")
    fun observeAll(): Flow<List<TrashEntity>>
}
package com.xcloak.spacexpert.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [TrashEntity::class, VaultEntity::class, DecoyVaultEntity::class], version = 3, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun trashDao(): TrashDao
    abstract fun vaultDao(): VaultDao
    abstract fun decoyVaultDao(): DecoyVaultDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "storage_manager_db"
                )
                    .fallbackToDestructiveMigration() // fine pre-release; replace with real Migration before shipping
                    .build().also { INSTANCE = it }
            }
    }
}
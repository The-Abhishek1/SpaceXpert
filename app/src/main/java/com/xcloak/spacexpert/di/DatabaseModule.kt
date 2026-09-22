package com.xcloak.spacexpert.di

import android.content.Context
import com.xcloak.spacexpert.data.AppDatabase
import com.xcloak.spacexpert.data.TrashDao
import com.xcloak.spacexpert.data.VaultDao
import com.xcloak.spacexpert.data.DecoyVaultDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase =
        AppDatabase.getInstance(context)

    @Provides
    fun provideTrashDao(db: AppDatabase): TrashDao = db.trashDao()

    @Provides
    fun provideVaultDao(db: AppDatabase): VaultDao = db.vaultDao()

    @Provides
    fun provideDecoyVaultDao(db: AppDatabase): DecoyVaultDao = db.decoyVaultDao()
}
package com.xcloak.spacexpert.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "vault_items")
data class VaultEntity(
    @PrimaryKey val id: String,
    val originalName: String,
    val encryptedPath: String,
    val addedAt: Long,
    val sizeBytes: Long
)
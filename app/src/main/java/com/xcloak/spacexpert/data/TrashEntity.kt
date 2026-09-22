package com.xcloak.spacexpert.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "trash_items")
data class TrashEntity(
    @PrimaryKey val id: String,
    val originalPath: String,
    val trashPath: String,
    val deletedAt: Long,
    val sizeBytes: Long,
    val expiresAt: Long
)
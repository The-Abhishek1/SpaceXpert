package com.xcloak.spacexpert.engine

import com.xcloak.spacexpert.data.DuplicateGroup
import com.xcloak.spacexpert.data.FileEntry
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.security.MessageDigest
import javax.inject.Inject

class DuplicateFinder @Inject constructor() {

    suspend fun findDuplicates(files: List<FileEntry>): List<DuplicateGroup> =
        withContext(Dispatchers.IO) {
            val sizeGroups = files.groupBy { it.size }.filter { it.value.size > 1 }
            val hashGroups = mutableMapOf<String, MutableList<FileEntry>>()

            for ((_, candidates) in sizeGroups) {
                for (file in candidates) {
                    val hash = sha256(file)
                    hashGroups.getOrPut(hash) { mutableListOf() }.add(file)
                }
            }

            hashGroups.filter { it.value.size > 1 }
                .map { (hash, group) -> DuplicateGroup(hash, group) }
        }

    private fun sha256(file: FileEntry): String {
        val digest = MessageDigest.getInstance("SHA-256")
        File(file.path).inputStream().use { input ->
            val buffer = ByteArray(8192)
            var read: Int
            while (input.read(buffer).also { read = it } != -1) {
                digest.update(buffer, 0, read)
            }
        }
        return digest.digest().joinToString("") { "%02x".format(it) }
    }
}
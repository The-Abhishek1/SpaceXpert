package com.xcloak.spacexpert.engine

import android.os.Environment
import com.xcloak.spacexpert.data.BrowsableFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject

class FileBrowser @Inject constructor() {

    fun rootPath(): String = Environment.getExternalStorageDirectory().path

    suspend fun list(path: String): List<BrowsableFile> = withContext(Dispatchers.IO) {
        val dir = File(path)
        val children = dir.listFiles() ?: return@withContext emptyList()

        children
            .filterNot { it.isHidden }
            .map {
                BrowsableFile(
                    name = it.name,
                    path = it.path,
                    isDirectory = it.isDirectory,
                    sizeBytes = if (it.isDirectory) 0L else it.length(),
                    lastModified = it.lastModified()
                )
            }
            .sortedWith(compareByDescending<BrowsableFile> { it.isDirectory }.thenBy { it.name.lowercase() })
    }

    suspend fun deletePermanently(path: String): Boolean = withContext(Dispatchers.IO) {
        File(path).deleteRecursively()
    }
}
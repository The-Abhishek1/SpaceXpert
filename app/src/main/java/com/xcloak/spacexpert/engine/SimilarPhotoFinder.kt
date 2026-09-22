package com.xcloak.spacexpert.engine

import com.xcloak.spacexpert.data.DuplicateGroup
import com.xcloak.spacexpert.data.FileEntry
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class SimilarPhotoFinder @Inject constructor(
    private val hasher: PerceptualHasher
) {
    /**
     * Groups photos that look visually similar (not byte-identical).
     * [maxDistance] controls sensitivity: 0-5 is "near-identical",
     * 6-10 catches resized/recompressed copies, above that gets noisy.
     */
    suspend fun findSimilar(
        files: List<FileEntry>,
        maxDistance: Int = 8
    ): List<DuplicateGroup> = withContext(Dispatchers.IO) {
        val hashed = files.mapNotNull { file ->
            hasher.computeHash(file.path)?.let { hash -> file to hash }
        }

        val visited = BooleanArray(hashed.size)
        val groups = mutableListOf<DuplicateGroup>()

        for (i in hashed.indices) {
            if (visited[i]) continue
            val cluster = mutableListOf(hashed[i].first)
            visited[i] = true

            for (j in i + 1 until hashed.size) {
                if (visited[j]) continue
                val distance = hasher.hammingDistance(hashed[i].second, hashed[j].second)
                if (distance <= maxDistance) {
                    cluster.add(hashed[j].first)
                    visited[j] = true
                }
            }

            if (cluster.size > 1) {
                groups.add(DuplicateGroup(hash = "similar-${hashed[i].second}", files = cluster))
            }
        }

        groups
    }
}
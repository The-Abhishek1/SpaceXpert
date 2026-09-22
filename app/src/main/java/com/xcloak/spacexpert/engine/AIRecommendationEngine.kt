package com.xcloak.spacexpert.engine

import com.xcloak.spacexpert.data.LargeFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

data class AIRecommendation(
    val title: String,
    val message: String,
    val potentialSavingsBytes: Long,
    val affectedFilesCount: Int,
    val type: RecommendationType
)

enum class RecommendationType {
    BLURRY_PHOTOS, MEMES, LARGE_SCREEN_RECORDINGS, DUPLICATE_PHOTOS
}

@Singleton
class AIRecommendationEngine @Inject constructor() {

    suspend fun getRecommendations(files: List<LargeFile>): List<AIRecommendation> = withContext(Dispatchers.Default) {
        // Simulate deep AI analysis latency
        delay(1500)
        
        val recommendations = mutableListOf<AIRecommendation>()

        // Mock detection logic based on filenames/extensions
        val screenRecordings = files.filter { it.name.contains("Screen", ignoreCase = true) && it.sizeBytes > 50_000_000 }
        if (screenRecordings.isNotEmpty()) {
            recommendations.add(
                AIRecommendation(
                    title = "Massive Warp Logs Detected",
                    message = "Found ${screenRecordings.size} screen recordings taking up significant sector space.",
                    potentialSavingsBytes = screenRecordings.sumOf { it.sizeBytes },
                    affectedFilesCount = screenRecordings.size,
                    type = RecommendationType.LARGE_SCREEN_RECORDINGS
                )
            )
        }

        val potentialMemes = files.filter { 
            (it.name.contains("meme", ignoreCase = true) || it.name.contains("whatsapp", ignoreCase = true)) &&
            it.sizeBytes < 500_000 
        }
        if (potentialMemes.size > 5) {
            recommendations.add(
                AIRecommendation(
                    title = "Redundant Meme Sector",
                    message = "We found ${potentialMemes.size} low-res space memes. Clear them to optimize warp efficiency.",
                    potentialSavingsBytes = potentialMemes.sumOf { it.sizeBytes },
                    affectedFilesCount = potentialMemes.size,
                    type = RecommendationType.MEMES
                )
            )
        }

        val blurryCandidates = files.filter { it.name.contains("IMG", ignoreCase = true) }.take(8)
        if (blurryCandidates.isNotEmpty()) {
            recommendations.add(
                AIRecommendation(
                    title = "Blurred Nebula captures",
                    message = "AI analysis suggests ${blurryCandidates.size} photos have low clarity. Clean to save 450MB.",
                    potentialSavingsBytes = 450 * 1024 * 1024L,
                    affectedFilesCount = blurryCandidates.size,
                    type = RecommendationType.BLURRY_PHOTOS
                )
            )
        }

        recommendations
    }
}

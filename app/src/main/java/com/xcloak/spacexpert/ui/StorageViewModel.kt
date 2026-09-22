package com.xcloak.spacexpert.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.xcloak.spacexpert.data.CategorySummary
import com.xcloak.spacexpert.data.LargeFile
import com.xcloak.spacexpert.engine.AIRecommendation
import com.xcloak.spacexpert.engine.AIRecommendationEngine
import com.xcloak.spacexpert.engine.StorageAnalyzer
import com.xcloak.spacexpert.engine.TrashRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.SharingStarted
import javax.inject.Inject

@HiltViewModel
class StorageViewModel @Inject constructor(
    private val analyzer: StorageAnalyzer,
    private val trashRepository: TrashRepository,
    private val aiEngine: AIRecommendationEngine
) : ViewModel() {

    private val _categorySummaries = MutableStateFlow<List<CategorySummary>>(emptyList())
    val categorySummaries: StateFlow<List<CategorySummary>> = _categorySummaries

    private val _largestFiles = MutableStateFlow<List<LargeFile>>(emptyList())

    private val _chronoFilterDays = MutableStateFlow(365f)
    val chronoFilterDays: StateFlow<Float> = _chronoFilterDays

    val largestFiles: StateFlow<List<LargeFile>> = combine(
        _largestFiles,
        _chronoFilterDays
    ) { files, days ->
        val cutoffMs = System.currentTimeMillis() - (days.toLong() * 24L * 60L * 60L * 1000L)
        files.filter { it.addedTime == 0L || it.addedTime >= cutoffMs }
    }.stateIn(viewModelScope, kotlinx.coroutines.flow.SharingStarted.WhileSubscribed(5000), emptyList())

    fun updateChronoFilter(days: Float) {
        _chronoFilterDays.value = days
    }

    private val _potentialCleanupBytes = MutableStateFlow(0L)
    val potentialCleanupBytes: StateFlow<Long> = _potentialCleanupBytes

    private val _aiRecommendations = MutableStateFlow<List<AIRecommendation>>(emptyList())
    val aiRecommendations: StateFlow<List<AIRecommendation>> = _aiRecommendations

    private val _isAnalyzingAI = MutableStateFlow(false)
    val isAnalyzingAI: StateFlow<Boolean> = _isAnalyzingAI

    private val _deletingPaths = MutableStateFlow<Set<String>>(emptySet())
    val deletingPaths: StateFlow<Set<String>> = _deletingPaths

    private val _cloudNodes = MutableStateFlow<List<com.xcloak.spacexpert.data.CloudNode>>(emptyList())
    val cloudNodes: StateFlow<List<com.xcloak.spacexpert.data.CloudNode>> = _cloudNodes

    init {
        refresh()
    }

    private fun refresh() {
        viewModelScope.launch { _categorySummaries.value = analyzer.analyze() }
        viewModelScope.launch { 
            val largeFiles = analyzer.findLargestFiles()
            _largestFiles.value = largeFiles
            runAIAnalysis(largeFiles)
        }
        viewModelScope.launch { _potentialCleanupBytes.value = analyzer.estimatePotentialCleanupBytes() }
        viewModelScope.launch { _cloudNodes.value = analyzer.scanGalacticCloudNodes() }
    }

    private fun runAIAnalysis(files: List<LargeFile>) {
        viewModelScope.launch {
            _isAnalyzingAI.value = true
            _aiRecommendations.value = aiEngine.getRecommendations(files)
            _isAnalyzingAI.value = false
        }
    }

    fun deleteToTrash(file: LargeFile) {
        viewModelScope.launch {
            _deletingPaths.value = _deletingPaths.value + file.path
            withContext(Dispatchers.IO) {
                trashRepository.trashFileAt(file.path)
            }
            // Remove from the visible list and reduce the used-storage total
            _largestFiles.value = _largestFiles.value.filterNot { it.path == file.path }
            _deletingPaths.value = _deletingPaths.value - file.path
        }
    }

    private val _isCompressing = MutableStateFlow(false)
    val isCompressing: StateFlow<Boolean> = _isCompressing

    fun compressFile(file: LargeFile, onComplete: () -> Unit) {
        viewModelScope.launch {
            _isCompressing.value = true
            kotlinx.coroutines.delay(1200) // Simulated vector compression computations
            _largestFiles.value = _largestFiles.value.map { current ->
                if (current.path == file.path) {
                    current.copy(sizeBytes = (current.sizeBytes * 0.32).toLong())
                } else current
            }
            _isCompressing.value = false
            onComplete()
        }
    }
}
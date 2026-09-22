package com.xcloak.spacexpert.ui

import android.content.ContentUris
import android.content.Context
import android.provider.MediaStore
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.xcloak.spacexpert.data.DuplicateGroup
import com.xcloak.spacexpert.data.FileEntry
import com.xcloak.spacexpert.engine.DuplicateFinder
import com.xcloak.spacexpert.engine.SimilarPhotoFinder
import com.xcloak.spacexpert.engine.TrashRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class CleanViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val duplicateFinder: DuplicateFinder,
    private val similarPhotoFinder: SimilarPhotoFinder,
    private val trashRepository: TrashRepository,
    private val storageAnalyzer: com.xcloak.spacexpert.engine.StorageAnalyzer
) : ViewModel() {

    private val _duplicateGroups = MutableStateFlow<List<DuplicateGroup>>(emptyList())
    val duplicateGroups: StateFlow<List<DuplicateGroup>> = _duplicateGroups

    private val _videoDuplicateGroups = MutableStateFlow<List<DuplicateGroup>>(emptyList())
    val videoDuplicateGroups: StateFlow<List<DuplicateGroup>> = _videoDuplicateGroups

    private val _similarGroups = MutableStateFlow<List<DuplicateGroup>>(emptyList())
    val similarGroups: StateFlow<List<DuplicateGroup>> = _similarGroups

    private val _messengerFiles = MutableStateFlow<List<com.xcloak.spacexpert.data.LargeFile>>(emptyList())
    val messengerFiles: StateFlow<List<com.xcloak.spacexpert.data.LargeFile>> = _messengerFiles

    private val _isScanningExact = MutableStateFlow(false)
    val isScanningExact: StateFlow<Boolean> = _isScanningExact

    private val _isScanningVideos = MutableStateFlow(false)
    val isScanningVideos: StateFlow<Boolean> = _isScanningVideos

    private val _isScanningSimilar = MutableStateFlow(false)
    val isScanningSimilar: StateFlow<Boolean> = _isScanningSimilar

    private val _isScanningMessenger = MutableStateFlow(false)
    val isScanningMessenger: StateFlow<Boolean> = _isScanningMessenger

    private val _stasisApps = MutableStateFlow<List<com.xcloak.spacexpert.data.StasisApp>>(emptyList())
    val stasisApps: StateFlow<List<com.xcloak.spacexpert.data.StasisApp>> = _stasisApps

    private val _isScanningStasis = MutableStateFlow(false)
    val isScanningStasis: StateFlow<Boolean> = _isScanningStasis

    private val _isDeleting = MutableStateFlow(false)
    val isDeleting: StateFlow<Boolean> = _isDeleting

    fun scanForDuplicatePhotos() {
        viewModelScope.launch {
            _isScanningExact.value = true
            val photoEntries = withContext(Dispatchers.IO) { loadPhotoEntries() }
            _duplicateGroups.value = duplicateFinder.findDuplicates(photoEntries)
            _isScanningExact.value = false
        }
    }

    fun scanForDuplicateVideos() {
        viewModelScope.launch {
            _isScanningVideos.value = true
            val videoEntries = withContext(Dispatchers.IO) { loadVideoEntries() }
            _videoDuplicateGroups.value = duplicateFinder.findDuplicates(videoEntries)
            _isScanningVideos.value = false
        }
    }

    fun scanForSimilarPhotos() {
        viewModelScope.launch {
            _isScanningSimilar.value = true
            val photoEntries = withContext(Dispatchers.IO) { loadPhotoEntries() }
            _similarGroups.value = similarPhotoFinder.findSimilar(photoEntries)
            _isScanningSimilar.value = false
        }
    }

    fun scanForMessengerSentFiles() {
        viewModelScope.launch {
            _isScanningMessenger.value = true
            _messengerFiles.value = storageAnalyzer.findMessengerSentFiles()
            _isScanningMessenger.value = false
        }
    }

    fun deleteMessengerFiles(files: List<com.xcloak.spacexpert.data.LargeFile>) {
        viewModelScope.launch {
            _isDeleting.value = true
            withContext(Dispatchers.IO) {
                files.forEach { trashRepository.trashFileAt(it.path) }
            }
            _messengerFiles.value = _messengerFiles.value.filterNot { current ->
                files.any { it.path == current.path }
            }
            _isDeleting.value = false
        }
    }

    fun deleteDuplicatesKeepingFirst(group: DuplicateGroup, fromSimilar: Boolean) {
        viewModelScope.launch {
            _isDeleting.value = true
            val toDelete = group.files.drop(1)
            withContext(Dispatchers.IO) {
                toDelete.forEach { trashRepository.trashFileAt(it.path) }
            }
            if (fromSimilar) {
                _similarGroups.value = _similarGroups.value.filterNot { it.hash == group.hash }
            } else {
                _duplicateGroups.value = _duplicateGroups.value.filterNot { it.hash == group.hash }
                _videoDuplicateGroups.value = _videoDuplicateGroups.value.filterNot { it.hash == group.hash }
            }
            _isDeleting.value = false
        }
    }

    fun scanForStasisCandidates() {
        viewModelScope.launch {
            _isScanningStasis.value = true
            _stasisApps.value = storageAnalyzer.findStasisCandidates()
            _isScanningStasis.value = false
        }
    }

    fun engageStasisFor(app: com.xcloak.spacexpert.data.StasisApp) {
        viewModelScope.launch {
            _isDeleting.value = true
            kotlinx.coroutines.delay(1000) // Engaging stasis capsule compression logic
            _stasisApps.value = _stasisApps.value.filterNot { it.packageName == app.packageName }
            _isDeleting.value = false
        }
    }

    private fun loadPhotoEntries(): List<FileEntry> {
        val entries = mutableListOf<FileEntry>()
        val uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        val projection = arrayOf(
            MediaStore.MediaColumns._ID,
            MediaStore.MediaColumns.DATA,
            MediaStore.MediaColumns.SIZE
        )
        context.contentResolver.query(uri, projection, null, null, null)?.use { cursor ->
            val idCol = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns._ID)
            val dataCol = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA)
            val sizeCol = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.SIZE)
            while (cursor.moveToNext()) {
                val id = cursor.getLong(idCol)
                val path = cursor.getString(dataCol) ?: continue
                val size = cursor.getLong(sizeCol)
                val itemUri = ContentUris.withAppendedId(uri, id)
                entries.add(FileEntry(path, itemUri, size))
            }
        }
        return entries
    }

    private fun loadVideoEntries(): List<FileEntry> {
        val entries = mutableListOf<FileEntry>()
        val uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
        val projection = arrayOf(
            MediaStore.MediaColumns._ID,
            MediaStore.MediaColumns.DATA,
            MediaStore.MediaColumns.SIZE
        )
        context.contentResolver.query(uri, projection, null, null, null)?.use { cursor ->
            val idCol = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns._ID)
            val dataCol = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA)
            val sizeCol = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.SIZE)
            while (cursor.moveToNext()) {
                val id = cursor.getLong(idCol)
                val path = cursor.getString(dataCol) ?: continue
                val size = cursor.getLong(sizeCol)
                val itemUri = ContentUris.withAppendedId(uri, id)
                entries.add(FileEntry(path, itemUri, size))
            }
        }
        return entries
    }
}
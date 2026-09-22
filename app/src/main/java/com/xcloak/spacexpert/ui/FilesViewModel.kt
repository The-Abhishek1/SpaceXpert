package com.xcloak.spacexpert.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.xcloak.spacexpert.data.BrowsableFile
import com.xcloak.spacexpert.engine.FileBrowser
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FilesViewModel @Inject constructor(
    private val fileBrowser: FileBrowser
) : ViewModel() {

    private val _currentPath = MutableStateFlow(fileBrowser.rootPath())
    val currentPath: StateFlow<String> = _currentPath

    private val _entries = MutableStateFlow<List<BrowsableFile>>(emptyList())
    val entries: StateFlow<List<BrowsableFile>> = _entries

    private val pathStack = ArrayDeque<String>()

    init {
        loadCurrent()
    }

    fun openFolder(path: String) {
        pathStack.addLast(_currentPath.value)
        _currentPath.value = path
        loadCurrent()
    }

    fun goBack(): Boolean {
        if (pathStack.isEmpty()) return false
        _currentPath.value = pathStack.removeLast()
        loadCurrent()
        return true
    }

    fun deletePermanently(file: BrowsableFile) {
        viewModelScope.launch {
            fileBrowser.deletePermanently(file.path)
            loadCurrent()
        }
    }

    private fun loadCurrent() {
        viewModelScope.launch {
            _entries.value = fileBrowser.list(_currentPath.value)
        }
    }
}
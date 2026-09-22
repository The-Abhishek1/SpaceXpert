package com.xcloak.spacexpert.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.xcloak.spacexpert.data.TrashEntity
import com.xcloak.spacexpert.engine.TrashRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RecoverViewModel @Inject constructor(
    private val trashRepository: TrashRepository
) : ViewModel() {

    val trashedItems: StateFlow<List<TrashEntity>> = trashRepository.observeTrash()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun restore(item: TrashEntity) {
        viewModelScope.launch { trashRepository.restore(item) }
    }
}
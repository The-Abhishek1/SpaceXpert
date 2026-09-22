package com.xcloak.spacexpert.ui

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.xcloak.spacexpert.data.VaultEntity
import com.xcloak.spacexpert.engine.VaultRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
@HiltViewModel
class VaultViewModel @Inject constructor(
    private val vaultRepository: VaultRepository
) : ViewModel() {

    private val _isUnlocked = MutableStateFlow(false)
    val isUnlocked: StateFlow<Boolean> = _isUnlocked

    private val _isDecoyMode = MutableStateFlow(false)
    val isDecoyMode: StateFlow<Boolean> = _isDecoyMode

    val vaultItems: StateFlow<List<VaultEntity>> = _isDecoyMode.flatMapLatest { decoyActive ->
        vaultRepository.observeVaultItems(decoyActive)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun onUnlocked(isDecoy: Boolean = false) {
        _isDecoyMode.value = isDecoy
        _isUnlocked.value = true
    }

    fun lockVault() {
        _isUnlocked.value = false
        _isDecoyMode.value = false
    }

    fun addToVault(uri: Uri, name: String, originalPath: String?) {
        viewModelScope.launch {
            vaultRepository.addToVault(uri, name, originalPath, _isDecoyMode.value)
        }
    }

    fun removeFromVault(item: VaultEntity) {
        viewModelScope.launch {
            vaultRepository.removeFromVault(item, _isDecoyMode.value)
        }
    }
}
package com.gameboost.pro.ui.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gameboost.pro.domain.model.ConnectionState
import com.gameboost.pro.domain.model.Server
import com.gameboost.pro.domain.usecase.GetConnectionStateUseCase
import com.gameboost.pro.domain.usecase.GetSelectedServerUseCase
import com.gameboost.pro.domain.usecase.ConnectVpnUseCase
import com.gameboost.pro.domain.usecase.DisconnectVpnUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val getConnectionStateUseCase: GetConnectionStateUseCase,
    private val getSelectedServerUseCase: GetSelectedServerUseCase,
    private val connectVpnUseCase: ConnectVpnUseCase,
    private val disconnectVpnUseCase: DisconnectVpnUseCase
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()
    
    init {
        observeConnectionState()
        loadSelectedServer()
    }
    
    private fun observeConnectionState() {
        viewModelScope.launch {
            getConnectionStateUseCase().collect { connectionState ->
                _uiState.value = _uiState.value.copy(
                    connectionState = connectionState,
                    isConnecting = connectionState == ConnectionState.CONNECTING
                )
            }
        }
    }
    
    private fun loadSelectedServer() {
        viewModelScope.launch {
            getSelectedServerUseCase().collect { server ->
                _uiState.value = _uiState.value.copy(selectedServer = server)
            }
        }
    }
    
    fun toggleConnection() {
        viewModelScope.launch {
            when (_uiState.value.connectionState) {
                ConnectionState.DISCONNECTED -> connectVpn()
                ConnectionState.CONNECTED -> disconnectVpn()
                ConnectionState.CONNECTING -> disconnectVpn()
            }
        }
    }
    
    private suspend fun connectVpn() {
        _uiState.value.selectedServer?.let { server ->
            connectVpnUseCase(server)
        }
    }
    
    private suspend fun disconnectVpn() {
        disconnectVpnUseCase()
    }
}

data class MainUiState(
    val connectionState: ConnectionState = ConnectionState.DISCONNECTED,
    val selectedServer: Server? = null,
    val isConnecting: Boolean = false,
    val currentPing: Int = 0,
    val detectedGame: String? = null
)


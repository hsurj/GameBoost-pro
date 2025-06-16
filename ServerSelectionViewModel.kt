package com.gameboost.pro.ui.servers

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gameboost.pro.data.repository.ServerRepository
import com.gameboost.pro.domain.model.Server
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ServerSelectionViewModel @Inject constructor(
    private val serverRepository: ServerRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(ServerSelectionUiState())
    val uiState: StateFlow<ServerSelectionUiState> = _uiState.asStateFlow()
    
    init {
        observeServers()
    }
    
    private fun observeServers() {
        viewModelScope.launch {
            serverRepository.servers.collect { servers ->
                updateFilteredServers(servers)
            }
        }
    }
    
    fun updateSearchQuery(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
        filterServers()
    }
    
    fun selectFilter(filterIndex: Int) {
        _uiState.value = _uiState.value.copy(selectedFilter = filterIndex)
        filterServers()
    }
    
    fun toggleAutoSelect() {
        val newValue = !_uiState.value.autoSelectEnabled
        _uiState.value = _uiState.value.copy(autoSelectEnabled = newValue)
    }
    
    fun selectServer(server: Server) {
        _uiState.value = _uiState.value.copy(selectedServerId = server.id)
    }
    
    fun toggleFavorite(serverId: String) {
        viewModelScope.launch {
            serverRepository.toggleFavorite(serverId)
        }
    }
    
    private fun updateFilteredServers(servers: List<Server>) {
        _uiState.value = _uiState.value.copy(allServers = servers)
        filterServers()
    }
    
    private fun filterServers() {
        val state = _uiState.value
        var filtered = state.allServers
        
        // Apply search filter
        if (state.searchQuery.isNotBlank()) {
            filtered = filtered.filter { server ->
                server.name.contains(state.searchQuery, ignoreCase = true) ||
                server.country.contains(state.searchQuery, ignoreCase = true) ||
                server.city.contains(state.searchQuery, ignoreCase = true)
            }
        }
        
        // Apply region filter
        filtered = when (state.selectedFilter) {
            1 -> filtered.filter { it.country in listOf("Germany", "Netherlands", "France", "United Kingdom", "Poland", "Spain") }
            2 -> filtered.filter { it.country in listOf("Japan", "Singapore", "South Korea") }
            3 -> filtered.filter { it.country in listOf("United States", "Canada", "Brazil") }
            else -> filtered // All servers
        }
        
        // Sort by ping (best first)
        filtered = filtered.sortedBy { it.ping }
        
        _uiState.value = _uiState.value.copy(filteredServers = filtered)
    }
}

data class ServerSelectionUiState(
    val allServers: List<Server> = emptyList(),
    val filteredServers: List<Server> = emptyList(),
    val searchQuery: String = "",
    val selectedFilter: Int = 0,
    val autoSelectEnabled: Boolean = true,
    val selectedServerId: String? = null
)


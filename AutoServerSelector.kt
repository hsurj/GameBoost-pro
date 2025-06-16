package com.gameboost.pro.service.network

import com.gameboost.pro.data.repository.ServerRepository
import com.gameboost.pro.domain.model.Server
import com.gameboost.pro.service.monitoring.PingMonitor
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AutoServerSelector @Inject constructor(
    private val serverRepository: ServerRepository,
    private val pingMonitor: PingMonitor
) {
    
    private val selectionScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    
    private val _selectedServer = MutableStateFlow<Server?>(null)
    val selectedServer: Flow<Server?> = _selectedServer.asStateFlow()
    
    private val _isAutoSelectionEnabled = MutableStateFlow(true)
    val isAutoSelectionEnabled: Flow<Boolean> = _isAutoSelectionEnabled.asStateFlow()
    
    private var selectionJob: Job? = null
    
    fun enableAutoSelection() {
        _isAutoSelectionEnabled.value = true
        startAutoSelection()
    }
    
    fun disableAutoSelection() {
        _isAutoSelectionEnabled.value = false
        selectionJob?.cancel()
    }
    
    private fun startAutoSelection() {
        selectionJob?.cancel()
        selectionJob = selectionScope.launch {
            while (_isAutoSelectionEnabled.value && _selectedServer.value == null) {
                try {
                    val optimalServer = findOptimalServer()
                    
                    // Only switch if the new server is significantly better
                    val currentServer = _selectedServer.value
                    if (shouldSwitchServer(currentServer, optimalServer)) {
                        _selectedServer.value = optimalServer
                    }
                    
                    delay(30000) // Re-evaluate every 30 seconds
                } catch (e: Exception) {
                    delay(60000) // Retry after 1 minute on error
                }
            }
        }
    }
    
    private suspend fun findOptimalServer(): Server? {
        val servers = serverRepository.servers.first()
        
        // Test ping to all servers concurrently
        val serverPings = servers.map { server ->
            async {
                val ping = pingMonitor.measurePing(server.getServerAddress())
                server to ping
            }
        }.awaitAll()
        
        // Calculate server scores based on multiple factors
        val serverScores = serverPings.map { (server, ping) ->
            val score = calculateServerScore(server, ping)
            server to score
        }
        
        // Return server with best score
        return serverScores.minByOrNull { it.second }?.first
    }
    
    private fun calculateServerScore(server: Server, ping: Int): Double {
        // Multi-factor scoring algorithm
        val pingScore = when {
            ping < 20 -> 1.0
            ping < 50 -> 2.0
            ping < 100 -> 3.0
            ping < 150 -> 4.0
            else -> 5.0
        }
        
        val loadScore = server.load / 20.0 // Normalize load (0-100 to 0-5)
        
        val regionScore = when (server.country) {
            "Germany", "Netherlands", "France" -> 1.0 // Prefer European servers
            "United Kingdom" -> 1.5
            "United States" -> 2.0
            else -> 3.0
        }
        
        // Weighted score: 50% ping, 30% load, 20% region
        return (pingScore * 0.5) + (loadScore * 0.3) + (regionScore * 0.2)
    }
        private fun shouldSwitchServer(currentServer: Server?, newServer: Server?): Boolean {
        if (newServer == null) return false
        if (currentServer == null) return true // Always select a server if none is selected
        if (currentServer.id == newServer.id) return false // No need to switch if it's the same server
        
        // Get current server's ping (assuming it's available or can be re-measured)
        val currentServerPing = pingMonitor.measurePing(currentServer.getServerAddress())
        val newServerPing = pingMonitor.measurePing(newServer.getServerAddress())
        
        // Only switch if the new server offers a significant improvement (e.g., 10% better ping or at least 10ms)
        val pingImprovement = currentServerPing - newServerPing
        val percentageImprovement = (pingImprovement.toDouble() / currentServerPing) * 100
        
        return pingImprovement > 10 || percentageImprovement > 10
    }
    
    fun manuallySelectServer(server: Server) {
        disableAutoSelection() // Disable auto-selection when user manually selects
        _selectedServer.value = server
    }()
        
        return when (gameName.lowercase()) {
            "bullet echo" -> {
                // For Bullet Echo, prioritize European servers with low ping
                val europeanServers = servers.filter { 
                    it.country in listOf("Germany", "Netherlands", "France", "United Kingdom")
                }
                
                val serverPings = europeanServers.map { server ->
                    async {
                        val ping = pingMonitor.measurePing(server.getServerAddress())
                        server to ping
                    }
                }.awaitAll()
                
                serverPings.minByOrNull { it.second }?.first
            }
            
            "fortnite" -> {
                // For Fortnite, balance ping and server stability
                val serverScores = servers.map { server ->
                    async {
                        val ping = pingMonitor.measurePing(server.getServerAddress())
                        val score = (ping * 0.7) + (server.load * 0.3)
                        server to score
                    }
                }.awaitAll()
                
                serverScores.minByOrNull { it.second }?.first
            }
            
            "valorant" -> {
                // For Valorant, prioritize ultra-low latency
                val lowLatencyServers = servers.filter { it.ping < 50 }
                if (lowLatencyServers.isNotEmpty()) {
                    lowLatencyServers.minByOrNull { it.ping }
                } else {
                    servers.minByOrNull { it.ping }
                }
            }
            
            else -> {
                // General optimization
                findOptimalServer()
            }
        }
    }
    
    fun manuallySelectServer(server: Server) {
        disableAutoSelection()
        _selectedServer.value = server
    }
    
    fun getCurrentServer(): Server? = _selectedServer.value
    
    suspend fun refreshServerList() {
        val servers = serverRepository.servers.first()
        
        // Update ping for all servers
        servers.forEach { server ->
            selectionScope.launch {
                val ping = pingMonitor.measurePing(server.getServerAddress())
                serverRepository.updateServerPing(server.id, ping)
            }
        }
    }
    
    fun getServerRecommendations(limit: Int = 5): Flow<List<Server>> = flow {
        val servers = serverRepository.servers.first()
        
        val serverScores = servers.map { server ->
            val ping = pingMonitor.measurePing(server.getServerAddress())
            val score = calculateServerScore(server, ping)
            server to score
        }
        
        val recommendations = serverScores
            .sortedBy { it.second }
            .take(limit)
            .map { it.first }
        
        emit(recommendations)
    }
}

private fun Server.getServerAddress(): String {
    return when (this.id) {
        "de-frankfurt-01" -> "de1.gameboost.pro"
        "nl-amsterdam-01" -> "nl1.gameboost.pro"
        "fr-paris-01" -> "fr1.gameboost.pro"
        "uk-london-01" -> "uk1.gameboost.pro"
        "pl-warsaw-01" -> "pl1.gameboost.pro"
        "es-madrid-01" -> "es1.gameboost.pro"
        "us-east-01" -> "us-east1.gameboost.pro"
        "us-west-01" -> "us-west1.gameboost.pro"
        "jp-tokyo-01" -> "jp1.gameboost.pro"
        "sg-singapore-01" -> "sg1.gameboost.pro"
        else -> "${this.country.lowercase().replace(" ", "")}.gameboost.pro"
    }
}


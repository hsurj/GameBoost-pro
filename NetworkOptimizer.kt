package com.gameboost.pro.service.network

import android.content.Context
import com.gameboost.pro.data.repository.ServerRepository
import com.gameboost.pro.domain.model.Server
import com.gameboost.pro.domain.model.GameInfo
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.net.InetAddress
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NetworkOptimizer @Inject constructor(
    @ApplicationContext private val context: Context,
    private val serverRepository: ServerRepository,
    private val pingMonitor: PingMonitor,
    private val gameDetector: GameDetector
) {
    
    private val optimizationScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    
    private val _optimizationState = MutableStateFlow(OptimizationState.IDLE)
    val optimizationState: Flow<OptimizationState> = _optimizationState.asStateFlow()
    
    private val _recommendedServer = MutableStateFlow<Server?>(null)
    val recommendedServer: Flow<Server?> = _recommendedServer.asStateFlow()
    
    fun startOptimization() {
        optimizationScope.launch {
            _optimizationState.value = OptimizationState.ANALYZING
            
            try {
                // Detect running games
                val detectedGame = gameDetector.detectRunningGame()
                
                // Get optimal server based on game and network conditions
                val optimalServer = findOptimalServer(detectedGame)
                
                // Update recommendation
                _recommendedServer.value = optimalServer
                _optimizationState.value = OptimizationState.OPTIMIZED
                
            } catch (e: Exception) {
                _optimizationState.value = OptimizationState.ERROR
            }
        }
    }
    
    private suspend fun findOptimalServer(gameInfo: GameInfo?): Server? {
        val servers = serverRepository.servers.first()
        
        // If game is detected, use game-specific optimization
        gameInfo?.let { game ->
            return findOptimalServerForGame(game, servers)
        }
        
        // Otherwise, use general optimization
        return findGeneralOptimalServer(servers)
    }
    
    private suspend fun findOptimalServerForGame(
        gameInfo: GameInfo,
        servers: List<Server>
    ): Server? {
        return when (gameInfo.name.lowercase()) {
            "bullet echo" -> {
                // Prioritize European servers for Bullet Echo
                val europeanServers = servers.filter { 
                    it.country in listOf("Germany", "Netherlands", "France", "United Kingdom")
                }
                
                // Test ping to European servers and select best one
                val serverPings = europeanServers.map { server ->
                    val ping = pingMonitor.measurePing(server.getServerAddress())
                    server to ping
                }
                
                serverPings.minByOrNull { it.second }?.first
            }
            else -> findGeneralOptimalServer(servers)
        }
    }
    
    private suspend fun findGeneralOptimalServer(servers: List<Server>): Server? {
        // Test ping to all servers and select best one
        val serverPings = servers.map { server ->
            val ping = pingMonitor.measurePing(server.getServerAddress())
            server to ping
        }
        
        // Consider both ping and server load
        return serverPings.minByOrNull { (server, ping) ->
            // Weighted score: 70% ping, 30% load
            (ping * 0.7) + (server.load * 0.3)
        }?.first
    }
    
    fun optimizeForGame(gameName: String): Flow<Server?> = flow {
        val servers = serverRepository.servers.first()
        val gameOptimizedServer = when (gameName.lowercase()) {
            "bullet echo" -> {
                // Bullet Echo optimization: prefer low-latency European servers
                servers.filter { it.country in listOf("Germany", "Netherlands", "France") }
                    .minByOrNull { it.ping }
            }
            "fortnite" -> {
                // Fortnite optimization: prefer servers with low packet loss
                servers.minByOrNull { it.ping + (it.load * 2) }
            }
            "valorant" -> {
                // Valorant optimization: prioritize ultra-low latency
                servers.filter { it.ping < 50 }.minByOrNull { it.ping }
            }
            else -> {
                // General optimization
                servers.minByOrNull { it.ping }
            }
        }
        emit(gameOptimizedServer)
    }
    
    fun getNetworkQualityScore(): Flow<Int> = flow {
        while (true) {
            val currentPing = pingMonitor.getCurrentPing()
            val packetLoss = pingMonitor.getPacketLoss()
            
            val score = calculateNetworkScore(currentPing, packetLoss)
            emit(score)
            
            delay(5000) // Update every 5 seconds
        }
    }
    
    private fun calculateNetworkScore(ping: Int, packetLoss: Float): Int {
        val pingScore = when {
            ping < 20 -> 100
            ping < 50 -> 90
            ping < 100 -> 70
            ping < 150 -> 50
            else -> 20
        }
        
        val packetLossScore = when {
            packetLoss < 0.1f -> 100
            packetLoss < 0.5f -> 80
            packetLoss < 1.0f -> 60
            packetLoss < 2.0f -> 40
            else -> 20
        }
        
        return ((pingScore * 0.7) + (packetLossScore * 0.3)).toInt()
    }
    
    fun applyGameSpecificOptimizations(gameInfo: GameInfo) {
        optimizationScope.launch {
            gameInfo.optimizationProfile?.let { profile ->
                // Apply DNS optimization
                profile.customDns?.let { dnsServers ->
                    applyCustomDns(dnsServers)
                }
                
                // Apply protocol optimization
                optimizeProtocolForGame(profile.preferredProtocol)
                
                // Apply regional optimization
                optimizeServerSelection(profile.preferredRegion)
            }
        }
    }
    
    private suspend fun applyCustomDns(dnsServers: List<String>) {
        // Apply custom DNS servers for better resolution
        // This would integrate with the VPN service to set custom DNS
    }
    
    private suspend fun optimizeProtocolForGame(protocol: com.gameboost.pro.domain.model.VpnProtocol) {
        // Switch to optimal protocol for the detected game
        // WireGuard for low latency, OpenVPN for stability, etc.
    }
    
    private suspend fun optimizeServerSelection(preferredRegion: String) {
        val servers = serverRepository.servers.first()
        val regionalServers = servers.filter { 
            it.country.contains(preferredRegion, ignoreCase = true) 
        }
        
        val optimalServer = regionalServers.minByOrNull { it.ping }
        _recommendedServer.value = optimalServer
    }
}

enum class OptimizationState {
    IDLE,
    ANALYZING,
    OPTIMIZED,
    ERROR
}

private fun Server.getServerAddress(): String {
    // Return server IP address or hostname
    return when (this.id) {
        "de-frankfurt-01" -> "de1.gameboost.pro"
        "nl-amsterdam-01" -> "nl1.gameboost.pro"
        "fr-paris-01" -> "fr1.gameboost.pro"
        "uk-london-01" -> "uk1.gameboost.pro"
        else -> "${this.country.lowercase()}.gameboost.pro"
    }
}


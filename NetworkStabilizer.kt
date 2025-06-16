package com.gameboost.pro.service.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import com.gameboost.pro.data.repository.ServerRepository
import com.gameboost.pro.domain.model.Server
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NetworkStabilizer @Inject constructor(
    @ApplicationContext private val context: Context,
    private val serverRepository: ServerRepository,
    private val pingMonitor: PingMonitor
) {
    
    private val stabilizationScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    
    private val _networkStability = MutableStateFlow(NetworkStability.STABLE)
    val networkStability: Flow<NetworkStability> = _networkStability.asStateFlow()
    
    private val _isInternetConnected = MutableStateFlow(true)
    val isInternetConnected: Flow<Boolean> = _isInternetConnected.asStateFlow()
    
    private var networkCallback: ConnectivityManager.NetworkCallback? = null
    
    init {
        startNetworkMonitoring()
    }
    
    private fun startNetworkMonitoring() {
        val networkRequest = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .addCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
            .build()
        
        networkCallback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                super.onAvailable(network)
                _isInternetConnected.value = true
                _networkStability.value = NetworkStability.STABLE
            }
            
            override fun onLost(network: Network) {
                super.onLost(network)
                handleNetworkLoss()
            }
            
            override fun onCapabilitiesChanged(
                network: Network,
                networkCapabilities: NetworkCapabilities
            ) {
                super.onCapabilitiesChanged(network, networkCapabilities)
                evaluateNetworkQuality(networkCapabilities)
            }
        }
        
        connectivityManager.registerNetworkCallback(networkRequest, networkCallback!!)
    }
    
    private fun handleNetworkLoss() {
        stabilizationScope.launch {
            _networkStability.value = NetworkStability.UNSTABLE
            
            // Try to maintain connection through alternative means
            val hasBackupConnection = checkForBackupConnection()
            
            if (!hasBackupConnection) {
                _isInternetConnected.value = false
                // Attempt to reconnect
                attemptReconnection()
            }
        }
    }
    
    private suspend fun checkForBackupConnection(): Boolean {
        val activeNetwork = connectivityManager.activeNetwork
        val networkCapabilities = connectivityManager.getNetworkCapabilities(activeNetwork)
        
        return networkCapabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true
    }
    
    private suspend fun attemptReconnection() {
        repeat(5) { attempt ->
            delay(2000 * (attempt + 1)) // Exponential backoff
            
            if (isInternetAvailable()) {
                _isInternetConnected.value = true
                _networkStability.value = NetworkStability.STABLE
                return
            }
        }
        
        // If all attempts fail, maintain VPN connection but mark as unstable
        _networkStability.value = NetworkStability.DEGRADED
    }
    
    private suspend fun isInternetAvailable(): Boolean {
        return try {
            val process = Runtime.getRuntime().exec("ping -c 1 8.8.8.8")
            val exitCode = process.waitFor()
            exitCode == 0
        } catch (e: Exception) {
            false
        }
    }
    
    private fun evaluateNetworkQuality(capabilities: NetworkCapabilities) {
        stabilizationScope.launch {
            val linkDownstreamBandwidth = capabilities.linkDownstreamBandwidthKbps
            val linkUpstreamBandwidth = capabilities.linkUpstreamBandwidthKbps
            
            val quality = when {
                linkDownstreamBandwidth > 10000 && linkUpstreamBandwidth > 1000 -> NetworkStability.EXCELLENT
                linkDownstreamBandwidth > 5000 && linkUpstreamBandwidth > 500 -> NetworkStability.STABLE
                linkDownstreamBandwidth > 1000 && linkUpstreamBandwidth > 100 -> NetworkStability.DEGRADED
                else -> NetworkStability.UNSTABLE
            }
            
            _networkStability.value = quality
        }
    }
    
    fun optimizeConnectionStability(server: Server) {
        stabilizationScope.launch {
            // Implement connection optimization techniques
            
            // 1. Adjust MTU for optimal packet size
            optimizeMtu(server)
            
            // 2. Implement keep-alive mechanism
            startKeepAlive(server)
            
            // 3. Monitor connection quality
            monitorConnectionQuality(server)
        }
    }
    
    private suspend fun optimizeMtu(server: Server) {
        // Test different MTU sizes to find optimal value
        val mtuSizes = listOf(1500, 1400, 1300, 1200)
        var bestMtu = 1500
        var bestLatency = Int.MAX_VALUE
        
        for (mtu in mtuSizes) {
            val latency = testMtuLatency(server, mtu)
            if (latency < bestLatency) {
                bestLatency = latency
                bestMtu = mtu
            }
        }
        
        // Apply optimal MTU (this would be implemented in VPN service)
        applyOptimalMtu(bestMtu)
    }
    
    private suspend fun testMtuLatency(server: Server, mtu: Int): Int {
        // Test latency with specific MTU size
        return try {
            pingMonitor.measurePingWithMtu(server.getServerAddress(), mtu)
        } catch (e: Exception) {
            Int.MAX_VALUE
        }
    }
    
    private fun applyOptimalMtu(mtu: Int) {
        // This would be implemented to configure the VPN interface with optimal MTU
        // For now, we'll store it for use in VPN configuration
    }
    
    private fun startKeepAlive(server: Server) {
        stabilizationScope.launch {
            while (_isInternetConnected.value) {
                try {
                    // Send keep-alive packet to maintain connection
                    pingMonitor.sendKeepAlive(server.getServerAddress())
                    delay(30000) // Send keep-alive every 30 seconds
                } catch (e: Exception) {
                    // Handle keep-alive failure
                    _networkStability.value = NetworkStability.UNSTABLE
                    break
                }
            }
        }
    }
    
    private fun monitorConnectionQuality(server: Server) {
        stabilizationScope.launch {
            while (_isInternetConnected.value) {
                try {
                    val ping = pingMonitor.measurePing(server.getServerAddress())
                    val packetLoss = pingMonitor.getPacketLoss()
                    
                    val quality = when {
                        ping < 50 && packetLoss < 0.1f -> NetworkStability.EXCELLENT
                        ping < 100 && packetLoss < 0.5f -> NetworkStability.STABLE
                        ping < 200 && packetLoss < 1.0f -> NetworkStability.DEGRADED
                        else -> NetworkStability.UNSTABLE
                    }
                    
                    _networkStability.value = quality
                    
                    delay(10000) // Check every 10 seconds
                } catch (e: Exception) {
                    _networkStability.value = NetworkStability.UNSTABLE
                    break
                }
            }
        }
    }
    
    fun getConnectionRecommendations(): Flow<List<String>> = flow {
        val recommendations = mutableListOf<String>()
        
        when (_networkStability.value) {
            NetworkStability.UNSTABLE -> {
                recommendations.add("Try switching to a different server")
                recommendations.add("Check your internet connection")
                recommendations.add("Consider using mobile data if on WiFi")
            }
            NetworkStability.DEGRADED -> {
                recommendations.add("Connection quality is reduced")
                recommendations.add("Consider switching to a closer server")
            }
            NetworkStability.STABLE -> {
                recommendations.add("Connection is stable")
            }
            NetworkStability.EXCELLENT -> {
                recommendations.add("Excellent connection quality")
                recommendations.add("Optimal for gaming")
            }
        }
        
        emit(recommendations)
    }
    
    fun cleanup() {
        networkCallback?.let { callback ->
            connectivityManager.unregisterNetworkCallback(callback)
        }
        stabilizationScope.cancel()
    }
}

enum class NetworkStability {
    EXCELLENT,
    STABLE,
    DEGRADED,
    UNSTABLE
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


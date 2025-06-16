package com.gameboost.pro.service.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import com.gameboost.pro.domain.model.NetworkStats
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NetworkAnalyzer @Inject constructor(
    @ApplicationContext private val context: Context
) {
    
    private val analysisScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    
    private val _networkQuality = MutableStateFlow(NetworkQuality.UNKNOWN)
    val networkQuality: Flow<NetworkQuality> = _networkQuality.asStateFlow()
    
    private val _connectionType = MutableStateFlow(ConnectionType.UNKNOWN)
    val connectionType: Flow<ConnectionType> = _connectionType.asStateFlow()
    
    private val _isConnected = MutableStateFlow(false)
    val isConnected: Flow<Boolean> = _isConnected.asStateFlow()
    
    private val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    
    private val networkCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            _isConnected.value = true
            analyzeNetwork()
        }
        
        override fun onLost(network: Network) {
            _isConnected.value = false
            _networkQuality.value = NetworkQuality.DISCONNECTED
        }
        
        override fun onCapabilitiesChanged(network: Network, networkCapabilities: NetworkCapabilities) {
            updateConnectionType(networkCapabilities)
            analyzeNetwork()
        }
    }
    
    init {
        registerNetworkCallback()
        analyzeNetwork()
    }
    
    private fun registerNetworkCallback() {
        val networkRequest = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()
        
        connectivityManager.registerNetworkCallback(networkRequest, networkCallback)
    }
    
    private fun updateConnectionType(capabilities: NetworkCapabilities) {
        _connectionType.value = when {
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> ConnectionType.WIFI
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> ConnectionType.CELLULAR
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> ConnectionType.ETHERNET
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_VPN) -> ConnectionType.VPN
            else -> ConnectionType.UNKNOWN
        }
    }
    
    private fun analyzeNetwork() {
        analysisScope.launch {
            try {
                val quality = performNetworkAnalysis()
                _networkQuality.value = quality
            } catch (e: Exception) {
                _networkQuality.value = NetworkQuality.POOR
            }
        }
    }
    
    private suspend fun performNetworkAnalysis(): NetworkQuality {
        return withContext(Dispatchers.IO) {
            val activeNetwork = connectivityManager.activeNetwork
            val capabilities = connectivityManager.getNetworkCapabilities(activeNetwork)
            
            if (capabilities == null) {
                return@withContext NetworkQuality.DISCONNECTED
            }
            
            // Analyze different aspects of network quality
            val signalStrength = getSignalStrength(capabilities)
            val bandwidth = estimateBandwidth(capabilities)
            val latency = measureLatency()
            val stability = assessStability()
            
            // Calculate overall quality score
            val qualityScore = calculateQualityScore(signalStrength, bandwidth, latency, stability)
            
            when {
                qualityScore >= 80 -> NetworkQuality.EXCELLENT
                qualityScore >= 60 -> NetworkQuality.GOOD
                qualityScore >= 40 -> NetworkQuality.FAIR
                qualityScore >= 20 -> NetworkQuality.POOR
                else -> NetworkQuality.VERY_POOR
            }
        }
    }
    
    private fun getSignalStrength(capabilities: NetworkCapabilities): Int {
        return when {
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> {
                // For WiFi, we'd need to access WiFi manager for signal strength
                // This is a simplified implementation
                75 // Assume good WiFi signal
            }
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> {
                // For cellular, we'd need to access telephony manager
                // This is a simplified implementation
                60 // Assume moderate cellular signal
            }
            else -> 50 // Default for other connection types
        }
    }
    
    private suspend fun estimateBandwidth(capabilities: NetworkCapabilities): Long {
        return withContext(Dispatchers.IO) {
            try {
                // Get downstream bandwidth estimate
                val downstreamKbps = capabilities.linkDownstreamBandwidthKbps
                val upstreamKbps = capabilities.linkUpstreamBandwidthKbps
                
                // Return average bandwidth in Mbps
                ((downstreamKbps + upstreamKbps) / 2 / 1000).toLong()
            } catch (e: Exception) {
                0L // Unknown bandwidth
            }
        }
    }
    
    private suspend fun measureLatency(): Int {
        return withContext(Dispatchers.IO) {
            try {
                val startTime = System.currentTimeMillis()
                val address = java.net.InetAddress.getByName("8.8.8.8")
                val reachable = address.isReachable(5000)
                val endTime = System.currentTimeMillis()
                
                if (reachable) {
                    (endTime - startTime).toInt()
                } else {
                    999 // High latency for unreachable
                }
            } catch (e: Exception) {
                999 // Error latency
            }
        }
    }
    
    private suspend fun assessStability(): Int {
        return withContext(Dispatchers.IO) {
            // Perform multiple ping tests to assess stability
            val pings = mutableListOf<Int>()
            
            repeat(5) {
                try {
                    val startTime = System.currentTimeMillis()
                    val address = java.net.InetAddress.getByName("8.8.8.8")
                    val reachable = address.isReachable(3000)
                    val endTime = System.currentTimeMillis()
                    
                    if (reachable) {
                        pings.add((endTime - startTime).toInt())
                    }
                } catch (e: Exception) {
                    // Ignore failed pings
                }
                delay(500) // Wait between pings
            }
            
            if (pings.isEmpty()) return@withContext 0
            
            // Calculate stability based on ping variation
            val avgPing = pings.average()
            val variation = pings.map { kotlin.math.abs(it - avgPing) }.average()
            
            when {
                variation < 10 -> 100 // Very stable
                variation < 25 -> 80  // Stable
                variation < 50 -> 60  // Moderate
                variation < 100 -> 40 // Unstable
                else -> 20            // Very unstable
            }
        }
    }
    
    private fun calculateQualityScore(
        signalStrength: Int,
        bandwidth: Long,
        latency: Int,
        stability: Int
    ): Int {
        val signalScore = signalStrength
        
        val bandwidthScore = when {
            bandwidth >= 50 -> 100
            bandwidth >= 25 -> 80
            bandwidth >= 10 -> 60
            bandwidth >= 5 -> 40
            bandwidth >= 1 -> 20
            else -> 0
        }
        
        val latencyScore = when {
            latency < 20 -> 100
            latency < 50 -> 80
            latency < 100 -> 60
            latency < 200 -> 40
            latency < 500 -> 20
            else -> 0
        }
        
        val stabilityScore = stability
        
        // Weighted average: 25% signal, 30% bandwidth, 30% latency, 15% stability
        return ((signalScore * 0.25) + (bandwidthScore * 0.30) + 
                (latencyScore * 0.30) + (stabilityScore * 0.15)).toInt()
    }
    
    fun getCurrentNetworkInfo(): NetworkInfo {
        val activeNetwork = connectivityManager.activeNetwork
        val capabilities = connectivityManager.getNetworkCapabilities(activeNetwork)
        
        return NetworkInfo(
            isConnected = _isConnected.value,
            connectionType = _connectionType.value,
            quality = _networkQuality.value,
            downstreamBandwidth = capabilities?.linkDownstreamBandwidthKbps ?: 0,
            upstreamBandwidth = capabilities?.linkUpstreamBandwidthKbps ?: 0
        )
    }
    
    fun isOptimalForGaming(): Boolean {
        return _networkQuality.value in listOf(NetworkQuality.EXCELLENT, NetworkQuality.GOOD) &&
               _connectionType.value in listOf(ConnectionType.WIFI, ConnectionType.ETHERNET)
    }
    
    fun getNetworkRecommendations(): List<String> {
        val recommendations = mutableListOf<String>()
        
        when (_connectionType.value) {
            ConnectionType.CELLULAR -> {
                recommendations.add("Switch to WiFi for better gaming performance")
                recommendations.add("Consider using 5G if available")
            }
            ConnectionType.WIFI -> {
                if (_networkQuality.value == NetworkQuality.POOR) {
                    recommendations.add("Move closer to your WiFi router")
                    recommendations.add("Check for WiFi interference")
                    recommendations.add("Consider upgrading your internet plan")
                }
            }
            else -> {
                recommendations.add("Check your internet connection")
            }
        }
        
        return recommendations
    }
}

enum class NetworkQuality {
    EXCELLENT,
    GOOD,
    FAIR,
    POOR,
    VERY_POOR,
    DISCONNECTED,
    UNKNOWN
}

enum class ConnectionType {
    WIFI,
    CELLULAR,
    ETHERNET,
    VPN,
    UNKNOWN
}

data class NetworkInfo(
    val isConnected: Boolean,
    val connectionType: ConnectionType,
    val quality: NetworkQuality,
    val downstreamBandwidth: Int,
    val upstreamBandwidth: Int
)


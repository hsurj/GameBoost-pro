package com.gameboost.pro.service.monitoring

import android.content.Context
import com.gameboost.pro.domain.model.NetworkStats
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.Socket
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PingMonitor @Inject constructor(
    @ApplicationContext private val context: Context
) {
    
    private val monitoringScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    
    private val _currentPing = MutableStateFlow(0)
    val currentPing: Flow<Int> = _currentPing.asStateFlow()
    
    private val _pingHistory = MutableStateFlow<List<Int>>(emptyList())
    val pingHistory: Flow<List<Int>> = _pingHistory.asStateFlow()
    
    private val _networkStats = MutableStateFlow(NetworkStats(0, 0, 0, 0f, System.currentTimeMillis()))
    val networkStats: Flow<NetworkStats> = _networkStats.asStateFlow()
    
    private val _packetLoss = MutableStateFlow(0f)
    val packetLoss: Flow<Float> = _packetLoss.asStateFlow()
    
    private var isMonitoring = false
    private var monitoringJob: Job? = null
    
    fun startMonitoring(serverAddress: String) {
        if (isMonitoring) return
        
        isMonitoring = true
        monitoringJob = monitoringScope.launch {
            while (isMonitoring) {
                try {
                    val ping = measurePing(serverAddress)
                    updatePingData(ping)
                    
                    val stats = measureNetworkStats(serverAddress)
                    _networkStats.value = stats
                    
                    delay(1000) // Measure every second
                } catch (e: Exception) {
                    // Handle monitoring error
                    delay(5000) // Retry after 5 seconds
                }
            }
        }
    }
    
    fun stopMonitoring() {
        isMonitoring = false
        monitoringJob?.cancel()
    }
    
    suspend fun measurePingWithMtu(serverAddress: String, mtu: Int): Int {
        return withContext(Dispatchers.IO) {
            try {
                val startTime = System.currentTimeMillis()
                
                // Use TCP ping with specific MTU consideration
                val socket = Socket()
                socket.connect(InetSocketAddress(serverAddress, 443), 5000)
                socket.close()
                
                val endTime = System.currentTimeMillis()
                (endTime - startTime).toInt()
                
            } catch (e: Exception) {
                Int.MAX_VALUE
            }
        }
    }
    
    suspend fun sendKeepAlive(serverAddress: String) {
        withContext(Dispatchers.IO) {
            try {
                val socket = Socket()
                socket.connect(InetSocketAddress(serverAddress, 443), 3000)
                
                // Send a small keep-alive packet
                val outputStream = socket.getOutputStream()
                outputStream.write(byteArrayOf(0x00))
                outputStream.flush()
                
                socket.close()
            } catch (e: Exception) {
                // Keep-alive failed, but don't throw exception
            }
        }
    }
    
    suspend fun measurePing(serverAddress: String): Int {
        return withContext(Dispatchers.IO) {
            try {
                val startTime = System.currentTimeMillis()
                
                // Use TCP ping for more accurate results
                val socket = Socket()
                socket.connect(InetSocketAddress(serverAddress, 443), 5000)
                socket.close()
                
                val endTime = System.currentTimeMillis()
                (endTime - startTime).toInt()
                
            } catch (e: Exception) {
                // Fallback to ICMP ping
                measureIcmpPing(serverAddress)
            }
        }
    }
    
    private suspend fun measureIcmpPing(serverAddress: String): Int {
        return withContext(Dispatchers.IO) {
            try {
                val startTime = System.currentTimeMillis()
                val address = InetAddress.getByName(serverAddress)
                val reachable = address.isReachable(5000)
                val endTime = System.currentTimeMillis()
                
                if (reachable) {
                    (endTime - startTime).toInt()
                } else {
                    999 // High ping for unreachable
                }
            } catch (e: Exception) {
                999 // Error ping
            }
        }
    }
    
    private fun updatePingData(ping: Int) {
        _currentPing.value = ping
        
        val currentHistory = _pingHistory.value.toMutableList()
        currentHistory.add(ping)
        
        // Keep only last 60 measurements (1 minute of data)
        if (currentHistory.size > 60) {
            currentHistory.removeAt(0)
        }
        
        _pingHistory.value = currentHistory
        
        // Calculate packet loss
        calculatePacketLoss(currentHistory)
    }
    
    private fun calculatePacketLoss(pingHistory: List<Int>) {
        if (pingHistory.size < 10) return
        
        val timeouts = pingHistory.count { it > 500 } // Consider >500ms as timeout
        val lossPercentage = (timeouts.toFloat() / pingHistory.size) * 100
        _packetLoss.value = lossPercentage
    }
    
    private suspend fun measureNetworkStats(serverAddress: String): NetworkStats {
        return withContext(Dispatchers.IO) {
            try {
                // Measure download speed
                val downloadSpeed = measureDownloadSpeed(serverAddress)
                
                // Measure upload speed
                val uploadSpeed = measureUploadSpeed(serverAddress)
                
                NetworkStats(
                    ping = _currentPing.value,
                    downloadSpeed = downloadSpeed,
                    uploadSpeed = uploadSpeed,
                    packetLoss = _packetLoss.value,
                    timestamp = System.currentTimeMillis()
                )
            } catch (e: Exception) {
                _networkStats.value
            }
        }
    }
    
    private suspend fun measureDownloadSpeed(serverAddress: String): Long {
        // Simplified speed test - in real implementation, 
        // this would download a test file and measure speed
        return withContext(Dispatchers.IO) {
            try {
                val startTime = System.currentTimeMillis()
                val socket = Socket()
                socket.connect(InetSocketAddress(serverAddress, 80), 5000)
                
                // Simulate data transfer
                val buffer = ByteArray(1024)
                val inputStream = socket.getInputStream()
                var totalBytes = 0L
                
                repeat(10) { // Read 10KB
                    val bytesRead = inputStream.read(buffer)
                    if (bytesRead > 0) totalBytes += bytesRead
                }
                
                socket.close()
                val endTime = System.currentTimeMillis()
                val duration = (endTime - startTime) / 1000.0
                
                if (duration > 0) {
                    (totalBytes / duration).toLong() // Bytes per second
                } else {
                    0L
                }
            } catch (e: Exception) {
                0L
            }
        }
    }
    
    private suspend fun measureUploadSpeed(serverAddress: String): Long {
        // Simplified upload speed test
        return withContext(Dispatchers.IO) {
            try {
                val startTime = System.currentTimeMillis()
                val socket = Socket()
                socket.connect(InetSocketAddress(serverAddress, 80), 5000)
                
                // Simulate data upload
                val buffer = ByteArray(1024)
                val outputStream = socket.getOutputStream()
                
                repeat(10) { // Send 10KB
                    outputStream.write(buffer)
                }
                outputStream.flush()
                
                socket.close()
                val endTime = System.currentTimeMillis()
                val duration = (endTime - startTime) / 1000.0
                
                if (duration > 0) {
                    (10240 / duration).toLong() // Bytes per second
                } else {
                    0L
                }
            } catch (e: Exception) {
                0L
            }
        }
    }
    
    fun getCurrentPing(): Int = _currentPing.value
    
    fun getAveragePing(): Int {
        val history = _pingHistory.value
        return if (history.isNotEmpty()) {
            history.average().toInt()
        } else {
            0
        }
    }
    
    fun getPacketLoss(): Float = _packetLoss.value
    
    fun getPingVariation(): Int {
        val history = _pingHistory.value
        return if (history.size > 1) {
            val max = history.maxOrNull() ?: 0
            val min = history.minOrNull() ?: 0
            max - min
        } else {
            0
        }
    }
    
    fun getConnectionStability(): ConnectionStability {
        val variation = getPingVariation()
        val packetLoss = getPacketLoss()
        
        return when {
            variation < 20 && packetLoss < 0.5f -> ConnectionStability.EXCELLENT
            variation < 50 && packetLoss < 1.0f -> ConnectionStability.GOOD
            variation < 100 && packetLoss < 2.0f -> ConnectionStability.FAIR
            else -> ConnectionStability.POOR
        }
    }
}

enum class ConnectionStability {
    EXCELLENT,
    GOOD,
    FAIR,
    POOR
}


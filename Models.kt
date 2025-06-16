package com.gameboost.pro.domain.model

enum class ConnectionState {
    DISCONNECTED,
    CONNECTING,
    CONNECTED
}

data class Server(
    val id: String,
    val name: String,
    val country: String,
    val city: String,
    val flagUrl: String,
    val ping: Int,
    val load: Int,
    val isFavorite: Boolean = false
)

data class NetworkStats(
    val ping: Int,
    val downloadSpeed: Long,
    val uploadSpeed: Long,
    val packetLoss: Float,
    val timestamp: Long
)

data class GameInfo(
    val packageName: String,
    val name: String,
    val isRunning: Boolean,
    val optimizationProfile: OptimizationProfile?
)

data class OptimizationProfile(
    val preferredProtocol: VpnProtocol,
    val preferredRegion: String,
    val customDns: List<String>?,
    val splitTunneling: Boolean
)

enum class VpnProtocol {
    WIREGUARD,
    OPENVPN,
    IKEV2
}


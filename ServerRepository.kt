package com.gameboost.pro.data.repository

import com.gameboost.pro.domain.model.Server
import com.gameboost.pro.domain.model.VpnProtocol
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ServerRepository @Inject constructor() {
    
    private val _servers = MutableStateFlow(getDefaultServers())
    val servers: Flow<List<Server>> = _servers.asStateFlow()
    
    private val _favoriteServers = MutableStateFlow<Set<String>>(emptySet())
    val favoriteServers: Flow<Set<String>> = _favoriteServers.asStateFlow()
    
    fun getServerById(id: String): Server? {
        return _servers.value.find { it.id == id }
    }
    
    fun getServersByRegion(region: String): List<Server> {
        return _servers.value.filter { it.country.contains(region, ignoreCase = true) }
    }
    
    fun getBestServerForGame(gameName: String): Server? {
        // Logic to select best server based on game requirements
        return when (gameName.lowercase()) {
            "bullet echo" -> getServersByRegion("Europe").minByOrNull { it.ping }
            else -> _servers.value.minByOrNull { it.ping }
        }
    }
    
    suspend fun toggleFavorite(serverId: String) {
        val currentFavorites = _favoriteServers.value.toMutableSet()
        if (currentFavorites.contains(serverId)) {
            currentFavorites.remove(serverId)
        } else {
            currentFavorites.add(serverId)
        }
        _favoriteServers.value = currentFavorites
        
        // Update server list with favorite status
        _servers.value = _servers.value.map { server ->
            if (server.id == serverId) {
                server.copy(isFavorite = currentFavorites.contains(serverId))
            } else {
                server
            }
        }
    }
    
    suspend fun updateServerPing(serverId: String, ping: Int) {
        _servers.value = _servers.value.map { server ->
            if (server.id == serverId) {
                server.copy(ping = ping)
            } else {
                server
            }
        }
    }
    
    private fun getDefaultServers(): List<Server> {
        return listOf(
            Server(
                id = "de-frankfurt-01",
                name = "Germany 1",
                country = "Germany",
                city = "Frankfurt",
                flagUrl = "🇩🇪",
                ping = 23,
                load = 45,
                protocol = VpnProtocol.WIREGUARD
            ),
            Server(
                id = "nl-amsterdam-01",
                name = "Netherlands 1",
                country = "Netherlands",
                city = "Amsterdam",
                flagUrl = "🇳🇱",
                ping = 31,
                load = 38,
                protocol = VpnProtocol.WIREGUARD
            ),
            Server(
                id = "fr-paris-01",
                name = "France 1",
                country = "France",
                city = "Paris",
                flagUrl = "🇫🇷",
                ping = 45,
                load = 52,
                protocol = VpnProtocol.WIREGUARD
            ),
            Server(
                id = "uk-london-01",
                name = "UK 1",
                country = "United Kingdom",
                city = "London",
                flagUrl = "🇬🇧",
                ping = 67,
                load = 61,
                protocol = VpnProtocol.WIREGUARD
            ),
            Server(
                id = "pl-warsaw-01",
                name = "Poland 1",
                country = "Poland",
                city = "Warsaw",
                flagUrl = "🇵🇱",
                ping = 89,
                load = 73,
                protocol = VpnProtocol.WIREGUARD
            ),
            Server(
                id = "es-madrid-01",
                name = "Spain 1",
                country = "Spain",
                city = "Madrid",
                flagUrl = "🇪🇸",
                ping = 112,
                load = 84,
                protocol = VpnProtocol.WIREGUARD
            ),
            Server(
                id = "us-east-01",
                name = "US East 1",
                country = "United States",
                city = "New York",
                flagUrl = "🇺🇸",
                ping = 145,
                load = 67,
                protocol = VpnProtocol.WIREGUARD
            ),
            Server(
                id = "us-west-01",
                name = "US West 1",
                country = "United States",
                city = "Los Angeles",
                flagUrl = "🇺🇸",
                ping = 178,
                load = 71,
                protocol = VpnProtocol.WIREGUARD
            ),
            Server(
                id = "jp-tokyo-01",
                name = "Japan 1",
                country = "Japan",
                city = "Tokyo",
                flagUrl = "🇯🇵",
                ping = 234,
                load = 56,
                protocol = VpnProtocol.WIREGUARD
            ),
            Server(
                id = "sg-singapore-01",
                name = "Singapore 1",
                country = "Singapore",
                city = "Singapore",
                flagUrl = "🇸🇬",
                ping = 198,
                load = 49,
                protocol = VpnProtocol.WIREGUARD
            )
        )
    }
}


package com.gameboost.pro.test

import com.gameboost.pro.domain.model.*
import com.gameboost.pro.data.repository.ServerRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class ServerRepositoryTest {
    
    private lateinit var serverRepository: ServerRepository
    
    @Before
    fun setup() {
        serverRepository = ServerRepository()
    }
    
    @Test
    fun `test default servers are loaded`() = runBlocking {
        val servers = serverRepository.servers.first()
        
        assertTrue("Should have default servers", servers.isNotEmpty())
        assertTrue("Should have at least 5 servers", servers.size >= 5)
    }
    
    @Test
    fun `test server by id retrieval`() {
        val server = serverRepository.getServerById("de-frankfurt-01")
        
        assertNotNull("Should find German server", server)
        assertEquals("Should be Frankfurt server", "Frankfurt", server?.city)
        assertEquals("Should be Germany", "Germany", server?.country)
    }
    
    @Test
    fun `test servers by region filtering`() {
        val europeanServers = serverRepository.getServersByRegion("Europe")
        
        assertTrue("Should have European servers", europeanServers.isNotEmpty())
        
        val germanServers = europeanServers.filter { it.country == "Germany" }
        assertTrue("Should have German servers", germanServers.isNotEmpty())
    }
    
    @Test
    fun `test best server for Bullet Echo`() {
        val bestServer = serverRepository.getBestServerForGame("Bullet Echo")
        
        assertNotNull("Should find best server for Bullet Echo", bestServer)
        
        // Should prefer European servers for Bullet Echo
        val europeanCountries = listOf("Germany", "Netherlands", "France", "United Kingdom")
        assertTrue("Should prefer European server for Bullet Echo", 
            bestServer?.country in europeanCountries)
    }
    
    @Test
    fun `test server ping update`() = runBlocking {
        val serverId = "de-frankfurt-01"
        val newPing = 25
        
        serverRepository.updateServerPing(serverId, newPing)
        
        val server = serverRepository.getServerById(serverId)
        assertEquals("Ping should be updated", newPing, server?.ping)
    }
    
    @Test
    fun `test favorite toggle`() = runBlocking {
        val serverId = "de-frankfurt-01"
        
        // Toggle favorite on
        serverRepository.toggleFavorite(serverId)
        
        val server = serverRepository.getServerById(serverId)
        assertTrue("Server should be favorite", server?.isFavorite == true)
        
        // Toggle favorite off
        serverRepository.toggleFavorite(serverId)
        
        val updatedServer = serverRepository.getServerById(serverId)
        assertFalse("Server should not be favorite", updatedServer?.isFavorite == true)
    }
}

class NetworkOptimizerTest {
    
    @Test
    fun `test optimization state enum`() {
        val states = OptimizationState.values()
        
        assertTrue("Should have IDLE state", OptimizationState.IDLE in states)
        assertTrue("Should have ANALYZING state", OptimizationState.ANALYZING in states)
        assertTrue("Should have OPTIMIZED state", OptimizationState.OPTIMIZED in states)
        assertTrue("Should have ERROR state", OptimizationState.ERROR in states)
    }
}

class VpnProtocolTest {
    
    @Test
    fun `test VPN protocols`() {
        val protocols = VpnProtocol.values()
        
        assertTrue("Should have WireGuard", VpnProtocol.WIREGUARD in protocols)
        assertTrue("Should have OpenVPN", VpnProtocol.OPENVPN in protocols)
        assertTrue("Should have IKEv2", VpnProtocol.IKEV2 in protocols)
    }
}

class ConnectionStateTest {
    
    @Test
    fun `test connection states`() {
        val states = ConnectionState.values()
        
        assertTrue("Should have DISCONNECTED", ConnectionState.DISCONNECTED in states)
        assertTrue("Should have CONNECTING", ConnectionState.CONNECTING in states)
        assertTrue("Should have CONNECTED", ConnectionState.CONNECTED in states)
    }
}

class GameInfoTest {
    
    @Test
    fun `test game info creation`() {
        val gameInfo = GameInfo(
            packageName = "com.zeptolab.bulletecho.google",
            name = "Bullet Echo",
            isRunning = true,
            optimizationProfile = OptimizationProfile(
                preferredProtocol = VpnProtocol.WIREGUARD,
                preferredRegion = "Europe",
                customDns = listOf("8.8.8.8", "1.1.1.1"),
                splitTunneling = false
            )
        )
        
        assertEquals("Package name should match", "com.zeptolab.bulletecho.google", gameInfo.packageName)
        assertEquals("Name should match", "Bullet Echo", gameInfo.name)
        assertTrue("Should be running", gameInfo.isRunning)
        assertNotNull("Should have optimization profile", gameInfo.optimizationProfile)
        assertEquals("Should prefer WireGuard", VpnProtocol.WIREGUARD, gameInfo.optimizationProfile?.preferredProtocol)
        assertEquals("Should prefer Europe", "Europe", gameInfo.optimizationProfile?.preferredRegion)
    }
}

class ServerTest {
    
    @Test
    fun `test server creation`() {
        val server = Server(
            id = "test-server-01",
            name = "Test Server 1",
            country = "Test Country",
            city = "Test City",
            flagUrl = "🏳️",
            ping = 50,
            load = 60,
            isFavorite = false
        )
        
        assertEquals("ID should match", "test-server-01", server.id)
        assertEquals("Name should match", "Test Server 1", server.name)
        assertEquals("Country should match", "Test Country", server.country)
        assertEquals("City should match", "Test City", server.city)
        assertEquals("Ping should match", 50, server.ping)
        assertEquals("Load should match", 60, server.load)
        assertFalse("Should not be favorite", server.isFavorite)
    }
    
    @Test
    fun `test server copy with favorite`() {
        val server = Server(
            id = "test-server-01",
            name = "Test Server 1",
            country = "Test Country",
            city = "Test City",
            flagUrl = "🏳️",
            ping = 50,
            load = 60,
            isFavorite = false
        )
        
        val favoriteServer = server.copy(isFavorite = true)
        
        assertTrue("Should be favorite", favoriteServer.isFavorite)
        assertEquals("Other properties should remain same", server.id, favoriteServer.id)
    }
}


package com.gameboost.pro.service.vpn

import android.content.Intent
import android.net.VpnService
import android.os.ParcelFileDescriptor
import androidx.lifecycle.LifecycleService
import com.gameboost.pro.domain.model.Server
import com.gameboost.pro.domain.model.VpnProtocol
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import javax.inject.Inject

@AndroidEntryPoint
class GameBoostVpnService : VpnService() {
    
    private var vpnInterface: ParcelFileDescriptor? = null
    private var serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    
    @Inject
    lateinit var vpnManager: VpnManager
    
    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_CONNECT -> {
                val server = intent.getParcelableExtra<Server>(EXTRA_SERVER)
                server?.let { connectToServer(it) }
            }
            ACTION_DISCONNECT -> {
                disconnect()
            }
        }
        return START_STICKY
    }
    
    private fun connectToServer(server: Server) {
        serviceScope.launch {
            try {
                // Request VPN permission if needed
                val intent = prepare(this@GameBoostVpnService)
                if (intent != null) {
                    // Permission needed - this should be handled in the UI
                    return@launch
                }
                
                // Create VPN interface
                vpnInterface = createVpnInterface(server)
                
                // Start VPN connection based on protocol
                when (server.protocol) {
                    VpnProtocol.WIREGUARD -> startWireGuardConnection(server)
                    VpnProtocol.OPENVPN -> startOpenVpnConnection(server)
                    VpnProtocol.IKEV2 -> startIkeV2Connection(server)
                }
                
                // Start foreground service
                startForeground(NOTIFICATION_ID, createNotification(server))
                
            } catch (e: Exception) {
                // Handle connection error
                disconnect()
            }
        }
    }
    
    private fun createVpnInterface(server: Server): ParcelFileDescriptor {
        return Builder()
            .setSession("GameBoost Pro")
            .addAddress("10.0.0.2", 24)
            .addDnsServer("8.8.8.8")
            .addDnsServer("8.8.4.4")
            .addRoute("0.0.0.0", 0) // Route all traffic through VPN
            .setMtu(1400) // Optimized MTU for better stability
            .setBlocking(false) // Non-blocking for better performance
            .establish()
            ?: throw IllegalStateException("Failed to create VPN interface")
    }
    
    private suspend fun startWireGuardConnection(server: Server) {
        // Implement WireGuard connection logic
        vpnManager.connectWireGuard(server, vpnInterface!!)
    }
    
    private suspend fun startOpenVpnConnection(server: Server) {
        // Implement OpenVPN connection logic
        vpnManager.connectOpenVpn(server, vpnInterface!!)
    }
    
    private suspend fun startIkeV2Connection(server: Server) {
        // Implement IKEv2 connection logic
        vpnManager.connectIkeV2(server, vpnInterface!!)
    }
    
    private fun disconnect() {
        serviceScope.launch {
            vpnManager.disconnect()
            vpnInterface?.close()
            vpnInterface = null
            stopForeground(true)
            stopSelf()
        }
    }
    
    private fun createNotificationChannel() {
        // Create notification channel for VPN service
    }
    
    private fun createNotification(server: Server): Notification {
        // Create notification for VPN connection
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("GameBoost Pro")
            .setContentText("Connected to ${server.name}")
            .setSmallIcon(R.drawable.ic_vpn)
            .build()
    }
    
    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
        vpnInterface?.close()
    }
    
    companion object {
        const val ACTION_CONNECT = "com.gameboost.pro.ACTION_CONNECT"
        const val ACTION_DISCONNECT = "com.gameboost.pro.ACTION_DISCONNECT"
        const val EXTRA_SERVER = "extra_server"
        const val NOTIFICATION_ID = 1001
        const val CHANNEL_ID = "vpn_channel"
    }
}


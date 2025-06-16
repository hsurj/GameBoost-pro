package com.gameboost.pro.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.gameboost.pro.domain.model.VpnProtocol
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

@Singleton
class ConfigRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    
    private object PreferencesKeys {
        val AUTO_CONNECT = booleanPreferencesKey("auto_connect")
        val SELECTED_PROTOCOL = stringPreferencesKey("selected_protocol")
        val KILL_SWITCH = booleanPreferencesKey("kill_switch")
        val GAME_DETECTION = booleanPreferencesKey("game_detection")
        val AUTO_OPTIMIZE = booleanPreferencesKey("auto_optimize")
        val PING_THRESHOLD = intPreferencesKey("ping_threshold")
        val CUSTOM_DNS = stringPreferencesKey("custom_dns")
        val SPLIT_TUNNELING = booleanPreferencesKey("split_tunneling")
        val SELECTED_SERVER_ID = stringPreferencesKey("selected_server_id")
        val NOTIFICATIONS_ENABLED = booleanPreferencesKey("notifications_enabled")
    }
    
    // Auto Connect
    val autoConnect: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.AUTO_CONNECT] ?: false
    }
    
    suspend fun setAutoConnect(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.AUTO_CONNECT] = enabled
        }
    }
    
    // Selected Protocol
    val selectedProtocol: Flow<VpnProtocol> = context.dataStore.data.map { preferences ->
        val protocolName = preferences[PreferencesKeys.SELECTED_PROTOCOL] ?: VpnProtocol.WIREGUARD.name
        VpnProtocol.valueOf(protocolName)
    }
    
    suspend fun setSelectedProtocol(protocol: VpnProtocol) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.SELECTED_PROTOCOL] = protocol.name
        }
    }
    
    // Kill Switch
    val killSwitch: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.KILL_SWITCH] ?: true
    }
    
    suspend fun setKillSwitch(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.KILL_SWITCH] = enabled
        }
    }
    
    // Game Detection
    val gameDetection: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.GAME_DETECTION] ?: true
    }
    
    suspend fun setGameDetection(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.GAME_DETECTION] = enabled
        }
    }
    
    // Auto Optimize
    val autoOptimize: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.AUTO_OPTIMIZE] ?: true
    }
    
    suspend fun setAutoOptimize(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.AUTO_OPTIMIZE] = enabled
        }
    }
    
    // Ping Threshold
    val pingThreshold: Flow<Int> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.PING_THRESHOLD] ?: 150
    }
    
    suspend fun setPingThreshold(threshold: Int) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.PING_THRESHOLD] = threshold
        }
    }
    
    // Custom DNS
    val customDns: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.CUSTOM_DNS] ?: "8.8.8.8,8.8.4.4"
    }
    
    suspend fun setCustomDns(dns: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.CUSTOM_DNS] = dns
        }
    }
    
    // Split Tunneling
    val splitTunneling: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.SPLIT_TUNNELING] ?: false
    }
    
    suspend fun setSplitTunneling(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.SPLIT_TUNNELING] = enabled
        }
    }
    
    // Selected Server
    val selectedServerId: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.SELECTED_SERVER_ID]
    }
    
    suspend fun setSelectedServerId(serverId: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.SELECTED_SERVER_ID] = serverId
        }
    }
    
    // Notifications
    val notificationsEnabled: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.NOTIFICATIONS_ENABLED] ?: true
    }
    
    suspend fun setNotificationsEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.NOTIFICATIONS_ENABLED] = enabled
        }
    }
}


package com.gameboost.pro.service.monitoring

import android.app.ActivityManager
import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import com.gameboost.pro.domain.model.GameInfo
import com.gameboost.pro.domain.model.OptimizationProfile
import com.gameboost.pro.domain.model.VpnProtocol
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GameDetector @Inject constructor(
    @ApplicationContext private val context: Context
) {
    
    private val detectionScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    
    private val _detectedGame = MutableStateFlow<GameInfo?>(null)
    val detectedGame: Flow<GameInfo?> = _detectedGame.asStateFlow()
    
    private val _isDetectionActive = MutableStateFlow(false)
    val isDetectionActive: Flow<Boolean> = _isDetectionActive.asStateFlow()
    
    private var detectionJob: Job? = null
    
    // Known gaming apps with their optimization profiles
    private val gameProfiles = mapOf(
        "com.zeptolab.bulletecho.google" to GameInfo(
            packageName = "com.zeptolab.bulletecho.google",
            name = "Bullet Echo",
            isRunning = false,
            optimizationProfile = OptimizationProfile(
                preferredProtocol = VpnProtocol.WIREGUARD,
                preferredRegion = "Europe",
                customDns = listOf("8.8.8.8", "1.1.1.1"),
                splitTunneling = false
            )
        ),
        "com.epicgames.fortnite" to GameInfo(
            packageName = "com.epicgames.fortnite",
            name = "Fortnite",
            isRunning = false,
            optimizationProfile = OptimizationProfile(
                preferredProtocol = VpnProtocol.WIREGUARD,
                preferredRegion = "US",
                customDns = listOf("8.8.8.8", "8.8.4.4"),
                splitTunneling = true
            )
        ),
        "com.riotgames.league.wildrift" to GameInfo(
            packageName = "com.riotgames.league.wildrift",
            name = "Wild Rift",
            isRunning = false,
            optimizationProfile = OptimizationProfile(
                preferredProtocol = VpnProtocol.WIREGUARD,
                preferredRegion = "Europe",
                customDns = listOf("1.1.1.1", "1.0.0.1"),
                splitTunneling = false
            )
        ),
        "com.garena.game.codm" to GameInfo(
            packageName = "com.garena.game.codm",
            name = "Call of Duty Mobile",
            isRunning = false,
            optimizationProfile = OptimizationProfile(
                preferredProtocol = VpnProtocol.WIREGUARD,
                preferredRegion = "US",
                customDns = listOf("8.8.8.8", "8.8.4.4"),
                splitTunneling = false
            )
        ),
        "com.pubg.imobile" to GameInfo(
            packageName = "com.pubg.imobile",
            name = "PUBG Mobile",
            isRunning = false,
            optimizationProfile = OptimizationProfile(
                preferredProtocol = VpnProtocol.WIREGUARD,
                preferredRegion = "Asia",
                customDns = listOf("8.8.8.8", "1.1.1.1"),
                splitTunneling = false
            )
        ),
        "com.supercell.clashofclans" to GameInfo(
            packageName = "com.supercell.clashofclans",
            name = "Clash of Clans",
            isRunning = false,
            optimizationProfile = OptimizationProfile(
                preferredProtocol = VpnProtocol.OPENVPN,
                preferredRegion = "Europe",
                customDns = listOf("8.8.8.8", "8.8.4.4"),
                splitTunneling = true
            )
        )
    )
    
    fun startDetection() {
        if (_isDetectionActive.value) return
        
        _isDetectionActive.value = true
        detectionJob = detectionScope.launch {
            while (_isDetectionActive.value) {
                try {
                    val runningGame = detectRunningGame()
                    _detectedGame.value = runningGame
                    
                    delay(3000) // Check every 3 seconds
                } catch (e: Exception) {
                    // Handle detection error
                    delay(5000) // Retry after 5 seconds
                }
            }
        }
    }
    
    fun stopDetection() {
        _isDetectionActive.value = false
        detectionJob?.cancel()
        _detectedGame.value = null
    }
    
    suspend fun detectRunningGame(): GameInfo? {
        return withContext(Dispatchers.IO) {
            try {
                val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
                val runningApps = activityManager.runningAppProcesses
                
                // Check if any known games are running
                for (app in runningApps) {
                    for (packageName in app.pkgList) {
                        gameProfiles[packageName]?.let { gameInfo ->
                            return@withContext gameInfo.copy(isRunning = true)
                        }
                    }
                }
                
                // Check for other gaming apps
                val installedGames = getInstalledGames()
                for (app in runningApps) {
                    for (packageName in app.pkgList) {
                        if (installedGames.contains(packageName)) {
                            return@withContext createGenericGameInfo(packageName)
                        }
                    }
                }
                
                null
            } catch (e: Exception) {
                null
            }
        }
    }
    
    private fun getInstalledGames(): Set<String> {
        return try {
            val packageManager = context.packageManager
            val installedApps = packageManager.getInstalledApplications(PackageManager.GET_META_DATA)
            
            installedApps.filter { app ->
                // Check if app is a game
                (app.flags and ApplicationInfo.FLAG_IS_GAME) != 0 ||
                app.category == ApplicationInfo.CATEGORY_GAME ||
                isLikelyGameApp(app.packageName)
            }.map { it.packageName }.toSet()
            
        } catch (e: Exception) {
            emptySet()
        }
    }
    
    private fun isLikelyGameApp(packageName: String): Boolean {
        val gameKeywords = listOf(
            "game", "play", "battle", "war", "fight", "shoot", "racing", 
            "puzzle", "adventure", "action", "strategy", "rpg", "mmo",
            "clash", "craft", "legends", "mobile", "online"
        )
        
        return gameKeywords.any { keyword ->
            packageName.lowercase().contains(keyword)
        }
    }
    
    private fun createGenericGameInfo(packageName: String): GameInfo {
        val appName = getAppName(packageName) ?: "Unknown Game"
        
        return GameInfo(
            packageName = packageName,
            name = appName,
            isRunning = true,
            optimizationProfile = OptimizationProfile(
                preferredProtocol = VpnProtocol.WIREGUARD,
                preferredRegion = "Auto",
                customDns = listOf("8.8.8.8", "1.1.1.1"),
                splitTunneling = false
            )
        )
    }
    
    private fun getAppName(packageName: String): String? {
        return try {
            val packageManager = context.packageManager
            val appInfo = packageManager.getApplicationInfo(packageName, 0)
            packageManager.getApplicationLabel(appInfo).toString()
        } catch (e: Exception) {
            null
        }
    }
    
    fun getOptimizationProfile(packageName: String): OptimizationProfile? {
        return gameProfiles[packageName]?.optimizationProfile
    }
    
    fun isGameRunning(packageName: String): Boolean {
        return try {
            val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            val runningApps = activityManager.runningAppProcesses
            
            runningApps.any { app ->
                app.pkgList.contains(packageName)
            }
        } catch (e: Exception) {
            false
        }
    }
    
    fun getSupportedGames(): List<GameInfo> {
        return gameProfiles.values.toList()
    }
    
    fun addCustomGameProfile(gameInfo: GameInfo) {
        // This would allow users to add custom game profiles
        // In a real implementation, this would persist to local storage
    }
}


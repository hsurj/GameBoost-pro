package com.gameboost.pro.ui.main

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gameboost.pro.domain.model.ConnectionState
import com.gameboost.pro.ui.components.*
import com.gameboost.pro.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: MainViewModel
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(16.dp)
    ) {
        // Header
        MainHeader(
            connectionState = uiState.connectionState
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Connection Status Card
        ConnectionStatusCard(
            connectionState = uiState.connectionState,
            isConnecting = uiState.isConnecting,
            onToggleConnection = viewModel::toggleConnection
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Server Info Card
        uiState.selectedServer?.let { server ->
            ServerInfoCard(
                server = server,
                ping = uiState.currentPing
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Performance Chart
        PerformanceChart(
            ping = uiState.currentPing
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Game Detection
        GameDetectionWidget(
            detectedGame = uiState.detectedGame
        )
        
        Spacer(modifier = Modifier.weight(1f))
        
        // Bottom Navigation
        BottomNavigationBar()
    }
}

@Composable
private fun MainHeader(
    connectionState: ConnectionState
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Shield,
                contentDescription = null,
                tint = PrimaryBlue,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "GameBoost Pro",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
        
        ConnectionStatusIndicator(connectionState = connectionState)
    }
}

@Composable
private fun ConnectionStatusIndicator(
    connectionState: ConnectionState
) {
    val (color, icon) = when (connectionState) {
        ConnectionState.CONNECTED -> Pair(SuccessGreen, Icons.Default.CheckCircle)
        ConnectionState.CONNECTING -> Pair(WarningOrange, Icons.Default.Sync)
        ConnectionState.DISCONNECTED -> Pair(ErrorRed, Icons.Default.Cancel)
    }
    
    Icon(
        imageVector = icon,
        contentDescription = null,
        tint = color,
        modifier = Modifier.size(24.dp)
    )
}


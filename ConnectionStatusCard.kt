package com.gameboost.pro.ui.components

import androidx.compose.animation.*
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
import com.gameboost.pro.domain.model.ConnectionState
import com.gameboost.pro.ui.theme.*

@Composable
fun ConnectionStatusCard(
    connectionState: ConnectionState,
    isConnecting: Boolean,
    onToggleConnection: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(
            containerColor = DarkSurface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Connection Status Icon and Text
            AnimatedContent(
                targetState = connectionState,
                transitionSpec = {
                    fadeIn() + slideInVertically() with fadeOut() + slideOutVertically()
                }
            ) { state ->
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    val (icon, color, text) = when (state) {
                        ConnectionState.CONNECTED -> Triple(
                            Icons.Default.CheckCircle,
                            SuccessGreen,
                            "متصل"
                        )
                        ConnectionState.CONNECTING -> Triple(
                            Icons.Default.Sync,
                            WarningOrange,
                            "جاري الاتصال..."
                        )
                        ConnectionState.DISCONNECTED -> Triple(
                            Icons.Default.Cancel,
                            ErrorRed,
                            "غير متصل"
                        )
                    }
                    
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = color,
                        modifier = Modifier.size(48.dp)
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = text,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Connection Toggle Button
            Button(
                onClick = onToggleConnection,
                enabled = !isConnecting,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = when (connectionState) {
                        ConnectionState.CONNECTED -> ErrorRed
                        else -> PrimaryBlue
                    }
                ),
                shape = RoundedCornerShape(28.dp)
            ) {
                if (isConnecting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                
                Text(
                    text = when (connectionState) {
                        ConnectionState.CONNECTED -> "قطع الاتصال"
                        ConnectionState.CONNECTING -> "جاري الاتصال..."
                        ConnectionState.DISCONNECTED -> "اتصال سريع"
                    },
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.White
                )
            }
        }
    }
}


package com.gameboost.pro.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import com.gameboost.pro.domain.model.Server
import com.gameboost.pro.ui.theme.*

@Composable
fun ServerInfoCard(
    server: Server,
    ping: Int,
    onServerClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onServerClick() }
            .clip(RoundedCornerShape(12.dp)),
        colors = CardDefaults.cardColors(
            containerColor = DarkSurface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Server Location Icon
            Icon(
                imageVector = Icons.Default.LocationOn,
                contentDescription = null,
                tint = PrimaryBlue,
                modifier = Modifier.size(32.dp)
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            // Server Info
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "${server.name} - ${server.city}",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.White
                )
                
                Text(
                    text = server.country,
                    fontSize = 14.sp,
                    color = OnSurfaceVariant
                )
            }
            
            // Ping Display
            Column(
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = "${ping}ms",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = getPingColor(ping)
                )
                
                // Ping Quality Indicator
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    repeat(3) { index ->
                        Box(
                            modifier = Modifier
                                .size(width = 8.dp, height = 12.dp - (index * 2).dp)
                                .background(
                                    color = if (index < getPingBars(ping)) {
                                        getPingColor(ping)
                                    } else {
                                        OnSurfaceVariant.copy(alpha = 0.3f)
                                    },
                                    shape = RoundedCornerShape(1.dp)
                                )
                        )
                        if (index < 2) Spacer(modifier = Modifier.width(2.dp))
                    }
                }
            }
            
            Spacer(modifier = Modifier.width(8.dp))
            
            // Arrow Icon
            Icon(
                imageVector = Icons.Default.KeyboardArrowRight,
                contentDescription = null,
                tint = OnSurfaceVariant,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

private fun getPingColor(ping: Int): Color {
    return when {
        ping < 50 -> SuccessGreen
        ping < 100 -> WarningOrange
        else -> ErrorRed
    }
}

private fun getPingBars(ping: Int): Int {
    return when {
        ping < 50 -> 3
        ping < 100 -> 2
        ping < 150 -> 1
        else -> 0
    }
}


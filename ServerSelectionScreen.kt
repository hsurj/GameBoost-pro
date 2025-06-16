package com.gameboost.pro.ui.servers

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.gameboost.pro.domain.model.Server
import com.gameboost.pro.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServerSelectionScreen(
    viewModel: ServerSelectionViewModel
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(16.dp)
    ) {
        // Header
        Text(
            text = "اختيار الخادم",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Search Bar
        OutlinedTextField(
            value = uiState.searchQuery,
            onValueChange = viewModel::updateSearchQuery,
            placeholder = { Text("البحث عن خادم...") },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = null,
                    tint = OnSurfaceVariant
                )
            },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = PrimaryBlue,
                unfocusedBorderColor = OnSurfaceVariant,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
            )
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Filter Tabs
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val filters = listOf("الكل", "أوروبا", "آسيا", "أمريكا")
            filters.forEachIndexed { index, filter ->
                FilterChip(
                    onClick = { viewModel.selectFilter(index) },
                    label = { Text(filter) },
                    selected = uiState.selectedFilter == index,
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = PrimaryBlue,
                        selectedLabelColor = Color.White
                    )
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Auto-select toggle
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "اختيار تلقائي",
                fontSize = 16.sp,
                color = Color.White
            )
            
            Switch(
                checked = uiState.autoSelectEnabled,
                onCheckedChange = viewModel::toggleAutoSelect,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = PrimaryBlue
                )
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Server List
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(uiState.filteredServers) { server ->
                ServerListItem(
                    server = server,
                    isSelected = server.id == uiState.selectedServerId,
                    onServerClick = { viewModel.selectServer(server) },
                    onFavoriteClick = { viewModel.toggleFavorite(server.id) }
                )
            }
        }
    }
}

@Composable
private fun ServerListItem(
    server: Server,
    isSelected: Boolean,
    onServerClick: () -> Unit,
    onFavoriteClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onServerClick() }
            .clip(RoundedCornerShape(12.dp)),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) PrimaryBlue.copy(alpha = 0.2f) else DarkSurface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Country Flag (emoji)
            Text(
                text = server.flagUrl,
                fontSize = 32.sp
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            // Server Info
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = server.name,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.White
                )
                
                Text(
                    text = server.city,
                    fontSize = 14.sp,
                    color = OnSurfaceVariant
                )
            }
            
            // Ping and Load
            Column(
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = "${server.ping}ms",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = getPingColor(server.ping)
                )
                
                // Load indicator
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    repeat(5) { index ->
                        Box(
                            modifier = Modifier
                                .size(width = 4.dp, height = 8.dp)
                                .background(
                                    color = if (index < (server.load / 20)) {
                                        getLoadColor(server.load)
                                    } else {
                                        OnSurfaceVariant.copy(alpha = 0.3f)
                                    },
                                    shape = RoundedCornerShape(1.dp)
                                )
                        )
                        if (index < 4) Spacer(modifier = Modifier.width(1.dp))
                    }
                }
            }
            
            Spacer(modifier = Modifier.width(8.dp))
            
            // Favorite button
            IconButton(
                onClick = onFavoriteClick
            ) {
                Icon(
                    imageVector = if (server.isFavorite) Icons.Default.Star else Icons.Default.StarBorder,
                    contentDescription = null,
                    tint = if (server.isFavorite) WarningOrange else OnSurfaceVariant
                )
            }
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

private fun getLoadColor(load: Int): Color {
    return when {
        load < 50 -> SuccessGreen
        load < 80 -> WarningOrange
        else -> ErrorRed
    }
}


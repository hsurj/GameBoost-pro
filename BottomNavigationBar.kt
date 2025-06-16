package com.gameboost.pro.ui.components

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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gameboost.pro.ui.theme.*

@Composable
fun BottomNavigationBar(
    selectedTab: Int = 0,
    onTabSelected: (Int) -> Unit = {},
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)),
        colors = CardDefaults.cardColors(
            containerColor = DarkSurface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            val tabs = listOf(
                BottomNavItem("الرئيسية", Icons.Default.Home, 0),
                BottomNavItem("الخوادم", Icons.Default.Storage, 1),
                BottomNavItem("الإحصائيات", Icons.Default.BarChart, 2),
                BottomNavItem("الإعدادات", Icons.Default.Settings, 3)
            )
            
            tabs.forEach { tab ->
                BottomNavTabItem(
                    item = tab,
                    isSelected = selectedTab == tab.index,
                    onClick = { onTabSelected(tab.index) }
                )
            }
        }
    }
}

@Composable
private fun BottomNavTabItem(
    item: BottomNavItem,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(
                if (isSelected) PrimaryBlue.copy(alpha = 0.2f) else Color.Transparent
            )
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = item.icon,
            contentDescription = item.title,
            tint = if (isSelected) PrimaryBlue else OnSurfaceVariant,
            modifier = Modifier.size(24.dp)
        )
        
        Spacer(modifier = Modifier.height(4.dp))
        
        Text(
            text = item.title,
            fontSize = 12.sp,
            fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal,
            color = if (isSelected) PrimaryBlue else OnSurfaceVariant
        )
    }
}

private data class BottomNavItem(
    val title: String,
    val icon: ImageVector,
    val index: Int
)


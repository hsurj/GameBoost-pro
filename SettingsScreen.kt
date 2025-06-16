package com.gameboost.pro.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gameboost.pro.ui.theme.*

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel
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
            text = "الإعدادات",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Connection Settings
            item {
                SettingsSection(
                    title = "إعدادات الاتصال",
                    icon = Icons.Default.Wifi
                ) {
                    SettingsToggleItem(
                        title = "اتصال تلقائي",
                        subtitle = "الاتصال تلقائياً عند بدء التطبيق",
                        checked = uiState.autoConnect,
                        onCheckedChange = viewModel::setAutoConnect
                    )
                    
                    SettingsClickableItem(
                        title = "البروتوكول",
                        subtitle = uiState.selectedProtocol.name,
                        icon = Icons.Default.Security,
                        onClick = { /* Show protocol selection */ }
                    )
                    
                    SettingsToggleItem(
                        title = "مفتاح الإيقاف",
                        subtitle = "قطع الإنترنت عند انقطاع VPN",
                        checked = uiState.killSwitch,
                        onCheckedChange = viewModel::setKillSwitch
                    )
                    
                    SettingsClickableItem(
                        title = "إعدادات DNS",
                        subtitle = "DNS مخصص للألعاب",
                        icon = Icons.Default.Dns,
                        onClick = { /* Show DNS settings */ }
                    )
                }
            }
            
            // Gaming Optimization
            item {
                SettingsSection(
                    title = "تحسين الألعاب",
                    icon = Icons.Default.SportsEsports
                ) {
                    SettingsToggleItem(
                        title = "كشف الألعاب",
                        subtitle = "كشف الألعاب تلقائياً وتطبيق التحسينات",
                        checked = uiState.gameDetection,
                        onCheckedChange = viewModel::setGameDetection
                    )
                    
                    SettingsToggleItem(
                        title = "تحسين تلقائي",
                        subtitle = "تطبيق أفضل الإعدادات للألعاب المكتشفة",
                        checked = uiState.autoOptimize,
                        onCheckedChange = viewModel::setAutoOptimize
                    )
                    
                    SettingsSliderItem(
                        title = "حد البينغ",
                        subtitle = "${uiState.pingThreshold}ms",
                        value = uiState.pingThreshold.toFloat(),
                        onValueChange = { viewModel.setPingThreshold(it.toInt()) },
                        valueRange = 50f..300f
                    )
                }
            }
            
            // Advanced Options
            item {
                SettingsSection(
                    title = "خيارات متقدمة",
                    icon = Icons.Default.Tune
                ) {
                    SettingsClickableItem(
                        title = "تقسيم النفق",
                        subtitle = "اختيار التطبيقات التي تستخدم VPN",
                        icon = Icons.Default.CallSplit,
                        onClick = { /* Show split tunneling */ }
                    )
                    
                    SettingsClickableItem(
                        title = "DNS مخصص",
                        subtitle = uiState.customDns,
                        icon = Icons.Default.Dns,
                        onClick = { /* Show custom DNS */ }
                    )
                }
            }
            
            // Notifications
            item {
                SettingsSection(
                    title = "الإشعارات",
                    icon = Icons.Default.Notifications
                ) {
                    SettingsToggleItem(
                        title = "تنبيهات الاتصال",
                        subtitle = "إشعارات عند الاتصال والانقطاع",
                        checked = uiState.notificationsEnabled,
                        onCheckedChange = viewModel::setNotificationsEnabled
                    )
                    
                    SettingsToggleItem(
                        title = "إشعارات الأداء",
                        subtitle = "تنبيهات عند تغير جودة الاتصال",
                        checked = uiState.performanceNotifications,
                        onCheckedChange = viewModel::setPerformanceNotifications
                    )
                }
            }
            
            // About
            item {
                SettingsSection(
                    title = "حول التطبيق",
                    icon = Icons.Default.Info
                ) {
                    SettingsClickableItem(
                        title = "إصدار التطبيق",
                        subtitle = "1.0.0",
                        icon = Icons.Default.AppSettingsAlt,
                        onClick = { }
                    )
                    
                    SettingsClickableItem(
                        title = "سياسة الخصوصية",
                        subtitle = "اطلع على سياسة الخصوصية",
                        icon = Icons.Default.PrivacyTip,
                        onClick = { }
                    )
                    
                    SettingsClickableItem(
                        title = "شروط الخدمة",
                        subtitle = "اطلع على شروط الاستخدام",
                        icon = Icons.Default.Description,
                        onClick = { }
                    )
                    
                    SettingsClickableItem(
                        title = "الدعم الفني",
                        subtitle = "تواصل مع فريق الدعم",
                        icon = Icons.Default.Support,
                        onClick = { }
                    )
                }
            }
        }
    }
}

@Composable
private fun SettingsSection(
    title: String,
    icon: ImageVector,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp)),
        colors = CardDefaults.cardColors(
            containerColor = DarkSurface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = PrimaryBlue,
                    modifier = Modifier.size(24.dp)
                )
                
                Spacer(modifier = Modifier.width(8.dp))
                
                Text(
                    text = title,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium,
                    color = PrimaryBlue
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            content()
        }
    }
}

@Composable
private fun SettingsToggleItem(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = Color.White
            )
            Text(
                text = subtitle,
                fontSize = 14.sp,
                color = OnSurfaceVariant
            )
        }
        
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = PrimaryBlue
            )
        )
    }
}

@Composable
private fun SettingsClickableItem(
    title: String,
    subtitle: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .clickable { onClick() }
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = OnSurfaceVariant,
            modifier = Modifier.size(20.dp)
        )
        
        Spacer(modifier = Modifier.width(12.dp))
        
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = Color.White
            )
            Text(
                text = subtitle,
                fontSize = 14.sp,
                color = OnSurfaceVariant
            )
        }
        
        Icon(
            imageVector = Icons.Default.KeyboardArrowRight,
            contentDescription = null,
            tint = OnSurfaceVariant,
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
private fun SettingsSliderItem(
    title: String,
    subtitle: String,
    value: Float,
    onValueChange: (Float) -> Unit,
    valueRange: ClosedFloatingPointRange<Float>
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = Color.White
            )
            Text(
                text = subtitle,
                fontSize = 14.sp,
                color = PrimaryBlue
            )
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = valueRange,
            colors = SliderDefaults.colors(
                thumbColor = PrimaryBlue,
                activeTrackColor = PrimaryBlue,
                inactiveTrackColor = OnSurfaceVariant.copy(alpha = 0.3f)
            )
        )
    }
}


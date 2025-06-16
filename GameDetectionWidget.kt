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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gameboost.pro.ui.theme.*

@Composable
fun GameDetectionWidget(
    detectedGame: String?,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
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
                    imageVector = Icons.Default.SportsEsports,
                    contentDescription = null,
                    tint = PrimaryBlue,
                    modifier = Modifier.size(24.dp)
                )
                
                Spacer(modifier = Modifier.width(8.dp))
                
                Text(
                    text = "كشف اللعبة",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.White
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            if (detectedGame != null) {
                // Game detected
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = detectedGame,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = SuccessGreen
                        )
                        Text(
                            text = "تم اكتشافها",
                            fontSize = 14.sp,
                            color = OnSurfaceVariant
                        )
                    }
                    
                    Button(
                        onClick = { /* Apply optimizations */ },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PrimaryBlue
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "تحسين",
                            color = Color.White,
                            fontSize = 14.sp
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Optimization recommendations
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = PrimaryBlue.copy(alpha = 0.1f)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp)
                    ) {
                        Text(
                            text = "توصيات التحسين",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = PrimaryBlue
                        )
                        
                        Spacer(modifier = Modifier.height(4.dp))
                        
                        val recommendations = getGameRecommendations(detectedGame)
                        recommendations.forEach { recommendation ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = SuccessGreen,
                                    modifier = Modifier.size(16.dp)
                                )
                                
                                Spacer(modifier = Modifier.width(8.dp))
                                
                                Text(
                                    text = recommendation,
                                    fontSize = 12.sp,
                                    color = OnSurfaceVariant
                                )
                            }
                            
                            if (recommendation != recommendations.last()) {
                                Spacer(modifier = Modifier.height(4.dp))
                            }
                        }
                    }
                }
            } else {
                // No game detected
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        tint = OnSurfaceVariant,
                        modifier = Modifier.size(32.dp)
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = "لم يتم اكتشاف أي لعبة",
                        fontSize = 14.sp,
                        color = OnSurfaceVariant,
                        fontWeight = FontWeight.Medium
                    )
                    
                    Text(
                        text = "ابدأ لعبة للحصول على تحسينات مخصصة",
                        fontSize = 12.sp,
                        color = OnSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
            }
        }
    }
}

private fun getGameRecommendations(gameName: String): List<String> {
    return when (gameName.lowercase()) {
        "bullet echo" -> listOf(
            "خادم أوروبا محدد تلقائياً",
            "بروتوكول WireGuard مُفعل",
            "DNS محسن للألعاب"
        )
        "fortnite" -> listOf(
            "خادم أمريكي محدد",
            "تقسيم النفق مُفعل",
            "تحسين معدل الإطارات"
        )
        else -> listOf(
            "خادم محسن محدد",
            "إعدادات عامة مطبقة",
            "مراقبة الأداء نشطة"
        )
    }
}


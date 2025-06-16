package com.gameboost.pro.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gameboost.pro.ui.theme.*
import kotlinx.coroutines.delay

@Composable
fun PerformanceChart(
    ping: Int,
    modifier: Modifier = Modifier
) {
    var pingHistory by remember { mutableStateOf(listOf<Int>()) }
    
    // Simulate real-time ping updates
    LaunchedEffect(ping) {
        while (true) {
            val newPing = (20..80).random() // Simulate ping variation
            pingHistory = (pingHistory + newPing).takeLast(30) // Keep last 30 points
            delay(1000) // Update every second
        }
    }
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(200.dp)
            .clip(RoundedCornerShape(12.dp)),
        colors = CardDefaults.cardColors(
            containerColor = DarkSurface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Chart Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "البينغ",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.White
                )
                
                Text(
                    text = "${pingHistory.lastOrNull() ?: ping}ms",
                    fontSize = 14.sp,
                    color = OnSurfaceVariant
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Chart Canvas
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                if (pingHistory.isNotEmpty()) {
                    drawPingChart(pingHistory)
                }
            }
            
            // Chart Footer with scale
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "0ms",
                    fontSize = 12.sp,
                    color = OnSurfaceVariant
                )
                Text(
                    text = "100ms",
                    fontSize = 12.sp,
                    color = OnSurfaceVariant
                )
                Text(
                    text = "200ms",
                    fontSize = 12.sp,
                    color = OnSurfaceVariant
                )
            }
        }
    }
}

private fun DrawScope.drawPingChart(pingHistory: List<Int>) {
    if (pingHistory.size < 2) return
    
    val maxPing = 200f
    val width = size.width
    val height = size.height
    val stepX = width / (pingHistory.size - 1)
    
    // Create path for the ping line
    val path = Path()
    val points = mutableListOf<Offset>()
    
    pingHistory.forEachIndexed { index, ping ->
        val x = index * stepX
        val y = height - (ping / maxPing * height)
        points.add(Offset(x, y))
        
        if (index == 0) {
            path.moveTo(x, y)
        } else {
            path.lineTo(x, y)
        }
    }
    
    // Draw grid lines
    drawGridLines(width, height)
    
    // Draw the ping line
    drawPath(
        path = path,
        color = ChartBlue,
        style = Stroke(width = 3.dp.toPx())
    )
    
    // Draw points
    points.forEach { point ->
        drawCircle(
            color = ChartBlue,
            radius = 4.dp.toPx(),
            center = point
        )
    }
}

private fun DrawScope.drawGridLines(width: Float, height: Float) {
    val gridColor = OnSurfaceVariant.copy(alpha = 0.2f)
    
    // Horizontal grid lines (ping levels)
    for (i in 0..4) {
        val y = height * i / 4
        drawLine(
            color = gridColor,
            start = Offset(0f, y),
            end = Offset(width, y),
            strokeWidth = 1.dp.toPx()
        )
    }
    
    // Vertical grid lines (time)
    for (i in 0..6) {
        val x = width * i / 6
        drawLine(
            color = gridColor,
            start = Offset(x, 0f),
            end = Offset(x, height),
            strokeWidth = 1.dp.toPx()
        )
    }
}


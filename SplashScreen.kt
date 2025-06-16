package com.gameboost.pro.ui.splash

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gameboost.pro.R
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    onSplashFinished: () -> Unit
) {
    var startAnimation by remember { mutableStateOf(false) }
    
    // Animation values
    val logoScale by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0.3f,
        animationSpec = tween(
            durationMillis = 1000,
            easing = FastOutSlowInEasing
        )
    )
    
    val logoAlpha by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0f,
        animationSpec = tween(
            durationMillis = 800,
            easing = LinearEasing
        )
    )
    
    val textAlpha by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0f,
        animationSpec = tween(
            durationMillis = 1000,
            delayMillis = 500,
            easing = LinearEasing
        )
    )
    
    val logoRotation by animateFloatAsState(
        targetValue = if (startAnimation) 360f else 0f,
        animationSpec = tween(
            durationMillis = 1200,
            easing = FastOutSlowInEasing
        )
    )
    
    // Start animation when composable is first composed
    LaunchedEffect(Unit) {
        startAnimation = true
        delay(3000) // Show splash for 3 seconds
        onSplashFinished()
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF1A1A2E),
                        Color(0xFF16213E),
                        Color(0xFF0F3460)
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // App Logo with rotation and scale animation
            Box(
                modifier = Modifier
                    .size(140.dp)
                    .scale(logoScale)
                    .alpha(logoAlpha),
                contentAlignment = Alignment.Center
            ) {
                // Outer glow effect
                Box(
                    modifier = Modifier
                        .size(140.dp)
                        .alpha(0.6f)
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    Color(0xFF00D4FF).copy(alpha = 0.7f),
                                    Color(0xFF00D4FF).copy(alpha = 0f)
                                )
                            ),
                            shape = androidx.compose.foundation.shape.CircleShape
                        )
                )
                
                // Rotating logo
                Image(
                    painter = painterResource(id = R.drawable.app_logo),
                    contentDescription = "GameBoost Pro Logo",
                    modifier = Modifier
                        .size(120.dp)
                        .rotate(logoRotation)
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // App Name
            Text(
                text = stringResource(R.string.app_name),
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center,
                modifier = Modifier.alpha(textAlpha)
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Tagline
            Text(
                text = stringResource(R.string.welcome_subtitle),
                fontSize = 16.sp,
                color = Color.White.copy(alpha = 0.8f),
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .alpha(textAlpha)
                    .padding(horizontal = 32.dp)
            )
        }
        
        // Loading indicator at bottom
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 64.dp)
                .alpha(textAlpha)
        ) {
            LoadingDots()
        }
    }
}

@Composable
private fun LoadingDots() {
    val infiniteTransition = rememberInfiniteTransition()
    
    val dot1Alpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(600),
            repeatMode = RepeatMode.Reverse
        )
    )
    
    val dot2Alpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, delayMillis = 200),
            repeatMode = RepeatMode.Reverse
        )
    )
    
    val dot3Alpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, delayMillis = 400),
            repeatMode = RepeatMode.Reverse
        )
    )
    
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(3) { index ->
            val alpha = when (index) {
                0 -> dot1Alpha
                1 -> dot2Alpha
                else -> dot3Alpha
            }
            
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .alpha(alpha)
                    .background(
                        color = Color(0xFF00D4FF),
                        shape = androidx.compose.foundation.shape.CircleShape
                    )
            )
        }
    }
}

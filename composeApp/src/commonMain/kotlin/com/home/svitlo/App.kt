package com.home.svitlo

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.home.svitlo.domain.model.InverterStatus
import org.jetbrains.compose.ui.tooling.preview.Preview

// Custom color palette
private val SunYellow = Color(0xFFFFD93D)
private val WarmOrange = Color(0xFFFF8C42)
private val SkyBlue = Color(0xFF6EC6FF)
private val DarkBlue = Color(0xFF1A237E)
private val MidnightBlue = Color(0xFF0D1B2A)
private val AlertRed = Color(0xFFE53935)
private val DarkRed = Color(0xFF4A1A1A)
private val ThinkingPurple = Color(0xFF7C4DFF)
private val DeepPurple = Color(0xFF311B92)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
@Preview
fun App() {
    val viewModel = remember { HomeViewModel() }
    val uiState by viewModel.uiState.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()

    MaterialTheme {
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = { viewModel.refresh() },
            modifier = Modifier.fillMaxSize()
        ) {
            AnimatedContent(
                targetState = uiState,
                transitionSpec = {
                    (fadeIn(animationSpec = tween(300)) + scaleIn(initialScale = 0.92f))
                        .togetherWith(fadeOut(animationSpec = tween(300)) + scaleOut(targetScale = 0.92f))
                },
                label = "state_transition"
            ) { state ->
                when (state) {
                    is HomeUiState.Loading -> LoadingScreen()
                    is HomeUiState.Success -> StatusScreen(status = state.status)
                    is HomeUiState.RateLimited -> RateLimitScreen()
                    is HomeUiState.Error -> ErrorScreen(message = state.message)
                }
            }
        }
    }
}

@Composable
private fun LoadingScreen() {
    val scrollState = rememberScrollState()
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(DarkBlue, MidnightBlue)
                )
            )
            .verticalScroll(scrollState),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            // Animated sun/light icon
            val infiniteTransition = rememberInfiniteTransition(label = "loading")
            val rotation by infiniteTransition.animateFloat(
                initialValue = 0f,
                targetValue = 360f,
                animationSpec = infiniteRepeatable(
                    animation = tween(2000, easing = LinearEasing),
                    repeatMode = RepeatMode.Restart
                ),
                label = "rotation"
            )

            Text(
                text = "☀️",
                fontSize = 80.sp,
                modifier = Modifier.rotate(rotation)
            )

            Spacer(modifier = Modifier.height(32.dp))

            CircularProgressIndicator(
                color = SunYellow,
                trackColor = SunYellow.copy(alpha = 0.2f),
                modifier = Modifier.size(48.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Перевіряємо світло...",
                color = Color.White.copy(alpha = 0.9f),
                fontSize = 20.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun StatusScreen(status: InverterStatus) {
    when (status) {
        InverterStatus.NORMAL -> PowerOnScreen()
        InverterStatus.OFF_GRID -> PowerOffScreen()
        else -> ProcessingScreen(status = status)
    }
}

@Composable
private fun PowerOnScreen() {
    val scrollState = rememberScrollState()
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(SkyBlue, DarkBlue)
                )
            )
            .verticalScroll(scrollState),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxSize().padding(32.dp)
        ) {
            // Glowing sun animation
            val infiniteTransition = rememberInfiniteTransition(label = "glow")
            val scale by infiniteTransition.animateFloat(
                initialValue = 1f,
                targetValue = 1.1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(1000),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "scale"
            )

            Text(
                text = "☀️",
                fontSize = (120 * scale).sp
            )

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "Світло є!",
                color = SunYellow,
                fontSize = 48.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Інвертор працює нормально",
                color = Color.White.copy(alpha = 0.8f),
                fontSize = 18.sp,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(48.dp))

            Text(
                text = "⬇️ Потягніть вниз для оновлення",
                color = Color.White.copy(alpha = 0.5f),
                fontSize = 14.sp
            )
        }
    }
}

@Composable
private fun PowerOffScreen() {
    val scrollState = rememberScrollState()
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(AlertRed.copy(alpha = 0.8f), DarkRed)
                )
            )
            .verticalScroll(scrollState),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxSize().padding(32.dp)
        ) {
            // Flashing warning animation
            val infiniteTransition = rememberInfiniteTransition(label = "flash")
            val alpha by infiniteTransition.animateFloat(
                initialValue = 0.6f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(500),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "alpha"
            )

            Text(
                text = "🔌",
                fontSize = 120.sp,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "Світло немає!",
                color = Color.White.copy(alpha = alpha),
                fontSize = 44.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Працює автономний режим",
                color = Color.White.copy(alpha = 0.8f),
                fontSize = 18.sp,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(48.dp))

            Text(
                text = "⬇️ Потягніть вниз для оновлення",
                color = Color.White.copy(alpha = 0.5f),
                fontSize = 14.sp
            )
        }
    }
}

@Composable
private fun ProcessingScreen(status: InverterStatus) {
    val scrollState = rememberScrollState()
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(ThinkingPurple, DeepPurple)
                )
            )
            .verticalScroll(scrollState),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxSize().padding(32.dp)
        ) {
            // Rotating thinking animation
            val infiniteTransition = rememberInfiniteTransition(label = "think")
            val rotation by infiniteTransition.animateFloat(
                initialValue = -15f,
                targetValue = 15f,
                animationSpec = infiniteRepeatable(
                    animation = tween(600),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "rotation"
            )

            Text(
                text = "🔄",
                fontSize = 120.sp,
                modifier = Modifier.rotate(rotation)
            )

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "Щось відбувається!",
                color = Color.White,
                fontSize = 40.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = status.description,
                color = Color.White.copy(alpha = 0.8f),
                fontSize = 18.sp,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Код: ${status.code}",
                color = Color.White.copy(alpha = 0.6f),
                fontSize = 14.sp,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(48.dp))

            Text(
                text = "⬇️ Потягніть вниз для оновлення",
                color = Color.White.copy(alpha = 0.5f),
                fontSize = 14.sp
            )
        }
    }
}

@Composable
private fun RateLimitScreen() {
    val scrollState = rememberScrollState()
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFFFF6F00), Color(0xFFE65100))
                )
            )
            .verticalScroll(scrollState),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxSize().padding(32.dp)
        ) {
            // Animated hand gesture
            val infiniteTransition = rememberInfiniteTransition(label = "wave")
            val rotation by infiniteTransition.animateFloat(
                initialValue = -20f,
                targetValue = 20f,
                animationSpec = infiniteRepeatable(
                    animation = tween(300),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "rotation"
            )

            Text(
                text = "🛑",
                fontSize = 100.sp,
                modifier = Modifier.rotate(rotation)
            )

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "Забагато хочеш!",
                color = Color.White,
                fontSize = 36.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Зачекай хвилинку...",
                color = Color.White.copy(alpha = 0.8f),
                fontSize = 18.sp,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(48.dp))

            Text(
                text = "⬇️ Потягніть вниз для оновлення",
                color = Color.White.copy(alpha = 0.5f),
                fontSize = 14.sp
            )
        }
    }
}

@Composable
private fun ErrorScreen(message: String) {
    val scrollState = rememberScrollState()
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFF424242), Color(0xFF212121))
                )
            )
            .verticalScroll(scrollState),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxSize().padding(32.dp)
        ) {
            Text(
                text = "⚠️",
                fontSize = 100.sp
            )

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "Помилка",
                color = WarmOrange,
                fontSize = 36.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = message,
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 16.sp,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(48.dp))

            Text(
                text = "⬇️ Потягніть вниз для оновлення",
                color = Color.White.copy(alpha = 0.5f),
                fontSize = 14.sp
            )
        }
    }
}


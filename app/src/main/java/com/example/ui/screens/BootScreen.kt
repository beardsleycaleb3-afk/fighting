package com.example.ui.screens

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.audio.SoundEffectsEngine
import com.example.ui.theme.BackgroundDark
import com.example.ui.theme.BorderDark
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.TapoutBrightRed
import com.example.ui.theme.TapoutCrimson
import com.example.ui.theme.TapoutGold
import com.example.ui.theme.TapoutNeonBlue
import com.example.ui.theme.TapoutOrange
import kotlinx.coroutines.delay

private val BOOT_LOG_LINES = listOf(
    "[INIT] Booting TAPOUT Android Engine...",
    "[SYSTEM] Viewport matrix calibrated (350x550 / 720x1440)",
    "[INPUT] Multi-touch haptic matrix initialized (No mouse/keyboard)",
    "[WARP] Connecting Quantum Time-Travel Coliseum...",
    "[ROSTER] Synchronizing Ninjutsu, MMA, Boxing & Greco-Roman entities...",
    "[AUDIO] Procedural 8-bit sound synth & tactiles online...",
    "[ARENA] Coliseum shaders and hitboxes compiled...",
    "[READY] Champions assembled. Tournament standby!"
)

@Composable
fun BootScreen(
    soundEngine: SoundEffectsEngine?,
    onEnterSelect: () -> Unit,
    onTriggerRecovery: () -> Unit,
    modifier: Modifier = Modifier
) {
    var progress by remember { mutableFloatStateOf(0f) }
    val displayedLogs = remember { mutableStateListOf<String>() }
    var isReady by remember { mutableStateOf(false) }
    val listState = rememberLazyListState()

    // Blinking animation for "TAP ANYWHERE TO ENTER"
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val tapAlpha by infiniteTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "tapAlpha"
    )

    // Progress & Log streaming ticker
    LaunchedEffect(Unit) {
        for (i in BOOT_LOG_LINES.indices) {
            displayedLogs.add(BOOT_LOG_LINES[i])
            progress = (i + 1).toFloat() / BOOT_LOG_LINES.size
            soundEngine?.vibrate(10, 60)
            listState.animateScrollToItem(displayedLogs.size - 1)
            delay(150)
        }
        progress = 1.0f
        isReady = true
        soundEngine?.playSelect()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundDark)
            .pointerInput(isReady) {
                detectTapGestures {
                    if (isReady) {
                        soundEngine?.playSelect()
                        onEnterSelect()
                    }
                }
            }
            .testTag("boot_screen_container"),
        contentAlignment = Alignment.Center
    ) {
        // Subtle ambient radial glow behind hero
        Box(
            modifier = Modifier
                .size(300.dp)
                .background(
                    Brush.radialGradient(
                        colors = listOf(TapoutCrimson.copy(alpha = 0.2f), Color.Transparent)
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 18.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Sleek Top Badge Pill
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(SurfaceCard)
                    .border(1.dp, Color(0x26FFFFFF), RoundedCornerShape(50))
                    .padding(horizontal = 12.dp, vertical = 4.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(if (isReady) TapoutGold else TapoutBrightRed)
                    )
                    Text(
                        text = "TIME TRAVEL MEGA TOURNAMENT",
                        fontSize = 8.5.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.5.sp,
                        color = Color(0xCCFFFFFF)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Main Hero Logo with sleek typography
            Text(
                text = "TAPOUT",
                fontSize = 52.sp,
                fontWeight = FontWeight.Black,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 6.sp,
                color = TapoutCrimson,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .shadow(elevation = 20.dp, shape = RoundedCornerShape(8.dp), ambientColor = TapoutCrimson, spotColor = TapoutBrightRed)
                    .testTag("boot_logo")
            )

            Text(
                text = "QUANTUM FIGHT SIMULATOR",
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 2.sp,
                color = TapoutOrange,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 2.dp, bottom = 16.dp)
            )

            // Sleek Terminal Console Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(SurfaceCard)
                    .border(1.dp, BorderDark, RoundedCornerShape(16.dp))
                    .padding(12.dp)
            ) {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(displayedLogs) { log ->
                        Text(
                            text = log,
                            color = when {
                                log.contains("[READY]") -> TapoutGold
                                log.contains("[INIT]") -> TapoutNeonBlue
                                else -> Color(0xFFE2E8F0)
                            },
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            lineHeight = 14.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Sleek Progress Bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(50))
                    .background(Color(0xFF14000B))
                    .border(1.dp, Color(0x1AFFFFFF), RoundedCornerShape(50))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(progress)
                        .height(6.dp)
                        .clip(RoundedCornerShape(50))
                        .background(
                            Brush.horizontalGradient(
                                listOf(TapoutCrimson, TapoutBrightRed, TapoutOrange)
                            )
                        )
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Tap to Enter Interactive Card / Prompt
            if (isReady) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(
                            Brush.horizontalGradient(
                                listOf(TapoutCrimson.copy(alpha = tapAlpha), Color(0xFF900028).copy(alpha = tapAlpha))
                            )
                        )
                        .border(1.dp, TapoutGold.copy(alpha = tapAlpha), RoundedCornerShape(14.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "▶ TAP TO ENTER COLISEUM ◀",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 2.sp,
                        color = Color.White,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.testTag("tap_to_enter_text")
                    )
                }
            } else {
                Text(
                    text = "INITIALIZING ${(progress * 100).toInt()}%...",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0x88FFFFFF),
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Recovery Mode small footer link
            Row(
                modifier = Modifier
                    .pointerInput(Unit) {
                        detectTapGestures {
                            onTriggerRecovery()
                        }
                    }
                    .padding(4.dp)
            ) {
                Text(
                    text = "[ DIAGNOSTIC RECOVERY ]",
                    fontSize = 9.sp,
                    fontFamily = FontFamily.Monospace,
                    color = Color(0x44FFFFFF)
                )
            }
        }
    }
}


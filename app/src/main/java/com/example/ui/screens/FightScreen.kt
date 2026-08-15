package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.audio.SoundEffectsEngine
import com.example.game.ActionState
import com.example.game.ComboTier
import com.example.game.FightEngine
import com.example.game.FighterEntity
import com.example.game.MatchStats
import com.example.model.Fighter
import com.example.model.Stage
import com.example.model.StageRoster
import com.example.ui.theme.ArenaDark
import com.example.ui.theme.BackgroundDark
import com.example.ui.theme.HudHpCpuEnd
import com.example.ui.theme.HudHpCpuStart
import com.example.ui.theme.HudHpPlayerEnd
import com.example.ui.theme.HudHpPlayerStart
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.TapoutBrightRed
import com.example.ui.theme.TapoutCrimson
import com.example.ui.theme.TapoutGold
import com.example.ui.theme.TapoutNeonBlue
import com.example.ui.theme.TapoutNeonPurple
import com.example.ui.theme.TapoutOrange
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun FightScreen(
    playerFighter: Fighter,
    enemyFighter: Fighter,
    stage: Stage = StageRoster.getStageById(playerFighter.homeStageId),
    soundEngine: SoundEffectsEngine?,
    onMatchOver: (MatchStats) -> Unit,
    onExitToSelect: () -> Unit,
    modifier: Modifier = Modifier
) {
    val engine = remember(playerFighter.id, enemyFighter.id, stage.id) {
        FightEngine(
            playerFighter = playerFighter,
            enemyFighter = enemyFighter,
            stage = stage,
            soundEngine = soundEngine,
            onMatchEnded = onMatchOver
        )
    }

    var frameTick by remember { mutableIntStateOf(0) }
    var soundActive by remember { mutableStateOf(soundEngine?.isSoundEnabled() ?: true) }

    // Game loop at 60fps
    LaunchedEffect(engine) {
        var lastNanos = System.nanoTime()
        while (!engine.isMatchOver) {
            withFrameNanos { now ->
                val dt = ((now - lastNanos) / 1_000_000_000f).coerceIn(0.001f, 0.05f)
                lastNanos = now
                engine.update(dt)
                frameTick++
            }
        }
    }

    // Special meter pulsating glow animation
    val infiniteTransition = rememberInfiniteTransition(label = "specialPulse")
    val specialGlow by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "specialGlow"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundDark)
            .testTag("fight_screen_container")
    ) {
        // TOP HUD (Header)
        SleekFightHud(
            engine = engine,
            stage = stage,
            onExit = onExitToSelect,
            soundActive = soundActive,
            onToggleSound = {
                soundActive = soundEngine?.toggleSound() ?: true
            }
        )

        // ARENA CANVAS & HUD OVERLAYS
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .background(ArenaDark)
        ) {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("fight_canvas")
            ) {
                drawStageAndFighters(engine, stage, frameTick)
            }

            // Real-Time Combo Counter HUD Overlay (Dynamic Scaling & Glow)
            RealtimeComboOverlay(
                comboCount = engine.playerCombo,
                comboTier = engine.comboTier,
                displayActive = engine.playerCombo > 1 && !engine.isMatchOver,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(start = 14.dp, top = 8.dp)
            )

            // Special Move Dramatic Cut-In Banner
            if (engine.specialCutInTimer > 0f && engine.specialCutInFighter != null) {
                val cutInFighter = engine.specialCutInFighter!!
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.Center)
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    Color.Transparent,
                                    cutInFighter.themeColor.copy(alpha = 0.85f),
                                    cutInFighter.accentColor.copy(alpha = 0.95f),
                                    Color.Transparent
                                )
                            )
                        )
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "★ ULTRA MOVE TRIGGERED ★",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 2.sp,
                            color = Color.White
                        )
                        Text(
                            text = cutInFighter.specialName,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black,
                            fontStyle = FontStyle.Italic,
                            color = TapoutGold
                        )
                        Text(
                            text = "\"${cutInFighter.specialQuote}\"",
                            fontSize = 10.sp,
                            fontStyle = FontStyle.Italic,
                            color = Color.White.copy(alpha = 0.9f)
                        )
                    }
                }
            }

            // Pre-Fight Intro Cinematic Dialogue Cut-In
            if (engine.introCinematicTimer > 0f) {
                PreFightIntroBanner(
                    engine = engine,
                    stage = stage,
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }

        // SLEEK TOUCH CONTROLS (D-Pad & Attack Action cluster)
        SleekTouchControls(
            engine = engine,
            specialGlow = specialGlow,
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp)
                .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
                .background(BackgroundDark)
                .border(
                    width = 1.dp,
                    color = Color(0x1AFFFFFF),
                    shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
                )
                .padding(horizontal = 12.dp, vertical = 8.dp)
        )
    }
}

@Composable
fun RealtimeComboOverlay(
    comboCount: Int,
    comboTier: ComboTier,
    displayActive: Boolean,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = displayActive,
        enter = fadeIn() + scaleIn(spring(dampingRatio = 0.6f, stiffness = 400f)),
        exit = fadeOut() + scaleOut(),
        modifier = modifier
    ) {
        // Dynamic scale factor according to combo streak
        val targetScale = when (comboTier) {
            ComboTier.GODLIKE -> 1.35f
            ComboTier.ULTRA -> 1.25f
            ComboTier.MEGA -> 1.15f
            ComboTier.GREAT -> 1.08f
            ComboTier.NORMAL -> 1.0f
            ComboTier.NONE -> 0.9f
        }
        val animScale by animateFloatAsState(targetValue = targetScale, label = "comboScale")

        val badgeColors = when (comboTier) {
            ComboTier.GODLIKE -> listOf(Color(0xFFFF0055), TapoutGold, Color(0xFFFF9900))
            ComboTier.ULTRA -> listOf(TapoutCrimson, TapoutGold)
            ComboTier.MEGA -> listOf(TapoutOrange, Color(0xFFFF3300))
            ComboTier.GREAT -> listOf(TapoutNeonBlue, TapoutNeonPurple)
            ComboTier.NORMAL -> listOf(TapoutNeonBlue.copy(alpha = 0.8f), Color(0xFF0055FF))
            ComboTier.NONE -> listOf(Color.DarkGray, Color.Black)
        }

        val titleText = when (comboTier) {
            ComboTier.GODLIKE -> "★ GODLIKE RAMPAGE! ★"
            ComboTier.ULTRA -> "⚡ ULTRA COMBO! ⚡"
            ComboTier.MEGA -> "🔥 MEGA STRIKE! 🔥"
            ComboTier.GREAT -> "GREAT COMBO!"
            ComboTier.NORMAL -> "COMBO!"
            ComboTier.NONE -> ""
        }

        Box(
            modifier = Modifier
                .scale(animScale)
                .shadow(
                    elevation = if (comboTier >= ComboTier.MEGA) 12.dp else 4.dp,
                    shape = RoundedCornerShape(10.dp),
                    spotColor = badgeColors.first(),
                    ambientColor = badgeColors.first()
                )
                .clip(RoundedCornerShape(10.dp))
                .background(Brush.horizontalGradient(badgeColors))
                .border(1.5.dp, Color.White.copy(alpha = 0.8f), RoundedCornerShape(10.dp))
                .padding(horizontal = 10.dp, vertical = 4.dp)
        ) {
            Column(horizontalAlignment = Alignment.Start) {
                Text(
                    text = titleText,
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp,
                    color = Color.White
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "$comboCount",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace,
                        color = TapoutGold
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "HITS",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        fontStyle = FontStyle.Italic,
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Composable
fun PreFightIntroBanner(
    engine: FightEngine,
    stage: Stage,
    modifier: Modifier = Modifier
) {
    val timeLeft = engine.introCinematicTimer
    val isReady = timeLeft <= 1.0f

    Box(
        modifier = modifier
            .fillMaxWidth(0.92f)
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xE60A000A))
            .border(1.5.dp, TapoutGold, RoundedCornerShape(16.dp))
            .padding(14.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Stage Tag
            Text(
                text = "STAGE ${stage.stageNumber}: ${stage.name}",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.5.sp,
                color = stage.floorAccent
            )

            // Versus Title Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = engine.player.fighter.name,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black,
                        color = engine.player.fighter.themeColor
                    )
                    Text(
                        text = "\"${engine.player.fighter.introQuote}\"",
                        fontSize = 9.sp,
                        fontStyle = FontStyle.Italic,
                        color = Color.White.copy(alpha = 0.85f),
                        maxLines = 2
                    )
                }

                Box(
                    modifier = Modifier
                        .padding(horizontal = 6.dp)
                        .clip(CircleShape)
                        .background(TapoutCrimson)
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text("VS", fontSize = 11.sp, fontWeight = FontWeight.Black, color = Color.White)
                }

                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        text = engine.enemy.fighter.name,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black,
                        color = engine.enemy.fighter.themeColor
                    )
                    Text(
                        text = "\"${engine.enemy.fighter.introQuote}\"",
                        fontSize = 9.sp,
                        fontStyle = FontStyle.Italic,
                        textAlign = TextAlign.End,
                        color = Color.White.copy(alpha = 0.85f),
                        maxLines = 2
                    )
                }
            }

            // Ready / Fight Callout
            Text(
                text = if (isReady) "★ FIGHT! ★" else "ROUND 1 - READY!",
                fontSize = 16.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 2.sp,
                color = if (isReady) TapoutBrightRed else TapoutGold
            )
        }
    }
}

@Composable
fun SleekFightHud(
    engine: FightEngine,
    stage: Stage,
    onExit: () -> Unit,
    soundActive: Boolean,
    onToggleSound: () -> Unit
) {
    val playerHpPct = (engine.player.hp.toFloat() / engine.player.maxHp).coerceIn(0f, 1f)
    val enemyHpPct = (engine.enemy.hp.toFloat() / engine.enemy.maxHp).coerceIn(0f, 1f)
    val playerHpInt = (playerHpPct * 100).toInt()
    val enemyHpInt = (enemyHpPct * 100).toInt()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xEE000000), Color(0x00000000))
                )
            )
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        // Quick control utility row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onExit,
                modifier = Modifier.size(28.dp).testTag("exit_fight_button")
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Exit to Select",
                    tint = Color(0x99FFFFFF),
                    modifier = Modifier.size(16.dp)
                )
            }

            Text(
                text = "STAGE ${stage.stageNumber} • ${stage.name}",
                fontSize = 8.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
                color = Color(0x99FFFFFF)
            )

            IconButton(
                onClick = onToggleSound,
                modifier = Modifier.size(28.dp)
            ) {
                Icon(
                    imageVector = if (soundActive) Icons.Default.VolumeUp else Icons.Default.VolumeOff,
                    contentDescription = "Toggle Audio",
                    tint = if (soundActive) TapoutOrange else Color(0x66FFFFFF),
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        // HUD Main Row with Player HP, Circular Timer, CPU HP
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Player 1 HP Column
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Text(
                        text = "P1 ${engine.player.fighter.name.uppercase()}",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp,
                        color = TapoutCrimson
                    )
                    Text(
                        text = "$playerHpInt%",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0x99FFFFFF)
                    )
                }

                // Health Bar Pill
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(9.dp)
                        .clip(RoundedCornerShape(50))
                        .background(Color(0xFF1A0008))
                        .border(1.dp, Color(0x26FFFFFF), RoundedCornerShape(50))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(playerHpPct)
                            .height(9.dp)
                            .clip(RoundedCornerShape(50))
                            .background(
                                Brush.horizontalGradient(
                                    listOf(HudHpPlayerStart, HudHpPlayerEnd)
                                )
                            )
                    )
                }

                // Super Meter Line
                val playerSpPct = (engine.player.superMeter / 100f).coerceIn(0f, 1f)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .clip(RoundedCornerShape(50))
                        .background(Color(0xFF140010))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(playerSpPct)
                            .height(4.dp)
                            .clip(RoundedCornerShape(50))
                            .background(
                                if (playerSpPct >= 1f) Brush.horizontalGradient(listOf(TapoutGold, TapoutBrightRed))
                                else Brush.horizontalGradient(listOf(TapoutNeonBlue, TapoutNeonPurple))
                            )
                    )
                }
            }

            // Circular Glowing Timer Hub
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(Color(0x99000000))
                    .border(2.dp, TapoutGold, CircleShape)
                    .shadow(elevation = 8.dp, shape = CircleShape, ambientColor = TapoutGold, spotColor = TapoutGold),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = engine.roundTimeSeconds.toInt().toString(),
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Monospace,
                    color = if (engine.roundTimeSeconds <= 10) TapoutBrightRed else TapoutGold
                )
            }

            // CPU Opponent HP Column
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Text(
                        text = "$enemyHpInt%",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0x99FFFFFF)
                    )
                    Text(
                        text = "CPU ${engine.enemy.fighter.name.uppercase()}",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp,
                        color = Color(0xFF94A3B8)
                    )
                }

                // Health Bar Pill (Opponent)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(9.dp)
                        .clip(RoundedCornerShape(50))
                        .background(Color(0xFF1A0008))
                        .border(1.dp, Color(0x26FFFFFF), RoundedCornerShape(50))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(enemyHpPct)
                            .height(9.dp)
                            .align(Alignment.CenterEnd)
                            .clip(RoundedCornerShape(50))
                            .background(
                                Brush.horizontalGradient(
                                    listOf(HudHpCpuStart, HudHpCpuEnd)
                                )
                            )
                    )
                }

                // Super Meter Line (Opponent)
                val enemySpPct = (engine.enemy.superMeter / 100f).coerceIn(0f, 1f)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .clip(RoundedCornerShape(50))
                        .background(Color(0xFF140010))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(enemySpPct)
                            .height(4.dp)
                            .align(Alignment.CenterEnd)
                            .clip(RoundedCornerShape(50))
                            .background(
                                if (enemySpPct >= 1f) Brush.horizontalGradient(listOf(TapoutGold, TapoutBrightRed))
                                else Brush.horizontalGradient(listOf(TapoutBrightRed, TapoutOrange))
                            )
                    )
                }
            }
        }
    }
}

fun DrawScope.drawStageAndFighters(engine: FightEngine, stage: Stage, tick: Int) {
    val canvasW = size.width
    val canvasH = size.height
    val scaleX = canvasW / engine.arenaWidth
    val scaleY = canvasH / engine.arenaHeight

    // Screen Shake
    val shakeOffset = if (engine.screenShake > 0f) {
        val amp = engine.screenShake
        Offset(
            (sin(tick * 1.5f) * amp).toFloat(),
            (sin(tick * 2.2f) * amp * 0.5f).toFloat()
        )
    } else Offset.Zero

    // 1. Stage Sky Backdrop
    drawRect(
        brush = Brush.verticalGradient(
            colors = listOf(stage.skyTop, stage.skyBottom),
            startY = 0f,
            endY = canvasH * 0.75f
        )
    )

    // Stage Specific Thematic Background Elements (Stages 1 through 9)
    when (stage.id) {
        "stage1" -> { // Kyoto Cherry Shadows (Pagoda & Sakura petals)
            // Distant Moon
            drawCircle(
                color = Color(0xCCFFEEDD),
                center = Offset(canvasW * 0.78f, canvasH * 0.25f),
                radius = 24f * scaleX
            )
            // Pagoda Roof Silhouette
            val path = Path().apply {
                moveTo(canvasW * 0.2f, canvasH * 0.55f)
                lineTo(canvasW * 0.35f, canvasH * 0.38f)
                lineTo(canvasW * 0.5f, canvasH * 0.55f)
                close()
            }
            drawPath(path, color = Color(0x33000000))
            // Falling Sakura Blossom Particles
            for (i in 0 until 8) {
                val sx = ((i * 45f + tick * (1.2f + i * 0.2f)) % canvasW)
                val sy = ((i * 30f + tick * (0.8f + i * 0.1f)) % (canvasH * 0.7f))
                drawCircle(color = Color(0x80FF70A0), center = Offset(sx, sy), radius = 3f * scaleX)
            }
        }
        "stage2" -> { // Neo Tokyo Skyline (Cyber Skyscrapers & Neon Billboards)
            for (i in 0 until 5) {
                val bx = i * (canvasW / 5)
                val bh = (canvasH * 0.3f) + (i % 3) * 35f
                drawRect(
                    color = Color(0x44001428),
                    topLeft = Offset(bx + 4f, canvasH * 0.75f - bh),
                    size = Size(canvasW / 5 - 8f, bh)
                )
                // Glowing Neon signs
                drawRect(
                    color = if (i % 2 == 0) TapoutNeonBlue.copy(alpha = 0.6f) else TapoutCrimson.copy(alpha = 0.6f),
                    topLeft = Offset(bx + 12f, canvasH * 0.75f - bh + 15f),
                    size = Size(14f, 40f)
                )
            }
        }
        "stage3" -> { // Brooklyn Brick Alley (Street Lamps & Brick Texture)
            drawRect(
                color = Color(0x22331400),
                topLeft = Offset(0f, canvasH * 0.2f),
                size = Size(canvasW, canvasH * 0.55f)
            )
            // Warm Street Lamp
            drawCircle(
                brush = Brush.radialGradient(
                    listOf(TapoutOrange.copy(alpha = 0.5f), Color.Transparent),
                    center = Offset(canvasW * 0.85f, canvasH * 0.35f),
                    radius = 50f * scaleX
                ),
                center = Offset(canvasW * 0.85f, canvasH * 0.35f),
                radius = 50f * scaleX
            )
        }
        "stage4" -> { // Imperial Rome Coliseum (Pillars & Torch Fire)
            val pillarCount = 6
            val pillarSpacing = canvasW / (pillarCount - 1)
            for (i in 0 until pillarCount) {
                val px = i * pillarSpacing + shakeOffset.x * 0.3f
                drawRect(
                    color = Color(0x33441020),
                    topLeft = Offset(px - 10f, 20f),
                    size = Size(20f, canvasH * 0.65f)
                )
                val flameAnim = sin(tick * 0.2f + i * 1.3f) * 3f
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(TapoutGold.copy(alpha = 0.6f), TapoutOrange.copy(alpha = 0.2f), Color.Transparent),
                        center = Offset(px, 45f + flameAnim),
                        radius = 22f
                    ),
                    center = Offset(px, 45f + flameAnim),
                    radius = 22f
                )
            }
        }
        "stage5" -> { // Quantum Nexus Void (Time Rift Swirl & Chrono Shards)
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(TapoutNeonPurple.copy(alpha = 0.7f), TapoutNeonBlue.copy(alpha = 0.3f), Color.Transparent),
                    center = Offset(canvasW * 0.5f, canvasH * 0.4f),
                    radius = 90f * scaleX
                ),
                center = Offset(canvasW * 0.5f, canvasH * 0.4f),
                radius = 90f * scaleX
            )
        }
        "stage6" -> { // Crimson Torii Shrine (Layered Torii Gates)
            for (i in 0 until 3) {
                val tScale = 0.5f + i * 0.25f
                val ty = canvasH * 0.35f + i * 25f
                drawLine(
                    color = TapoutBrightRed.copy(alpha = 0.4f + i * 0.2f),
                    start = Offset(canvasW * 0.5f - 60f * tScale, ty),
                    end = Offset(canvasW * 0.5f + 60f * tScale, ty),
                    strokeWidth = 6f * tScale
                )
            }
        }
        "stage7" -> { // Hex Containment Lab (Laser Grid & Cyber Hex)
            for (i in 0 until 6) {
                val lx = i * (canvasW / 5)
                drawLine(
                    color = Color(0x3300FFCC),
                    start = Offset(lx, 0f),
                    end = Offset(lx, canvasH * 0.75f),
                    strokeWidth = 1.5f
                )
            }
        }
        "stage8" -> { // Volcanic Crucible (Molten Lava Falls & Magma Glow)
            drawCircle(
                brush = Brush.radialGradient(
                    listOf(Color(0x80FF4500), Color.Transparent),
                    center = Offset(canvasW * 0.5f, canvasH * 0.65f),
                    radius = 110f * scaleX
                ),
                center = Offset(canvasW * 0.5f, canvasH * 0.65f),
                radius = 110f * scaleX
            )
        }
        "stage9" -> { // Celestial Pantheon (Golden Olympus Summit & Constellations)
            drawCircle(
                brush = Brush.radialGradient(
                    listOf(Color(0x60FFE066), Color.Transparent),
                    center = Offset(canvasW * 0.5f, canvasH * 0.35f),
                    radius = 100f * scaleX
                ),
                center = Offset(canvasW * 0.5f, canvasH * 0.35f),
                radius = 100f * scaleX
            )
        }
    }

    // 2. Arena Floor
    val groundY = 240f * scaleY + shakeOffset.y
    drawRect(
        brush = Brush.verticalGradient(
            colors = listOf(stage.floorColor, Color(0xFF080004)),
            startY = groundY,
            endY = canvasH
        ),
        topLeft = Offset(0f, groundY),
        size = Size(canvasW, canvasH - groundY)
    )

    // Floor Divider Line & Accents
    drawLine(
        color = stage.floorAccent.copy(alpha = 0.75f),
        start = Offset(0f, groundY),
        end = Offset(canvasW, groundY),
        strokeWidth = 2.5f
    )

    for (i in 0 until 9) {
        val gx = i * (canvasW / 8)
        drawLine(
            color = stage.floorAccent.copy(alpha = 0.18f),
            start = Offset(gx, groundY),
            end = Offset(gx + (i - 4) * 14f, canvasH),
            strokeWidth = 1f
        )
    }

    // 3. Draw Fighters
    drawFighterFigure(engine.player, scaleX, scaleY, shakeOffset, tick)
    drawFighterFigure(engine.enemy, scaleX, scaleY, shakeOffset, tick)

    // 4. Draw Special Projectiles
    for (wave in engine.specialWaves) {
        val wx = wave.x * scaleX + shakeOffset.x
        val wy = wave.y * scaleY + shakeOffset.y
        drawCircle(
            brush = Brush.radialGradient(
                listOf(Color.White, wave.color, Color.Transparent),
                center = Offset(wx, wy),
                radius = wave.radius * scaleX * 1.6f
            ),
            center = Offset(wx, wy),
            radius = wave.radius * scaleX * 1.6f
        )
        drawCircle(
            color = Color.White,
            center = Offset(wx, wy),
            radius = wave.radius * scaleX * 0.5f
        )
    }

    // 5. Draw Combat Particles
    for (p in engine.particles) {
        val px = p.x * scaleX + shakeOffset.x
        val py = p.y * scaleY + shakeOffset.y
        drawCircle(
            color = p.color.copy(alpha = p.life.coerceIn(0f, 1f)),
            center = Offset(px, py),
            radius = p.size * p.life
        )
    }
}

fun DrawScope.drawFighterFigure(
    entity: FighterEntity,
    scaleX: Float,
    scaleY: Float,
    shakeOffset: Offset,
    tick: Int
) {
    val fx = entity.x * scaleX + shakeOffset.x
    val fy = entity.y * scaleY + shakeOffset.y
    val dir = if (entity.isFacingRight) 1f else -1f
    val fColor = entity.fighter.themeColor
    val accColor = entity.fighter.accentColor
    val isHit = entity.hurtFlash > 0f || entity.state == ActionState.HURT
    val isSuper = entity.state == ActionState.SPECIAL
    val isKnockedOut = entity.state == ActionState.KO || entity.hp <= 0

    // Ground Shadow Oval
    drawOval(
        color = Color(0x66000000),
        topLeft = Offset(fx - 24f * scaleX, 238f * scaleY - 4f + shakeOffset.y),
        size = Size(48f * scaleX, 10f * scaleY)
    )

    // Super Aura Glow
    if (isSuper) {
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(accColor.copy(alpha = 0.6f), fColor.copy(alpha = 0.2f), Color.Transparent),
                center = Offset(fx, fy - 35f * scaleY),
                radius = 50f * scaleX
            ),
            center = Offset(fx, fy - 35f * scaleY),
            radius = 50f * scaleX
        )
    }

    val bodyColor = when {
        isHit -> Color.White
        isKnockedOut -> Color(0xFF666666)
        else -> fColor
    }

    if (isKnockedOut) {
        drawRoundRect(
            color = bodyColor,
            topLeft = Offset(fx - 30f * scaleX, fy - 14f * scaleY),
            size = Size(60f * scaleX, 14f * scaleY),
            cornerRadius = CornerRadius(6f, 6f)
        )
        drawCircle(
            color = accColor,
            center = Offset(fx + dir * 28f * scaleX, fy - 8f * scaleY),
            radius = 9f * scaleX
        )
        return
    }

    val idleBob = sin(tick * 0.15f + if (entity.isFacingRight) 0f else 2f) * 2.5f * scaleY
    val headY = fy - 62f * scaleY + idleBob
    val torsoY = fy - 48f * scaleY + idleBob

    // Torso Body
    drawRoundRect(
        color = bodyColor,
        topLeft = Offset(fx - 12f * scaleX, torsoY),
        size = Size(24f * scaleX, 32f * scaleY),
        cornerRadius = CornerRadius(6f, 6f)
    )

    // Accent Belt / Chest Insignia
    drawRoundRect(
        color = accColor,
        topLeft = Offset(fx - 10f * scaleX, torsoY + 14f * scaleY),
        size = Size(20f * scaleX, 5f * scaleY),
        cornerRadius = CornerRadius(2f, 2f)
    )

    // Head
    drawCircle(
        color = if (isHit) Color.White else accColor,
        center = Offset(fx + dir * 2f * scaleX, headY),
        radius = 11f * scaleX
    )

    // Visor / Mask / Eyes
    drawLine(
        color = if (entity.isFacingRight) TapoutBrightRed else TapoutNeonBlue,
        start = Offset(fx + dir * 2f * scaleX, headY - 1f),
        end = Offset(fx + dir * 11f * scaleX, headY - 1f),
        strokeWidth = 3f * scaleX
    )

    // Legs
    when (entity.state) {
        ActionState.KICKING -> {
            drawLine(
                color = bodyColor,
                start = Offset(fx, torsoY + 28f * scaleY),
                end = Offset(fx + dir * 36f * scaleX, torsoY + 6f * scaleY),
                strokeWidth = 7f * scaleX
            )
            drawLine(
                color = bodyColor,
                start = Offset(fx - dir * 4f * scaleX, torsoY + 28f * scaleY),
                end = Offset(fx - dir * 6f * scaleX, fy),
                strokeWidth = 6f * scaleX
            )
            drawCircle(
                color = TapoutGold.copy(alpha = 0.7f),
                center = Offset(fx + dir * 36f * scaleX, torsoY + 6f * scaleY),
                radius = 8f * scaleX
            )
        }
        else -> {
            val legAnim = if (entity.state == ActionState.WALKING) sin(tick * 0.3f) * 8f * scaleX else 4f * scaleX
            drawLine(
                color = bodyColor,
                start = Offset(fx - 5f * scaleX, torsoY + 28f * scaleY),
                end = Offset(fx - 8f * scaleX - legAnim, fy),
                strokeWidth = 6f * scaleX
            )
            drawLine(
                color = bodyColor,
                start = Offset(fx + 5f * scaleX, torsoY + 28f * scaleY),
                end = Offset(fx + 8f * scaleX + legAnim, fy),
                strokeWidth = 6f * scaleX
            )
        }
    }

    // Arms
    when (entity.state) {
        ActionState.PUNCHING -> {
            drawLine(
                color = if (isHit) Color.White else accColor,
                start = Offset(fx, torsoY + 8f * scaleY),
                end = Offset(fx + dir * 34f * scaleX, torsoY + 8f * scaleY),
                strokeWidth = 7f * scaleX
            )
            drawCircle(
                color = TapoutBrightRed,
                center = Offset(fx + dir * 34f * scaleX, torsoY + 8f * scaleY),
                radius = 6f * scaleX
            )
        }
        ActionState.BLOCKING -> {
            drawRoundRect(
                color = TapoutNeonBlue.copy(alpha = 0.85f),
                topLeft = Offset(fx + dir * 8f * scaleX, torsoY - 2f * scaleY),
                size = Size(8f * scaleX, 26f * scaleY),
                cornerRadius = CornerRadius(4f, 4f)
            )
            drawArc(
                color = TapoutNeonBlue.copy(alpha = 0.5f),
                startAngle = if (entity.isFacingRight) -60f else 120f,
                sweepAngle = 120f,
                useCenter = false,
                topLeft = Offset(fx + dir * 6f * scaleX, torsoY - 14f * scaleY),
                size = Size(20f * scaleX, 48f * scaleY),
                style = Stroke(width = 3f * scaleX)
            )
        }
        ActionState.SPECIAL -> {
            drawLine(
                color = TapoutGold,
                start = Offset(fx, torsoY + 6f * scaleY),
                end = Offset(fx + dir * 28f * scaleX, torsoY - 4f * scaleY),
                strokeWidth = 7f * scaleX
            )
            drawCircle(
                brush = Brush.radialGradient(
                    listOf(Color.White, TapoutGold, Color.Transparent),
                    center = Offset(fx + dir * 28f * scaleX, torsoY - 4f * scaleY),
                    radius = 16f * scaleX
                ),
                center = Offset(fx + dir * 28f * scaleX, torsoY - 4f * scaleY),
                radius = 16f * scaleX
            )
        }
        else -> {
            drawLine(
                color = bodyColor,
                start = Offset(fx - dir * 4f * scaleX, torsoY + 8f * scaleY),
                end = Offset(fx + dir * 14f * scaleX, torsoY + 12f * scaleY),
                strokeWidth = 5f * scaleX
            )
            drawCircle(
                color = accColor,
                center = Offset(fx + dir * 14f * scaleX, torsoY + 12f * scaleY),
                radius = 4.5f * scaleX
            )
        }
    }
}

@Composable
fun SleekTouchControls(
    engine: FightEngine,
    specialGlow: Float,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 1. LEFT: SLEEK D-PAD
        Box(
            modifier = Modifier
                .size(138.dp)
                .padding(2.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier.size(134.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Row 1: UP
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    horizontalArrangement = Arrangement.Center
                ) {
                    SleekDPadButton(
                        symbol = "▲",
                        tag = "dpad_up",
                        isPressed = engine.inputUp,
                        onStateChanged = { engine.inputUp = it }
                    )
                }

                // Row 2: LEFT, CENTER DOT, RIGHT
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SleekDPadButton(
                        symbol = "◀",
                        tag = "dpad_left",
                        modifier = Modifier.weight(1f),
                        isPressed = engine.inputLeft,
                        onStateChanged = { engine.inputLeft = it }
                    )

                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.Black)
                            .border(1.dp, Color(0x14FFFFFF), RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(TapoutCrimson)
                        )
                    }

                    SleekDPadButton(
                        symbol = "▶",
                        tag = "dpad_right",
                        modifier = Modifier.weight(1f),
                        isPressed = engine.inputRight,
                        onStateChanged = { engine.inputRight = it }
                    )
                }

                // Row 3: DOWN
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    horizontalArrangement = Arrangement.Center
                ) {
                    SleekDPadButton(
                        symbol = "▼",
                        tag = "dpad_down",
                        isPressed = engine.inputDown,
                        onStateChanged = { engine.inputDown = it }
                    )
                }
            }
        }

        // 2. RIGHT: SLEEK ATTACK BUTTONS
        Column(
            modifier = Modifier
                .width(160.dp)
                .height(138.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // PUNCH (P)
                SleekActionButton(
                    label = "PUNCH",
                    letter = "P",
                    tag = "btn_punch",
                    activeColor = TapoutOrange,
                    modifier = Modifier.weight(1f),
                    onTrigger = { engine.onPlayerPunch() }
                )

                // KICK (K)
                SleekActionButton(
                    label = "KICK",
                    letter = "K",
                    tag = "btn_kick",
                    activeColor = TapoutGold,
                    modifier = Modifier.weight(1f),
                    onTrigger = { engine.onPlayerKick() }
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // BLOCK (B)
                SleekHoldButton(
                    label = "BLOCK",
                    letter = "B",
                    tag = "btn_block",
                    activeColor = TapoutNeonBlue,
                    modifier = Modifier.weight(1f),
                    onStateChange = { engine.inputBlock = it }
                )

                // ULTRA / SPECIAL (S)
                val specialReady = engine.player.superMeter >= 100f
                SleekUltraButton(
                    ready = specialReady,
                    glowAlpha = specialGlow,
                    modifier = Modifier.weight(1f),
                    onTrigger = {
                        if (specialReady) engine.onPlayerSpecial()
                    }
                )
            }
        }
    }
}

@Composable
fun SleekDPadButton(
    symbol: String,
    tag: String,
    isPressed: Boolean,
    modifier: Modifier = Modifier.size(38.dp),
    onStateChanged: (Boolean) -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(if (isPressed) TapoutCrimson else SurfaceCard)
            .border(1.dp, if (isPressed) TapoutBrightRed else Color(0x1AFFFFFF), RoundedCornerShape(10.dp))
            .pointerInput(Unit) {
                awaitPointerEventScope {
                    while (true) {
                        val event = awaitPointerEvent()
                        when (event.type) {
                            PointerEventType.Press -> onStateChanged(true)
                            PointerEventType.Release, PointerEventType.Exit -> onStateChanged(false)
                        }
                    }
                }
            }
            .testTag(tag),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = symbol,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = if (isPressed) Color.White else Color(0x66FFFFFF)
        )
    }
}

@Composable
fun SleekActionButton(
    label: String,
    letter: String,
    tag: String,
    activeColor: Color,
    modifier: Modifier = Modifier,
    onTrigger: () -> Unit
) {
    var pressed by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(18.dp))
            .background(if (pressed) activeColor.copy(alpha = 0.45f) else SurfaceCard)
            .border(
                1.dp,
                if (pressed) activeColor else Color(0x1AFFFFFF),
                RoundedCornerShape(18.dp)
            )
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        pressed = true
                        onTrigger()
                        tryAwaitRelease()
                        pressed = false
                    }
                )
            }
            .testTag(tag),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = label,
                fontSize = 8.sp,
                fontWeight = FontWeight.Bold,
                color = if (pressed) activeColor else Color(0x66FFFFFF),
                letterSpacing = 0.5.sp
            )
            Text(
                text = letter,
                fontSize = 17.sp,
                fontWeight = FontWeight.Black,
                fontStyle = FontStyle.Italic,
                color = if (pressed) Color.White else Color.White.copy(alpha = 0.9f)
            )
        }
    }
}

@Composable
fun SleekHoldButton(
    label: String,
    letter: String,
    tag: String,
    activeColor: Color,
    modifier: Modifier = Modifier,
    onStateChange: (Boolean) -> Unit
) {
    var pressed by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(18.dp))
            .background(if (pressed) activeColor.copy(alpha = 0.45f) else SurfaceCard)
            .border(
                1.dp,
                if (pressed) activeColor else Color(0x1AFFFFFF),
                RoundedCornerShape(18.dp)
            )
            .pointerInput(Unit) {
                awaitPointerEventScope {
                    while (true) {
                        val event = awaitPointerEvent()
                        when (event.type) {
                            PointerEventType.Press -> {
                                pressed = true
                                onStateChange(true)
                            }
                            PointerEventType.Release, PointerEventType.Exit -> {
                                pressed = false
                                onStateChange(false)
                            }
                        }
                    }
                }
            }
            .testTag(tag),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = label,
                fontSize = 8.sp,
                fontWeight = FontWeight.Bold,
                color = if (pressed) activeColor else Color(0x66FFFFFF),
                letterSpacing = 0.5.sp
            )
            Text(
                text = letter,
                fontSize = 17.sp,
                fontWeight = FontWeight.Black,
                fontStyle = FontStyle.Italic,
                color = if (pressed) Color.White else Color.White.copy(alpha = 0.9f)
            )
        }
    }
}

@Composable
fun SleekUltraButton(
    ready: Boolean,
    glowAlpha: Float,
    modifier: Modifier = Modifier,
    onTrigger: () -> Unit
) {
    var pressed by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(18.dp))
            .background(
                if (ready) {
                    Brush.verticalGradient(
                        listOf(TapoutCrimson.copy(alpha = glowAlpha), Color(0xFF8A0020).copy(alpha = glowAlpha))
                    )
                } else if (pressed) {
                    SolidColor(TapoutCrimson.copy(alpha = 0.3f))
                } else {
                    SolidColor(SurfaceCard)
                }
            )
            .border(
                width = if (ready) 1.5.dp else 1.dp,
                color = if (ready) TapoutGold else Color(0x1AFFFFFF),
                shape = RoundedCornerShape(18.dp)
            )
            .pointerInput(ready) {
                detectTapGestures(
                    onPress = {
                        if (ready) {
                            pressed = true
                            onTrigger()
                            tryAwaitRelease()
                            pressed = false
                        }
                    }
                )
            }
            .testTag("btn_special"),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = if (ready) "ULTRA" else "SPECIAL",
                fontSize = 8.sp,
                fontWeight = FontWeight.Bold,
                color = if (ready) Color.White.copy(alpha = 0.85f) else Color(0x44FFFFFF),
                letterSpacing = 0.5.sp
            )
            Text(
                text = "S",
                fontSize = 17.sp,
                fontWeight = FontWeight.Black,
                fontStyle = FontStyle.Italic,
                color = if (ready) TapoutGold else Color(0x66FFFFFF)
            )
        }
    }
}

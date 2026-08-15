package com.example.ui.screens

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
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
import com.example.ui.theme.BorderDark
import com.example.ui.theme.HudHpCpuEnd
import com.example.ui.theme.HudHpCpuStart
import com.example.ui.theme.HudHpPlayerEnd
import com.example.ui.theme.HudHpPlayerStart
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.TapoutBrightRed
import com.example.ui.theme.TapoutCrimson
import com.example.ui.theme.TapoutGold
import com.example.ui.theme.TapoutNeonBlue
import com.example.ui.theme.TapoutOrange
import kotlinx.coroutines.delay

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
    var isPaused by remember { mutableStateOf(false) }
    var masterVolume by remember { mutableFloatStateOf(0.8f) }
    var hapticEnabled by remember { mutableStateOf(true) }

    // Dialogue State (Story Mode Message Window)
    var showDialogueWindow by remember { mutableStateOf(true) }
    var dialogueStep by remember { mutableIntStateOf(0) }
    var currentTypedText by remember { mutableStateOf("") }
    var isTyping by remember { mutableStateOf(false) }

    // Final Smash Cinematic State
    var showSmashCinematic by remember { mutableStateOf(false) }

    val dialogueSequence = remember(playerFighter, enemyFighter) {
        listOf(
            Triple(playerFighter.name, "${playerFighter.spiritAnimalSymbol} ${playerFighter.title}", playerFighter.dialoguePrologue),
            Triple(enemyFighter.name, "${enemyFighter.spiritAnimalSymbol} ${enemyFighter.title}", playerFighter.dialogueRivalResponse),
            Triple(playerFighter.name, "${playerFighter.spiritAnimalSymbol} ${playerFighter.title}", playerFighter.dialogueVsRival),
            Triple("SYSTEM", "⚡ TOURNAMENT ARBITER", "Fighters in position! Battle begins now!")
        )
    }

    // Typewriter effect for dialogue
    LaunchedEffect(dialogueStep, showDialogueWindow) {
        if (showDialogueWindow && dialogueStep < dialogueSequence.size) {
            val targetText = dialogueSequence[dialogueStep].third
            currentTypedText = ""
            isTyping = true
            for (char in targetText) {
                currentTypedText += char
                delay(20)
            }
            isTyping = false
        }
    }

    // Game loop at 60fps (paused during dialogue or pause menu)
    LaunchedEffect(engine, isPaused, showDialogueWindow, showSmashCinematic) {
        var lastNanos = System.nanoTime()
        while (!engine.isMatchOver) {
            withFrameNanos { now ->
                val dt = ((now - lastNanos) / 1_000_000_000f).coerceIn(0.001f, 0.05f)
                lastNanos = now
                if (!isPaused && !showDialogueWindow && !showSmashCinematic) {
                    engine.update(dt)
                    frameTick++
                }
            }
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "specialPulse")
    val smashPulse by infiniteTransition.animateFloat(
        initialValue = 0.7f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "smashPulse"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundDark)
            .testTag("fight_screen_container")
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // TOP HUD (Header with Pause / Settings button)
            SleekFightHud(
                engine = engine,
                stage = stage,
                onPause = { isPaused = true },
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

                // Combo Counter Overlay
                RealtimeComboOverlay(
                    comboCount = engine.playerCombo,
                    comboTier = engine.comboTier,
                    displayActive = engine.playerCombo > 1 && !engine.isMatchOver,
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(start = 12.dp, top = 6.dp)
                )

                // Story Message Window Dialogue
                if (showDialogueWindow && dialogueStep < dialogueSequence.size) {
                    val (speaker, title, text) = dialogueSequence[dialogueStep]
                    StoryDialogueWindow(
                        speakerName = speaker,
                        speakerTitle = title,
                        dialogueText = if (isTyping) currentTypedText else text,
                        isTyping = isTyping,
                        onAdvance = {
                            if (isTyping) {
                                currentTypedText = text
                                isTyping = false
                            } else {
                                soundEngine?.playSelect()
                                if (dialogueStep + 1 < dialogueSequence.size) {
                                    dialogueStep++
                                } else {
                                    showDialogueWindow = false
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth(0.94f)
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 12.dp)
                    )
                }
            }

            // BOTTOM ACTION CONTROLS (TCL 350x550 VIEWPORT)
            BottomCombatBar(
                engine = engine,
                playerFighter = playerFighter,
                smashPulse = smashPulse,
                onMoveAhead = {
                    // Round 30px Right Arrow Button on Bottom Left (No up/down/left needed)
                    soundEngine?.playSelect()
                    if (showDialogueWindow) {
                        if (isTyping) {
                            currentTypedText = dialogueSequence[dialogueStep].third
                            isTyping = false
                        } else if (dialogueStep + 1 < dialogueSequence.size) {
                            dialogueStep++
                        } else {
                            showDialogueWindow = false
                        }
                    } else {
                        // Forward attack dash / step forward
                        engine.onPlayerPunch()
                    }
                },
                onFinalSmash = {
                    // Spirit Animal Final Blow Melee Smash Attack Button on Bottom Right
                    if (engine.player.superMeter >= 100f && !showSmashCinematic) {
                        showSmashCinematic = true
                        soundEngine?.playSpecial()
                        soundEngine?.vibrate(25, 120)
                        engine.onPlayerSpecial()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
                    .background(SurfaceCard)
                    .border(1.dp, BorderDark, RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            )
        }

        // PAUSE & SETTINGS MODAL OVERLAY
        if (isPaused) {
            PauseSettingsModal(
                masterVolume = masterVolume,
                soundActive = soundActive,
                hapticEnabled = hapticEnabled,
                onVolumeChange = { masterVolume = it },
                onToggleSound = { soundActive = soundEngine?.toggleSound() ?: true },
                onToggleHaptic = { hapticEnabled = it },
                onResume = { isPaused = false },
                onRestart = {
                    isPaused = false
                    engine.player.hp = engine.player.maxHp
                    engine.enemy.hp = engine.enemy.maxHp
                    engine.player.superMeter = 100f
                },
                onQuit = {
                    isPaused = false
                    onExitToSelect()
                }
            )
        }

        // FINAL BLOW SPIRIT ANIMAL SMASH CINEMATIC MODAL
        if (showSmashCinematic) {
            FinalBlowSmashCinematicOverlay(
                fighter = playerFighter,
                onFinished = { showSmashCinematic = false }
            )
        }
    }
}

@Composable
fun SleekFightHud(
    engine: FightEngine,
    stage: Stage,
    onPause: () -> Unit,
    soundActive: Boolean,
    onToggleSound: () -> Unit
) {
    val playerHpRatio = (engine.player.hp.toFloat() / engine.player.maxHp.toFloat().coerceAtLeast(1f)).coerceIn(0f, 1f)
    val enemyHpRatio = (engine.enemy.hp.toFloat() / engine.enemy.maxHp.toFloat().coerceAtLeast(1f)).coerceIn(0f, 1f)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(SurfaceCard)
            .padding(horizontal = 8.dp, vertical = 6.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        // TOP LINE: Fighter Names, Timer & Pause
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Player Tag & Element
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = engine.playerFighter.avatarCode,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black,
                    color = engine.playerFighter.accentColor
                )
                Text(
                    text = "${engine.playerFighter.elementSymbol} ${engine.playerFighter.name}",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            // Round Timer & Pause Button
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xFF220010))
                        .border(1.dp, TapoutGold, RoundedCornerShape(6.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "${engine.roundTimeSeconds.toInt()}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace,
                        color = if (engine.roundTimeSeconds < 10) TapoutBrightRed else TapoutGold
                    )
                }

                IconButton(
                    onClick = onPause,
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF1E0012))
                        .border(1.dp, BorderDark, CircleShape)
                        .testTag("pause_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Pause / Settings",
                        tint = TapoutGold,
                        modifier = Modifier.size(15.dp)
                    )
                }
            }

            // Enemy Tag & Element
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "${engine.enemyFighter.name} ${engine.enemyFighter.elementSymbol}",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = engine.enemyFighter.avatarCode,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black,
                    color = engine.enemyFighter.accentColor
                )
            }
        }

        // HEALTH BARS
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Player HP
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(10.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color(0xFF240010))
                    .border(1.dp, Color(0x33FFFFFF), RoundedCornerShape(4.dp))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(playerHpRatio)
                        .height(10.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(
                            Brush.horizontalGradient(listOf(HudHpPlayerStart, HudHpPlayerEnd))
                        )
                )
            }

            // VS Emblem
            Text(
                text = "VS",
                fontSize = 8.sp,
                fontWeight = FontWeight.Black,
                color = TapoutGold
            )

            // Enemy HP
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(10.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color(0xFF240010))
                    .border(1.dp, Color(0x33FFFFFF), RoundedCornerShape(4.dp))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(enemyHpRatio)
                        .height(10.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(
                            Brush.horizontalGradient(listOf(HudHpCpuEnd, HudHpCpuStart))
                        )
                )
            }
        }

        // SUPER METER & SPIRIT ANIMAL STATUS BAR
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "P1 SPIRIT: ${engine.playerFighter.spiritAnimalSymbol} ${engine.playerFighter.spiritAnimal}",
                fontSize = 7.5.sp,
                fontWeight = FontWeight.Bold,
                color = TapoutGold
            )
            Text(
                text = "SUPER ${(engine.player.superMeter).toInt()}%",
                fontSize = 7.5.sp,
                fontWeight = FontWeight.Black,
                fontFamily = FontFamily.Monospace,
                color = if (engine.player.superMeter >= 100f) TapoutGold else TapoutNeonBlue
            )
            Text(
                text = "RIVAL: ${engine.enemyFighter.spiritAnimalSymbol} ${engine.enemyFighter.spiritAnimal}",
                fontSize = 7.5.sp,
                fontWeight = FontWeight.Bold,
                color = TapoutCrimson
            )
        }
    }
}

@Composable
fun RealtimeComboOverlay(
    comboCount: Int,
    comboTier: ComboTier,
    displayActive: Boolean,
    modifier: Modifier = Modifier
) {
    if (!displayActive || comboCount <= 1) return

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xCC000000))
            .border(1.dp, TapoutGold, RoundedCornerShape(8.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = "$comboCount HITS!",
                fontSize = 12.sp,
                fontWeight = FontWeight.Black,
                fontFamily = FontFamily.Monospace,
                color = TapoutGold
            )
            Text(
                text = comboTier.name,
                fontSize = 8.sp,
                fontWeight = FontWeight.Bold,
                color = TapoutBrightRed
            )
        }
    }
}

fun DrawScope.drawStageAndFighters(
    engine: FightEngine,
    stage: Stage,
    frameTick: Int
) {
    val w = size.width
    val h = size.height

    // 1. Stage Background
    drawRect(
        brush = Brush.verticalGradient(
            listOf(stage.skyTop, stage.skyBottom)
        )
    )

    // Floor
    val floorY = h * 0.72f
    drawRect(
        brush = Brush.verticalGradient(
            listOf(stage.floorColor, stage.floorColor.copy(alpha = 0.8f))
        ),
        topLeft = Offset(0f, floorY),
        size = Size(w, h - floorY)
    )
    drawLine(
        color = stage.floorAccent,
        start = Offset(0f, floorY),
        end = Offset(w, floorY),
        strokeWidth = 3f
    )

    // 2. Draw Fighters
    drawFighterEntity(engine.player, engine.playerFighter, isPlayer = true, floorY = floorY, frameTick = frameTick)
    drawFighterEntity(engine.enemy, engine.enemyFighter, isPlayer = false, floorY = floorY, frameTick = frameTick)
}

fun DrawScope.drawFighterEntity(
    entity: FighterEntity,
    fighter: Fighter,
    isPlayer: Boolean,
    floorY: Float,
    frameTick: Int
) {
    val x = entity.x
    val y = floorY - 50f + (entity.y - entity.groundY)
    val width = 28f
    val height = 48f

    // Shadow
    drawOval(
        color = Color(0x66000000),
        topLeft = Offset(x - 18f, floorY - 4f),
        size = Size(36f, 8f)
    )

    // Body
    val bodyColor = if (entity.hurtFlash > 0f) Color.White else fighter.themeColor
    drawRoundRect(
        color = bodyColor,
        topLeft = Offset(x - width / 2f, y - height / 2f),
        size = Size(width, height),
        cornerRadius = CornerRadius(6f, 6f)
    )

    // Head
    drawCircle(
        color = fighter.accentColor,
        center = Offset(x, y - height / 2f - 10f),
        radius = 10f
    )

    // Elemental Aura Glow
    drawCircle(
        color = fighter.themeColor.copy(alpha = 0.35f),
        center = Offset(x, y),
        radius = 24f,
        style = Stroke(width = 2f)
    )

    // Attack punch / kick limb extension
    if (entity.state == ActionState.PUNCHING) {
        val reach = if (isPlayer) 22f else -22f
        drawLine(
            color = TapoutGold,
            start = Offset(x, y - 6f),
            end = Offset(x + reach, y - 6f),
            strokeWidth = 5f
        )
    } else if (entity.state == ActionState.KICKING) {
        val reach = if (isPlayer) 24f else -24f
        drawLine(
            color = TapoutBrightRed,
            start = Offset(x, y + 10f),
            end = Offset(x + reach, y + 6f),
            strokeWidth = 6f
        )
    }
}

@Composable
fun StoryDialogueWindow(
    speakerName: String,
    speakerTitle: String,
    dialogueText: String,
    isTyping: Boolean,
    onAdvance: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xF0100010))
            .border(1.5.dp, TapoutGold, RoundedCornerShape(12.dp))
            .shadow(elevation = 12.dp, shape = RoundedCornerShape(12.dp), spotColor = TapoutGold)
            .pointerInput(Unit) {
                detectTapGestures { onAdvance() }
            }
            .padding(10.dp)
            .testTag("story_dialogue_window")
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = speakerName,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Monospace,
                    color = TapoutGold
                )
                Text(
                    text = speakerTitle,
                    fontSize = 7.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = TapoutNeonBlue
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color(0x55000000))
                    .padding(8.dp)
            ) {
                Text(
                    text = dialogueText,
                    fontSize = 9.sp,
                    lineHeight = 13.sp,
                    color = Color.White
                )
            }

            Text(
                text = if (isTyping) "TAP TO SKIP ▶" else "TAP TO ADVANCE ▶",
                fontSize = 7.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                color = TapoutGold,
                textAlign = TextAlign.End,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
fun BottomCombatBar(
    engine: FightEngine,
    playerFighter: Fighter,
    smashPulse: Float,
    onMoveAhead: () -> Unit,
    onFinalSmash: () -> Unit,
    modifier: Modifier = Modifier
) {
    val smashReady = engine.player.superMeter >= 100f

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // BOTTOM LEFT: Round 30px Right Arrow Button to Move Ahead (No up/down/left needed)
        Box(
            modifier = Modifier
                .size(30.dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(Color(0xFF24001A), Color(0xFF0A0008))
                    )
                )
                .border(1.5.dp, TapoutNeonBlue, CircleShape)
                .shadow(elevation = 6.dp, shape = CircleShape, spotColor = TapoutNeonBlue, ambientColor = TapoutNeonBlue)
                .pointerInput(Unit) {
                    detectTapGestures { onMoveAhead() }
                }
                .testTag("btn_move_ahead_right_arrow"),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.ArrowForward,
                contentDescription = "Move Ahead",
                tint = TapoutNeonBlue,
                modifier = Modifier.size(16.dp)
            )
        }

        // CENTER ATTACK BUTTONS (JAB & KICK)
        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF1E0012))
                    .border(1.dp, TapoutOrange, RoundedCornerShape(8.dp))
                .pointerInput(Unit) {
                    detectTapGestures { engine.onPlayerPunch() }
                }
                    .padding(horizontal = 10.dp, vertical = 6.dp)
                    .testTag("btn_punch"),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "👊 JAB",
                    fontSize = 8.5.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    color = Color.White
                )
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF1E0012))
                    .border(1.dp, TapoutGold, RoundedCornerShape(8.dp))
                .pointerInput(Unit) {
                    detectTapGestures { engine.onPlayerKick() }
                }
                    .padding(horizontal = 10.dp, vertical = 6.dp)
                    .testTag("btn_kick"),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "🦵 KICK",
                    fontSize = 8.5.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    color = Color.White
                )
            }
        }

        // BOTTOM RIGHT: Spirit Animal Final Blow Melee Smash Attack Button
        Box(
            modifier = Modifier
                .height(34.dp)
                .clip(RoundedCornerShape(17.dp))
                .background(
                    if (smashReady) {
                        Brush.horizontalGradient(
                            listOf(TapoutBrightRed.copy(alpha = smashPulse), TapoutGold.copy(alpha = smashPulse))
                        )
                    } else {
                        Brush.horizontalGradient(
                            listOf(Color(0xFF330010), Color(0xFF22000A))
                        )
                    }
                )
                .border(
                    width = if (smashReady) 1.5.dp else 1.dp,
                    color = if (smashReady) TapoutGold else Color(0x33FFFFFF),
                    shape = RoundedCornerShape(17.dp)
                )
                .shadow(
                    elevation = if (smashReady) 10.dp else 0.dp,
                    shape = RoundedCornerShape(17.dp),
                    spotColor = TapoutGold,
                    ambientColor = TapoutBrightRed
                )
                .pointerInput(smashReady) {
                    detectTapGestures { onFinalSmash() }
                }
                .padding(horizontal = 8.dp)
                .testTag("btn_final_smash"),
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Text(
                    text = playerFighter.spiritAnimalSymbol,
                    fontSize = 11.sp
                )
                Text(
                    text = if (smashReady) "SPIRIT SMASH" else "CHARGING",
                    fontSize = 7.5.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Monospace,
                    color = if (smashReady) Color.Black else Color.White.copy(alpha = 0.6f)
                )
            }
        }
    }
}

@Composable
fun PauseSettingsModal(
    masterVolume: Float,
    soundActive: Boolean,
    hapticEnabled: Boolean,
    onVolumeChange: (Float) -> Unit,
    onToggleSound: () -> Unit,
    onToggleHaptic: (Boolean) -> Unit,
    onResume: () -> Unit,
    onRestart: () -> Unit,
    onQuit: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xD9000000))
            .pointerInput(Unit) { detectTapGestures { } },
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.88f)
                .clip(RoundedCornerShape(16.dp))
                .background(SurfaceCard)
                .border(1.5.dp, BorderDark, RoundedCornerShape(16.dp))
                .padding(14.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "⚙️ PAUSE & SETTINGS",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Monospace,
                    color = TapoutGold
                )

                // Volume Slider
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Master Volume: ${(masterVolume * 100).toInt()}%",
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Slider(
                        value = masterVolume,
                        onValueChange = onVolumeChange,
                        colors = SliderDefaults.colors(
                            thumbColor = TapoutCrimson,
                            activeTrackColor = TapoutCrimson
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // Sound Toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("8-Bit Sound Effects", fontSize = 8.5.sp, color = Color.White)
                    Switch(
                        checked = soundActive,
                        onCheckedChange = { onToggleSound() },
                        colors = SwitchDefaults.colors(checkedThumbColor = TapoutGold, checkedTrackColor = TapoutCrimson)
                    )
                }

                // Haptic Vibration Toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Haptic Feedback", fontSize = 8.5.sp, color = Color.White)
                    Switch(
                        checked = hapticEnabled,
                        onCheckedChange = onToggleHaptic,
                        colors = SwitchDefaults.colors(checkedThumbColor = TapoutGold, checkedTrackColor = TapoutCrimson)
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Actions
                Button(
                    onClick = onResume,
                    colors = ButtonDefaults.buttonColors(containerColor = TapoutCrimson),
                    modifier = Modifier.fillMaxWidth().height(36.dp)
                ) {
                    Text("RESUME BATTLE ▶", fontSize = 9.sp, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = onRestart,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E0018)),
                    modifier = Modifier.fillMaxWidth().height(34.dp)
                ) {
                    Text("RESTART ROUND 🔄", fontSize = 8.5.sp, color = Color.White)
                }

                Button(
                    onClick = onQuit,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF18000C)),
                    modifier = Modifier.fillMaxWidth().height(34.dp)
                ) {
                    Text("MAIN MENU 🏠", fontSize = 8.5.sp, color = Color.White.copy(alpha = 0.8f))
                }
            }
        }
    }
}

@Composable
fun FinalBlowSmashCinematicOverlay(
    fighter: Fighter,
    onFinished: () -> Unit
) {
    LaunchedEffect(Unit) {
        delay(2200)
        onFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFA000000))
            .pointerInput(Unit) { detectTapGestures { onFinished() } },
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "💥 FINAL BLOW MELEE FINISH 💥",
                fontSize = 11.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 2.sp,
                color = TapoutBrightRed
            )

            Text(
                text = fighter.spiritAnimalSymbol,
                fontSize = 48.sp
            )

            Text(
                text = fighter.finalBlowMeleeSmashName,
                fontSize = 14.sp,
                fontWeight = FontWeight.Black,
                fontFamily = FontFamily.Monospace,
                color = TapoutGold,
                textAlign = TextAlign.Center
            )

            Text(
                text = "${fighter.elementSymbol} ${fighter.elementalState} × ${fighter.spiritAnimal}",
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                color = TapoutNeonBlue
            )

            Text(
                text = "\"${fighter.finalBlowSmashQuote}\"",
                fontSize = 9.sp,
                fontStyle = FontStyle.Italic,
                color = Color.White,
                textAlign = TextAlign.Center
            )
        }
    }
}

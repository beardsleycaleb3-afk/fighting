package com.example.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Landscape
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.SportsMma
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults.SecondaryIndicator
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.audio.SoundEffectsEngine
import com.example.model.Fighter
import com.example.model.PersonalityTrait
import com.example.model.Roster
import com.example.model.Stage
import com.example.model.StageRoster
import com.example.ui.theme.BackgroundDark
import com.example.ui.theme.BorderDark
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
fun SelectScreen(
    soundEngine: SoundEffectsEngine?,
    onChampionChosen: (playerFighter: Fighter, enemyFighter: Fighter, stage: Stage) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedFighter by remember { mutableStateOf(Roster.fighters[0]) }
    var selectedStage by remember { mutableStateOf(StageRoster.getStageById(Roster.fighters[0].homeStageId)) }
    var selectedTab by remember { mutableIntStateOf(0) } // 0: ROSTER & STATS, 1: ANIMATED PORTRAIT & STORY DOSSIER, 2: STAGES (1-9)
    var soundActive by remember { mutableStateOf(soundEngine?.isSoundEnabled() ?: true) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundDark)
            .padding(horizontal = 10.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Sleek Top Navigation & Sound Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "TIME TOURNAMENT",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 1.5.sp,
                    color = TapoutCrimson
                )
                Text(
                    text = "CHAMPION DOSSIER & BATTLEGROUNDS",
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Medium,
                    letterSpacing = 0.5.sp,
                    color = Color(0x99FFFFFF)
                )
            }

            IconButton(
                onClick = {
                    val res = soundEngine?.toggleSound() ?: true
                    soundActive = res
                },
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(SurfaceCard)
                    .border(1.dp, BorderDark, CircleShape)
                    .testTag("sound_toggle_button")
            ) {
                Icon(
                    imageVector = if (soundActive) Icons.Default.VolumeUp else Icons.Default.VolumeOff,
                    contentDescription = "Toggle Sound",
                    tint = if (soundActive) TapoutOrange else Color(0x66FFFFFF),
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Tab Navigation Header
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = SurfaceCard,
            contentColor = TapoutGold,
            indicator = { tabPositions ->
                SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                    color = TapoutCrimson,
                    height = 2.5.dp
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .border(1.dp, BorderDark, RoundedCornerShape(10.dp))
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = {
                    selectedTab = 0
                    soundEngine?.playSelect()
                },
                text = { Text("ROSTER", fontSize = 9.5.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace) },
                icon = { Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(13.dp)) }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = {
                    selectedTab = 1
                    soundEngine?.playSelect()
                },
                text = { Text("DOSSIER & LORE", fontSize = 9.5.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace) },
                icon = { Icon(Icons.Default.AutoStories, contentDescription = null, modifier = Modifier.size(13.dp)) }
            )
            Tab(
                selected = selectedTab == 2,
                onClick = {
                    selectedTab = 2
                    soundEngine?.playSelect()
                },
                text = { Text("STAGES (1-9)", fontSize = 9.5.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace) },
                icon = { Icon(Icons.Default.Landscape, contentDescription = null, modifier = Modifier.size(13.dp)) }
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Main Tab Content Area
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            when (selectedTab) {
                0 -> {
                    // TAB 0: ROSTER GRID & QUICK STATS
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Horizontal Roster Selector
                        LazyRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            items(Roster.fighters) { f ->
                                CompactFighterPill(
                                    fighter = f,
                                    isSelected = selectedFighter.id == f.id,
                                    onSelect = {
                                        selectedFighter = f
                                        selectedStage = StageRoster.getStageById(f.homeStageId)
                                        soundEngine?.playSelect()
                                    }
                                )
                            }
                        }

                        // Selected Fighter Summary with Mini Animated Portrait & Stats
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                                .clip(RoundedCornerShape(14.dp))
                                .background(SurfaceCard)
                                .border(1.dp, selectedFighter.themeColor.copy(alpha = 0.5f), RoundedCornerShape(14.dp))
                                .padding(10.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxSize(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                // Left Column: Mini Animated Portrait Canvas
                                Box(
                                    modifier = Modifier
                                        .width(115.dp)
                                        .fillMaxSize()
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(Color(0xFF0C0008))
                                        .border(1.dp, selectedFighter.themeColor.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                                ) {
                                    AnimatedFighterPortrait(
                                        fighter = selectedFighter,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                    // Era Tag Overlay
                                    Box(
                                        modifier = Modifier
                                            .align(Alignment.BottomCenter)
                                            .padding(bottom = 4.dp)
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(Color(0xCC000000))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = selectedFighter.avatarCode,
                                            fontSize = 8.sp,
                                            fontWeight = FontWeight.Black,
                                            color = selectedFighter.accentColor
                                        )
                                    }
                                }

                                // Right Column: Bio Brief & Stats Meters
                                Column(
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxSize(),
                                    verticalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Text(
                                            text = selectedFighter.name,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Black,
                                            fontFamily = FontFamily.Monospace,
                                            color = selectedFighter.accentColor
                                        )
                                        Text(
                                            text = selectedFighter.title,
                                            fontSize = 8.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = TapoutGold
                                        )
                                        Text(
                                            text = selectedFighter.era,
                                            fontSize = 7.5.sp,
                                            color = Color(0x99FFFFFF)
                                        )

                                        Spacer(modifier = Modifier.height(4.dp))

                                        Text(
                                            text = "ULTRA: ${selectedFighter.specialName}",
                                            fontSize = 8.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            fontFamily = FontFamily.Monospace,
                                            color = TapoutGold
                                        )

                                        Text(
                                            text = "\"${selectedFighter.introQuote}\"",
                                            fontSize = 8.sp,
                                            fontStyle = FontStyle.Italic,
                                            color = Color(0xB3FFFFFF),
                                            lineHeight = 11.sp,
                                            maxLines = 2,
                                            modifier = Modifier.padding(top = 2.dp)
                                        )
                                    }

                                    // Stat Bars
                                    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                                        StatBar(label = "PWR", value = selectedFighter.statPower, icon = Icons.Default.SportsMma, color = TapoutBrightRed)
                                        StatBar(label = "SPD", value = selectedFighter.statSpeed, icon = Icons.Default.Speed, color = TapoutGold)
                                        StatBar(label = "DEF", value = selectedFighter.statDefense, icon = Icons.Default.Shield, color = selectedFighter.themeColor)
                                        StatBar(label = "SPC", value = selectedFighter.statSpecial, icon = Icons.Default.Bolt, color = TapoutOrange)
                                    }
                                }
                            }
                        }
                    }
                }

                1 -> {
                    // TAB 1: FULL CHARACTER DOSSIER & ANIMATED PORTRAIT (Storyline, Traits, Lore)
                    CharacterDossierDetailView(
                        fighter = selectedFighter,
                        soundEngine = soundEngine,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                2 -> {
                    // TAB 2: STAGES SELECT (Stages 1 through 9)
                    val stageScrollState = rememberScrollState()
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(stageScrollState),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        StageRoster.stages.forEach { st ->
                            StageCardItem(
                                stage = st,
                                isSelected = selectedStage.id == st.id,
                                onSelect = {
                                    selectedStage = st
                                    soundEngine?.playSelect()
                                }
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Selected Summary Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(SurfaceCard)
                .padding(horizontal = 10.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "P1: ${selectedFighter.name}",
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                color = selectedFighter.accentColor
            )
            Text(
                text = "STAGE ${selectedStage.stageNumber}: ${selectedStage.name}",
                fontSize = 8.sp,
                fontWeight = FontWeight.Medium,
                color = selectedStage.floorAccent
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Confirm & Enter Coliseum Button
        Button(
            onClick = {
                soundEngine?.playSelect()
                val otherFighters = Roster.fighters.filter { it.id != selectedFighter.id }
                val opponent = otherFighters.random()
                onChampionChosen(selectedFighter, opponent, selectedStage)
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(44.dp)
                .clip(RoundedCornerShape(12.dp))
                .shadow(elevation = 10.dp, shape = RoundedCornerShape(12.dp), ambientColor = TapoutCrimson, spotColor = TapoutBrightRed)
                .testTag("enter_coliseum_button"),
            colors = ButtonDefaults.buttonColors(
                containerColor = TapoutCrimson,
                contentColor = Color.White
            )
        ) {
            Text(
                text = "ENTER ARENA →",
                fontSize = 13.sp,
                fontWeight = FontWeight.Black,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 2.sp
            )
        }
    }
}

@Composable
fun CharacterDossierDetailView(
    fighter: Fighter,
    soundEngine: SoundEffectsEngine?,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    var dossierTab by remember { mutableIntStateOf(0) } // 0: NARRATIVE STORYLINE, 1: PERSONALITY TRAITS, 2: COMBAT STYLE & ULTRA

    Column(
        modifier = modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(14.dp))
            .background(SurfaceCard)
            .border(1.dp, fighter.themeColor.copy(alpha = 0.5f), RoundedCornerShape(14.dp))
            .padding(10.dp)
    ) {
        // TOP SECTION: HERO ANIMATED PORTRAIT CANVAS + TITLE
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(130.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Animated Portrait Canvas
            Box(
                modifier = Modifier
                    .width(110.dp)
                    .fillMaxSize()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF080006))
                    .border(1.5.dp, fighter.themeColor, RoundedCornerShape(12.dp))
                    .testTag("animated_portrait_canvas")
            ) {
                AnimatedFighterPortrait(
                    fighter = fighter,
                    modifier = Modifier.fillMaxSize()
                )
            }

            // Hero Header Info
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = fighter.avatarCode,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            color = fighter.accentColor
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color(0x33000000))
                                .border(1.dp, Color(0x22FFFFFF), RoundedCornerShape(4.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "HP: ${fighter.maxHp}",
                                fontSize = 7.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = TapoutGold
                            )
                        }
                    }

                    Text(
                        text = fighter.name,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace,
                        color = Color.White
                    )
                    Text(
                        text = fighter.title,
                        fontSize = 8.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = fighter.accentColor
                    )
                    Text(
                        text = fighter.era,
                        fontSize = 7.5.sp,
                        color = Color(0x99FFFFFF),
                        modifier = Modifier.padding(top = 1.dp)
                    )
                }

                // Rivalry Callout
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0x2A000000))
                        .padding(horizontal = 6.dp, vertical = 3.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("RIVAL: ", fontSize = 7.5.sp, fontWeight = FontWeight.Bold, color = TapoutCrimson)
                        Text(fighter.rivalFighterName, fontSize = 8.sp, fontWeight = FontWeight.Black, color = Color.White)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Dossier Inner Sub-Tabs
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            DossierSubTabButton(
                label = "STORYLINE",
                isSelected = dossierTab == 0,
                modifier = Modifier.weight(1f),
                onClick = {
                    dossierTab = 0
                    soundEngine?.playSelect()
                }
            )
            DossierSubTabButton(
                label = "TRAITS",
                isSelected = dossierTab == 1,
                modifier = Modifier.weight(1f),
                onClick = {
                    dossierTab = 1
                    soundEngine?.playSelect()
                }
            )
            DossierSubTabButton(
                label = "COMBAT LORE",
                isSelected = dossierTab == 2,
                modifier = Modifier.weight(1f),
                onClick = {
                    dossierTab = 2
                    soundEngine?.playSelect()
                }
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Dossier Inner Scrollable Details
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .verticalScroll(scrollState)
        ) {
            when (dossierTab) {
                0 -> {
                    // SUB-TAB 0: TOURNAMENT NARRATIVE & STORYLINE
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        NarrativeCard(
                            title = "TIMELINE ORIGIN",
                            content = fighter.timelineOrigin,
                            headerColor = TapoutGold
                        )
                        NarrativeCard(
                            title = "BACKGROUND BIOGRAPHY",
                            content = fighter.bio,
                            headerColor = fighter.accentColor
                        )
                        NarrativeCard(
                            title = "TOURNAMENT STORY ARC",
                            content = fighter.storyArc,
                            headerColor = TapoutCrimson
                        )
                        NarrativeCard(
                            title = "COMBAT PHILOSOPHY",
                            content = "\"${fighter.combatPhilosophy}\"",
                            headerColor = TapoutNeonBlue
                        )
                    }
                }

                1 -> {
                    // SUB-TAB 1: PERSONALITY TRAITS & TEMPERAMENT
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "CORE PERSONALITY MATRIX",
                            fontSize = 8.5.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 1.sp,
                            color = TapoutGold
                        )
                        fighter.traits.forEach { trait ->
                            PersonalityTraitCard(trait = trait)
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        // Voice Callouts
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0x22000000))
                                .padding(8.dp)
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text("CHARACTER VOICE LOGS", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = TapoutGold)
                                VoiceLineRow(type = "INTRO", quote = fighter.introQuote)
                                VoiceLineRow(type = "TAUNT", quote = fighter.tauntQuote)
                                VoiceLineRow(type = "SPECIAL", quote = fighter.specialQuote)
                                VoiceLineRow(type = "VICTORY", quote = fighter.victoryQuote)
                            }
                        }
                    }
                }

                2 -> {
                    // SUB-TAB 2: COMBAT STYLE, GEAR & ULTRA MOVE
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        NarrativeCard(
                            title = "MARTIAL ARTS DISCIPLINE",
                            content = "${fighter.style} • Master Level",
                            headerColor = TapoutNeonPurple
                        )
                        NarrativeCard(
                            title = "SIGNATURE ULTRA: ${fighter.specialName}",
                            content = fighter.signatureMoveDescription,
                            headerColor = TapoutBrightRed
                        )
                        NarrativeCard(
                            title = "EQUIPMENT & WEAPONRY",
                            content = fighter.weaponEquipment,
                            headerColor = TapoutOrange
                        )

                        // Stats Summary
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0x22000000))
                                .padding(8.dp)
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                                Text("COMBAT ATTRIBUTES", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = TapoutGold)
                                StatBar(label = "POWER", value = fighter.statPower, icon = Icons.Default.SportsMma, color = TapoutBrightRed)
                                StatBar(label = "SPEED", value = fighter.statSpeed, icon = Icons.Default.Speed, color = TapoutGold)
                                StatBar(label = "DEFENSE", value = fighter.statDefense, icon = Icons.Default.Shield, color = fighter.themeColor)
                                StatBar(label = "SPECIAL", value = fighter.statSpecial, icon = Icons.Default.Bolt, color = TapoutOrange)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AnimatedFighterPortrait(
    fighter: Fighter,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "portraitAnim")

    // Breathing / idle bob animation
    val bobOffset by infiniteTransition.animateFloat(
        initialValue = -3f,
        targetValue = 3f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "portraitBob"
    )

    // Energy pulse aura
    val auraScale by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "auraScale"
    )

    // Eye gleam sweep
    val gleamPos by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "gleamPos"
    )

    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val cx = w / 2f
        val cy = h / 2f + bobOffset

        // 1. Dynamic Radial Aura Background
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(fighter.themeColor.copy(alpha = 0.45f * auraScale), Color.Transparent),
                center = Offset(cx, cy),
                radius = w * 0.55f * auraScale
            ),
            center = Offset(cx, cy),
            radius = w * 0.55f * auraScale
        )

        // 2. Character Specific Background Particle Effects
        when (fighter.id) {
            "ninja" -> {
                // Swirling Void Kunai / Shadow Smoke
                for (i in 0 until 5) {
                    val angle = (gleamPos * 360f + i * 72f) * (Math.PI / 180f)
                    val px = cx + (cos(angle) * 32f).toFloat()
                    val py = cy + (sin(angle) * 22f).toFloat()
                    drawCircle(color = fighter.accentColor.copy(alpha = 0.6f), center = Offset(px, py), radius = 2.5f)
                }
            }
            "mma" -> {
                // Cyber Grid Lines
                for (i in 0 until 4) {
                    val gy = h * 0.2f + i * (h * 0.18f)
                    drawLine(
                        color = TapoutBrightRed.copy(alpha = 0.25f),
                        start = Offset(0f, gy),
                        end = Offset(w, gy),
                        strokeWidth = 1f
                    )
                }
            }
            "boxer" -> {
                // Lightning Ring Arcs
                drawArc(
                    color = TapoutNeonBlue.copy(alpha = 0.5f),
                    startAngle = gleamPos * 360f,
                    sweepAngle = 90f,
                    useCenter = false,
                    topLeft = Offset(cx - 36f, cy - 36f),
                    size = Size(72f, 72f),
                    style = Stroke(width = 2f)
                )
            }
            "wrestler" -> {
                // Roman Golden Laurel Rays
                for (i in 0 until 6) {
                    val rad = (i * 60f) * (Math.PI / 180f)
                    drawLine(
                        color = TapoutGold.copy(alpha = 0.35f),
                        start = Offset(cx, cy),
                        end = Offset(cx + (cos(rad) * 45f).toFloat(), cy + (sin(rad) * 45f).toFloat()),
                        strokeWidth = 1.5f
                    )
                }
            }
            "valkyrie" -> {
                // Asgardian Plasma Ring
                drawCircle(
                    color = Color(0xFF00E5FF).copy(alpha = 0.4f),
                    center = Offset(cx, cy),
                    radius = 38f * auraScale,
                    style = Stroke(width = 1.5f)
                )
            }
            "warlord" -> {
                // Volcanic Magma Sparks
                for (i in 0 until 6) {
                    val sy = h - ((gleamPos * 100f + i * 18f) % h)
                    val sx = cx + ((i - 2.5f) * 14f)
                    drawCircle(color = TapoutOrange.copy(alpha = 0.7f), center = Offset(sx, sy), radius = 2f)
                }
            }
        }

        // 3. Fighter Silhouette Figure
        val headY = cy - 26f
        val chestY = cy - 6f
        val shoulderW = 28f

        // Shoulders / Torso
        drawRoundRect(
            color = fighter.themeColor,
            topLeft = Offset(cx - shoulderW, chestY),
            size = Size(shoulderW * 2f, 48f),
            cornerRadius = CornerRadius(8f, 8f)
        )

        // Chest Plate / Accents
        drawRoundRect(
            color = fighter.accentColor,
            topLeft = Offset(cx - shoulderW * 0.7f, chestY + 12f),
            size = Size(shoulderW * 1.4f, 8f),
            cornerRadius = CornerRadius(4f, 4f)
        )

        // Collar / Neck
        drawRect(
            color = fighter.themeColor,
            topLeft = Offset(cx - 8f, headY + 14f),
            size = Size(16f, 10f)
        )

        // Head Base
        drawCircle(
            color = fighter.accentColor,
            center = Offset(cx, headY),
            radius = 18f
        )

        // Mask / Headgear
        drawRoundRect(
            color = fighter.themeColor,
            topLeft = Offset(cx - 16f, headY - 14f),
            size = Size(32f, 18f),
            cornerRadius = CornerRadius(6f, 6f)
        )

        // Glowing Visor / Eyes
        val eyeGlow = Brush.horizontalGradient(
            listOf(Color.White, fighter.accentColor, Color.White),
            startX = cx - 12f + (gleamPos * 10f),
            endX = cx + 12f
        )
        drawLine(
            brush = eyeGlow,
            start = Offset(cx - 10f, headY - 1f),
            end = Offset(cx + 10f, headY - 1f),
            strokeWidth = 3.5f
        )

        // Fighter Emblem / Belt Core
        drawCircle(
            color = TapoutGold,
            center = Offset(cx, chestY + 36f),
            radius = 5.5f
        )
    }
}

@Composable
fun PersonalityTraitCard(trait: PersonalityTrait) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0x22000000))
            .border(1.dp, trait.badgeColor.copy(alpha = 0.35f), RoundedCornerShape(8.dp))
            .padding(7.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(trait.badgeColor)
                    )
                    Text(
                        text = trait.name.uppercase(),
                        fontSize = 8.5.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace,
                        color = trait.badgeColor
                    )
                }
                Text(
                    text = "${(trait.ratingPct * 100).toInt()}% MASTERY",
                    fontSize = 7.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0x99FFFFFF)
                )
            }

            Text(
                text = trait.description,
                fontSize = 8.sp,
                color = Color(0xCCFFFFFF),
                lineHeight = 11.sp
            )

            // Mastery bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(3.dp)
                    .clip(RoundedCornerShape(50))
                    .background(Color(0xFF14000C))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(trait.ratingPct)
                        .height(3.dp)
                        .clip(RoundedCornerShape(50))
                        .background(trait.badgeColor)
                )
            }
        }
    }
}

@Composable
fun NarrativeCard(
    title: String,
    content: String,
    headerColor: Color
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0x22000000))
            .padding(8.dp)
    ) {
        Column {
            Text(
                text = title,
                fontSize = 8.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 0.5.sp,
                color = headerColor
            )
            Text(
                text = content,
                fontSize = 8.5.sp,
                color = Color(0xDDFFFFFF),
                lineHeight = 12.5.sp,
                modifier = Modifier.padding(top = 2.dp)
            )
        }
    }
}

@Composable
fun VoiceLineRow(type: String, quote: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = "$type:",
            fontSize = 7.5.sp,
            fontWeight = FontWeight.Bold,
            color = TapoutOrange,
            modifier = Modifier.width(38.dp)
        )
        Text(
            text = "\"$quote\"",
            fontSize = 8.sp,
            fontStyle = FontStyle.Italic,
            color = Color(0xDDFFFFFF),
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun DossierSubTabButton(
    label: String,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .height(26.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(if (isSelected) TapoutCrimson else Color(0x33000000))
            .border(1.dp, if (isSelected) TapoutBrightRed else Color(0x1AFFFFFF), RoundedCornerShape(6.dp))
            .pointerInput(label) {
                detectTapGestures { onClick() }
            },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontSize = 8.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            color = if (isSelected) Color.White else Color(0x88FFFFFF)
        )
    }
}

@Composable
fun CompactFighterPill(
    fighter: Fighter,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    Box(
        modifier = Modifier
            .width(95.dp)
            .height(58.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(if (isSelected) Color(0xFF2A0314) else SurfaceCard)
            .border(
                width = if (isSelected) 1.5.dp else 1.dp,
                color = if (isSelected) fighter.themeColor else BorderDark,
                shape = RoundedCornerShape(10.dp)
            )
            .pointerInput(fighter.id) {
                detectTapGestures { onSelect() }
            }
            .padding(6.dp)
            .testTag("fighter_card_${fighter.id}")
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = fighter.avatarCode,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Black,
                    color = fighter.accentColor
                )
                Text(
                    text = "${fighter.maxHp} HP",
                    fontSize = 7.5.sp,
                    color = Color(0x66FFFFFF)
                )
            }
            Text(
                text = fighter.name,
                fontSize = 8.5.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                color = if (isSelected) Color.White else Color(0xDDFFFFFF),
                maxLines = 1
            )
        }
    }
}

@Composable
fun StageCardItem(
    stage: Stage,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(if (isSelected) Color(0xFF2A0818) else SurfaceCard)
            .border(
                width = if (isSelected) 1.5.dp else 1.dp,
                color = if (isSelected) stage.floorAccent else BorderDark,
                shape = RoundedCornerShape(10.dp)
            )
            .pointerInput(stage.id) {
                detectTapGestures { onSelect() }
            }
            .padding(8.dp)
            .testTag("stage_card_${stage.id}")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(stage.floorAccent.copy(alpha = 0.3f))
                            .padding(horizontal = 5.dp, vertical = 1.dp)
                    ) {
                        Text("STAGE ${stage.stageNumber}", fontSize = 7.5.sp, fontWeight = FontWeight.Bold, color = stage.floorAccent)
                    }
                    Text(
                        text = stage.name,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = Color.White
                    )
                }
                Text(
                    text = "${stage.subtitle} • ${stage.era}",
                    fontSize = 7.5.sp,
                    color = Color(0x88FFFFFF),
                    modifier = Modifier.padding(top = 2.dp)
                )
                Text(
                    text = stage.lore,
                    fontSize = 7.5.sp,
                    fontStyle = FontStyle.Italic,
                    color = Color(0xAAFFFFFF),
                    maxLines = 1,
                    modifier = Modifier.padding(top = 1.dp)
                )
            }

            if (isSelected) {
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .clip(CircleShape)
                        .background(TapoutGold),
                    contentAlignment = Alignment.Center
                ) {
                    Text("✓", fontSize = 10.sp, fontWeight = FontWeight.Black, color = Color.Black)
                }
            }
        }
    }
}

@Composable
fun StatBar(
    label: String,
    value: Float,
    icon: ImageVector,
    color: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(11.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = label,
            fontSize = 8.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            color = Color(0x99FFFFFF),
            modifier = Modifier.width(24.dp)
        )
        Box(
            modifier = Modifier
                .weight(1f)
                .height(4.dp)
                .clip(RoundedCornerShape(50))
                .background(Color(0xFF14000B))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(value)
                    .height(4.dp)
                    .clip(RoundedCornerShape(50))
                    .background(color)
            )
        }
    }
}

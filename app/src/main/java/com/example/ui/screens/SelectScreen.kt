package com.example.ui.screens

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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
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
import com.example.ui.theme.TapoutOrange

@Composable
fun SelectScreen(
    soundEngine: SoundEffectsEngine?,
    onChampionChosen: (playerFighter: Fighter, enemyFighter: Fighter, stage: Stage) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedFighter by remember { mutableStateOf(Roster.fighters[0]) }
    var selectedStage by remember { mutableStateOf(StageRoster.getStageById(Roster.fighters[0].homeStageId)) }
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Roster & Stats, 1: Character Story & Bio, 2: Stage Select
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
                    text = "SELECT FIGHTER & BATTLEGROUND",
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
                text = { Text("ROSTER", fontSize = 10.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace) },
                icon = { Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(13.dp)) }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = {
                    selectedTab = 1
                    soundEngine?.playSelect()
                },
                text = { Text("STORY & BIO", fontSize = 10.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace) },
                icon = { Icon(Icons.Default.AutoStories, contentDescription = null, modifier = Modifier.size(13.dp)) }
            )
            Tab(
                selected = selectedTab == 2,
                onClick = {
                    selectedTab = 2
                    soundEngine?.playSelect()
                },
                text = { Text("STAGES (1-9)", fontSize = 10.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace) },
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
                    // TAB 0: ROSTER GRID & STATS
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // 3x2 Grid for Roster
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

                        // Selected Fighter Stats Card
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                                .clip(RoundedCornerShape(14.dp))
                                .background(SurfaceCard)
                                .border(1.dp, selectedFighter.themeColor.copy(alpha = 0.5f), RoundedCornerShape(14.dp))
                                .padding(10.dp)
                        ) {
                            Column(
                                modifier = Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text(
                                                text = selectedFighter.name,
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Black,
                                                fontFamily = FontFamily.Monospace,
                                                color = selectedFighter.accentColor
                                            )
                                            Text(
                                                text = selectedFighter.title,
                                                fontSize = 8.5.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = TapoutGold
                                            )
                                        }

                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(50))
                                                .background(Color(0x33000000))
                                                .border(1.dp, Color(0x22FFFFFF), RoundedCornerShape(50))
                                                .padding(horizontal = 8.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = selectedFighter.era,
                                                fontSize = 7.5.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xCCFFFFFF)
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(4.dp))

                                    Text(
                                        text = "ULTRA: ${selectedFighter.specialName}",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace,
                                        color = TapoutGold
                                    )

                                    Text(
                                        text = "\"${selectedFighter.introQuote}\"",
                                        fontSize = 8.5.sp,
                                        fontStyle = FontStyle.Italic,
                                        color = Color(0xB3FFFFFF),
                                        lineHeight = 12.sp,
                                        modifier = Modifier.padding(top = 2.dp, bottom = 4.dp)
                                    )
                                }

                                // Stats meters
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

                1 -> {
                    // TAB 1: STORY & BIO VIEW
                    val scrollState = rememberScrollState()
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(14.dp))
                            .background(SurfaceCard)
                            .border(1.dp, selectedFighter.themeColor.copy(alpha = 0.5f), RoundedCornerShape(14.dp))
                            .padding(12.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(scrollState),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "${selectedFighter.name} • STORY & LORE",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Black,
                                fontFamily = FontFamily.Monospace,
                                color = selectedFighter.accentColor
                            )

                            // Personality Block
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0x22000000))
                                    .padding(8.dp)
                            ) {
                                Column {
                                    Text("PERSONALITY & DISCIPLINE", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = TapoutGold)
                                    Text(selectedFighter.personality, fontSize = 9.sp, color = Color.White)
                                }
                            }

                            // Origin Bio
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0x22000000))
                                    .padding(8.dp)
                            ) {
                                Column {
                                    Text("ORIGIN & BACKGROUND", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = TapoutNeonBlue)
                                    Text(selectedFighter.bio, fontSize = 9.sp, color = Color(0xDDFFFFFF), lineHeight = 13.sp)
                                }
                            }

                            // Story Arc
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0x22000000))
                                    .padding(8.dp)
                            ) {
                                Column {
                                    Text("TOURNAMENT OBJECTIVE", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = TapoutCrimson)
                                    Text(selectedFighter.storyArc, fontSize = 9.sp, color = Color(0xDDFFFFFF), lineHeight = 13.sp)
                                }
                            }

                            // Battle Quotes
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0x22000000))
                                    .padding(8.dp)
                            ) {
                                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                    Text("VOICE CALLOUTS", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = TapoutGold)
                                    Text("Special: \"${selectedFighter.specialQuote}\"", fontSize = 8.5.sp, fontStyle = FontStyle.Italic, color = Color(0xCCFFFFFF))
                                    Text("Victory: \"${selectedFighter.victoryQuote}\"", fontSize = 8.5.sp, fontStyle = FontStyle.Italic, color = Color(0xCCFFFFFF))
                                }
                            }
                        }
                    }
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

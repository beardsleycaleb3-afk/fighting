package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.audio.SoundEffectsEngine
import com.example.game.MatchStats
import com.example.ui.theme.BackgroundDark
import com.example.ui.theme.BorderDark
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.TapoutBrightRed
import com.example.ui.theme.TapoutCrimson
import com.example.ui.theme.TapoutGold

@Composable
fun ResultScreen(
    stats: MatchStats,
    soundEngine: SoundEffectsEngine?,
    onRematch: () -> Unit,
    onCharacterSelect: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isWin = stats.winnerIsPlayer
    val bannerColor = if (isWin) TapoutGold else TapoutBrightRed
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundDark)
            .padding(14.dp)
            .verticalScroll(scrollState),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Result Title Banner
        Text(
            text = if (isWin) "VICTORY" else "DEFEATED",
            fontSize = 38.sp,
            fontWeight = FontWeight.Black,
            fontFamily = FontFamily.Monospace,
            letterSpacing = 5.sp,
            color = bannerColor,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .shadow(elevation = 16.dp, shape = RoundedCornerShape(8.dp), ambientColor = bannerColor, spotColor = bannerColor)
                .testTag("result_title")
        )

        Box(
            modifier = Modifier
                .padding(top = 4.dp, bottom = 12.dp)
                .clip(RoundedCornerShape(50))
                .background(SurfaceCard)
                .border(1.dp, if (isWin) TapoutGold.copy(alpha = 0.5f) else Color(0x33FFFFFF), RoundedCornerShape(50))
                .padding(horizontal = 14.dp, vertical = 3.dp)
        ) {
            Text(
                text = "GRADE: ${stats.rankGrade}",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 1.5.sp,
                color = if (isWin) TapoutGold else Color(0xCCFFFFFF),
                textAlign = TextAlign.Center
            )
        }

        // Storyline Epilogue Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(SurfaceCard)
                .border(1.dp, if (isWin) TapoutGold.copy(alpha = 0.4f) else BorderDark, RoundedCornerShape(14.dp))
                .padding(10.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = if (isWin) "VICTORY STORYLINE" else "DEFEAT DISPATCH",
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 1.sp,
                    color = if (isWin) TapoutGold else TapoutCrimson
                )
                Text(
                    text = if (isWin) "\"${stats.playerFighter.victoryQuote}\"" else "\"The temporal anomaly was too severe... but the fight is not over.\"",
                    fontSize = 9.5.sp,
                    fontStyle = FontStyle.Italic,
                    color = Color.White
                )
                Text(
                    text = if (isWin) "${stats.playerFighter.name} advances through Stage ${stats.stage.stageNumber} towards the celestial summit." else "${stats.enemyFighter.name} secured the arena advantage in ${stats.stage.name}.",
                    fontSize = 8.5.sp,
                    color = Color(0xAAFFFFFF)
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Combat Stats Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(SurfaceCard)
                .border(1.dp, BorderDark, RoundedCornerShape(14.dp))
                .padding(12.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "MATCH METRICS",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 1.sp,
                    color = TapoutCrimson
                )

                MetricRow(label = "CHAMPION", value = stats.playerFighter.name)
                MetricRow(label = "OPPONENT", value = stats.enemyFighter.name)
                MetricRow(label = "BATTLEGROUND", value = "STAGE ${stats.stage.stageNumber}: ${stats.stage.name}")
                MetricRow(label = "MAX COMBO STREAK", value = "${stats.maxCombo}x HITS")
                MetricRow(label = "TOTAL STRIKES", value = "${stats.totalHits}")
                MetricRow(label = "DAMAGE INFLICTED", value = "${stats.damageDealt} HP")
                MetricRow(label = "TIME REMAINING", value = "${stats.timeRemaining}s")
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Rematch Button
        Button(
            onClick = {
                soundEngine?.playSelect()
                onRematch()
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(44.dp)
                .clip(RoundedCornerShape(12.dp))
                .shadow(elevation = 10.dp, shape = RoundedCornerShape(12.dp), ambientColor = TapoutCrimson, spotColor = TapoutBrightRed)
                .testTag("btn_rematch"),
            colors = ButtonDefaults.buttonColors(
                containerColor = TapoutCrimson,
                contentColor = Color.White
            )
        ) {
            Text(
                text = "REMATCH",
                fontSize = 13.sp,
                fontWeight = FontWeight.Black,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 2.sp
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Character Select Button
        OutlinedButton(
            onClick = {
                soundEngine?.playSelect()
                onCharacterSelect()
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(44.dp)
                .clip(RoundedCornerShape(12.dp))
                .border(1.dp, BorderDark, RoundedCornerShape(12.dp))
                .testTag("btn_menu"),
            colors = ButtonDefaults.outlinedButtonColors(
                containerColor = SurfaceCard,
                contentColor = Color.White
            )
        ) {
            Text(
                text = "CHARACTER & STAGE SELECT",
                fontSize = 11.5.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 1.sp
            )
        }
    }
}

@Composable
fun MetricRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 8.5.sp,
            fontFamily = FontFamily.Monospace,
            color = Color(0x88FFFFFF)
        )
        Text(
            text = value,
            fontSize = 9.5.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            color = Color.White
        )
    }
}

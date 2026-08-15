package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.example.audio.SoundEffectsEngine
import com.example.game.MatchStats
import com.example.model.Fighter
import com.example.model.Roster
import com.example.model.Stage
import com.example.model.StageRoster
import com.example.ui.screens.BootScreen
import com.example.ui.screens.FightScreen
import com.example.ui.screens.RecoveryScreen
import com.example.ui.screens.ResultScreen
import com.example.ui.screens.SelectScreen
import com.example.ui.theme.MyApplicationTheme

enum class AppScreen {
    BOOT,
    SELECT,
    FIGHT,
    RESULT,
    RECOVERY
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                TapoutApp()
            }
        }
    }
}

@Composable
fun TapoutApp() {
    val context = LocalContext.current
    val soundEngine = remember { SoundEffectsEngine(context) }

    DisposableEffect(soundEngine) {
        onDispose {
            soundEngine.release()
        }
    }

    var currentScreen by remember { mutableStateOf(AppScreen.BOOT) }
    var selectedPlayer by remember { mutableStateOf(Roster.fighters[0]) }
    var selectedEnemy by remember { mutableStateOf(Roster.fighters[1]) }
    var selectedStage by remember { mutableStateOf(StageRoster.stages[0]) }
    var lastMatchStats by remember { mutableStateOf<MatchStats?>(null) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(com.example.ui.theme.BackgroundDark)
            .safeDrawingPadding()
    ) {
        when (currentScreen) {
            AppScreen.BOOT -> {
                BootScreen(
                    soundEngine = soundEngine,
                    onEnterSelect = {
                        currentScreen = AppScreen.SELECT
                    },
                    onTriggerRecovery = {
                        currentScreen = AppScreen.RECOVERY
                    }
                )
            }

            AppScreen.SELECT -> {
                SelectScreen(
                    soundEngine = soundEngine,
                    onChampionChosen = { player, enemy, stage ->
                        selectedPlayer = player
                        selectedEnemy = enemy
                        selectedStage = stage
                        currentScreen = AppScreen.FIGHT
                    }
                )
            }

            AppScreen.FIGHT -> {
                FightScreen(
                    playerFighter = selectedPlayer,
                    enemyFighter = selectedEnemy,
                    stage = selectedStage,
                    soundEngine = soundEngine,
                    onMatchOver = { stats ->
                        lastMatchStats = stats
                        currentScreen = AppScreen.RESULT
                    },
                    onExitToSelect = {
                        currentScreen = AppScreen.SELECT
                    }
                )
            }

            AppScreen.RESULT -> {
                val stats = lastMatchStats
                if (stats != null) {
                    ResultScreen(
                        stats = stats,
                        soundEngine = soundEngine,
                        onRematch = {
                            currentScreen = AppScreen.FIGHT
                        },
                        onCharacterSelect = {
                            currentScreen = AppScreen.SELECT
                        }
                    )
                } else {
                    currentScreen = AppScreen.SELECT
                }
            }

            AppScreen.RECOVERY -> {
                RecoveryScreen(
                    soundEngine = soundEngine,
                    onReload = {
                        currentScreen = AppScreen.BOOT
                    }
                )
            }
        }
    }
}

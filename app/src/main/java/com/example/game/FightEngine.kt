package com.example.game

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import com.example.audio.SoundEffectsEngine
import com.example.model.Fighter
import com.example.model.Stage
import com.example.model.StageRoster
import com.example.ui.theme.TapoutBrightRed
import com.example.ui.theme.TapoutCrimson
import com.example.ui.theme.TapoutGold
import com.example.ui.theme.TapoutNeonBlue
import com.example.ui.theme.TapoutNeonPurple
import com.example.ui.theme.TapoutOrange
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.random.Random

enum class ActionState {
    IDLE,
    WALKING,
    JUMPING,
    CROUCHING,
    PUNCHING,
    KICKING,
    SPECIAL,
    BLOCKING,
    HURT,
    KO
}

enum class ComboTier {
    NONE,
    NORMAL,    // 2-3 hits
    GREAT,     // 4-5 hits
    MEGA,      // 6-8 hits
    ULTRA,     // 9-11 hits
    GODLIKE    // 12+ hits
}

data class HitParticle(
    var x: Float,
    var y: Float,
    var vx: Float,
    var vy: Float,
    var color: Color,
    var life: Float = 1.0f,
    val size: Float = 4f
)

data class DamageFloater(
    val text: String,
    var x: Float,
    var y: Float,
    val color: Color,
    var life: Float = 1.0f
)

data class SpecialWave(
    var x: Float,
    var y: Float,
    val vx: Float,
    val color: Color,
    val isPlayer: Boolean,
    val damage: Int,
    var radius: Float = 18f,
    var active: Boolean = true
)

class FighterEntity(
    val fighter: Fighter,
    var x: Float,
    var y: Float,
    val isPlayer: Boolean
) {
    var vx: Float = 0f
    var vy: Float = 0f
    var hp: Int = fighter.maxHp
    val maxHp: Int = fighter.maxHp
    var superMeter: Float = 25f // 0f to 100f
    var state: ActionState = ActionState.IDLE
    var stateTimer: Float = 0f
    var isFacingRight: Boolean = isPlayer
    var isBlocking: Boolean = false
    var hurtFlash: Float = 0f

    val groundY = 240f
    val bodyWidth = 36f
    val bodyHeight = 64f

    fun reset(startX: Float) {
        x = startX
        y = groundY
        vx = 0f
        vy = 0f
        hp = maxHp
        superMeter = 25f
        state = ActionState.IDLE
        stateTimer = 0f
        isFacingRight = isPlayer
        isBlocking = false
        hurtFlash = 0f
    }
}

data class MatchStats(
    val winnerIsPlayer: Boolean,
    val playerFighter: Fighter,
    val enemyFighter: Fighter,
    val stage: Stage,
    val totalHits: Int,
    val maxCombo: Int,
    val damageDealt: Int,
    val timeRemaining: Int,
    val rankGrade: String
)

class FightEngine(
    val playerFighter: Fighter,
    val enemyFighter: Fighter,
    val stage: Stage = StageRoster.getStageById(playerFighter.homeStageId),
    private val soundEngine: SoundEffectsEngine?,
    val onMatchEnded: (MatchStats) -> Unit
) {
    val arenaWidth = 330f
    val arenaHeight = 320f

    val player = FighterEntity(playerFighter, 70f, 240f, isPlayer = true)
    val enemy = FighterEntity(enemyFighter, 260f, 240f, isPlayer = false)

    var roundTimeSeconds: Float = 99f
    var isMatchOver: Boolean = false
    var screenShake: Float = 0f

    // Real-Time Combo Tracking & HUD Surge
    var playerCombo: Int = 0
    var playerMaxCombo: Int = 0
    var playerTotalHits: Int = 0
    var playerDamageDealt: Int = 0
    var comboResetTimer: Float = 0f
    var comboDisplayTimer: Float = 0f
    var comboTier: ComboTier = ComboTier.NONE

    // Cinematic & Intro States
    var introCinematicTimer: Float = 3.2f // 3.2s intro pre-match dialogue cutscene
    var specialCutInTimer: Float = 0f
    var specialCutInFighter: Fighter? = null

    val particles = mutableListOf<HitParticle>()
    val damageFloaters = mutableListOf<DamageFloater>()
    val specialWaves = mutableListOf<SpecialWave>()

    // Continuous input states
    var inputLeft = false
    var inputRight = false
    var inputUp = false
    var inputDown = false
    var inputBlock = false

    // AI timing
    private var aiDecisionTimer = 0f
    private var aiNextActionDelay = 0.2f
    private var aiMovingDir = 0

    fun update(dt: Float) {
        if (isMatchOver) return

        // Intro cinematic timer countdown
        if (introCinematicTimer > 0f) {
            introCinematicTimer = max(0f, introCinematicTimer - dt)
            return
        }

        // Special cut-in cinematic freeze effect
        if (specialCutInTimer > 0f) {
            specialCutInTimer = max(0f, specialCutInTimer - dt)
        }

        roundTimeSeconds = max(0f, roundTimeSeconds - dt)
        if (roundTimeSeconds <= 0f) {
            endMatch(timeOut = true)
            return
        }

        // Screen shake decay
        if (screenShake > 0f) {
            screenShake = max(0f, screenShake - dt * 25f)
        }

        // Combo decay tracking
        if (comboResetTimer > 0f) {
            comboResetTimer -= dt
            if (comboResetTimer <= 0f) {
                playerCombo = 0
                comboTier = ComboTier.NONE
            }
        }
        if (comboDisplayTimer > 0f) {
            comboDisplayTimer = max(0f, comboDisplayTimer - dt)
        }

        // Update orientations
        player.isFacingRight = player.x <= enemy.x
        enemy.isFacingRight = enemy.x < player.x

        // Update player logic
        updatePlayerEntity(dt)

        // Update AI logic
        updateEnemyAI(dt)

        // Update physics & bounds
        updatePhysics(player, dt)
        updatePhysics(enemy, dt)

        // Update special projectiles
        updateSpecialWaves(dt)

        // Update particles
        updateParticles(dt)

        // Check KOs
        if (enemy.hp <= 0 && enemy.state != ActionState.KO) {
            enemy.state = ActionState.KO
            enemy.hp = 0
            isMatchOver = true
            soundEngine?.playVictory()
            endMatch(timeOut = false)
        } else if (player.hp <= 0 && player.state != ActionState.KO) {
            player.state = ActionState.KO
            player.hp = 0
            isMatchOver = true
            soundEngine?.playDefeat()
            endMatch(timeOut = false)
        }
    }

    private fun updatePlayerEntity(dt: Float) {
        if (player.state == ActionState.KO) return

        player.stateTimer += dt
        if (player.hurtFlash > 0f) player.hurtFlash = max(0f, player.hurtFlash - dt * 4f)

        when (player.state) {
            ActionState.PUNCHING -> {
                if (player.stateTimer > 0.25f) player.state = ActionState.IDLE
            }
            ActionState.KICKING -> {
                if (player.stateTimer > 0.32f) player.state = ActionState.IDLE
            }
            ActionState.SPECIAL -> {
                if (player.stateTimer > 0.45f) player.state = ActionState.IDLE
            }
            ActionState.HURT -> {
                if (player.stateTimer > 0.28f) player.state = ActionState.IDLE
            }
            else -> {
                player.isBlocking = inputBlock
                if (inputBlock) {
                    player.state = ActionState.BLOCKING
                    player.vx = 0f
                } else if (inputDown && player.y >= player.groundY) {
                    player.state = ActionState.CROUCHING
                    player.vx = 0f
                } else {
                    if (inputUp && player.y >= player.groundY) {
                        player.vy = -player.fighter.jumpPower
                        player.state = ActionState.JUMPING
                        soundEngine?.playJump()
                        spawnDust(player.x, player.groundY)
                    }

                    if (inputLeft) {
                        player.vx = -player.fighter.speed * 40f
                        if (player.y >= player.groundY) player.state = ActionState.WALKING
                    } else if (inputRight) {
                        player.vx = player.fighter.speed * 40f
                        if (player.y >= player.groundY) player.state = ActionState.WALKING
                    } else {
                        player.vx = 0f
                        if (player.y >= player.groundY && player.state != ActionState.JUMPING) {
                            player.state = ActionState.IDLE
                        }
                    }
                }
            }
        }
    }

    private fun updateEnemyAI(dt: Float) {
        if (enemy.state == ActionState.KO) return

        enemy.stateTimer += dt
        if (enemy.hurtFlash > 0f) enemy.hurtFlash = max(0f, enemy.hurtFlash - dt * 4f)

        when (enemy.state) {
            ActionState.PUNCHING -> {
                if (enemy.stateTimer > 0.26f) enemy.state = ActionState.IDLE
            }
            ActionState.KICKING -> {
                if (enemy.stateTimer > 0.34f) enemy.state = ActionState.IDLE
            }
            ActionState.SPECIAL -> {
                if (enemy.stateTimer > 0.45f) enemy.state = ActionState.IDLE
            }
            ActionState.HURT -> {
                if (enemy.stateTimer > 0.28f) enemy.state = ActionState.IDLE
            }
            else -> {
                aiDecisionTimer += dt
                val dist = abs(player.x - enemy.x)

                if (aiDecisionTimer >= aiNextActionDelay) {
                    aiDecisionTimer = 0f
                    aiNextActionDelay = Random.nextFloat() * 0.22f + 0.14f

                    if (enemy.superMeter >= 100f && dist < 140f && Random.nextFloat() < 0.70f) {
                        triggerEnemySpecial()
                    } else if (dist < 55f) {
                        val roll = Random.nextFloat()
                        if (player.state == ActionState.PUNCHING || player.state == ActionState.KICKING) {
                            if (roll < 0.45f) {
                                enemy.isBlocking = true
                                enemy.state = ActionState.BLOCKING
                                enemy.vx = 0f
                            } else {
                                enemy.isBlocking = false
                                triggerEnemyPunch()
                            }
                        } else if (roll < 0.40f) {
                            triggerEnemyPunch()
                        } else if (roll < 0.75f) {
                            triggerEnemyKick()
                        } else if (roll < 0.90f) {
                            aiMovingDir = if (enemy.x > player.x) 1 else -1
                        }
                    } else if (dist < 110f) {
                        val roll = Random.nextFloat()
                        if (roll < 0.35f && enemy.y >= enemy.groundY) {
                            enemy.vy = -enemy.fighter.jumpPower
                            enemy.state = ActionState.JUMPING
                            aiMovingDir = if (player.x > enemy.x) 1 else -1
                        } else if (roll < 0.75f) {
                            aiMovingDir = if (player.x > enemy.x) 1 else -1
                        } else {
                            aiMovingDir = 0
                        }
                    } else {
                        aiMovingDir = if (player.x > enemy.x) 1 else -1
                    }
                }

                if (enemy.state != ActionState.PUNCHING && enemy.state != ActionState.KICKING && enemy.state != ActionState.SPECIAL) {
                    if (enemy.isBlocking) {
                        enemy.state = ActionState.BLOCKING
                        enemy.vx = 0f
                    } else {
                        enemy.vx = aiMovingDir * enemy.fighter.speed * 36f
                        if (enemy.vx != 0f && enemy.y >= enemy.groundY) {
                            enemy.state = ActionState.WALKING
                        } else if (enemy.y >= enemy.groundY && enemy.state != ActionState.JUMPING) {
                            enemy.state = ActionState.IDLE
                        }
                    }
                }
            }
        }
    }

    private fun updatePhysics(entity: FighterEntity, dt: Float) {
        if (entity.y < entity.groundY || entity.vy < 0) {
            entity.vy += 22f * dt * 60f
            entity.y += entity.vy * dt
            if (entity.y >= entity.groundY) {
                entity.y = entity.groundY
                entity.vy = 0f
                if (entity.state == ActionState.JUMPING) {
                    entity.state = ActionState.IDLE
                    spawnDust(entity.x, entity.groundY)
                }
            }
        }

        entity.x += entity.vx * dt
        val minX = 22f
        val maxX = arenaWidth - 22f
        entity.x = entity.x.coerceIn(minX, maxX)
    }

    fun onPlayerPunch() {
        if (introCinematicTimer > 0f || player.state == ActionState.KO || player.state == ActionState.HURT) return
        player.state = ActionState.PUNCHING
        player.stateTimer = 0f
        soundEngine?.playPunch()

        val dist = abs(player.x - enemy.x)
        val yDiff = abs(player.y - enemy.y)
        if (dist <= 62f && yDiff <= 45f) {
            applyHitToEnemy(
                damage = player.fighter.punchDamage,
                isHeavy = false,
                isSpecial = false,
                sourceName = "PUNCH"
            )
        }
    }

    fun onPlayerKick() {
        if (introCinematicTimer > 0f || player.state == ActionState.KO || player.state == ActionState.HURT) return
        player.state = ActionState.KICKING
        player.stateTimer = 0f
        soundEngine?.playKick()

        val dist = abs(player.x - enemy.x)
        val yDiff = abs(player.y - enemy.y)
        if (dist <= 72f && yDiff <= 45f) {
            applyHitToEnemy(
                damage = player.fighter.kickDamage,
                isHeavy = true,
                isSpecial = false,
                sourceName = "KICK"
            )
        }
    }

    fun onPlayerSpecial() {
        if (introCinematicTimer > 0f || player.state == ActionState.KO || player.state == ActionState.HURT) return
        if (player.superMeter < 100f) return

        player.superMeter = 0f
        player.state = ActionState.SPECIAL
        player.stateTimer = 0f
        screenShake = 14f
        specialCutInTimer = 0.6f
        specialCutInFighter = player.fighter
        soundEngine?.playSpecial()

        val dir = if (player.isFacingRight) 1f else -1f
        specialWaves.add(
            SpecialWave(
                x = player.x + (dir * 25f),
                y = player.y - 30f,
                vx = dir * 290f,
                color = player.fighter.themeColor,
                isPlayer = true,
                damage = player.fighter.specialDamage
            )
        )

        val dist = abs(player.x - enemy.x)
        if (dist <= 85f) {
            applyHitToEnemy(
                damage = player.fighter.specialDamage,
                isHeavy = true,
                isSpecial = true,
                sourceName = player.fighter.specialName
            )
        }
    }

    private fun triggerEnemyPunch() {
        enemy.state = ActionState.PUNCHING
        enemy.stateTimer = 0f
        soundEngine?.playPunch()

        val dist = abs(player.x - enemy.x)
        val yDiff = abs(player.y - enemy.y)
        if (dist <= 62f && yDiff <= 45f) {
            applyHitToPlayer(
                damage = enemy.fighter.punchDamage,
                isHeavy = false,
                isSpecial = false
            )
        }
    }

    private fun triggerEnemyKick() {
        enemy.state = ActionState.KICKING
        enemy.stateTimer = 0f
        soundEngine?.playKick()

        val dist = abs(player.x - enemy.x)
        val yDiff = abs(player.y - enemy.y)
        if (dist <= 72f && yDiff <= 45f) {
            applyHitToPlayer(
                damage = enemy.fighter.kickDamage,
                isHeavy = true,
                isSpecial = false
            )
        }
    }

    private fun triggerEnemySpecial() {
        enemy.superMeter = 0f
        enemy.state = ActionState.SPECIAL
        enemy.stateTimer = 0f
        screenShake = 12f
        specialCutInTimer = 0.5f
        specialCutInFighter = enemy.fighter
        soundEngine?.playSpecial()

        val dir = if (enemy.isFacingRight) 1f else -1f
        specialWaves.add(
            SpecialWave(
                x = enemy.x + (dir * 25f),
                y = enemy.y - 30f,
                vx = dir * 260f,
                color = enemy.fighter.themeColor,
                isPlayer = false,
                damage = enemy.fighter.specialDamage
            )
        )

        val dist = abs(player.x - enemy.x)
        if (dist <= 80f) {
            applyHitToPlayer(
                damage = enemy.fighter.specialDamage,
                isHeavy = true,
                isSpecial = true
            )
        }
    }

    private fun applyHitToEnemy(damage: Int, isHeavy: Boolean, isSpecial: Boolean, sourceName: String) {
        val blocked = enemy.isBlocking
        val actualDamage = if (blocked) max(2, (damage * 0.20f).toInt()) else damage

        enemy.hp = max(0, enemy.hp - actualDamage)
        enemy.hurtFlash = 1.0f
        playerDamageDealt += actualDamage
        playerTotalHits++

        // Real-Time Combo Counter increment & decay reset
        playerCombo++
        comboResetTimer = 1.85f // 1.85s window to continue combo
        comboDisplayTimer = 2.0f
        if (playerCombo > playerMaxCombo) playerMaxCombo = playerCombo

        comboTier = when {
            playerCombo >= 12 -> ComboTier.GODLIKE
            playerCombo >= 9 -> ComboTier.ULTRA
            playerCombo >= 6 -> ComboTier.MEGA
            playerCombo >= 4 -> ComboTier.GREAT
            playerCombo >= 2 -> ComboTier.NORMAL
            else -> ComboTier.NONE
        }

        // Super meter gain
        player.superMeter = min(100f, player.superMeter + (if (isSpecial) 0f else 18f))
        enemy.superMeter = min(100f, enemy.superMeter + 10f)

        if (blocked) {
            soundEngine?.playBlock()
            spawnHitSpark(enemy.x, enemy.y - 30f, TapoutNeonBlue, count = 6)
            damageFloaters.add(DamageFloater("BLOCKED $actualDamage", enemy.x, enemy.y - 65f, TapoutNeonBlue))
        } else {
            enemy.state = ActionState.HURT
            enemy.stateTimer = 0f
            screenShake = if (isSpecial) 15f else if (isHeavy) 9f else 5f

            val pushDir = if (player.x < enemy.x) 1f else -1f
            enemy.x = (enemy.x + pushDir * (if (isSpecial) 24f else 14f)).coerceIn(20f, arenaWidth - 20f)

            val sparkColor = if (isSpecial) player.fighter.themeColor else if (playerCombo >= 5) TapoutOrange else TapoutGold
            spawnHitSpark(enemy.x, enemy.y - 35f, sparkColor, count = if (isSpecial) 22 else 12)

            val hitText = if (isSpecial) "★ CRITICAL $actualDamage!" else if (playerCombo > 1) "${playerCombo}x HIT -$actualDamage" else "-$actualDamage"
            val textColor = if (isSpecial) TapoutGold else if (playerCombo >= 4) TapoutOrange else TapoutBrightRed
            damageFloaters.add(DamageFloater(hitText, enemy.x, enemy.y - 60f, textColor))
        }
    }

    private fun applyHitToPlayer(damage: Int, isHeavy: Boolean, isSpecial: Boolean) {
        val blocked = player.isBlocking
        val actualDamage = if (blocked) max(2, (damage * 0.20f).toInt()) else damage

        player.hp = max(0, player.hp - actualDamage)
        player.hurtFlash = 1.0f

        // Reset player combo on taking damage
        if (!blocked) {
            playerCombo = 0
            comboTier = ComboTier.NONE
            comboResetTimer = 0f
        }

        enemy.superMeter = min(100f, enemy.superMeter + (if (isSpecial) 0f else 15f))
        player.superMeter = min(100f, player.superMeter + 12f)

        if (blocked) {
            soundEngine?.playBlock()
            spawnHitSpark(player.x, player.y - 30f, TapoutNeonBlue, count = 6)
            damageFloaters.add(DamageFloater("GUARD $actualDamage", player.x, player.y - 65f, TapoutNeonBlue))
        } else {
            player.state = ActionState.HURT
            player.stateTimer = 0f
            screenShake = if (isSpecial) 14f else 6f

            val pushDir = if (enemy.x < player.x) 1f else -1f
            player.x = (player.x + pushDir * (if (isSpecial) 22f else 12f)).coerceIn(20f, arenaWidth - 20f)

            val sparkColor = if (isSpecial) enemy.fighter.themeColor else TapoutBrightRed
            spawnHitSpark(player.x, player.y - 35f, sparkColor, count = if (isSpecial) 18 else 8)
            damageFloaters.add(DamageFloater("-$actualDamage", player.x, player.y - 60f, TapoutBrightRed))
        }
    }

    private fun updateSpecialWaves(dt: Float) {
        val iterator = specialWaves.iterator()
        while (iterator.hasNext()) {
            val wave = iterator.next()
            wave.x += wave.vx * dt

            val target = if (wave.isPlayer) enemy else player
            val dist = abs(wave.x - target.x)
            val yDist = abs(wave.y - (target.y - 30f))

            if (dist < 28f && yDist < 40f && wave.active) {
                wave.active = false
                if (wave.isPlayer) {
                    applyHitToEnemy(wave.damage, isHeavy = true, isSpecial = true, sourceName = "SUPER BLAST")
                } else {
                    applyHitToPlayer(wave.damage, isHeavy = true, isSpecial = true)
                }
            }

            if (!wave.active || wave.x < 0f || wave.x > arenaWidth) {
                iterator.remove()
            }
        }
    }

    private fun updateParticles(dt: Float) {
        val pIter = particles.iterator()
        while (pIter.hasNext()) {
            val p = pIter.next()
            p.x += p.vx * dt
            p.y += p.vy * dt
            p.life -= dt * 2.8f
            if (p.life <= 0f) pIter.remove()
        }

        val dIter = damageFloaters.iterator()
        while (dIter.hasNext()) {
            val d = dIter.next()
            d.y -= 25f * dt
            d.life -= dt * 1.5f
            if (d.life <= 0f) dIter.remove()
        }
    }

    private fun spawnHitSpark(x: Float, y: Float, color: Color, count: Int = 10) {
        for (i in 0 until count) {
            val angle = Random.nextFloat() * 2f * Math.PI.toFloat()
            val speed = Random.nextFloat() * 140f + 40f
            particles.add(
                HitParticle(
                    x = x,
                    y = y,
                    vx = kotlin.math.cos(angle) * speed,
                    vy = kotlin.math.sin(angle) * speed,
                    color = color,
                    life = 1.0f,
                    size = Random.nextFloat() * 4f + 2f
                )
            )
        }
    }

    private fun spawnDust(x: Float, y: Float) {
        for (i in 0 until 5) {
            particles.add(
                HitParticle(
                    x = x + (Random.nextFloat() * 20f - 10f),
                    y = y - 4f,
                    vx = (Random.nextFloat() * 60f - 30f),
                    vy = -Random.nextFloat() * 30f,
                    color = Color(0x66C8A080),
                    life = 0.6f,
                    size = 5f
                )
            )
        }
    }

    private fun endMatch(timeOut: Boolean) {
        val playerWin = if (timeOut) {
            (player.hp.toFloat() / player.maxHp) >= (enemy.hp.toFloat() / enemy.maxHp)
        } else {
            enemy.hp <= 0
        }

        val rank = when {
            playerWin && player.hp >= player.maxHp * 0.85f -> "S+ RANK (PERFECT)"
            playerWin && player.hp >= player.maxHp * 0.6f -> "A RANK (EXCELLENT)"
            playerWin -> "B RANK (VICTORY)"
            else -> "C RANK (DEFEAT)"
        }

        val stats = MatchStats(
            winnerIsPlayer = playerWin,
            playerFighter = playerFighter,
            enemyFighter = enemyFighter,
            stage = stage,
            totalHits = playerTotalHits,
            maxCombo = playerMaxCombo,
            damageDealt = playerDamageDealt,
            timeRemaining = roundTimeSeconds.toInt(),
            rankGrade = rank
        )
        onMatchEnded(stats)
    }
}

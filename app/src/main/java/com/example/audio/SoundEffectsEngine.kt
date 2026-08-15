package com.example.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.sin
import kotlin.random.Random

class SoundEffectsEngine(context: Context) {
    private val job = SupervisorJob()
    private val scope = CoroutineScope(Dispatchers.Default + job)
    private var soundEnabled = true

    private val vibrator: Vibrator? = try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            vibratorManager?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
    } catch (_: Exception) {
        null
    }

    fun isSoundEnabled(): Boolean = soundEnabled
    fun toggleSound(): Boolean {
        soundEnabled = !soundEnabled
        return soundEnabled
    }

    fun release() {
        try {
            job.cancel()
        } catch (_: Exception) {}
    }

    fun vibrate(durationMs: Long, amplitude: Int = 180) {
        try {
            if (vibrator?.hasVibrator() == true) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator.vibrate(
                        VibrationEffect.createOneShot(
                            durationMs.coerceAtLeast(10),
                            amplitude.coerceIn(1, 255)
                        )
                    )
                } else {
                    @Suppress("DEPRECATION")
                    vibrator.vibrate(durationMs)
                }
            }
        } catch (_: Exception) {
            // Ignored if permissions or hardware missing
        }
    }

    private fun playSynthesizedWave(
        sampleRate: Int = 22050,
        generateSamples: (Int, Int) -> ShortArray
    ) {
        if (!soundEnabled) return
        scope.launch {
            var audioTrack: AudioTrack? = null
            try {
                val numSamples = (sampleRate * 0.35).toInt()
                val samples = generateSamples(sampleRate, numSamples)

                val minBuf = AudioTrack.getMinBufferSize(
                    sampleRate,
                    AudioFormat.CHANNEL_OUT_MONO,
                    AudioFormat.ENCODING_PCM_16BIT
                )
                val bufferSizeInBytes = maxOf(if (minBuf > 0) minBuf * 2 else 4096, samples.size * 2)

                audioTrack = AudioTrack.Builder()
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_GAME)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .build()
                    )
                    .setAudioFormat(
                        AudioFormat.Builder()
                            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                            .setSampleRate(sampleRate)
                            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                            .build()
                    )
                    .setBufferSizeInBytes(bufferSizeInBytes)
                    .setTransferMode(AudioTrack.MODE_STREAM)
                    .build()

                audioTrack.play()
                audioTrack.write(samples, 0, samples.size)
                
                val playDurationMs = ((samples.size.toDouble() / sampleRate) * 1000.0).toLong() + 30L
                delay(playDurationMs)
            } catch (_: Exception) {
                // Audio synthesis fallback safe
            } finally {
                try {
                    audioTrack?.stop()
                    audioTrack?.release()
                } catch (_: Exception) {
                    // Safe cleanup
                }
            }
        }
    }

    fun playPunch() {
        vibrate(30, 150)
        playSynthesizedWave { sampleRate, _ ->
            val duration = (sampleRate * 0.12).toInt()
            val buffer = ShortArray(duration)
            for (i in 0 until duration) {
                val t = i.toDouble() / sampleRate
                val freq = 220.0 - (t / 0.12) * 120.0 // downward pitch drop
                val decay = 1.0 - (i.toDouble() / duration)
                val tone = sin(2.0 * Math.PI * freq * t)
                val noise = (Random.nextDouble() * 2.0 - 1.0) * 0.4
                buffer[i] = ((tone + noise) * decay * 24000).toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
            }
            buffer
        }
    }

    fun playKick() {
        vibrate(45, 200)
        playSynthesizedWave { sampleRate, _ ->
            val duration = (sampleRate * 0.16).toInt()
            val buffer = ShortArray(duration)
            for (i in 0 until duration) {
                val t = i.toDouble() / sampleRate
                val freq = 160.0 - (t / 0.16) * 110.0
                val decay = (1.0 - (i.toDouble() / duration))
                val wave = sin(2.0 * Math.PI * freq * t)
                val impactNoise = if (i < duration * 0.3) (Random.nextDouble() * 2 - 1) * 0.5 else 0.0
                buffer[i] = ((wave + impactNoise) * decay * decay * 28000).toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
            }
            buffer
        }
    }

    fun playBlock() {
        vibrate(20, 100)
        playSynthesizedWave { sampleRate, _ ->
            val duration = (sampleRate * 0.10).toInt()
            val buffer = ShortArray(duration)
            for (i in 0 until duration) {
                val t = i.toDouble() / sampleRate
                val freq = 850.0 + (t / 0.10) * 200.0 // high metallic deflection
                val decay = 1.0 - (i.toDouble() / duration)
                val wave = sin(2.0 * Math.PI * freq * t)
                buffer[i] = (wave * decay * 18000).toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
            }
            buffer
        }
    }

    fun playSpecial() {
        vibrate(80, 255)
        playSynthesizedWave { sampleRate, _ ->
            val duration = (sampleRate * 0.35).toInt()
            val buffer = ShortArray(duration)
            for (i in 0 until duration) {
                val t = i.toDouble() / sampleRate
                // charging laser rise then explosion sweep
                val freq = if (t < 0.15) 300.0 + (t / 0.15) * 800.0 else 200.0 - ((t - 0.15) / 0.20) * 150.0
                val decay = if (t < 0.15) 1.0 else 1.0 - ((t - 0.15) / 0.20)
                val wave = sin(2.0 * Math.PI * freq * t)
                val distortion = if (wave > 0.3) 1.0 else if (wave < -0.3) -1.0 else wave
                val noise = if (t > 0.12) (Random.nextDouble() * 2.0 - 1.0) * 0.4 else 0.0
                buffer[i] = ((distortion + noise) * decay * 26000).toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
            }
            buffer
        }
    }

    fun playJump() {
        playSynthesizedWave { sampleRate, _ ->
            val duration = (sampleRate * 0.08).toInt()
            val buffer = ShortArray(duration)
            for (i in 0 until duration) {
                val t = i.toDouble() / sampleRate
                val freq = 300.0 + (t / 0.08) * 350.0
                val decay = 1.0 - (i.toDouble() / duration)
                val wave = sin(2.0 * Math.PI * freq * t)
                buffer[i] = (wave * decay * 12000).toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
            }
            buffer
        }
    }

    fun playSelect() {
        vibrate(15, 80)
        playSynthesizedWave { sampleRate, _ ->
            val duration = (sampleRate * 0.06).toInt()
            val buffer = ShortArray(duration)
            for (i in 0 until duration) {
                val t = i.toDouble() / sampleRate
                val freq = 520.0
                val decay = 1.0 - (i.toDouble() / duration)
                val wave = sin(2.0 * Math.PI * freq * t)
                buffer[i] = (wave * decay * 16000).toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
            }
            buffer
        }
    }

    fun playVictory() {
        vibrate(100, 220)
        playSynthesizedWave { sampleRate, _ ->
            val duration = (sampleRate * 0.5).toInt()
            val buffer = ShortArray(duration)
            val notes = listOf(523.25, 659.25, 783.99, 1046.50) // C5, E5, G5, C6 arpeggio
            val noteDuration = duration / notes.size
            for (i in 0 until duration) {
                val noteIdx = (i / noteDuration).coerceIn(0, notes.size - 1)
                val t = (i % noteDuration).toDouble() / sampleRate
                val freq = notes[noteIdx]
                val decay = 1.0 - ((i % noteDuration).toDouble() / noteDuration) * 0.6
                val wave = sin(2.0 * Math.PI * freq * t)
                buffer[i] = (wave * decay * 22000).toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
            }
            buffer
        }
    }

    fun playDefeat() {
        vibrate(90, 180)
        playSynthesizedWave { sampleRate, _ ->
            val duration = (sampleRate * 0.45).toInt()
            val buffer = ShortArray(duration)
            val notes = listOf(440.0, 392.0, 349.23, 293.66) // A4, G4, F4, D4 descending
            val noteDuration = duration / notes.size
            for (i in 0 until duration) {
                val noteIdx = (i / noteDuration).coerceIn(0, notes.size - 1)
                val t = (i % noteDuration).toDouble() / sampleRate
                val freq = notes[noteIdx]
                val decay = 1.0 - ((i % noteDuration).toDouble() / noteDuration) * 0.7
                val wave = sin(2.0 * Math.PI * freq * t)
                buffer[i] = (wave * decay * 20000).toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
            }
            buffer
        }
    }
}

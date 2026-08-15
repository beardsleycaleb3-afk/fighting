/**
 * AudioClock - Audio Synchronization, BPM Beat Clock & SFX Scheduler
 * Extends Clock to synchronize 8-bit sound effects, music tempo, and combat beat markers.
 */
import { Clock } from './clock.js';

export class AudioClock extends Clock {
  constructor(bpm = 130) {
    super('AudioClock');
    this.bpm = bpm;
    this.beatInterval = 60 / bpm; // duration of 1 quarter note in seconds
    this.beatAccumulator = 0;
    this.currentBeat = 0;
    this.onBeat = null;
  }

  setBPM(bpm) {
    this.bpm = Math.max(30, bpm);
    this.beatInterval = 60 / this.bpm;
  }

  tick(overrideTimestamp = null) {
    super.tick(overrideTimestamp);

    if (this.isPaused) return this;

    this.beatAccumulator += this.deltaTime;
    if (this.beatAccumulator >= this.beatInterval) {
      this.beatAccumulator -= this.beatInterval;
      this.currentBeat++;
      if (this.onBeat) {
        this.onBeat(this.currentBeat, this.currentBeat % 4);
      }
    }

    return this;
  }

  resetBeats() {
    this.currentBeat = 0;
    this.beatAccumulator = 0;
  }
}

export const audioClock = new AudioClock(130);

/**
 * GameClock - Match Round Timer & 60Hz Fixed-Step Game State Clock
 * Extends Clock to manage arcade 99-second round countdowns, hit-freeze frames, and match states.
 */
import { Clock } from './clock.js';

export class GameClock extends Clock {
  constructor() {
    super('GameClock');
    this.roundDuration = 99; // 99 seconds classic arcade timer
    this.roundRemaining = 99;
    this.fixedStep = 1 / 60; // 60Hz fixed update
    this.accumulator = 0;
    this.hitStopFrames = 0;
    this.roundActive = false;
    this.onRoundTimeout = null;
    this.onSecondTick = null;
    this.lastSecondRecorded = 99;
  }

  startRound(duration = 99) {
    this.roundDuration = duration;
    this.roundRemaining = duration;
    this.lastSecondRecorded = Math.ceil(duration);
    this.roundActive = true;
    this.start();
  }

  stopRound() {
    this.roundActive = false;
  }

  triggerHitStop(frames = 6) {
    this.hitStopFrames = frames;
  }

  tick(overrideTimestamp = null) {
    super.tick(overrideTimestamp);

    if (this.isPaused) return this;

    // Handle hit-stop freeze frames
    if (this.hitStopFrames > 0) {
      this.hitStopFrames--;
      return this;
    }

    if (this.roundActive) {
      this.roundRemaining -= this.deltaTime;
      const currentIntSec = Math.max(0, Math.ceil(this.roundRemaining));

      if (currentIntSec !== this.lastSecondRecorded) {
        this.lastSecondRecorded = currentIntSec;
        if (this.onSecondTick) this.onSecondTick(currentIntSec);
      }

      if (this.roundRemaining <= 0) {
        this.roundRemaining = 0;
        this.roundActive = false;
        if (this.onRoundTimeout) this.onRoundTimeout();
      }
    }

    return this;
  }

  getFormattedTime() {
    const sec = Math.ceil(this.roundRemaining);
    return sec < 10 ? `0${sec}` : `${sec}`;
  }
}

export const gameClock = new GameClock();

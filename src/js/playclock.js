/**
 * PlayClock - Turn Windows, Combo Windows & Quick-Time Action Clocks
 * Extends Clock to handle rapid combat inputs, combo decay windows, and dialogue pacing.
 */
import { Clock } from './clock.js';

export class PlayClock extends Clock {
  constructor() {
    super('PlayClock');
    this.comboDecayWindow = 1.8; // seconds before combo counter resets
    this.comboTimeLeft = 0;
    this.qteWindow = 0;
    this.isQteActive = false;
    this.onComboTimeout = null;
    this.onQteTimeout = null;
  }

  registerHit(duration = 1.8) {
    this.comboDecayWindow = duration;
    this.comboTimeLeft = duration;
  }

  startQTE(duration = 2.0, onTimeout = null) {
    this.qteWindow = duration;
    this.isQteActive = true;
    this.onQteTimeout = onTimeout;
  }

  resolveQTE() {
    this.isQteActive = false;
    this.qteWindow = 0;
  }

  tick(overrideTimestamp = null) {
    super.tick(overrideTimestamp);

    if (this.isPaused) return this;

    // Combo Decay Tracking
    if (this.comboTimeLeft > 0) {
      this.comboTimeLeft -= this.deltaTime;
      if (this.comboTimeLeft <= 0) {
        this.comboTimeLeft = 0;
        if (this.onComboTimeout) this.onComboTimeout();
      }
    }

    // QTE Window Tracking
    if (this.isQteActive && this.qteWindow > 0) {
      this.qteWindow -= this.deltaTime;
      if (this.qteWindow <= 0) {
        this.isQteActive = false;
        this.qteWindow = 0;
        if (this.onQteTimeout) this.onQteTimeout();
      }
    }

    return this;
  }

  getComboProgress() {
    if (this.comboDecayWindow <= 0) return 0;
    return Math.max(0, this.comboTimeLeft / this.comboDecayWindow);
  }
}

export const playClock = new PlayClock();

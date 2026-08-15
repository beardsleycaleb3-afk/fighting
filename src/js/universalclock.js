/**
 * UniversalClock - Master Timing Coordinator & Global Time Dilation
 * Extends Clock to synchronize GameClock, PlayClock, and AudioClock across the engine.
 */
import { Clock } from './clock.js';
import { gameClock } from './gameclock.js';
import { playClock } from './playclock.js';
import { audioClock } from './audioclock.js';

export class UniversalClock extends Clock {
  constructor() {
    super('UniversalClock');
    this.clocks = new Map([
      ['game', gameClock],
      ['play', playClock],
      ['audio', audioClock]
    ]);
    this.globalDilation = 1.0;
    this.isTicking = false;
    this.rafId = null;
  }

  registerClock(name, clockInstance) {
    this.clocks.set(name, clockInstance);
    return this;
  }

  getClock(name) {
    return this.clocks.get(name);
  }

  setGlobalTimeScale(scale) {
    this.globalDilation = Math.max(0, scale);
    this.setTimeScale(this.globalDilation);
    this.clocks.forEach(clock => clock.setTimeScale(this.globalDilation));
  }

  tick(timestamp = null) {
    const now = timestamp !== null ? timestamp : performance.now();
    super.tick(now);

    if (!this.isPaused) {
      this.clocks.forEach(clock => clock.tick(now));
    }
    return this;
  }

  startGlobalLoop() {
    if (this.isTicking) return;
    this.isTicking = true;
    this.start();

    const loop = (timestamp) => {
      if (!this.isTicking) return;
      this.tick(timestamp);
      this.rafId = requestAnimationFrame(loop);
    };
    this.rafId = requestAnimationFrame(loop);
  }

  stopGlobalLoop() {
    this.isTicking = false;
    if (this.rafId) {
      cancelAnimationFrame(this.rafId);
      this.rafId = null;
    }
  }

  pauseAll() {
    this.pause();
    this.clocks.forEach(c => c.pause());
  }

  resumeAll() {
    this.resume();
    this.clocks.forEach(c => c.resume());
  }
}

export const universalClock = new UniversalClock();

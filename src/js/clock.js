/**
 * Clock - High-Precision Base Timing Engine
 * Tracks elapsed time, delta time (dt), time scale (slow-mo / turbo), and frame rate.
 */
export class Clock {
  constructor(name = 'Clock') {
    this.name = name;
    this.startTime = performance.now();
    this.lastTime = this.startTime;
    this.currentTime = this.startTime;
    this.elapsedTime = 0;
    this.deltaTime = 0;
    this.timeScale = 1.0;
    this.isPaused = false;
    this.tickListeners = new Set();
  }

  start() {
    this.startTime = performance.now();
    this.lastTime = this.startTime;
    this.currentTime = this.startTime;
    this.elapsedTime = 0;
    this.isPaused = false;
    return this;
  }

  tick(overrideTimestamp = null) {
    if (this.isPaused) {
      this.deltaTime = 0;
      return this;
    }

    const now = overrideTimestamp !== null ? overrideTimestamp : performance.now();
    const rawDelta = (now - this.lastTime) / 1000;
    this.deltaTime = Math.min(rawDelta * this.timeScale, 0.1); // Clamp max step to 100ms
    this.elapsedTime += this.deltaTime;
    this.lastTime = now;
    this.currentTime = now;

    this.tickListeners.forEach(listener => {
      try {
        listener(this.deltaTime, this.elapsedTime, this);
      } catch (err) {
        console.error(`Clock [${this.name}] listener error:`, err);
      }
    });

    return this;
  }

  pause() {
    this.isPaused = true;
  }

  resume() {
    if (this.isPaused) {
      this.lastTime = performance.now();
      this.isPaused = false;
    }
  }

  setTimeScale(scale) {
    this.timeScale = Math.max(0, scale);
  }

  onTick(listener) {
    this.tickListeners.add(listener);
    return () => this.tickListeners.delete(listener);
  }

  reset() {
    this.start();
  }
}

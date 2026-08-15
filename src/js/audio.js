/**
 * Time Tournament - 8-Bit / Arcade Sound & Haptic Synthesizer via Web Audio API
 */
class ArcadeAudioEngine {
  constructor() {
    this.ctx = null;
    this.volume = 0.8;
    this.soundEnabled = true;
    this.vibrationEnabled = true;
  }

  init() {
    if (!this.soundEnabled) return;
    if (!this.ctx) {
      const AudioContext = window.AudioContext || window.webkitAudioContext;
      this.ctx = new AudioContext();
    }
    if (this.ctx.state === 'suspended') {
      this.ctx.resume();
    }
  }

  vibrate(ms = 25) {
    if (this.vibrationEnabled && navigator.vibrate) {
      try {
        navigator.vibrate(ms);
      } catch (_) {}
    }
  }

  setVolume(val) {
    this.volume = Math.max(0, Math.min(1, val));
  }

  setSoundEnabled(enabled) {
    this.soundEnabled = enabled;
    if (!enabled && this.ctx && this.ctx.state === 'running') {
      this.ctx.suspend();
    }
  }

  playPunch() {
    if (!this.soundEnabled) return;
    this.init();
    this.vibrate(15);
    const ctx = this.ctx;
    if (!ctx) return;
    const osc = ctx.createOscillator();
    const gain = ctx.createGain();
    osc.type = 'sawtooth';
    osc.frequency.setValueAtTime(180, ctx.currentTime);
    osc.frequency.exponentialRampToValueAtTime(40, ctx.currentTime + 0.08);
    gain.gain.setValueAtTime(0.3 * this.volume, ctx.currentTime);
    gain.gain.exponentialRampToValueAtTime(0.01, ctx.currentTime + 0.08);
    osc.connect(gain);
    gain.connect(ctx.destination);
    osc.start();
    osc.stop(ctx.currentTime + 0.08);
  }

  playKick() {
    if (!this.soundEnabled) return;
    this.init();
    this.vibrate(25);
    const ctx = this.ctx;
    if (!ctx) return;
    const osc = ctx.createOscillator();
    const gain = ctx.createGain();
    osc.type = 'square';
    osc.frequency.setValueAtTime(240, ctx.currentTime);
    osc.frequency.exponentialRampToValueAtTime(50, ctx.currentTime + 0.12);
    gain.gain.setValueAtTime(0.4 * this.volume, ctx.currentTime);
    gain.gain.exponentialRampToValueAtTime(0.01, ctx.currentTime + 0.12);
    osc.connect(gain);
    gain.connect(ctx.destination);
    osc.start();
    osc.stop(ctx.currentTime + 0.12);
  }

  playSpecial() {
    if (!this.soundEnabled) return;
    this.init();
    this.vibrate(40);
    const ctx = this.ctx;
    if (!ctx) return;
    const osc = ctx.createOscillator();
    const gain = ctx.createGain();
    osc.type = 'sawtooth';
    osc.frequency.setValueAtTime(120, ctx.currentTime);
    osc.frequency.linearRampToValueAtTime(880, ctx.currentTime + 0.25);
    osc.frequency.exponentialRampToValueAtTime(80, ctx.currentTime + 0.45);
    gain.gain.setValueAtTime(0.5 * this.volume, ctx.currentTime);
    gain.gain.exponentialRampToValueAtTime(0.01, ctx.currentTime + 0.45);
    osc.connect(gain);
    gain.connect(ctx.destination);
    osc.start();
    osc.stop(ctx.currentTime + 0.45);
  }

  playFinalBlowSmash() {
    if (!this.soundEnabled) return;
    this.init();
    this.vibrate(100);
    const ctx = this.ctx;
    if (!ctx) return;

    // Sub-bass impact rumble
    const subOsc = ctx.createOscillator();
    const subGain = ctx.createGain();
    subOsc.type = 'sine';
    subOsc.frequency.setValueAtTime(90, ctx.currentTime);
    subOsc.frequency.exponentialRampToValueAtTime(25, ctx.currentTime + 0.6);
    subGain.gain.setValueAtTime(0.7 * this.volume, ctx.currentTime);
    subGain.gain.exponentialRampToValueAtTime(0.01, ctx.currentTime + 0.6);
    subOsc.connect(subGain);
    subGain.connect(ctx.destination);
    subOsc.start();
    subOsc.stop(ctx.currentTime + 0.6);

    // Spirit Animal roar harmonic
    const roarOsc = ctx.createOscillator();
    const roarGain = ctx.createGain();
    roarOsc.type = 'sawtooth';
    roarOsc.frequency.setValueAtTime(320, ctx.currentTime);
    roarOsc.frequency.linearRampToValueAtTime(740, ctx.currentTime + 0.2);
    roarOsc.frequency.exponentialRampToValueAtTime(110, ctx.currentTime + 0.7);
    roarGain.gain.setValueAtTime(0.5 * this.volume, ctx.currentTime);
    roarGain.gain.exponentialRampToValueAtTime(0.01, ctx.currentTime + 0.7);
    roarOsc.connect(roarGain);
    roarGain.connect(ctx.destination);
    roarOsc.start();
    roarOsc.stop(ctx.currentTime + 0.7);
  }

  playSelect() {
    if (!this.soundEnabled) return;
    this.init();
    this.vibrate(10);
    const ctx = this.ctx;
    if (!ctx) return;
    const osc = ctx.createOscillator();
    const gain = ctx.createGain();
    osc.type = 'triangle';
    osc.frequency.setValueAtTime(440, ctx.currentTime);
    osc.frequency.setValueAtTime(880, ctx.currentTime + 0.06);
    gain.gain.setValueAtTime(0.25 * this.volume, ctx.currentTime);
    gain.gain.exponentialRampToValueAtTime(0.01, ctx.currentTime + 0.15);
    osc.connect(gain);
    gain.connect(ctx.destination);
    osc.start();
    osc.stop(ctx.currentTime + 0.15);
  }

  playTypewriter() {
    if (!this.soundEnabled) return;
    this.init();
    const ctx = this.ctx;
    if (!ctx) return;
    const osc = ctx.createOscillator();
    const gain = ctx.createGain();
    osc.type = 'square';
    osc.frequency.setValueAtTime(800 + Math.random() * 200, ctx.currentTime);
    gain.gain.setValueAtTime(0.06 * this.volume, ctx.currentTime);
    gain.gain.exponentialRampToValueAtTime(0.001, ctx.currentTime + 0.025);
    osc.connect(gain);
    gain.connect(ctx.destination);
    osc.start();
    osc.stop(ctx.currentTime + 0.025);
  }

  playCombo(count) {
    if (!this.soundEnabled) return;
    this.init();
    this.vibrate(15);
    const ctx = this.ctx;
    if (!ctx) return;
    const baseFreq = Math.min(300 + count * 80, 1200);
    const osc = ctx.createOscillator();
    const gain = ctx.createGain();
    osc.type = 'sine';
    osc.frequency.setValueAtTime(baseFreq, ctx.currentTime);
    osc.frequency.linearRampToValueAtTime(baseFreq * 1.5, ctx.currentTime + 0.08);
    gain.gain.setValueAtTime(0.35 * this.volume, ctx.currentTime);
    gain.gain.exponentialRampToValueAtTime(0.01, ctx.currentTime + 0.1);
    osc.connect(gain);
    gain.connect(ctx.destination);
    osc.start();
    osc.stop(ctx.currentTime + 0.1);
  }

  playVictory() {
    if (!this.soundEnabled) return;
    this.init();
    const ctx = this.ctx;
    if (!ctx) return;
    const notes = [523.25, 659.25, 783.99, 1046.50];
    notes.forEach((freq, index) => {
      const osc = ctx.createOscillator();
      const gain = ctx.createGain();
      osc.type = 'square';
      osc.frequency.setValueAtTime(freq, ctx.currentTime + index * 0.12);
      gain.gain.setValueAtTime(0, ctx.currentTime + index * 0.12);
      gain.gain.linearRampToValueAtTime(0.3 * this.volume, ctx.currentTime + index * 0.12 + 0.02);
      gain.gain.exponentialRampToValueAtTime(0.01, ctx.currentTime + index * 0.12 + 0.25);
      osc.connect(gain);
      gain.connect(ctx.destination);
      osc.start(ctx.currentTime + index * 0.12);
      osc.stop(ctx.currentTime + index * 0.12 + 0.25);
    });
  }
}

export const sound = new ArcadeAudioEngine();

/**
 * Manager - Master Engine Coordinator & Subsystem Facade
 * Provides a unified entry point and orchestration bridge for all engine managers and clocks.
 */
import { srcManager } from './srcmanager.js';
import { assetManager } from './assetmanager.js';
import { spriteManager } from './spritemanager.js';
import { jsManager } from './jsmanager.js';
import { universalClock } from './universalclock.js';
import { gameClock } from './gameclock.js';
import { playClock } from './playclock.js';
import { audioClock } from './audioclock.js';
import { contractor } from './contractor.js';
import { communicator } from './communicator.js';
import { composer } from './composer.js';
import { sound } from './audio.js';

export class Manager {
  constructor() {
    this.src = srcManager;
    this.assets = assetManager;
    this.sprites = spriteManager;
    this.js = jsManager;
    this.universalClock = universalClock;
    this.gameClock = gameClock;
    this.playClock = playClock;
    this.audioClock = audioClock;
    this.contractor = contractor;
    this.communicator = communicator;
    this.composer = composer;
    this.audio = sound;
    this.isInitialized = false;
  }

  async initialize() {
    if (this.isInitialized) return this;

    // Register subsystem instances in SrcManager
    this.src.register('SrcManager', this.src);
    this.src.register('AssetManager', this.assets);
    this.src.register('SpriteManager', this.sprites);
    this.src.register('JSManager', this.js);
    this.src.register('UniversalClock', this.universalClock);
    this.src.register('Contractor', this.contractor);
    this.src.register('Communicator', this.communicator);
    this.src.register('Composer', this.composer);

    // Setup global clocks
    this.universalClock.startGlobalLoop();

    // Setup message broker bridge
    this.communicator.on('combat:hit', (data) => {
      this.gameClock.triggerHitStop(4);
      this.playClock.registerHit(1.8);
    });

    this.communicator.on('match:pause', () => {
      this.universalClock.pauseAll();
    });

    this.communicator.on('match:resume', () => {
      this.universalClock.resumeAll();
    });

    this.isInitialized = true;
    return this;
  }

  getSystemDiagnostics() {
    return {
      manifest: this.src.getManifest(),
      assets: {
        loaded: this.assets.loadedAssets,
        total: this.assets.totalAssets
      },
      clocks: {
        gameTimeRemaining: this.gameClock.getFormattedTime(),
        audioBeat: this.audioClock.currentBeat,
        globalTimeScale: this.universalClock.globalDilation
      },
      plugins: this.js.getSystemInfo(),
      historyLength: this.communicator.history.length
    };
  }
}

export const manager = new Manager();

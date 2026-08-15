/**
 * SpriteManager - Sprite Atlas Slicing, Animation Cycles & Hitbox Tracking
 * Controls multi-directional animation states (jab, kick, idle, smash, hurt) and frame timing.
 */
import { JAB_SPRITE_FRAMES } from './sprites.js';

export class SpriteAnimation {
  constructor(name, frames = [], frameRate = 12, loop = true) {
    this.name = name;
    this.frames = frames;
    this.frameRate = frameRate;
    this.frameDuration = 1000 / frameRate;
    this.loop = loop;
    this.currentIndex = 0;
    this.lastFrameTime = 0;
    this.isFinished = false;
  }

  reset() {
    this.currentIndex = 0;
    this.lastFrameTime = 0;
    this.isFinished = false;
  }

  update(timestamp) {
    if (this.frames.length === 0 || this.isFinished) return;
    if (this.lastFrameTime === 0) {
      this.lastFrameTime = timestamp;
      return;
    }

    const elapsed = timestamp - this.lastFrameTime;
    if (elapsed >= this.frameDuration) {
      const stepCount = Math.floor(elapsed / this.frameDuration);
      this.lastFrameTime = timestamp - (elapsed % this.frameDuration);
      this.currentIndex += stepCount;

      if (this.currentIndex >= this.frames.length) {
        if (this.loop) {
          this.currentIndex = this.currentIndex % this.frames.length;
        } else {
          this.currentIndex = this.frames.length - 1;
          this.isFinished = true;
        }
      }
    }
  }

  getCurrentFrame() {
    if (this.frames.length === 0) return null;
    return this.frames[this.currentIndex];
  }
}

export class SpriteManager {
  constructor() {
    this.animations = new Map();
    this.currentAnimation = null;
    this.orientation = 'east'; // 'east', 'west'
    this.hitboxes = new Map();
    this.initDefaultAnimations();
  }

  initDefaultAnimations() {
    this.registerAnimation('jab', JAB_SPRITE_FRAMES, 10, false);
    this.registerAnimation('idle', JAB_SPRITE_FRAMES.slice(0, 1), 2, true);
    this.play('idle');
  }

  registerAnimation(name, frames, fps = 12, loop = true) {
    const anim = new SpriteAnimation(name, frames, fps, loop);
    this.animations.set(name, anim);
    return anim;
  }

  play(name, loopOverride = null, onComplete = null) {
    const anim = this.animations.get(name);
    if (!anim) return;
    if (loopOverride !== null) anim.loop = loopOverride;
    anim.reset();
    this.currentAnimation = anim;
    this.onCompleteCallback = onComplete;
  }

  setOrientation(dir) {
    this.orientation = dir;
  }

  update(timestamp) {
    if (this.currentAnimation) {
      this.currentAnimation.update(timestamp);
      if (this.currentAnimation.isFinished && this.onCompleteCallback) {
        const cb = this.onCompleteCallback;
        this.onCompleteCallback = null;
        cb();
      }
    }
  }

  draw(ctx, x, y, width, height) {
    if (!this.currentAnimation) return false;
    const frame = this.currentAnimation.getCurrentFrame();
    if (!frame) return false;

    ctx.save();
    if (this.orientation === 'west') {
      ctx.translate(x + width, y);
      ctx.scale(-1, 1);
      ctx.drawImage(frame, 0, 0, width, height);
    } else {
      ctx.drawImage(frame, x, y, width, height);
    }
    ctx.restore();
    return true;
  }
}

export const spriteManager = new SpriteManager();

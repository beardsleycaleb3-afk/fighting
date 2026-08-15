/**
 * Time Tournament - Fighter Sprite Animation Module
 * Imports and manages fighter east jab sequence frames:
 * - assets/sprites/fighter/east/jab/frame_000.png
 * - assets/sprites/fighter/east/jab/frame_001.png
 * - assets/sprites/fighter/east/jab/frame_002.png
 */

export const JAB_SPRITE_FRAMES = [
  'assets/sprites/fighter/east/jab/frame_000.png',
  'assets/sprites/fighter/east/jab/frame_001.png',
  'assets/sprites/fighter/east/jab/frame_002.png'
];

export class SpriteAnimationEngine {
  constructor() {
    this.images = [];
    this.isLoaded = false;
    this.currentFrame = 0;
    this.isPlaying = false;
    this.fps = 12;
    this.lastFrameTime = 0;
    this.frameDuration = 1000 / this.fps;
    this.onFrameChange = null;
    this.onComplete = null;
    this.preload();
  }

  preload() {
    let loadedCount = 0;
    this.images = JAB_SPRITE_FRAMES.map((src, index) => {
      const img = new Image();
      img.onload = () => {
        loadedCount++;
        if (loadedCount === JAB_SPRITE_FRAMES.length) {
          this.isLoaded = true;
        }
      };
      img.src = src;
      return img;
    });
  }

  play(onCompleteCallback = null) {
    this.currentFrame = 0;
    this.isPlaying = true;
    this.lastFrameTime = performance.now();
    this.onComplete = onCompleteCallback;
  }

  update(currentTime) {
    if (!this.isPlaying) return;

    if (currentTime - this.lastFrameTime >= this.frameDuration) {
      this.currentFrame++;
      this.lastFrameTime = currentTime;
      if (this.onFrameChange) {
        this.onFrameChange(this.currentFrame);
      }

      if (this.currentFrame >= JAB_SPRITE_FRAMES.length) {
        this.currentFrame = 0;
        this.isPlaying = false;
        if (this.onComplete) {
          this.onComplete();
        }
      }
    }
  }

  getCurrentImage() {
    if (this.images.length > 0 && this.images[this.currentFrame]) {
      return this.images[this.currentFrame];
    }
    return null;
  }

  drawFrame(ctx, x, y, width, height) {
    const img = this.getCurrentImage();
    if (img && img.complete && img.naturalWidth !== 0) {
      ctx.save();
      ctx.drawImage(img, x, y, width, height);
      ctx.restore();
      return true;
    }
    return false;
  }
}

export const spriteEngine = new SpriteAnimationEngine();

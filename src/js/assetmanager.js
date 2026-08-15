/**
 * AssetManager - Asset Preloading, Caching & Resource Registry
 * Manages images, audio buffers, sprite sheets, and JSON configs with progress tracking.
 */
export class AssetManager {
  constructor() {
    this.images = new Map();
    this.audioBuffers = new Map();
    this.jsonCache = new Map();
    this.queue = [];
    this.totalAssets = 0;
    this.loadedAssets = 0;
    this.progressCallbacks = [];
  }

  queueImage(key, url) {
    this.queue.push({ type: 'image', key, url });
    this.totalAssets++;
    return this;
  }

  queueAudio(key, url) {
    this.queue.push({ type: 'audio', key, url });
    this.totalAssets++;
    return this;
  }

  queueJson(key, url) {
    this.queue.push({ type: 'json', key, url });
    this.totalAssets++;
    return this;
  }

  onProgress(callback) {
    this.progressCallbacks.push(callback);
    return this;
  }

  notifyProgress() {
    const pct = this.totalAssets === 0 ? 1 : this.loadedAssets / this.totalAssets;
    this.progressCallbacks.forEach(cb => cb(pct, this.loadedAssets, this.totalAssets));
  }

  async loadAll() {
    const loadPromises = this.queue.map(item => this.loadItem(item));
    await Promise.all(loadPromises);
    this.queue = [];
    return this;
  }

  async loadItem(item) {
    try {
      if (item.type === 'image') {
        const img = new Image();
        img.crossOrigin = 'anonymous';
        img.src = item.url;
        await new Promise((res, rej) => {
          img.onload = () => res(img);
          img.onerror = () => rej(new Error(`Failed image: ${item.url}`));
        });
        this.images.set(item.key, img);
      } else if (item.type === 'json') {
        const res = await fetch(item.url);
        const data = await res.json();
        this.jsonCache.set(item.key, data);
      }
      this.loadedAssets++;
      this.notifyProgress();
    } catch (err) {
      console.warn(`AssetManager: Fallback for ${item.key}:`, err.message);
      this.loadedAssets++;
      this.notifyProgress();
    }
  }

  getImage(key) {
    return this.images.get(key) || null;
  }

  getJson(key) {
    return this.jsonCache.get(key) || null;
  }

  has(key) {
    return this.images.has(key) || this.audioBuffers.has(key) || this.jsonCache.has(key);
  }

  clear() {
    this.images.clear();
    this.audioBuffers.clear();
    this.jsonCache.clear();
    this.queue = [];
    this.loadedAssets = 0;
    this.totalAssets = 0;
  }
}

export const assetManager = new AssetManager();

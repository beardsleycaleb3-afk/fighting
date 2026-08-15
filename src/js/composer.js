/**
 * Composer - Scene, UI & Visual Layer Composition Engine
 * Assembles dynamic view components, HUD overlays, dialogue speech bubbles, and modal layers.
 */
import { communicator } from './communicator.js';

export class Composer {
  constructor() {
    this.layers = new Map();
    this.activeScene = 'menu';
  }

  registerLayer(name, elementId) {
    const el = document.getElementById(elementId);
    if (el) {
      this.layers.set(name, el);
    }
    return this;
  }

  transitionTo(sceneName) {
    this.activeScene = sceneName;
    communicator.emit('scene:changed', { scene: sceneName });
  }

  composeDamagePopup(x, y, damage, isCrit = false) {
    const popup = document.createElement('div');
    popup.className = 'damage-popup';
    popup.style.position = 'absolute';
    popup.style.left = `${x}px`;
    popup.style.top = `${y}px`;
    popup.style.color = isCrit ? '#FF1A5E' : '#FFC837';
    popup.style.fontFamily = 'var(--font-mono)';
    popup.style.fontSize = isCrit ? '14px' : '11px';
    popup.style.fontWeight = '900';
    popup.style.pointerEvents = 'none';
    popup.style.transition = 'all 0.6s ease-out';
    popup.style.zIndex = '50';
    popup.textContent = isCrit ? `💥 -${damage}` : `-${damage}`;

    document.body.appendChild(popup);

    requestAnimationFrame(() => {
      popup.style.transform = 'translateY(-25px) scale(1.1)';
      popup.style.opacity = '0';
    });

    setTimeout(() => {
      if (popup.parentNode) popup.parentNode.removeChild(popup);
    }, 650);
  }

  composeComboBanner(count, moveName = null) {
    const text = moveName ? `${count} HIT COMBO! (${moveName})` : `${count} HIT COMBO!`;
    communicator.emit('ui:combo', { count, moveName, text });
  }

  setLayerVisibility(name, visible) {
    const el = this.layers.get(name);
    if (el) {
      el.style.display = visible ? 'flex' : 'none';
    }
  }
}

export const composer = new Composer();

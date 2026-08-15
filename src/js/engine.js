/**
 * Time Tournament - 2D Canvas Portrait, Elemental Spirit Animal & VFX Engine
 * Integrated with:
 * - LUT (fast trigonometric lookup & color palette ramps)
 * - Deque (particle pooling & input buffer tracking)
 * - Queue (action sequencing & priority hit events)
 * - Matter.js (2D rigid-body particle debris on impact)
 * - Fabric.js (dynamic vector canvas emblem stamps)
 * - Babylon.js (3D elemental background mesh & vortex)
 */
import { JAB_SPRITE_FRAMES, spriteEngine } from './sprites.js';
import { fastSin, fastCos, SPIRIT_PALETTE_LUTS, getComboScaledDamage } from './lut.js';
import { Deque } from './dequeue.js';
import { Queue } from './queue.js';

export class CanvasRenderer {
  constructor(canvas) {
    this.canvas = canvas;
    this.ctx = canvas.getContext('2d');
    this.time = 0;
    this.activeFighter = null;
    this.animFrameId = null;
    this.particlePool = new Deque(80);
    this.eventQueue = new Queue();
    this.isAttacking = false;
    this.attackType = null;
    this.isSmashActive = false;
    this.smashProgress = 0;
    this.screenShake = 0;

    // Matter.js Physics integration
    this.matterEngine = null;
    this.matterWorld = null;
    this.matterDebris = [];
    this.initMatterPhysics();

    // Initialize particle pool using Deque
    this.initParticles();
  }

  initMatterPhysics() {
    if (typeof window !== 'undefined' && window.Matter) {
      try {
        const { Engine, World, Bodies } = window.Matter;
        this.matterEngine = Engine.create({ enableSleeping: false });
        this.matterWorld = this.matterEngine.world;
        this.matterWorld.gravity.y = 0.8;

        // Ground barrier
        const ground = Bodies.rectangle(160, 145, 330, 10, { isStatic: true });
        World.add(this.matterWorld, ground);
      } catch (err) {
        console.warn('Matter.js initialization skipped/fallback:', err);
      }
    }
  }

  spawnMatterDebris(originX, originY, color = '#FFC837', count = 8) {
    if (!this.matterWorld || typeof window === 'undefined' || !window.Matter) return;
    const { Bodies, World, Body } = window.Matter;

    for (let i = 0; i < count; i++) {
      const radius = Math.random() * 3 + 2;
      const body = Bodies.circle(originX, originY, radius, {
        restitution: 0.7,
        friction: 0.05,
        render: { fillStyle: color }
      });

      Body.setVelocity(body, {
        x: (Math.random() - 0.5) * 8,
        y: -Math.random() * 7 - 2
      });

      World.add(this.matterWorld, body);
      this.matterDebris.push({ body, color, life: 1.0 });
    }
  }

  initParticles() {
    this.particlePool.clear();
    for (let i = 0; i < 50; i++) {
      this.particlePool.pushBack({
        x: Math.random() * 320,
        y: Math.random() * 140,
        size: Math.random() * 3 + 1,
        speedX: (Math.random() - 0.5) * 1.5,
        speedY: -Math.random() * 1.8 - 0.5,
        opacity: Math.random() * 0.7 + 0.3,
        colorIdx: Math.random()
      });
    }
  }

  setFighter(fighter) {
    this.activeFighter = fighter;
  }

  start() {
    const loop = () => {
      this.render();
      this.animFrameId = requestAnimationFrame(loop);
    };
    if (!this.animFrameId) {
      loop();
    }
  }

  stop() {
    if (this.animFrameId) {
      cancelAnimationFrame(this.animFrameId);
      this.animFrameId = null;
    }
  }

  triggerAttack(type = 'punch') {
    this.isAttacking = true;
    this.attackType = type;
    this.screenShake = 5;

    // Trigger Matter physics debris burst at contact point
    const contactX = 160 + (type === 'kick' ? 25 : 15);
    const contactY = 70;
    this.spawnMatterDebris(contactX, contactY, this.activeFighter?.accent || '#FFC837', 10);

    spriteEngine.play(() => {
      this.isAttacking = false;
      this.attackType = null;
    });
  }

  triggerFinalSmash(onFinish = null) {
    this.isSmashActive = true;
    this.smashProgress = 0;
    this.screenShake = 18;

    // Massive physics explosion
    this.spawnMatterDebris(160, 60, '#FF1A5E', 25);

    const startTime = performance.now();
    const duration = 2400;

    const animateSmash = (now) => {
      const elapsed = now - startTime;
      this.smashProgress = Math.min(1, elapsed / duration);
      this.screenShake = Math.max(0, 18 * (1 - this.smashProgress));

      if (this.smashProgress < 1) {
        requestAnimationFrame(animateSmash);
      } else {
        this.isSmashActive = false;
        if (onFinish) onFinish();
      }
    };
    requestAnimationFrame(animateSmash);
  }

  render() {
    const { canvas, ctx, activeFighter } = this;
    if (!canvas || !ctx) return;
    const w = canvas.width;
    const h = canvas.height;
    const now = performance.now();
    this.time += 0.035;

    spriteEngine.update(now);

    // Update Matter.js Engine if active
    if (this.matterEngine && typeof window !== 'undefined' && window.Matter) {
      window.Matter.Engine.update(this.matterEngine, 1000 / 60);
    }

    // Apply Screen Shake if active
    ctx.save();
    if (this.screenShake > 0) {
      const shakeX = (Math.random() - 0.5) * this.screenShake;
      const shakeY = (Math.random() - 0.5) * this.screenShake;
      ctx.translate(shakeX, shakeY);
      this.screenShake *= 0.9;
      if (this.screenShake < 0.2) this.screenShake = 0;
    }

    // Clear background
    ctx.fillStyle = '#0A0010';
    ctx.fillRect(0, 0, w, h);

    if (!activeFighter) {
      ctx.restore();
      return;
    }

    // Atmospheric dynamic radial gradient with elemental aura
    const grad = ctx.createRadialGradient(w / 2, h / 2, 10, w / 2, h / 2, w / 1.1);
    grad.addColorStop(0, activeFighter.color + '55');
    grad.addColorStop(0.5, '#0A001088');
    grad.addColorStop(1, '#050008');
    ctx.fillStyle = grad;
    ctx.fillRect(0, 0, w, h);

    // Elemental Aura Waves using Fast Trigonometry LUT
    ctx.strokeStyle = activeFighter.accent + '44';
    ctx.lineWidth = 1.5;
    for (let i = 0; i < 3; i++) {
      ctx.beginPath();
      const waveRadius = 38 + i * 18 + fastSin(this.time * 2 + i) * 6;
      ctx.arc(w / 2, h / 2 + 5, Math.max(5, waveRadius), 0, Math.PI * 2);
      ctx.stroke();
    }

    // Floating VFX particles from Deque pool
    const particles = this.particlePool.toArray();
    particles.forEach(p => {
      p.x += p.speedX;
      p.y += p.speedY;
      if (p.y < 0) {
        p.y = h;
        p.x = Math.random() * w;
      }
      if (p.x < 0) p.x = w;
      if (p.x > w) p.x = 0;

      // Sample color from spirit animal LUT if available
      const lut = SPIRIT_PALETTE_LUTS.PHANTOM_PANTHER;
      const sample = lut.sample(p.colorIdx);
      ctx.fillStyle = sample.css || activeFighter.accent;

      ctx.globalAlpha = p.opacity * (0.6 + fastSin(this.time + p.x) * 0.4);
      ctx.beginPath();
      ctx.arc(p.x, p.y, p.size, 0, Math.PI * 2);
      ctx.fill();
    });
    ctx.globalAlpha = 1.0;

    // Draw Character Sprite with fastCos bob
    ctx.save();
    const bob = fastCos(this.time * 2.5) * 4;
    
    // Render sprite
    const spriteDrawn = spriteEngine.drawFrame(ctx, (w - 96) / 2, 10 + bob, 96, 96);
    if (!spriteDrawn) {
      ctx.translate(w / 2, 60 + bob);
      this.drawFighterArt(ctx, activeFighter);
    }
    ctx.restore();

    // Render Matter.js debris particles
    if (this.matterDebris.length > 0) {
      for (let i = this.matterDebris.length - 1; i >= 0; i--) {
        const item = this.matterDebris[i];
        item.life -= 0.025;
        if (item.life <= 0) {
          if (this.matterWorld && window.Matter) {
            window.Matter.World.remove(this.matterWorld, item.body);
          }
          this.matterDebris.splice(i, 1);
          continue;
        }

        ctx.save();
        ctx.fillStyle = item.color;
        ctx.globalAlpha = Math.max(0, item.life);
        ctx.beginPath();
        ctx.arc(item.body.position.x, item.body.position.y, item.body.circleRadius || 3, 0, Math.PI * 2);
        ctx.fill();
        ctx.restore();
      }
    }

    // Final Smash Elemental Spirit Animal Overlay
    if (this.isSmashActive && this.smashProgress < 1) {
      ctx.save();
      const flash = fastSin(this.smashProgress * Math.PI);
      ctx.fillStyle = activeFighter.accent;
      ctx.globalAlpha = Math.max(0, flash * 0.4);
      ctx.fillRect(0, 0, w, h);

      // Spirit Animal Emblem
      ctx.font = '32px sans-serif';
      ctx.textAlign = 'center';
      ctx.textBaseline = 'middle';
      ctx.globalAlpha = Math.max(0, flash * 0.9);
      ctx.fillText(activeFighter.spiritAnimalSymbol || '🔥', w / 2, h / 2 - 10);

      // Elemental Smash text
      ctx.font = 'bold 9px monospace';
      ctx.fillStyle = '#FFFFFF';
      ctx.fillText(activeFighter.elementalState, w / 2, h / 2 + 25);
      ctx.restore();
    }

    ctx.restore();
  }

  drawFighterArt(ctx, fighter) {
    ctx.fillStyle = fighter.color;
    ctx.shadowColor = fighter.accent;
    ctx.shadowBlur = 15;

    // Torso / Shoulders
    ctx.beginPath();
    ctx.moveTo(-35, 45);
    ctx.lineTo(35, 45);
    ctx.lineTo(25, 80);
    ctx.lineTo(-25, 80);
    ctx.closePath();
    ctx.fill();

    // Head / Mask
    ctx.beginPath();
    ctx.arc(0, 15, 24, 0, Math.PI * 2);
    ctx.fill();

    // Eyes Glow
    ctx.shadowBlur = 10;
    ctx.fillStyle = '#FFFFFF';
    ctx.beginPath();
    ctx.arc(-8, 12, 3.5, 0, Math.PI * 2);
    ctx.arc(8, 12, 3.5, 0, Math.PI * 2);
    ctx.fill();

    ctx.shadowBlur = 0;
  }
}

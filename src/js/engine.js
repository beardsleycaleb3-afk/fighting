/**
 * Time Tournament - 2D Canvas Portrait & VFX Engine
 */
export class CanvasRenderer {
  constructor(canvas) {
    this.canvas = canvas;
    this.ctx = canvas.getContext('2d');
    this.time = 0;
    this.activeFighter = null;
    this.animFrameId = null;
    this.particles = [];
    this.initParticles();
  }

  initParticles() {
    this.particles = [];
    for (let i = 0; i < 35; i++) {
      this.particles.push({
        x: Math.random() * 320,
        y: Math.random() * 220,
        size: Math.random() * 3 + 1,
        speedX: (Math.random() - 0.5) * 1.5,
        speedY: -Math.random() * 1.8 - 0.5,
        opacity: Math.random() * 0.7 + 0.3
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

  render() {
    const { canvas, ctx, activeFighter } = this;
    if (!canvas || !ctx) return;
    const w = canvas.width;
    const h = canvas.height;
    this.time += 0.035;

    // Clear background
    ctx.fillStyle = '#0F0318';
    ctx.fillRect(0, 0, w, h);

    if (!activeFighter) return;

    // Atmospheric dynamic radial gradient
    const grad = ctx.createRadialGradient(w / 2, h / 2, 10, w / 2, h / 2, w / 1.1);
    grad.addColorStop(0, activeFighter.color + '44');
    grad.addColorStop(0.6, '#0A001088');
    grad.addColorStop(1, '#050008');
    ctx.fillStyle = grad;
    ctx.fillRect(0, 0, w, h);

    // Grid Floor
    ctx.strokeStyle = activeFighter.color + '22';
    ctx.lineWidth = 1;
    for (let y = 140; y < h; y += 15) {
      ctx.beginPath();
      ctx.moveTo(0, y);
      ctx.lineTo(w, y);
      ctx.stroke();
    }

    // Aura rings
    ctx.save();
    ctx.translate(w / 2, h / 2 + 10);
    const pulse = 1 + Math.sin(this.time * 2.5) * 0.08;
    ctx.scale(pulse, pulse);

    ctx.strokeStyle = activeFighter.accent + '55';
    ctx.lineWidth = 2;
    ctx.beginPath();
    ctx.arc(0, 0, 68, 0, Math.PI * 2);
    ctx.stroke();

    ctx.strokeStyle = activeFighter.color + '33';
    ctx.lineWidth = 1;
    ctx.setLineDash([6, 6]);
    ctx.beginPath();
    ctx.arc(0, 0, 78, this.time, this.time + Math.PI * 2);
    ctx.stroke();
    ctx.setLineDash([]);
    ctx.restore();

    // Floating VFX particles
    ctx.fillStyle = activeFighter.accent;
    this.particles.forEach(p => {
      p.x += p.speedX;
      p.y += p.speedY;
      if (p.y < 0) {
        p.y = h;
        p.x = Math.random() * w;
      }
      if (p.x < 0) p.x = w;
      if (p.x > w) p.x = 0;

      ctx.globalAlpha = p.opacity * (0.6 + Math.sin(this.time + p.x) * 0.4);
      ctx.beginPath();
      ctx.arc(p.x, p.y, p.size, 0, Math.PI * 2);
      ctx.fill();
    });
    ctx.globalAlpha = 1.0;

    // Draw Stylized Retro Fighter Silhouette / Portrait
    ctx.save();
    const bob = Math.sin(this.time * 2) * 4;
    ctx.translate(w / 2, 70 + bob);

    // Fighter specific character render
    this.drawFighterArt(ctx, activeFighter);

    ctx.restore();
  }

  drawFighterArt(ctx, fighter) {
    // Character Head & Torso
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

    // Fighter distinct gear
    ctx.shadowBlur = 0;
    if (fighter.id === 'ninja') {
      // Scarf / Headband tails
      ctx.fillStyle = fighter.accent;
      ctx.beginPath();
      ctx.moveTo(-20, 15);
      ctx.quadraticCurveTo(-45, 10 + Math.sin(this.time * 4) * 8, -60, 25);
      ctx.lineTo(-20, 20);
      ctx.closePath();
      ctx.fill();
    } else if (fighter.id === 'mma') {
      // Cybernetic Headpiece & Visor
      ctx.fillStyle = '#FF9966';
      ctx.fillRect(-15, 8, 30, 4);
    } else if (fighter.id === 'boxer') {
      // Boxing Gloves
      ctx.fillStyle = '#FFC837';
      ctx.beginPath();
      ctx.arc(-38, 55, 14, 0, Math.PI * 2);
      ctx.arc(38, 55, 14, 0, Math.PI * 2);
      ctx.fill();
    } else if (fighter.id === 'wrestler') {
      // Roman Laurel / Crest
      ctx.fillStyle = '#FFD700';
      ctx.beginPath();
      ctx.arc(0, -6, 12, Math.PI, 0);
      ctx.stroke();
    } else if (fighter.id === 'valkyrie') {
      // Energy Wings
      ctx.strokeStyle = '#00E5FF';
      ctx.lineWidth = 3;
      ctx.beginPath();
      ctx.moveTo(-25, 45);
      ctx.lineTo(-65, 15 + Math.sin(this.time * 3) * 6);
      ctx.moveTo(25, 45);
      ctx.lineTo(65, 15 + Math.sin(this.time * 3) * 6);
      ctx.stroke();
    } else if (fighter.id === 'warlord') {
      // Lava Horns / Spikes
      ctx.fillStyle = '#FF3300';
      ctx.beginPath();
      ctx.moveTo(-16, 0);
      ctx.lineTo(-26, -18);
      ctx.lineTo(-8, -4);
      ctx.moveTo(16, 0);
      ctx.lineTo(26, -18);
      ctx.lineTo(8, -4);
      ctx.fill();
    }
  }
}

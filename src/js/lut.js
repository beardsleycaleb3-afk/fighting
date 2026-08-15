/**
 * Lookup Tables (LUT) for High-Performance Gaming & Color Grading
 * Provides precomputed trigonometric values, elemental matchup matrices,
 * color palette ramps, damage curve calculations, and audio pitch tables.
 */

// 1. Precomputed Trigonometric Lookup Table (360 degrees, 1024 precision steps)
export const TRIG_STEPS = 1024;
export const SIN_LUT = new Float32Array(TRIG_STEPS);
export const COS_LUT = new Float32Array(TRIG_STEPS);

for (let i = 0; i < TRIG_STEPS; i++) {
  const rad = (i / TRIG_STEPS) * Math.PI * 2;
  SIN_LUT[i] = Math.sin(rad);
  COS_LUT[i] = Math.cos(rad);
}

export function fastSin(rad) {
  const normalized = ((rad % (Math.PI * 2)) + Math.PI * 2) % (Math.PI * 2);
  const index = Math.floor((normalized / (Math.PI * 2)) * TRIG_STEPS) % TRIG_STEPS;
  return SIN_LUT[index];
}

export function fastCos(rad) {
  const normalized = ((rad % (Math.PI * 2)) + Math.PI * 2) % (Math.PI * 2);
  const index = Math.floor((normalized / (Math.PI * 2)) * TRIG_STEPS) % TRIG_STEPS;
  return COS_LUT[index];
}

// 2. Elemental Affinity Matrix Lookup Table
// Elements: Fire (0), Water/Frost (1), Wind/Gale (2), Earth/Mountain (3), Lightning (4), Void (5)
export const ELEMENT_INDICES = {
  FIRE: 0,
  FROST: 1,
  WIND: 2,
  EARTH: 3,
  LIGHTNING: 4,
  VOID: 5
};

// Row = Attacker, Column = Defender -> Multiplier
export const ELEMENTAL_MATCHUP_LUT = [
  // FIRE   FROST  WIND   EARTH  LIGHTNING VOID
  [1.0,   1.35,  1.20,  0.80,  1.0,      1.10], // FIRE
  [0.75,  1.0,   1.0,   1.30,  0.85,     1.15], // FROST
  [0.85,  1.10,  1.0,   1.25,  1.30,     0.90], // WIND
  [1.25,  0.80,  0.85,  1.0,   1.35,     1.00], // EARTH
  [1.00,  1.20,  0.75,  0.80,  1.0,      1.30], // LIGHTNING
  [1.15,  1.15,  1.15,  1.15,  1.15,     1.00]  // VOID
];

export function getElementalMultiplier(attackerElemIdx, defenderElemIdx) {
  const row = ELEMENTAL_MATCHUP_LUT[attackerElemIdx] || ELEMENTAL_MATCHUP_LUT[0];
  return row[defenderElemIdx] || 1.0;
}

// 3. Color Ramp Lookup Table for Elemental Auras & Energy Shaders
export class ColorRampLUT {
  constructor(stops) {
    this.table = new Array(256);
    this.generate(stops);
  }

  generate(stops) {
    // stops is array of { pos: 0..1, r, g, b, a }
    const canvas = document.createElement('canvas');
    canvas.width = 256;
    canvas.height = 1;
    const ctx = canvas.getContext('2d');
    if (!ctx) return;

    const grad = ctx.createLinearGradient(0, 0, 256, 0);
    stops.forEach(s => {
      grad.addColorStop(s.pos, `rgba(${s.r},${s.g},${s.b},${s.a !== undefined ? s.a : 1})`);
    });

    ctx.fillStyle = grad;
    ctx.fillRect(0, 0, 256, 1);
    const imgData = ctx.getImageData(0, 0, 256, 1).data;

    for (let i = 0; i < 256; i++) {
      const idx = i * 4;
      this.table[i] = {
        r: imgData[idx],
        g: imgData[idx + 1],
        b: imgData[idx + 2],
        a: imgData[idx + 3] / 255,
        css: `rgba(${imgData[idx]},${imgData[idx+1]},${imgData[idx+2]},${(imgData[idx+3]/255).toFixed(2)})`
      };
    }
  }

  sample(t) {
    const clamped = Math.max(0, Math.min(1, t));
    const idx = Math.floor(clamped * 255);
    return this.table[idx] || this.table[0];
  }
}

// 4. Pre-built LUTs for Spirit Animals
export const SPIRIT_PALETTE_LUTS = {
  PHANTOM_PANTHER: new ColorRampLUT([
    { pos: 0.0, r: 192, g: 38, b: 211, a: 0.9 },
    { pos: 0.5, r: 0, g: 229, b: 255, a: 0.7 },
    { pos: 1.0, r: 10, g: 0, b: 16, a: 0.2 }
  ]),
  THUNDER_TIGER: new ColorRampLUT([
    { pos: 0.0, r: 255, g: 200, b: 55, a: 1.0 },
    { pos: 0.6, r: 255, g: 112, b: 32, a: 0.8 },
    { pos: 1.0, r: 230, g: 0, b: 69, a: 0.1 }
  ]),
  CRIMSON_PHOENIX: new ColorRampLUT([
    { pos: 0.0, r: 255, g: 26, b: 94, a: 1.0 },
    { pos: 0.5, r: 255, g: 112, b: 32, a: 0.85 },
    { pos: 1.0, r: 255, g: 200, b: 55, a: 0.3 }
  ]),
  FROST_WOLF: new ColorRampLUT([
    { pos: 0.0, r: 0, g: 229, b: 255, a: 1.0 },
    { pos: 0.5, r: 120, g: 240, b: 255, a: 0.8 },
    { pos: 1.0, r: 10, g: 20, b: 50, a: 0.2 }
  ])
};

// 5. Damage Falloff & Combo Scaling LUT (1-30 hit chain)
export const COMBO_DAMAGE_SCALING_LUT = new Float32Array(32);
for (let hits = 0; hits < 32; hits++) {
  // Proration: each hit scales down smoothly to prevent infinite touch-of-death
  COMBO_DAMAGE_SCALING_LUT[hits] = Math.max(0.20, Math.pow(0.92, hits));
}

export function getComboScaledDamage(baseDamage, hitCount) {
  const idx = Math.min(31, Math.max(0, hitCount));
  return baseDamage * COMBO_DAMAGE_SCALING_LUT[idx];
}

import { FIGHTERS } from './fighters.js';
import { STAGES } from './stages.js';
import { sound } from './audio.js';
import { CanvasRenderer } from './engine.js';

class TimeTournamentApp {
  constructor() {
    this.selectedFighter = FIGHTERS[0];
    this.activeTab = 'story';
    this.comboCount = 0;
    this.comboTimer = null;
    this.renderer = null;
  }

  init() {
    // Setup Canvas
    const canvas = document.getElementById('portraitCanvas');
    if (canvas) {
      this.renderer = new CanvasRenderer(canvas);
      this.renderer.setFighter(this.selectedFighter);
      this.renderer.start();
    }

    this.renderRosterGrid();
    this.renderStagesGrid();
    this.updateDetailView();
    this.attachEventListeners();
    this.registerServiceWorker();
  }

  registerServiceWorker() {
    if ('serviceWorker' in navigator) {
      window.addEventListener('load', () => {
        navigator.serviceWorker.register('./sw.js')
          .then((reg) => console.log('Service Worker Registered!', reg.scope))
          .catch((err) => console.warn('Service Worker registration failed:', err));
      });
    }
  }

  renderRosterGrid() {
    const grid = document.getElementById('rosterGrid');
    if (!grid) return;
    grid.innerHTML = '';

    FIGHTERS.forEach(fighter => {
      const isSelected = fighter.id === this.selectedFighter.id;
      const card = document.createElement('div');
      card.className = `roster-card ${isSelected ? 'selected' : ''}`;
      card.style.borderColor = isSelected ? fighter.color : 'rgba(255,255,255,0.12)';
      card.style.boxShadow = isSelected ? `0 0 15px ${fighter.color}66` : 'none';

      card.innerHTML = `
        <div class="roster-badge" style="background: ${fighter.color}">${fighter.code}</div>
        <div class="roster-info">
          <div class="roster-name" style="color: ${isSelected ? fighter.color : '#FFFFFF'}">${fighter.name}</div>
          <div class="roster-era">${fighter.era}</div>
        </div>
      `;

      card.addEventListener('click', () => {
        sound.playSelect();
        this.selectFighter(fighter);
      });

      grid.appendChild(card);
    });
  }

  renderStagesGrid() {
    const stageContainer = document.getElementById('stagesGrid');
    if (!stageContainer) return;
    stageContainer.innerHTML = '';

    STAGES.forEach((stage, idx) => {
      const stageCard = document.createElement('div');
      stageCard.className = 'stage-card';
      stageCard.style.borderLeft = `4px solid ${stage.groundBorder}`;
      stageCard.innerHTML = `
        <div class="stage-header">
          <span class="stage-num" style="color: ${stage.groundBorder}">STAGE 0${idx + 1}</span>
          <span class="stage-era">${stage.era}</span>
        </div>
        <div class="stage-name">${stage.name}</div>
        <div class="stage-desc">${stage.desc}</div>
        <div class="stage-meta">
          <span>🎵 ${stage.musicTheme}</span>
          <span>✨ ${stage.particleType.toUpperCase()}</span>
        </div>
      `;
      stageContainer.appendChild(stageCard);
    });
  }

  selectFighter(fighter) {
    this.selectedFighter = fighter;
    if (this.renderer) {
      this.renderer.setFighter(fighter);
    }
    this.renderRosterGrid();
    this.updateDetailView();
  }

  updateDetailView() {
    const f = this.selectedFighter;

    // Header & Meta
    const nameEl = document.getElementById('fighterName');
    const titleEl = document.getElementById('fighterTitle');
    const styleEl = document.getElementById('fighterStyle');
    const quoteEl = document.getElementById('fighterQuote');
    const originEl = document.getElementById('fighterOrigin');

    if (nameEl) {
      nameEl.textContent = f.name;
      nameEl.style.color = f.color;
    }
    if (titleEl) titleEl.textContent = `★ ${f.title} ★`;
    if (styleEl) styleEl.textContent = `${f.style} • ${f.era}`;
    if (quoteEl) quoteEl.textContent = `"${f.introQuote}"`;
    if (originEl) originEl.textContent = f.timelineOrigin;

    // Combat Stats Bars
    this.updateStatBar('statPower', f.statPower, f.color);
    this.updateStatBar('statSpeed', f.statSpeed, f.accent);
    this.updateStatBar('statDefense', f.statDefense, f.color);
    this.updateStatBar('statSpecial', f.statSpecial, f.accent);

    // Tab Content Updates
    this.renderTabContent();
  }

  updateStatBar(id, value, color) {
    const bar = document.getElementById(id);
    const valText = document.getElementById(id + 'Val');
    if (bar) {
      bar.style.width = `${Math.round(value * 100)}%`;
      bar.style.backgroundColor = color;
    }
    if (valText) {
      valText.textContent = `${Math.round(value * 100)}%`;
    }
  }

  renderTabContent() {
    const f = this.selectedFighter;
    const tabContainer = document.getElementById('tabContent');
    if (!tabContainer) return;

    if (this.activeTab === 'story') {
      tabContainer.innerHTML = `
        <div class="lore-box">
          <div class="lore-section-title" style="color:${f.accent}">CHRONO BIOGRAPHY</div>
          <p class="lore-text">${f.bio}</p>

          <div class="lore-section-title" style="color:${f.accent}; margin-top: 16px;">TOURNAMENT ARC & STAKES</div>
          <p class="lore-text">${f.storyArc}</p>

          <div class="lore-meta-grid">
            <div class="lore-meta-card">
              <span class="label">ASSIGNED RIVAL</span>
              <span class="val" style="color:${f.color}">${f.rival}</span>
            </div>
            <div class="lore-meta-card">
              <span class="label">EQUIPPED WEAPONRY</span>
              <span class="val">${f.weapons}</span>
            </div>
          </div>
        </div>
      `;
    } else if (this.activeTab === 'traits') {
      tabContainer.innerHTML = `
        <div class="traits-container">
          ${f.traits.map(t => `
            <div class="trait-card" style="border-left: 3px solid ${t.color}">
              <div class="trait-header">
                <span class="trait-name" style="color: ${t.color}">${t.name}</span>
                <span class="trait-pct">${Math.round(t.pct * 100)}% MASTERY</span>
              </div>
              <div class="trait-bar-bg">
                <div class="trait-bar-fill" style="width: ${t.pct * 100}%; background: ${t.color}"></div>
              </div>
              <div class="trait-desc">${t.desc}</div>
            </div>
          `).join('')}
        </div>
      `;
    } else if (this.activeTab === 'combat') {
      tabContainer.innerHTML = `
        <div class="combat-lore-box">
          <div class="special-move-banner" style="border-color: ${f.color}">
            <div class="special-badge" style="background: ${f.color}">SIGNATURE ULTRA</div>
            <div class="special-move-name" style="color: ${f.accent}">${f.specialName}</div>
            <div class="special-move-desc">${f.signatureMove}</div>
          </div>

          <div class="lore-section-title" style="color:${f.accent}; margin-top: 16px;">COMBAT PHILOSOPHY</div>
          <blockquote class="philosophy-quote" style="border-left-color: ${f.color}">${f.combatPhilosophy}</blockquote>

          <div class="quotes-grid">
            <div class="quote-card">
              <span class="quote-type">TAUNT:</span>
              <span class="quote-content">"${f.tauntQuote}"</span>
            </div>
            <div class="quote-card">
              <span class="quote-type">SPECIAL MOVE CRY:</span>
              <span class="quote-content">"${f.specialQuote}"</span>
            </div>
            <div class="quote-card">
              <span class="quote-type">VICTORY ROAR:</span>
              <span class="quote-content">"${f.victoryQuote}"</span>
            </div>
          </div>
        </div>
      `;
    }
  }

  triggerHit(type) {
    this.comboCount++;
    if (type === 'punch') sound.playPunch();
    else if (type === 'kick') sound.playKick();
    else if (type === 'special') sound.playSpecial();

    sound.playCombo(this.comboCount);

    const comboEl = document.getElementById('comboDisplay');
    const comboNum = document.getElementById('comboNumber');
    const comboLabel = document.getElementById('comboLabel');

    if (comboEl && comboNum) {
      comboEl.classList.remove('hidden');
      comboNum.textContent = `${this.comboCount}x`;
      
      let scale = 1.0 + Math.min(this.comboCount * 0.08, 0.8);
      comboNum.style.transform = `scale(${scale})`;

      if (this.comboCount >= 8) {
        comboLabel.textContent = "GODLIKE COMBO!";
        comboNum.style.color = "#FF1A5E";
      } else if (this.comboCount >= 5) {
        comboLabel.textContent = "SUPER STREAK!";
        comboNum.style.color = "#FFC837";
      } else if (this.comboCount >= 3) {
        comboLabel.textContent = "COMBO HIT!";
        comboNum.style.color = "#00E5FF";
      } else {
        comboLabel.textContent = "HIT";
        comboNum.style.color = "#FFFFFF";
      }
    }

    clearTimeout(this.comboTimer);
    this.comboTimer = setTimeout(() => {
      this.comboCount = 0;
      if (comboEl) comboEl.classList.add('hidden');
    }, 1800);
  }

  attachEventListeners() {
    // Tab switching
    const tabs = document.querySelectorAll('.tab-btn');
    tabs.forEach(tab => {
      tab.addEventListener('click', (e) => {
        tabs.forEach(t => t.classList.remove('active'));
        e.currentTarget.classList.add('active');
        this.activeTab = e.currentTarget.dataset.tab;
        sound.playSelect();
        this.renderTabContent();
      });
    });

    // Test Move buttons
    const punchBtn = document.getElementById('btnPunch');
    const kickBtn = document.getElementById('btnKick');
    const specialBtn = document.getElementById('btnSpecial');

    if (punchBtn) punchBtn.addEventListener('click', () => this.triggerHit('punch'));
    if (kickBtn) kickBtn.addEventListener('click', () => this.triggerHit('kick'));
    if (specialBtn) specialBtn.addEventListener('click', () => this.triggerHit('special'));
  }
}

document.addEventListener('DOMContentLoaded', () => {
  const app = new TimeTournamentApp();
  app.init();
});

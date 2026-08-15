/**
 * Time Tournament - Core Application Controller
 * Handles Main Menu, Start Game Screen, Pause Menu, Dialogue VN System,
 * Tournament Championship Bracket Tree, Fighter Emblem Customizer,
 * Rivalries, Strengths/Weaknesses, Spirit Animal Final Blow Melee Smash,
 * and 30px bottom-left right-arrow touch navigation.
 *
 * Utilizes:
 * - lut.js (Lookup tables for elemental multipliers & combo damage proration)
 * - array.js (ArrayUtils for shuffling, partitioning & sampling)
 * - list.js (CircularLinkedList for stage rotation & DoublyLinkedList for dialogue history)
 * - tree.js (TournamentTree bracket generator & SkillTree perks)
 * - trie.js (CommandTrie for combo sequence recognition)
 * - queue.js (Queue for dialogue and event dispatching)
 * - dequeue.js (Deque for 60-frame input buffer)
 * - babylon.js, fabric.js, matter.js integrations
 */
import { FIGHTERS } from './fighters.js';
import { STAGES } from './stages.js';
import { sound } from './audio.js';
import { CanvasRenderer } from './engine.js';
import { JAB_SPRITE_FRAMES, spriteEngine } from './sprites.js';
import { getElementalMultiplier, getComboScaledDamage, ELEMENT_INDICES } from './lut.js';
import { ArrayUtils } from './array.js';
import { CircularLinkedList, DoublyLinkedList } from './list.js';
import { TournamentTree, SkillTreeNode } from './tree.js';
import { CommandTrie } from './trie.js';
import { Queue } from './queue.js';
import { Deque } from './dequeue.js';
import { manager } from './manager.js';
import { communicator } from './communicator.js';
import { contractor } from './contractor.js';
import { composer } from './composer.js';
import { universalClock } from './universalclock.js';
import { gameClock } from './gameclock.js';
import { playClock } from './playclock.js';
import { audioClock } from './audioclock.js';
import { srcManager } from './srcmanager.js';
import { assetManager } from './assetmanager.js';
import { spriteManager } from './spritemanager.js';
import { jsManager } from './jsmanager.js';

export class TimeTournamentApp {
  constructor() {
    this.manager = manager;
    this.communicator = communicator;
    this.contractor = contractor;
    this.composer = composer;
    this.universalClock = universalClock;
    this.gameClock = gameClock;
    this.playClock = playClock;
    this.audioClock = audioClock;
    this.srcManager = srcManager;
    this.assetManager = assetManager;
    this.spriteManager = spriteManager;
    this.jsManager = jsManager;

    this.selectedFighter = FIGHTERS[0];
    this.selectedRival = FIGHTERS[1];
    this.stageList = new CircularLinkedList(STAGES);
    this.selectedStage = this.stageList.getCurrent() || STAGES[0];
    this.currentView = 'menu'; // 'menu', 'start_game', 'battle', 'story_dialogue', 'dossier', 'bracket'
    this.activeTab = 'story';
    this.comboCount = 0;
    this.comboTimer = null;
    this.renderer = null;
    this.spriteFrames = JAB_SPRITE_FRAMES;
    this.isPaused = false;

    // Data Structures
    this.inputBuffer = new Deque(60); // 60-frame rolling input buffer
    this.commandTrie = new CommandTrie();
    this.dialogueHistory = new DoublyLinkedList();
    this.actionQueue = new Queue();
    this.tournamentTree = new TournamentTree(FIGHTERS);

    // Combat State
    this.playerHp = 100;
    this.playerMaxHp = 100;
    this.rivalHp = 100;
    this.rivalMaxHp = 100;
    this.playerSuperMeter = 100; // 0-100%
    this.isBattleOver = false;

    // Dialogue State
    this.typewriterInterval = null;
    this.isTyping = false;

    // Settings
    this.settings = {
      soundEnabled: true,
      volume: 0.8,
      vibrationEnabled: true,
      textSpeedMode: 'normal'
    };

    // Initialize Trie with combo commands
    this.initCommandTrie();
    this.setupClocksAndEvents();
  }

  setupClocksAndEvents() {
    this.manager.initialize();

    // Validate fighters data schema via contractor
    FIGHTERS.forEach(f => this.contractor.validate('Fighter', f));

    // Connect GameClock countdown to match timer HUD
    this.gameClock.onSecondTick = (secRemaining) => {
      const timeEl = document.getElementById('battleTimerSec');
      if (timeEl) timeEl.textContent = secRemaining < 10 ? `0${secRemaining}` : `${secRemaining}`;
    };

    this.gameClock.onRoundTimeout = () => {
      if (!this.isBattleOver && this.currentView === 'battle') {
        const playerWon = this.playerHp >= this.rivalHp;
        this.endBattle(playerWon);
      }
    };

    this.playClock.onComboTimeout = () => {
      this.comboCount = 0;
    };
  }

  initCommandTrie() {
    // Register special combat combos into Trie
    this.commandTrie.insert(['PUNCH', 'PUNCH', 'KICK'], { name: 'TRIPLE TEMPEST', bonusDamage: 22, sound: 'combo' });
    this.commandTrie.insert(['KICK', 'PUNCH', 'SPECIAL'], { name: 'ELEMENTAL CRUSH', bonusDamage: 30, sound: 'special' });
    this.commandTrie.insert(['PUNCH', 'SPECIAL', 'PUNCH'], { name: 'VOID FLURRY', bonusDamage: 28, sound: 'special' });
    this.commandTrie.insert(['MOVE_AHEAD', 'PUNCH'], { name: 'DASHING JAB', bonusDamage: 16, sound: 'punch' });
  }

  init() {
    const canvas = document.getElementById('portraitCanvas');
    if (canvas) {
      this.renderer = new CanvasRenderer(canvas);
      this.renderer.setFighter(this.selectedFighter);
      this.renderer.start();
    }

    this.renderRosterGrid();
    this.renderStagesGrid();
    this.renderTournamentBracket();
    this.selectFighter(this.selectedFighter);
    this.attachEventListeners();
    this.initFabricCanvas();
    this.initBabylonBackdrop();
    this.showView('menu');
  }

  initFabricCanvas() {
    // Optional Fabric.js integration for custom badge canvas
    const fabricCanvasEl = document.getElementById('fabricBadgeCanvas');
    if (fabricCanvasEl && typeof window !== 'undefined' && window.fabric) {
      try {
        const fCanvas = new window.fabric.Canvas('fabricBadgeCanvas', {
          width: 80,
          height: 80,
          backgroundColor: '#1E0012'
        });
        const circle = new window.fabric.Circle({
          radius: 32,
          fill: this.selectedFighter.color,
          stroke: this.selectedFighter.accent,
          strokeWidth: 2,
          left: 8,
          top: 8
        });
        const text = new window.fabric.Text(this.selectedFighter.spiritAnimalSymbol || '🐆', {
          fontSize: 28,
          originX: 'center',
          originY: 'center',
          left: 40,
          top: 40
        });
        fCanvas.add(circle);
        fCanvas.add(text);
        this.fabricCanvas = fCanvas;
      } catch (e) {
        console.warn('Fabric.js initialization skipped/fallback:', e);
      }
    }
  }

  initBabylonBackdrop() {
    // Optional Babylon.js integration for 3D time warp effect
    const babylonCanvas = document.getElementById('babylonCanvas');
    if (babylonCanvas && typeof window !== 'undefined' && window.BABYLON) {
      try {
        const engine = new window.BABYLON.Engine(babylonCanvas, true);
        const scene = new window.BABYLON.Scene(engine);
        scene.clearColor = new window.BABYLON.Color4(0.05, 0, 0.08, 1);

        const camera = new window.BABYLON.ArcRotateCamera('cam', -Math.PI / 2, Math.PI / 2.5, 5, window.BABYLON.Vector3.Zero(), scene);
        const light = new window.BABYLON.HemisphericLight('light', new window.BABYLON.Vector3(0, 1, 0), scene);
        light.intensity = 0.8;

        const sphere = window.BABYLON.MeshBuilder.CreateTorusKnot('knot', { radius: 1.2, tube: 0.3, radialSegments: 32 }, scene);
        const mat = new window.BABYLON.StandardMaterial('mat', scene);
        mat.emissiveColor = new window.BABYLON.Color3(0.9, 0.1, 0.4);
        mat.wireframe = true;
        sphere.material = mat;

        engine.runRenderLoop(() => {
          sphere.rotation.y += 0.02;
          sphere.rotation.x += 0.01;
          scene.render();
        });
      } catch (err) {
        console.warn('Babylon.js initialization skipped/fallback:', err);
      }
    }
  }

  showView(viewName) {
    this.currentView = viewName;
    const views = ['viewMenu', 'viewStartGame', 'viewBattle', 'viewDialogue', 'viewDossier', 'viewBracket'];
    views.forEach(id => {
      const el = document.getElementById(id);
      if (el) el.style.display = 'none';
    });

    const targetId = `view${this.capitalize(viewName)}`;
    const targetEl = document.getElementById(targetId);
    if (targetEl) {
      targetEl.style.display = 'flex';
    }

    if (viewName === 'battle') {
      this.startBattle();
    } else if (viewName === 'story_dialogue') {
      this.startStoryDialogue();
    } else if (viewName === 'bracket') {
      this.renderTournamentBracket();
    }
  }

  capitalize(str) {
    if (str === 'start_game') return 'StartGame';
    if (str === 'story_dialogue') return 'Dialogue';
    return str.charAt(0).toUpperCase() + str.slice(1);
  }

  selectFighter(fighter) {
    this.selectedFighter = fighter;
    // Auto-match rival using ArrayUtils
    const rival = FIGHTERS.find(f => f.name === fighter.rival) || ArrayUtils.sample(FIGHTERS.filter(f => f.id !== fighter.id), 1)[0];
    this.selectedRival = rival;

    if (this.renderer) {
      this.renderer.setFighter(fighter);
    }

    this.updateFighterUI(fighter);
    this.updateStartGamePreview();
  }

  updateFighterUI(fighter) {
    const setSafeText = (id, text) => {
      const el = document.getElementById(id);
      if (el) el.textContent = text;
    };

    setSafeText('fighterName', fighter.name);
    setSafeText('fighterRealName', fighter.realName || fighter.name);
    setSafeText('fighterElementState', `${fighter.elementSymbol || '⚡'} ${fighter.elementalState}`);
    setSafeText('fighterSpiritAnimal', `${fighter.spiritAnimalSymbol || '🐆'} SPIRIT: ${fighter.spiritAnimal}`);
    setSafeText('fighterTitle', `★ ${fighter.title} ★`);
    setSafeText('fighterStyle', `${fighter.style} • ${fighter.era}`);
    setSafeText('fighterQuote', `"${fighter.introQuote}"`);

    // Update active roster card
    document.querySelectorAll('.roster-card').forEach(card => {
      if (card.dataset.id === fighter.id) {
        card.classList.add('selected');
        card.style.borderColor = fighter.accent;
        card.style.boxShadow = `0 0 10px ${fighter.color}66`;
      } else {
        card.classList.remove('selected');
        card.style.borderColor = 'rgba(255, 255, 255, 0.12)';
        card.style.boxShadow = 'none';
      }
    });

    this.renderTabContent();
  }

  updateStartGamePreview() {
    const p1 = this.selectedFighter;
    const p2 = this.selectedRival;

    const setEl = (id, text) => {
      const el = document.getElementById(id);
      if (el) el.textContent = text;
    };

    setEl('sgP1Name', p1.name);
    setEl('sgP1Element', `${p1.elementSymbol} ${p1.elementalState}`);
    setEl('sgP1Spirit', `${p1.spiritAnimalSymbol} ${p1.spiritAnimal}`);
    setEl('sgP1Origin', p1.originType);

    setEl('sgP2Name', p2.name);
    setEl('sgP2Element', `${p2.elementSymbol} ${p2.elementalState}`);
    setEl('sgP2Spirit', `${p2.spiritAnimalSymbol} ${p2.spiritAnimal}`);
    setEl('sgP2Origin', p2.originType);

    // Strengths & Weaknesses matchup check with Lookup Table (LUT)
    const matchupEl = document.getElementById('sgMatchupAnalysis');
    if (matchupEl) {
      const p1ElemIdx = ELEMENT_INDICES[p1.elementCategory?.toUpperCase()] ?? 0;
      const p2ElemIdx = ELEMENT_INDICES[p2.elementCategory?.toUpperCase()] ?? 1;
      const mult = getElementalMultiplier(p1ElemIdx, p2ElemIdx);

      if (mult > 1.1) {
        matchupEl.innerHTML = `<span style="color:#00E5FF;font-weight:bold;">⚡ ELEMENTAL ADVANTAGE (×${mult.toFixed(2)}):</span> ${p1.name} deals bonus damage against ${p2.name}!`;
      } else if (mult < 0.9) {
        matchupEl.innerHTML = `<span style="color:#FF1A5E;font-weight:bold;">⚠️ ELEMENTAL RESISTANCE (×${mult.toFixed(2)}):</span> ${p2.name}'s element resists ${p1.name}!`;
      } else {
        matchupEl.innerHTML = `<span style="color:#FFC837;font-weight:bold;">⚔️ BALANCED RIVALRY (×${mult.toFixed(2)}):</span> Pure martial mastery and spirit animal timing will decide the outcome!`;
      }
    }
  }

  renderRosterGrid() {
    const grid = document.getElementById('rosterGrid');
    if (!grid) return;
    grid.innerHTML = '';

    FIGHTERS.forEach(fighter => {
      const card = document.createElement('div');
      card.className = 'roster-card';
      card.dataset.id = fighter.id;
      card.innerHTML = `
        <div style="display:flex; justify-content:space-between; align-items:center;">
          <div class="roster-badge" style="background:${fighter.color}">${fighter.code}</div>
          <span style="font-size:8px;">${fighter.spiritAnimalSymbol || '🔥'}</span>
        </div>
        <div class="roster-name" style="color:${fighter.accent}">${fighter.name}</div>
        <div class="roster-era">${fighter.elementalState}</div>
      `;

      const handleSelect = (e) => {
        if (e && e.cancelable) e.preventDefault();
        sound.playSelect();
        this.selectFighter(fighter);
      };

      card.addEventListener('pointerdown', handleSelect, { passive: false });
      card.addEventListener('touchstart', handleSelect, { passive: false });

      grid.appendChild(card);
    });
  }

  renderStagesGrid() {
    const grid = document.getElementById('stagesGrid');
    if (!grid) return;
    grid.innerHTML = '';

    STAGES.forEach(stage => {
      const card = document.createElement('div');
      card.className = 'stage-card';
      card.innerHTML = `
        <div class="stage-header">
          <span style="color:${stage.themeColor}">STAGE ${stage.number} • ${stage.era}</span>
          <span>${stage.hazard}</span>
        </div>
        <div class="stage-name">${stage.name}</div>
        <div class="stage-desc">${stage.description}</div>
      `;

      const handleStageSelect = (e) => {
        if (e && e.cancelable) e.preventDefault();
        this.selectedStage = stage;
        sound.playSelect();
        document.querySelectorAll('.stage-card').forEach(c => c.style.borderColor = 'rgba(255,255,255,0.12)');
        card.style.borderColor = stage.themeColor;
      };

      card.addEventListener('pointerdown', handleStageSelect, { passive: false });
      card.addEventListener('touchstart', handleStageSelect, { passive: false });

      grid.appendChild(card);
    });
  }

  renderTournamentBracket() {
    const container = document.getElementById('bracketContainer');
    if (!container) return;

    const root = this.tournamentTree.root;
    if (!root) return;

    container.innerHTML = `
      <div style="font-family:var(--font-mono); font-size:9px; font-weight:800; color:var(--gold); margin-bottom:4px; text-align:center;">
        🏆 ARCADE CHAMPIONSHIP TREE
      </div>
      <div style="display:flex; flex-direction:column; gap:6px;">
        <div style="background:rgba(255,255,255,0.06); padding:6px; border-radius:6px; border:1px solid var(--border-dark);">
          <div style="font-size:7px; color:var(--neon-blue); font-weight:bold;">GRAND FINALS</div>
          <div style="display:flex; justify-content:space-between; font-size:8px; font-weight:bold;">
            <span>${root.leftMatch?.winner?.name || 'SEMI-FINALIST A'}</span>
            <span style="color:var(--gold);">VS</span>
            <span>${root.rightMatch?.winner?.name || 'SEMI-FINALIST B'}</span>
          </div>
        </div>
      </div>
    `;
  }

  renderTabContent() {
    const tabContent = document.getElementById('tabContent');
    if (!tabContent) return;
    const f = this.selectedFighter;

    if (this.activeTab === 'story') {
      tabContent.innerHTML = `
        <div class="lore-section-title" style="color:${f.accent}">HUMAN IDENTITY & SPIRITUAL AWAKENING</div>
        <div class="lore-text" style="margin-bottom:6px;"><strong>Real Identity:</strong> ${f.realName} (${f.originType})</div>
        <div class="lore-text">${f.bio}</div>
        <div class="lore-meta-grid">
          <div class="lore-meta-card">
            <span class="label">ELEMENTAL STATE</span>
            <span class="val" style="color:${f.accent}">${f.elementSymbol} ${f.elementalState}</span>
          </div>
          <div class="lore-meta-card">
            <span class="label">SPIRIT ANIMAL</span>
            <span class="val" style="color:#FFC837">${f.spiritAnimalSymbol} ${f.spiritAnimal}</span>
          </div>
          <div class="lore-meta-card">
            <span class="label">DESIGNATED RIVAL</span>
            <span class="val" style="color:#FF1A5E">${f.rival}</span>
          </div>
          <div class="lore-meta-card">
            <span class="label">HOME TIMELINE</span>
            <span class="val">${f.era}</span>
          </div>
        </div>
      `;
    } else if (this.activeTab === 'traits') {
      tabContent.innerHTML = `
        <div class="lore-section-title" style="color:${f.accent}">ELEMENTAL STRENGTHS & WEAKNESSES</div>
        <div style="display:flex; flex-direction:column; gap:5px; margin-bottom:8px;">
          <div style="background:rgba(0, 229, 255, 0.1); border-left:3px solid #00E5FF; padding:4px 6px; border-radius:4px;">
            <div style="font-size:7.5px; font-weight:800; color:#00E5FF;">💪 ELEMENTAL ADVANTAGES:</div>
            ${f.strengths?.map(s => `<div style="font-size:7px; color:rgba(255,255,255,0.85);">• <strong>${s.element}:</strong> ${s.reason}</div>`).join('') || ''}
          </div>
          <div style="background:rgba(255, 26, 94, 0.1); border-left:3px solid #FF1A5E; padding:4px 6px; border-radius:4px;">
            <div style="font-size:7.5px; font-weight:800; color:#FF1A5E;">⚠️ ELEMENTAL WEAKNESSES:</div>
            ${f.weaknesses?.map(w => `<div style="font-size:7px; color:rgba(255,255,255,0.85);">• <strong>${w.element}:</strong> ${w.reason}</div>`).join('') || ''}
          </div>
        </div>
        <div class="lore-section-title" style="color:${f.accent}">MARTIAL TRAITS</div>
        <div class="traits-container">
          ${f.traits.map(t => `
            <div class="trait-card">
              <div class="trait-header">
                <span>${t.name}</span>
                <span style="color:${t.color}">${Math.round(t.pct * 100)}%</span>
              </div>
              <div class="trait-bar-bg">
                <div class="trait-bar-fill" style="width:${t.pct * 100}%; background:${t.color}"></div>
              </div>
              <div class="trait-desc">${t.desc}</div>
            </div>
          `).join('')}
        </div>
      `;
    } else if (this.activeTab === 'combat') {
      const smash = f.finalBlowMeleeSmash;
      tabContent.innerHTML = `
        <div class="special-move-banner" style="border-left-color:${f.accent}">
          <span class="special-badge" style="background:${f.color}">💥 FINAL BLOW MELEE FINISH</span>
          <div class="special-move-name" style="color:#FFC837">${smash.name}</div>
          <div style="font-size:7px; color:${f.accent}; margin-bottom:3px;">
            <strong>SPIRIT BLEND:</strong> ${f.originType} × ${f.elementalState} × ${smash.animalBlend}
          </div>
          <div class="special-move-desc">${smash.description}</div>
          <div style="font-size:7.5px; font-style:italic; color:#FFF; margin-top:3px;">"${smash.quote}"</div>
        </div>

        <div class="lore-section-title" style="color:${f.accent}">RIVALRY LORE: VS ${f.rival}</div>
        <div style="font-size:7.5px; line-height:11px; background:rgba(0,0,0,0.3); padding:5px; border-radius:4px; margin-bottom:6px;">
          ${f.rivalryReason || 'A clash of ancient destinies and temporal supremacy.'}
        </div>

        <div class="lore-section-title" style="color:${f.accent}">COMBAT PHILOSOPHY</div>
        <div class="philosophy-quote">"${f.combatPhilosophy}"</div>
      `;
    }
  }

  // --- STORY DIALOGUE (VISUAL NOVEL) SYSTEM USING DOUBLY LINKED LIST ---
  startStoryDialogue() {
    const p1 = this.selectedFighter;
    const p2 = this.selectedRival;

    this.dialogueHistory = new DoublyLinkedList();
    this.dialogueHistory.append({ speaker: p1.name, title: p1.title, text: p1.dialogue?.prologue || p1.introQuote, color: p1.color, accent: p1.accent, avatar: p1.spiritAnimalSymbol });
    this.dialogueHistory.append({ speaker: p2.name, title: p2.title, text: p1.dialogue?.rivalResponse || p2.introQuote, color: p2.color, accent: p2.accent, avatar: p2.spiritAnimalSymbol });
    this.dialogueHistory.append({ speaker: p1.name, title: p1.title, text: p1.dialogue?.vsRival || p1.specialQuote, color: p1.color, accent: p1.accent, avatar: p1.spiritAnimalSymbol });
    this.dialogueHistory.append({ speaker: "SYSTEM", title: "ROUND 1: ENGAGE", text: "Fighters in position! Battle begins now!", color: "#FFC837", accent: "#FFF", avatar: "⚡" });

    this.dialogueHistory.resetCursor();
    this.displayCurrentDialogue();
  }

  displayCurrentDialogue() {
    const current = this.dialogueHistory.current();
    if (!current) {
      this.showView('battle');
      return;
    }

    const speakerEl = document.getElementById('dialogueSpeaker');
    const titleEl = document.getElementById('dialogueTitle');
    const avatarEl = document.getElementById('dialogueAvatar');
    const textEl = document.getElementById('dialogueText');

    if (speakerEl) {
      speakerEl.textContent = current.speaker;
      speakerEl.style.color = current.accent;
    }
    if (titleEl) titleEl.textContent = current.title;
    if (avatarEl) {
      avatarEl.textContent = current.avatar;
      avatarEl.style.borderColor = current.color;
    }

    // Typewriter effect
    if (textEl) {
      textEl.textContent = '';
      if (this.typewriterInterval) clearInterval(this.typewriterInterval);

      if (this.settings.textSpeedMode === 'instant') {
        textEl.textContent = current.text;
        this.isTyping = false;
      } else {
        this.isTyping = true;
        let charIndex = 0;
        const speed = this.settings.textSpeedMode === 'fast' ? 12 : 25;
        this.typewriterInterval = setInterval(() => {
          if (charIndex < current.text.length) {
            textEl.textContent += current.text.charAt(charIndex);
            if (charIndex % 3 === 0) sound.playTypewriter();
            charIndex++;
          } else {
            clearInterval(this.typewriterInterval);
            this.isTyping = false;
          }
        }, speed);
      }
    }
  }

  stepDialogueForward() {
    if (this.isTyping) {
      if (this.typewriterInterval) clearInterval(this.typewriterInterval);
      const textEl = document.getElementById('dialogueText');
      const curr = this.dialogueHistory.current();
      if (textEl && curr) textEl.textContent = curr.text;
      this.isTyping = false;
    } else {
      sound.playSelect();
      const next = this.dialogueHistory.stepForward();
      if (next) {
        this.displayCurrentDialogue();
      } else {
        this.showView('battle');
      }
    }
  }

  // --- BATTLE SYSTEM WITH TRIE COMBO & LUT DAMAGE PRORATION ---
  startBattle() {
    this.playerHp = this.selectedFighter.maxHp;
    this.playerMaxHp = this.selectedFighter.maxHp;
    this.rivalHp = this.selectedRival.maxHp;
    this.rivalMaxHp = this.selectedRival.maxHp;
    this.playerSuperMeter = 100;
    this.isBattleOver = false;
    this.inputBuffer.clear();

    this.gameClock.startRound(99);
    this.communicator.emit('match:start', {
      player: this.selectedFighter,
      rival: this.selectedRival,
      stage: this.selectedStage
    });

    this.updateBattleHUD();
  }

  updateBattleHUD() {
    const p1HpBar = document.getElementById('battleP1HpBar');
    const p2HpBar = document.getElementById('battleP2HpBar');
    const p1HpVal = document.getElementById('battleP1HpVal');
    const p2HpVal = document.getElementById('battleP2HpVal');
    const superBar = document.getElementById('battleSuperMeterBar');

    if (p1HpBar) p1HpBar.style.width = `${Math.max(0, (this.playerHp / this.playerMaxHp) * 100)}%`;
    if (p2HpBar) p2HpBar.style.width = `${Math.max(0, (this.rivalHp / this.rivalMaxHp) * 100)}%`;
    if (p1HpVal) p1HpVal.textContent = `${Math.max(0, Math.round(this.playerHp))} HP`;
    if (p2HpVal) p2HpVal.textContent = `${Math.max(0, Math.round(this.rivalHp))} HP`;
    if (superBar) superBar.style.width = `${this.playerSuperMeter}%`;

    const smashBtn = document.getElementById('btnFinalSmash');
    if (smashBtn) {
      if (this.playerSuperMeter >= 100) {
        smashBtn.classList.add('ready');
      } else {
        smashBtn.classList.remove('ready');
      }
    }
  }

  triggerHit(type) {
    if (this.isBattleOver || this.isPaused) return;

    // Push token to rolling input buffer Deque
    const token = type.toUpperCase();
    this.inputBuffer.pushBack(token);

    // Check Trie for special combo execution
    const recentInputs = this.inputBuffer.getLastN(3);
    const comboMatch = this.commandTrie.search(recentInputs);

    let baseDamage = 10;
    if (type === 'punch') {
      baseDamage = this.selectedFighter.punchDamage || 14;
      sound.playPunch();
    } else if (type === 'kick') {
      baseDamage = this.selectedFighter.kickDamage || 20;
      sound.playKick();
    } else if (type === 'special') {
      baseDamage = this.selectedFighter.specialDamage || 35;
      sound.playSpecial();
    }

    if (comboMatch) {
      baseDamage += comboMatch.bonusDamage;
      sound.playSpecial();
      this.composer.composeComboBanner(this.comboCount + 1, comboMatch.name);
    }

    this.comboCount++;
    this.playClock.registerHit(1.8);
    clearTimeout(this.comboTimer);
    this.comboTimer = setTimeout(() => {
      this.comboCount = 0;
    }, 1800);

    // Apply LUT damage scaling proration
    const scaledDamage = Math.round(getComboScaledDamage(baseDamage, this.comboCount));

    sound.playCombo(this.comboCount);

    if (this.renderer) {
      this.renderer.triggerAttack(type);
    }

    this.communicator.emit('combat:hit', {
      type,
      damage: scaledDamage,
      combo: this.comboCount,
      isCombo: !!comboMatch
    });

    // Damage Popup
    const hitX = window.innerWidth / 2 + (Math.random() * 40 - 20);
    const hitY = 160 + (Math.random() * 20 - 10);
    this.composer.composeDamagePopup(hitX, hitY, scaledDamage, !!comboMatch);

    // Apply damage to rival
    this.rivalHp -= scaledDamage;
    this.playerSuperMeter = Math.min(100, this.playerSuperMeter + 15);

    // AI Counter Attack
    setTimeout(() => {
      if (!this.isBattleOver && this.rivalHp > 0) {
        const counterDamage = Math.floor(Math.random() * 12 + 6);
        this.playerHp -= counterDamage;
        if (this.playerHp <= 0) {
          this.endBattle(false);
        }
        this.updateBattleHUD();
      }
    }, 300);

    if (this.rivalHp <= 0) {
      this.endBattle(true);
    }

    this.updateBattleHUD();
  }

  // --- FINAL BLOW SPIRIT ANIMAL MELEE SMASH ---
  triggerFinalSmashMeleeAttack() {
    if (this.isBattleOver || this.isPaused) return;

    this.communicator.emit('combat:smash', {
      fighter: this.selectedFighter,
      smash: this.selectedFighter.finalBlowMeleeSmash
    });

    sound.playFinalBlowSmash();
    if (this.renderer) {
      this.renderer.triggerFinalSmash();
    }

    const modal = document.getElementById('smashCinematicModal');
    const titleEl = document.getElementById('smashCinematicTitle');
    const quoteEl = document.getElementById('smashCinematicQuote');
    const spiritEl = document.getElementById('smashCinematicSpirit');
    const smashVideo = document.getElementById('smashVideoPlayer');

    const smash = this.selectedFighter.finalBlowMeleeSmash;

    if (titleEl) titleEl.textContent = smash.name;
    if (quoteEl) quoteEl.textContent = `"${smash.quote}"`;
    if (spiritEl) spiritEl.textContent = `${this.selectedFighter.spiritAnimalSymbol} ${this.selectedFighter.spiritAnimal} AWAKENED!`;

    if (modal) {
      modal.style.display = 'flex';
      modal.classList.add('flash-active');
    }

    if (smashVideo) {
      try {
        smashVideo.currentTime = 0;
        smashVideo.play().catch(() => {});
      } catch (_) {}
    }

    // Deal Massive Final Smash Damage
    setTimeout(() => {
      this.rivalHp = 0;
      this.playerSuperMeter = 0;
      this.updateBattleHUD();

      setTimeout(() => {
        if (modal) modal.style.display = 'none';
        this.endBattle(true);
      }, 1800);
    }, 1200);
  }

  endBattle(playerWon) {
    this.isBattleOver = true;
    const resultModal = document.getElementById('battleResultModal');
    const titleEl = document.getElementById('battleResultTitle');
    const quoteEl = document.getElementById('battleResultQuote');

    if (playerWon) {
      sound.playVictory();
      if (titleEl) {
        titleEl.textContent = '🏆 VICTORY!';
        titleEl.style.color = '#FFC837';
      }
      if (quoteEl) quoteEl.textContent = `"${this.selectedFighter.victoryQuote}"`;
    } else {
      if (titleEl) {
        titleEl.textContent = '💀 DEFEAT';
        titleEl.style.color = '#FF1A5E';
      }
      if (quoteEl) quoteEl.textContent = `"${this.selectedRival.victoryQuote}"`;
    }

    if (resultModal) resultModal.style.display = 'flex';
  }

  // --- PAUSE & SETTINGS SYSTEM ---
  togglePause(forceState = null) {
    this.isPaused = forceState !== null ? forceState : !this.isPaused;
    const pauseModal = document.getElementById('pauseSettingsModal');
    if (pauseModal) {
      pauseModal.style.display = this.isPaused ? 'flex' : 'none';
    }
    sound.playSelect();
  }

  applySettings() {
    sound.setSoundEnabled(this.settings.soundEnabled);
    sound.setVolume(this.settings.volume);
    sound.vibrationEnabled = this.settings.vibrationEnabled;
  }

  // --- TOUCH EVENT BINDINGS (NO ONCLICK, NO MOUSE, NO KEYBOARD) ---
  attachEventListeners() {
    const bindTouch = (id, handler) => {
      const el = document.getElementById(id);
      if (!el) return;
      const fn = (e) => {
        if (e && e.cancelable) e.preventDefault();
        handler(e);
      };
      el.addEventListener('pointerdown', fn, { passive: false });
      el.addEventListener('touchstart', fn, { passive: false });
    };

    // Navigation Buttons
    bindTouch('btnMenuStart', () => { sound.playSelect(); this.showView('start_game'); });
    bindTouch('btnMenuStory', () => { sound.playSelect(); this.showView('story_dialogue'); });
    bindTouch('btnMenuDossier', () => { sound.playSelect(); this.showView('dossier'); });
    bindTouch('btnMenuBracket', () => { sound.playSelect(); this.showView('bracket'); });
    bindTouch('btnMenuSettings', () => { this.togglePause(true); });

    bindTouch('btnStartGameEngage', () => { sound.playSelect(); this.showView('story_dialogue'); });
    bindTouch('btnStartGameBack', () => { sound.playSelect(); this.showView('menu'); });

    bindTouch('btnDossierBack', () => { sound.playSelect(); this.showView('menu'); });
    bindTouch('btnBracketBack', () => { sound.playSelect(); this.showView('menu'); });

    // Pause Controls
    bindTouch('btnPauseHeader', () => this.togglePause(true));
    bindTouch('btnResumeGame', () => this.togglePause(false));
    bindTouch('btnRestartMatch', () => { this.togglePause(false); this.startBattle(); });
    bindTouch('btnQuitToMenu', () => {
      this.togglePause(false);
      const resModal = document.getElementById('battleResultModal');
      if (resModal) resModal.style.display = 'none';
      this.showView('menu');
    });

    // Dialogue Navigation & 30px Round Right Arrow Button
    bindTouch('btnAdvanceDialogue', () => this.stepDialogueForward());
    bindTouch('btnRoundArrowMoveAhead', () => {
      sound.playSelect();
      if (this.currentView === 'story_dialogue') {
        this.stepDialogueForward();
      } else if (this.currentView === 'battle') {
        this.triggerHit('punch');
      } else if (this.currentView === 'start_game') {
        this.showView('story_dialogue');
      } else {
        this.showView('start_game');
      }
    });

    // Battle Actions
    bindTouch('btnBattlePunch', () => this.triggerHit('punch'));
    bindTouch('btnBattleKick', () => this.triggerHit('kick'));
    bindTouch('btnBattleSpecial', () => this.triggerHit('special'));
    bindTouch('btnFinalSmash', () => this.triggerFinalSmashMeleeAttack());

    // Result Modal Rematch
    bindTouch('btnResultRematch', () => {
      const resModal = document.getElementById('battleResultModal');
      if (resModal) resModal.style.display = 'none';
      this.startBattle();
    });

    // Settings Toggles
    const soundToggle = document.getElementById('toggleSound');
    if (soundToggle) {
      soundToggle.addEventListener('change', (e) => {
        this.settings.soundEnabled = e.target.checked;
        this.applySettings();
      });
    }

    const vibToggle = document.getElementById('toggleVibration');
    if (vibToggle) {
      vibToggle.addEventListener('change', (e) => {
        this.settings.vibrationEnabled = e.target.checked;
        this.applySettings();
      });
    }

    const volSlider = document.getElementById('sliderVolume');
    if (volSlider) {
      volSlider.addEventListener('input', (e) => {
        this.settings.volume = parseFloat(e.target.value);
        this.applySettings();
      });
    }

    const speedSelect = document.getElementById('selectTextSpeed');
    if (speedSelect) {
      speedSelect.addEventListener('change', (e) => {
        this.settings.textSpeedMode = e.target.value;
      });
    }

    // Dossier Tabs
    const tabs = document.querySelectorAll('.tab-btn');
    tabs.forEach(tab => {
      const handleTab = (e) => {
        if (e && e.cancelable) e.preventDefault();
        tabs.forEach(t => t.classList.remove('active'));
        tab.classList.add('active');
        this.activeTab = tab.dataset.tab;
        sound.playSelect();
        this.renderTabContent();
      };
      tab.addEventListener('pointerdown', handleTab, { passive: false });
      tab.addEventListener('touchstart', handleTab, { passive: false });
    });
  }
}

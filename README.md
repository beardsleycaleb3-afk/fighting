# Time Tournament: Champion of Eras 🥊⏳

A high-octane 2D retro-arcade fighting game and narrative tournament manual built for both Android (Jetpack Compose & Kotlin) and progressive web runtime.

![Time Tournament Banner](assets/sprites/backgrounds/stage1.png)

## ⚡ Overview

**Time Tournament** brings together legendary champions abducted from disparate epochs throughout spacetime to compete for the mythical **Chrono Core** at the summit of the Celestial Pantheon.

- **Fast-Paced Fighting Engine**: Hitboxes, frame-data striking, low sweeps, high roundhouses, jump cancels, blocking physics, and combo multipliers (up to Godlike 10x).
- **Cinematic Ultra Attacks**: Unique super gauge mechanics powering signature ultra attacks for each fighter.
- **Rich Character Lore & Dossiers**: Timeline origins, personality trait matrices with mastery percentages, combat philosophies, and full voice line logs.
- **Interactive Multi-Era Battlegrounds**: 9 unique stages spanning Feudal Kyoto, Neo Tokyo 2099, Brooklyn 1974, Imperial Rome 79 AD, and the Volcanic Caldera.
- **PWA & Android Support**: Runs seamlessly with Progressive Web App offline capabilities (Service Worker & Manifest) as well as native Android Jetpack Compose.

---

## 🥋 The Roster

| # | Fighter | Title | Era | Fighting Style | Signature Ultra |
|---|---------|-------|-----|----------------|-----------------|
| 01 | **Shadow Ninja** | *The Phantom Blade* | 1580 Feudal Kyoto | Ninjutsu & Shadowstep | Void Blade Dash |
| 02 | **Titan MMA** | *Cybernetic Octagon King* | 2099 Neo Tokyo | Cyber Freestyle Combat | Titan Impact Knee |
| 03 | **Iron Boxer** | *The Bronx Thunderbolt* | 1974 Brooklyn NYC | Classic Heavyweight Boxing | Dragon Uppercut |
| 04 | **Colosseum Grappler** | *The Imperial Lion* | 79 AD Ancient Rome | Greco-Roman Olympian | Seismic Olympia Suplex |
| 05 | **Cyber Valkyrie** | *Astral Shield Maiden* | 2140 Valkyrie Corps | Synthetic Valhalla Combat | Aurora Plasma Spear |
| 06 | **Volcanic Warlord** | *Scorched Earth Berserker* | Primeval Flame Epoch | Lava Crushing Brawler | Magma Eruption Slam |

---

## 🏛️ Arena Stages (1–9)

1. **Stage 1: Kyoto Cherry Shadows** (1580 Kyoto) — Midnight pagoda rooftop bathed in moonlit falling sakura petals.
2. **Stage 2: Neo Tokyo Skyline** (2099 Cyber Epoch) — Neon skyscrapers and rain-soaked cyber billboards.
3. **Stage 3: Brooklyn Brick Alley** (1974 Golden Era) — Steam vents, graffiti brickwork, and classic boxing roots.
4. **Stage 4: Imperial Rome Coliseum** (79 AD Classical Antiquity) — Roaring crowds and torch-lit stone amphitheater.
5. **Stage 5: Viking Fjords Aurora** (980 AD Norse Lands) — Frozen glaciers beneath celestial northern lights.
6. **Stage 6: Cyber Grid Nexus** (3000 Quantum Void) — Digital wireframe platform suspended in quantum space.
7. **Stage 7: Asgardian Skybridge** (2140 Valkyrie Fleet) — Plasma barriers defending high-altitude orbital stations.
8. **Stage 8: Volcanic Crucible** (Primeval Flame) — Molten lava chambers with eruptive pyroclastic pillars.
9. **Stage 9: Celestial Pantheon** (Apex of Time) — Golden summit harboring the tournament's Chrono Core.

---

## 📁 Repository Structure

```
├── README.md                      # Comprehensive tournament guide & documentation
├── index.html                     # Interactive Web Tournament Hub & PWA Entrypoint
├── manifest.json                  # PWA Web Application Manifest
├── sw.js                          # Service Worker for offline PWA asset caching
├── icon192.png                    # PWA Application Icon (192x192)
├── icon512.png                    # PWA Application Icon (512x512)
├── assets/
│   └── sprites/
│       └── backgrounds/
│           └── stage1.png         # Stage 1 Kyoto Pagoda Background Artwork
├── src/
│   └── js/
│       ├── fighters.js            # Complete fighter lore, stats, quotes, & traits dataset
│       ├── stages.js              # 9 Stage arenas dataset with environmental colors & lore
│       ├── audio.js               # Web Audio API 8-bit / arcade sound synthesizer
│       ├── engine.js              # Canvas combat & animated portrait renderer
│       └── app.js                 # UI interactions, tabs, PWA registration, & tournament logic
└── app/                           # Native Android Jetpack Compose Project
    └── src/main/java/com/example/ # Kotlin ViewModels, Compose screens, Audio engine, & Models
```

---

## 🚀 Getting Started

### Web / PWA
Simply open `index.html` in any modern web browser or serve using any static HTTP server:
```bash
npx serve .
# or
python3 -m http.server 8080
```

### Android Application
Build and run with Gradle:
```bash
gradle :app:assembleDebug
```

---

## 📜 License
MIT License. Built with passion for retro arcade combat and immersive character lore.

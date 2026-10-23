# ⚡ PotCounter (Fabric 1.21 – 1.21.11)

A high-performance, competitive Fabric client mod that accurately tracks splash Healing II potions used by opponents during PvP encounters.

Designed for high-intensity PvP servers (Duels, Crystal PvP, Nodebuff, BoxPvP) with seamless rendering and zero performance degradation.

---

## 📥 Download

All official releases and compiled .jar files are distributed exclusively on **[Modrinth](https://modrinth.com/mod/potcounter)**.

---

## ✨ Features

- **🎯 Smart Mutual Combat Detection:**
  - Counter activates only when mutual PvP hits are exchanged.
  - Independent tracking for multiple opponents with automatic active-target switching.
  - Opponent pot count starts accurately at -1 on the very first potion thrown.
- **🏷️ Nametag Integration:**
  - Injects dynamic potion counts directly beside opponent nametags (Player -4).
  - Full compatibility with server custom name tags, armor stands, and display entities.
- **📊 Modern Draggable HUD Menu:**
  - Minimalist, sleek black & amber bordered card.
  - Displays only pure opponent usernames and live pot counts (no duplicate nametags or clutter).
  - Fully customizable: drag to reposition, resize handle, opacity slider, scale slider, and color palettes.
- **🔊 PvP Sound Effects:**
  - Distinct auditory feedback when opponents splash potions (Orb, Anvil, Pop, Ding, Glass, etc.).
- **⚡ Maximum Performance & Compatibility:**
  - Official Fabric API render pipelines (HudRenderCallback).
  - 100% compatible with **Sodium**, **ImmediatelyFast**, **FerriteCore**, and **ModernFix**.

---

## 🌿 Branches & Minecraft Versions

This repository serves as the official source code for PotCounter. Each supported version has its own dedicated branch:

| Minecraft Version | Source Branch | Architecture Details |
| :--- | :--- | :--- |
| **1.21 – 1.21.1** | [1.21-1.21.1](https://github.com/RealBnc/PotCounter/tree/1.21-1.21.1) | MatrixStack 3D, classic nametag rendering |
| **1.21.2 – 1.21.3** | [1.21.2-1.21.3](https://github.com/RealBnc/PotCounter/tree/1.21.2-1.21.3) | Bundles era, PlayerEntityRenderState |
| **1.21.4** | [1.21.4](https://github.com/RealBnc/PotCounter/tree/1.21.4) | Pale Garden update, HudRenderCallback |
| **1.21.5** | [1.21.5](https://github.com/RealBnc/PotCounter/tree/1.21.5) | Isolated 1.21.5 architecture |
| **1.21.6** | [1.21.6](https://github.com/RealBnc/PotCounter/tree/1.21.6) | RenderPipelines & 2D Matrix3x2fStack |
| **1.21.7** | [1.21.7](https://github.com/RealBnc/PotCounter/tree/1.21.7) | RenderPipelines GUI textures |
| **1.21.8** | [1.21.8](https://github.com/RealBnc/PotCounter/tree/1.21.8) | 1.21.8 Yarn mappings |
| **1.21.9** | [1.21.9](https://github.com/RealBnc/PotCounter/tree/1.21.9) | KeyBinding.Category & Vector velocity |
| **1.21.10** | [1.21.10](https://github.com/RealBnc/PotCounter/tree/1.21.10) | Clean HUD username lookup |
| **1.21.11** | [1.21.11](https://github.com/RealBnc/PotCounter/tree/1.21.11) | Native 1.21.11 modern render pipelines |

---

## 🛠️ Keybinds & In-Game Usage

- **Default Keybind:** Press O in-game to open the PotCounter Configuration Menu.
- **Commands:**
  - Toggle Display: Switch between **HUD Menu** or **Nametag** mode.
  - Move HUD: Click **"Menu Appearance"** to drag or resize the HUD box anywhere on screen.
  - Palette Themes: Choose from 6 custom color schemes (Classic Amber, Cyberpunk Neon, Emerald, Crimson, Amethyst, Clean Slate).

---

## 🔨 Building from Source

To build any version locally, clone this repo and switch to the desired branch:

`ash
git clone https://github.com/RealBnc/PotCounter.git
cd PotCounter
git checkout <version-branch>
./gradlew build
`

The compiled jar will be located in uild/libs/.

---

## 👥 Authors
Developed by **RealBnc** and **m4ssivee**.

## 📄 License
This project is licensed under the MIT License.

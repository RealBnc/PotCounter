# ⚡ PotCounter (Fabric 1.21 – 1.21.11)

A high-performance, competitive Fabric client mod that accurately tracks splash Healing II potions used by opponents during PvP encounters.

Designed for high-intensity PvP servers (Duels, Crystal PvP, Nodebuff, BoxPvP) with seamless rendering and zero performance degradation.

---

## ✨ Features

- **🎯 Smart Mutual Combat Detection:**
  - Counter activates only when mutual PvP hits are exchanged.
  - Independent tracking for multiple opponents with automatic active-target switching.
  - Opponent pot count starts accurately at `-1` on the very first potion thrown.
- **🏷️ Nametag Integration:**
  - Injects dynamic potion counts directly beside opponent nametags (`Player -4`).
  - Full compatibility with server custom name tags, armor stands, and display entities.
- **📊 Modern Draggable HUD Menu:**
  - Minimalist, sleek black & amber bordered card.
  - Displays only pure opponent usernames and live pot counts (no duplicate nametags or clutter).
  - Fully customizable: drag to reposition, resize handle, opacity slider, scale slider, and color palettes.
- **🔊 PvP Sound Effects:**
  - Distinct auditory feedback when opponents splash potions (Orb, Anvil, Pop, Ding, Glass, etc.).
- **⚡ Maximum Performance & Compatibility:**
  - Official Fabric API render pipelines (`HudRenderCallback`).
  - 100% compatible with **Sodium**, **ImmediatelyFast**, **FerriteCore**, and **ModernFix**.

---

## 📦 Supported Versions & Downloads

Every Minecraft release has a dedicated, tailored `.jar` compiled against its official Yarn mappings:

| Minecraft Version | Download File | Notes |
| :--- | :--- | :--- |
| **1.21 – 1.21.1** | `PotCounter-[1.21-1.21.1].jar` | MatrixStack 3D, classic nametag rendering |
| **1.21.2 – 1.21.3** | `PotCounter-[1.21.2-1.21.3].jar` | Bundles era, PlayerEntityRenderState |
| **1.21.4** | `PotCounter-[1.21.4].jar` | Pale Garden update, HudRenderCallback |
| **1.21.5** | `PotCounter-[1.21.5].jar` | Isolated 1.21.5 architecture |
| **1.21.6** | `PotCounter-[1.21.6].jar` | RenderPipelines & 2D Matrix3x2fStack |
| **1.21.7** | `PotCounter-[1.21.7].jar` | RenderPipelines GUI textures |
| **1.21.8** | `PotCounter-[1.21.8].jar` | 1.21.8 Yarn build |
| **1.21.9** | `PotCounter-[1.21.9].jar` | KeyBinding.Category & Vector velocity |
| **1.21.10** | `PotCounter-[1.21.10].jar` | Fixed clean HUD username lookup |
| **1.21.11** | `PotCounter-[1.21.11].jar` | Native 1.21.11 modern render pipelines |

---

## 🛠️ Keybinds & In-Game Usage

- **Default Keybind:** Press `O` in-game to open the PotCounter Configuration Menu.
- **Commands:**
  - Toggle Display: Switch between **HUD Menu** or **Nametag** mode.
  - Move HUD: Click **"Menu Appearance"** to drag or resize the HUD box anywhere on screen.
  - Palette Themes: Choose from 6 custom color schemes (Classic Amber, Cyberpunk Neon, Emerald, Crimson, Amethyst, Clean Slate).

---

## 🔨 Building from Source

To build any version, clone the repository and run:

```bash
git checkout <version-branch>
./gradlew build
```

Built jars will be output to `build/libs/`.

---

## 📄 License
Developed by **RealBnc** and **m4ssivee**.

This project is licensed under the MIT License.


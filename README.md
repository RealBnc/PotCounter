# PotCounter

A lightweight Fabric mod that tracks how many splash health potions opponents use during PvP fights.

Works across all 1.21 versions (1.21 to 1.21.11).

## ⚠️ Requirements
PotCounter strictly requires **[m4lib](https://modrinth.com/mod/m4lib)** to work:
- **Modrinth:** [Download m4lib](https://modrinth.com/mod/m4lib)
- **GitHub:** [m4ssivee/m4lib](https://github.com/m4ssivee/m4lib)

## Features
- **Accurate Counter:** Shows how many pots the opponent used, starting at -1 on their first splash.
- **Display Modes:** Put the counter beside their nametag or on a draggable HUD card on your screen.
- **Mutual Combat:** Only starts tracking when you and the opponent trade hits.
- **Sound Alerts:** Plays custom sound alerts when an opponent splashes a potion.
- **Settings Menu:** Press O in-game to customize position, scale, opacity, colors, and sounds.
- **Compatibility:** Fully compatible with Sodium, ImmediatelyFast, and ModernFix.

## 📥 Downloads
You can download the mod from **[Modrinth](https://modrinth.com/mod/potcounter)** or grab the specific jar for your version below:

| Minecraft Version | GitHub Download | Notes |
| :--- | :--- | :--- |
| **1.21 – 1.21.1** | [Download](https://github.com/RealBnc/PotCounter/releases/download/v1.0.0/PotCounter-[1.21-1.21.1].jar) | MatrixStack 3D, classic nametag rendering |
| **1.21.2 – 1.21.3** | [Download](https://github.com/RealBnc/PotCounter/releases/download/v1.0.0/PotCounter-[1.21.2-1.21.3].jar) | Bundles update, PlayerEntityRenderState |
| **1.21.4** | [Download](https://github.com/RealBnc/PotCounter/releases/download/v1.0.0/PotCounter-[1.21.4].jar) | Pale Garden update, HudRenderCallback |
| **1.21.5** | [Download](https://github.com/RealBnc/PotCounter/releases/download/v1.0.0/PotCounter-[1.21.5].jar) | Dedicated 1.21.5 build |
| **1.21.6** | [Download](https://github.com/RealBnc/PotCounter/releases/download/v1.0.0/PotCounter-[1.21.6].jar) | RenderPipelines & 2D Matrix3x2fStack |
| **1.21.7** | [Download](https://github.com/RealBnc/PotCounter/releases/download/v1.0.0/PotCounter-[1.21.7].jar) | RenderPipelines GUI textures |
| **1.21.8** | [Download](https://github.com/RealBnc/PotCounter/releases/download/v1.0.0/PotCounter-[1.21.8].jar) | 1.21.8 Yarn mappings |
| **1.21.9** | [Download](https://github.com/RealBnc/PotCounter/releases/download/v1.0.0/PotCounter-[1.21.9].jar) | KeyBinding.Category & Vector velocity |
| **1.21.10** | [Download](https://github.com/RealBnc/PotCounter/releases/download/v1.0.0/PotCounter-[1.21.10].jar) | Clean HUD username lookup |
| **1.21.11** | [Download](https://github.com/RealBnc/PotCounter/releases/download/v1.0.0/PotCounter-[1.21.11].jar) | Native 1.21.11 modern render pipelines |

> All releases are also listed on the **[GitHub Releases Page](https://github.com/RealBnc/PotCounter/releases)**.

## Source Branches
Each Minecraft release has its own source branch in this repo:
1.21-1.21.1 | 1.21.2-1.21.3 | 1.21.4 | 1.21.5 | 1.21.6 | 1.21.7 | 1.21.8 | 1.21.9 | 1.21.10 | 1.21.11

## Building
`ash
git checkout <branch>
./gradlew build
`

## Credits
Made by **RealBnc** & **m4ssivee**.

# PotCounter

A client-side Fabric mod that tracks how many splash health potions your opponent uses during PvP fights.

Works across all 1.21 releases (1.21 through 1.21.11).

## Features
- **Accurate Counter:** Shows how many pots the opponent used, starting at -1 on their first splash.
- **Display Options:** Put the counter next to their nametag or on a draggable HUD on your screen.
- **Mutual Combat Only:** Only counts pots when you and the opponent are actually fighting each other.
- **Sound Alerts:** Plays a sound when an opponent splashes a potion (configurable).
- **In-Game Menu:** Press O to toggle display modes, move the HUD, change colors, or pick sounds.
- **Performance:** Fully compatible with Sodium, ImmediatelyFast, and ModernFix.

## Download
All compiled jars are available on **[Modrinth](https://modrinth.com/mod/potcounter)**.

## Source Branches
Each Minecraft version has its own dedicated branch:
- 1.21-1.21.1
- 1.21.2-1.21.3
- 1.21.4
- 1.21.5
- 1.21.6
- 1.21.7
- 1.21.8
- 1.21.9
- 1.21.10
- 1.21.11

## Building
`ash
git checkout <branch>
./gradlew build
`
Built jars will be in uild/libs/.

## Credits
Made by **RealBnc** & **m4ssivee**.

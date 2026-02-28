# WoodCutter

WoodCutter adds balanced stonecutter recipes for wood (As well as stone, and deepslate as inspired by [26.1 Snapshot 8](https://www.minecraft.net/en-us/article/minecraft-26-1-snapshot-8))

![1](https://i.imgur.com/JDXKDDp.png)

## Features

- Logs/wood/stems/hyphae can be processed directly in the stonecutter into same-family wood derivatives.
- Planks can be processed into common wooden derivatives in stonecutter-style ratios.
- Deepslate can be cut directly into cobbled, polished, brick, and tile variants.
- Stone can be cut directly into cobblestone and mossy cobblestone variants.

## Compatibility

- Paper: `1.21.x`
- Fabric (server): `1.21.11+` (official Mojang mappings / mojmap)

## Build

```bash
./gradlew build
```

On Windows:

```powershell
.\gradlew.bat build
```

Build outputs:

- Paper jar: `paper/build/libs/woodcutter-paper-1.0.0.jar`
- Fabric jar: `fabric/build/libs/woodcutter-fabric-1.0.0.jar`

## Install

### Paper

1. Stop the server.
2. Put `woodcutter-paper-1.0.0.jar` in `plugins/`.
3. Start the server.

### Fabric (server)

1. Install Fabric Loader for `1.21.11+`.
2. Put `woodcutter-fabric-1.0.0.jar` in `mods/`.
3. Start the server.

## Project Layout

- `common/` shared recipe definitions (`recipes.csv`)
- `paper/` Paper plugin implementation
- `fabric/` Fabric mod + generated recipe data during build

## License

Licensed under `GNU General Public License v3.0` (GPL-3.0-only). See [LICENSE](LICENSE).
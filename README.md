# WoodCutter

WoodCutter adds balanced stonecutter recipes for wood (As well as stone, and deepslate as inspired by [26.1 Snapshot 8](https://www.minecraft.net/en-us/article/minecraft-26-1-snapshot-8))

![1](https://i.imgur.com/JDXKDDp.png)

## Features

- Logs/wood/stems/hyphae can be processed directly in the stonecutter into same-family wood derivatives.
- Planks can be processed into common wooden derivatives in stonecutter-style ratios.
- Deepslate can be cut directly into cobbled, polished, brick, and tile variants.
- Stone can be cut directly into cobblestone and mossy cobblestone variants.
- Configurable stonecutter contact damage for mobs and players (`1 heart` by default).

## Compatibility

- Paper: `1.21.x & 26.2` 
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

- Paper jar: `paper/build/libs/woodcutter-paper-1.0.1.jar`
- Fabric jar: `fabric/build/libs/woodcutter-fabric-1.0.1.jar`

## Install

### Paper

1. Stop the server.
2. Put `woodcutter-paper-1.0.1.jar` in `plugins/`.
3. Start the server.

#### Stonecutter Damage Command (Paper)

- `/stonecutterdamage status` shows current state and damage amount.
- `/stonecutterdamage on` enables damage.
- `/stonecutterdamage off` disables damage entirely.
- `/stonecutterdamage set <hearts>` sets damage in hearts (supports decimals like `0.5`).
- Permission: `woodcutter.command.stonecutterdamage` (default: `op`).

Default config values (`plugins/WoodCutter/config.yml`):

```yml
stonecutter-damage:
  enabled: true
  hearts: 1.0
```

### Fabric (server)

1. Install Fabric Loader for `1.21.11+`.
2. Put `woodcutter-fabric-1.0.1.jar` in `mods/`.
3. Start the server.

#### Stonecutter Damage Command (Fabric)

- `/stonecutterdamage status` shows current state and damage amount.
- `/stonecutterdamage on` enables damage.
- `/stonecutterdamage off` disables damage entirely.
- `/stonecutterdamage set <hearts>` sets damage in hearts (supports decimals like `0.5`).
- Permission level: `2` (ops by default).

Default config values (`config/woodcutter.properties`):

```properties
stonecutter.damage.enabled=true
stonecutter.damage.hearts=1.0
```

## Project Layout

- `common/` shared recipe definitions (`recipes.csv`)
- `paper/` Paper plugin implementation
- `fabric/` Fabric mod + generated recipe data during build

## License

Licensed under `GNU General Public License v3.0` (GPL-3.0-only). See [LICENSE](LICENSE).

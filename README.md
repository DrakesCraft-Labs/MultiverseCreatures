# MultiverseCreatures (Fork by JackStar6677-1)

Plugin for custom themed creatures and items on Spigot/Paper 1.20.6.

## Current features
- Shaggy replacement on Wandering Trader spawn (30% chance).
- Custom trades for Scooby Cookies and Excalibur Sword.
- Scooby Cookies grant Resistance VI for 10 seconds.
- Optional player-skin disguise system for themed mobs.

## New in this fork: Player skins on entities
This fork keeps normal mob AI/trades, but can render the entity as a player skin using `LibsDisguises`.

How it works:
- Entity is still a real `WanderingTrader` (safe for vanilla behavior).
- Visual appearance becomes a player model/skin when enabled.

Config file: `plugins/MultiverseCreatures/config.yml`

```yaml
skins:
  enabled: false
  source: fixed                # fixed | nearest-player | random-online-player | entity-name
  fixed-name: Notch
  nearest-player-max-distance: 64.0
```

Notes:
- Requires `LibsDisguises` in `/plugins` to apply skins.
- `source: fixed` and other name-based modes depend on resolvable skin names.
- For offline/non-premium environments, use a skin provider plugin (for example `SkinsRestorer`) and valid skin names from that provider.

## Requirements
- Minecraft server: Paper/Spigot 1.20.6
- Java: 21+
- Optional: LibsDisguises (for skin rendering)

## Build
```bash
mvn clean package -DskipTests
```
Output jar:
- `target/MultiverseCreatures-v1.0.jar`

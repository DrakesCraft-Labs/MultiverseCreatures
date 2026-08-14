# 🧪 Crafting Components

The plugin's loot system is intentionally **simple and thematic**: each mob drops a unique ingredient, and those ingredients are used to craft the legendary items. Drops happen on the mob's death and respect a `drop-chance` (in `config.yml`), which is rolled per-kill.

Drop sources — for each component, the mob and chance:

| Component | Source mob | Drop chance | Theme tag |
|---|---|---|---|
| **Wheel Essence** `§f§lWheel Essence` (NETHERITE_SCRAP) | Mahoraga | 75% | Multiverse (JJK) |
| **Chaos Orb** `§d§lChaos Orb` (NETHER_STAR) | Chaos Mage | 60% | Multiverse |
| **Ender Fragment** `§3§lEnder Fragment` (ENDER_PEARL) | Ender Knight | 55% | Multiverse |
| **Frost Heart** `§b§lFrost Heart` (BLUE_ICE) | Frost Golem | 75% | Multiverse |
| **Magma Core** `§6§lMagma Core` (MAGMA_CREAM) | Flame Elemental | 60% | Multiverse |
| **Storm Crystal** `§e§lStorm Crystal` (QUARTZ) | Storm Caller | 60% | Multiverse |
| **Venom Gland** `§2§lVenom Gland` (SPIDER_EYE) | Venom Witch | 60% | Multiverse |
| **Void Essence** `§5§lVoid Essence` (ENDER_EYE) | Void Crawler | 50% | Multiverse |
| **Reaper Essence** `§0§lReaper Essence` (SOUL_LANTERN) | Soul Reaper | 60% | Multiverse |
| **Reinforced Bone** `§f§lReinforced Bone` (BONE) | Bone Shield | 80% | Multiverse |
| **Bone Marrow** `§f§lBone Marrow` (BONE_MEAL) | crafted from **Reinforced Bone** + Redstone Blocks + Nether Wart | — | Multiverse |
| **Ossified Plate** `§f§lOssified Plate` (CALCITE) | crafted from **Bone Marrow** + Calcite + Diamond | — | Multiverse |
| **Molten Marrow** `§6§lMolten Marrow` (REDSTONE) | **Blast Furnace ONLY**: 1 Ossified Plate (100 ticks, 0.5 XP) | — | Multiverse |
| **Shadow Cloak Fragment** `§8§lShadow Cloak Fragment` (BLACK_WOOL) | Shadow Rogue | 50% | Multiverse |
| **Obsidian Shard** `§8§lObsidian Shard` (OBSIDIAN) | Obsidian Guard | 85% | Multiverse |
| **Head Slime Heart** `§a§lHead Slime Heart` (SLIME_BALL) | Head Slime | always (100%) | Slime Kingdom |
| **Military Component** `§a§lMilitary Component` (GUNPOWDER) | each ZombieHorseTrap unit | 30% (`zombie-horse-trap.military-component-drop-chance`) | Military |
| **Star Core** `§e§lStar Core` (NETHER_STAR) | special / "from a superior entity" | — | Multiverse |
| **Wheel Core** `§6§lWheel Core` (MUSIC_DISC_OTHERSIDE) | crafted from **Wheel Essence** + Diamond Block + Nether Star; **must be smelted in a Blast Furnace** | — | Multiverse (JJK) |
| **Molten Wheel Core** `§6§lMolten Wheel Core` (BLAZE_POWDER) | **Blast Furnace**: 1 Wheel Core (100 ticks, 0.5 XP) | — | Multiverse (JJK) |
| **Molten Netherite** `§8§lMolten Netherite` (ANCIENT_DEBRIS) | **Blast Furnace**: 1 Refined Netherite (100 ticks, 0.5 XP) | — | Multiverse |
| **Refined Wheel Core** `§6§lRefined Wheel Core` (MUSIC_DISC_OTHERSIDE) | crafted from **Molten Wheel Core** + **Molten Netherite** | — | Multiverse (JJK) |
| **Reaper Core** `§0§lReaper Core` (WITHER_ROSE) | crafted from **Reaper Essence** + Soul Sand + Nether Star | — | Multiverse |
| **Refined Netherite** `§8§lRefined Netherite` (NETHERITE_INGOT) | crafted from **4 Star Core** (corners) + **4 Netherite Scrap** (sides) + **Compressed Gold Block** (center, 9 Gold Blocks) | — | Multiverse |

Each component is just a `msc_<name>` tagged ingredient — it does **nothing on its own**, but it's required to craft the corresponding legendary item.

---

## Crafting chains (loot → item)

```
Mahoraga ─┬─► Wheel Essence ────► Eight-Handled Wheel (helmet)
Chaos Mage ─► Chaos Orb ────────► Chaos Forge (reforge tool)
Ender Knight ─► Ender Fragment ─► Aether Pullshot (trident)
Frost Golem ─► Frost Heart ─────► Frost Heart (off-hand)
Flame Elemental ─► Magma Core ──► Cinder Greatsword
Storm Caller ─► Storm Crystal ──► Skyfire Talisman
Venom Witch ─► Venom Gland ─────► Venomfang (dagger)
Void Crawler ─► Void Essence ───► Nullshear Edge
Soul Reaper ─► Reaper Essence ──► Soulreap Scythe
Bone Shield ─► Reinforced Bone ─► [Bone Marrow ─► Ossified Plate ─► *Blast Furnace (only)* Molten Marrow] ─► Marrow Aegis (shield)
Shadow Rogue ─► Shadow Cloak ──► Veilwalker Mantle (off-hand)
Obsidian Guard ─► Obsidian Shard► Obsidian Bastion (4-piece set)
Head Slime ─► Head Slime Heart ─► Head Slime Gelatin (food)
ZombieHorseTrap ─► Military Comp► Military Mine (camouflaged TNT)
Superior entity ─► Star Core ───► Excalibur (and beyond...)
Mahoraga ─► Wheel Essence ─► Wheel Core ─► [Blast Furnace] Molten Wheel Core ─► Refined Wheel Core ─► Eight-Handled Wheel (helmet)
Refined Netherite ─► [Blast Furnace] Molten Netherite ─► (mixed with Molten Wheel Core)
Soul Reaper ─► Reaper Essence ─► Reaper Core ─► Soulreap Scythe
Obsidian Guard ─► Obsidian Shard + Refined Netherite (4 Star Core + 4 Scrap + Compressed Gold Block) ─► Obsidian Bastion
```

---

## Give command (testing only)

You can hand yourself any component directly for testing:

```
/msc give chaosorb
/msc give enderfragment    (alias: ender)
/msc give frostheart        (alias: frost)
/msc give magmacore         (alias: magma)
/msc give obsidianshard     (alias: shard)
/msc give reaperessence     (alias: reaper)
/msc give reinforcedbone    (alias: bone)
/msc give bonemarrow        (alias: marrow)
/msc give ossifiedplate     (alias: plate)
/msc give moltenmarrow
/msc give shadowcloak       (alias: cloak)
/msc give stormcrystal      (alias: storm)
/msc give venomgland        (alias: venom)
/msc give voidessence       (alias: void)
/msc give wheelessence      (alias: whelessence)
/msc give headslimeheart    (alias: heart)
/msc give militarycomponent (alias: component)
/msc give starcore          (alias: star)
/msc give wheelcore
/msc give moltenwheelcore   (alias: moltenwheel)
/msc give moltennetherite   (alias: molten)
/msc give refinedwheelcore  (alias: refinedwheel)
/msc give reapercore
/msc give refinednetherite
```

See [Commands](./Commands.md) for the full reference.

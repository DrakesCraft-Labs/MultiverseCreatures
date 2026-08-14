# ⚔️ Weapons

All weapons are unbreakable and use a `msc_<item>` persistent data tag. Each one has a `ItemBuilder`-constructed ItemStack with lore and is registered with a custom recipe in `MultiverseCreatures.registerRecipes()`. The behaviour lives in a `listener/<Name>Handler.java`.

Use **`/msc give <item>`** (OP-only) to obtain any weapon during testing. See [Commands](./Commands.md) for the full give list.

---

## 🗡️ Melee

### Excalibur (Avalon — Arthurian legend)

> "Whosoever holds this sword, if they be worthy, shall possess the power of the Sun itself."

| Stat | Value |
|---|---|
| Material | NETHERITE_SWORD |
| Passive | Grants **Strength III** while held |
| Active | **Solar Flare** — Right-Click fires a beam in look direction |
| Beam range | `excalibur.solar-flare.range` (20) |
| Beam radius | 1.5 |
| Damage | 12 + 100 fire ticks + Blindness I (60t) + knockback (1.5 horiz / 0.8 vert) |
| Cooldown | `excalibur.solar-flare.cooldown-ms` (15 s) |
| Particles | FLAME, SOUL_FIRE_FLAME, END_ROD, ELECTRIC_SPARK |
| Sounds | ENTITY_LIGHTNING_BOLT_THUNDER, BLOCK_BEACON_POWER_SELECT |

- **Give:** `/msc give excalibur` (alias `sword`)
- **Trade:** from the Multiverse Merchant for 16 Star Cores + 32 Netherite Ingots
- **Theme:** Avalon (Arthurian)

---

### Cinder Greatsword

> "Where it falls, the world burns."

A two-handed netherite blade forged from the heart of a Flame Elemental.

| Stat | Value |
|---|---|
| Material | NETHERITE_SWORD |
| Passive 1 | **Two-handed**: cannot pair with any off-hand item (swap cancelled) |
| Passive 2 | Struck foes catch fire (Fire Aspect II) |
| Passive 3 | Wielder gains **Fire Resistance** while held |
| Active | **Cinder Slam** — Right-Click, AoE 5-block radius |
| Slam damage | 12 + sets enemies ablaze for 4 s + knockback (0.6 Y) |
| Slam cooldown | `SLAM_COOLDOWN_MS` (10 s) |
| Particles | EXPLOSION, FLAME, LAVA |

- **Give:** `/msc give cindergreatsword` (alias `greatsword`)
- **Theme:** Multiverse (Flame Elemental drop chain via Magma Core)

---

### Nullshear Edge

> "It is not there, and yet it is."

A blade that cuts the seam between the world and the nothing behind it.

| Stat | Value |
|---|---|
| Material | NETHERITE_SWORD |
| Passive 1 | Each strike inflicts **30%** of damage as **void damage** (ignores armor) |
| Passive 2 | Striking outdoors has **10%** chance to apply **Darkness** (5 s) |
| Active | **Void Blink** — Shift + Right-Click |
| Blink range | 30 blocks |
| Blink cooldown | 20 s |
| Particles | PORTAL at both ends |

- **Give:** `/msc give nullshearedge` (alias `nullshear`)
- **Theme:** Multiverse (Void Crawler chain via Void Essence)

---

### Soulreap Scythe

> "Each soul makes the blade heavier, yet the wielder lighter."

A curved void-steel scythe humming with the lament of the unreaped.

| Stat | Value |
|---|---|
| Material | NETHERITE_HOE |
| Passive | Each strike drains **4 HP** and heals the wielder **2 HP** |
| Soul counter | Each strike collects a soul (3 souls on kill) — stored via `msc_soulreap_counter` PDC |
| Active | **Reap** — passive trigger |
| Reap trigger | After collecting **10 souls** |
| Reap duration | 10 seconds |
| Reap effects | 2× damage, improved lifesteal, soul aura (Strength I + REAP_DAMAGE_MULTIPLIER = 2.0) |
| Sounds | ENTITY_WITHER_SPAWN on activation |

- **Give:** `/msc give soulreapscythe` (alias `scythe`)
- **Theme:** Multiverse (Soul Reaper chain via Reaper Essence)

---

### Venomfang

> "One drop can dissolve a man's resolve..."

A dagger distilled from the corrosive venom of a Venom Witch.

| Stat | Value |
|---|---|
| Material | IRON_SWORD |
| Passive | Each strike applies **Poison I** (5 s) and **Wither I** (4 s) |
| Particles / Sound | ITEM_SLIME, ENTITY_SPIDER_AMBIENT |
| Unbreakable | Yes |

- **Give:** `/msc give venomfang` (alias `dagger`)
- **Craft:** from **Venom Gland** (dropped by Venom Witch) + Gold Block + Sword Mold + Stick
- **Theme:** Multiverse (Venom Witch chain via Venom Gland)

---

## 🏹 Ranged

### Aether Pullshot

> "A leash not of rope, but of distance denied."

A trident forged from an Ender Fragment, strung with a leash of threadbare space.

| Stat | Value |
|---|---|
| Material | TRIDENT |
| Enchant | Loyalty III (always returns) |
| Active | **Aether Pull** — Right-Click Entity |
| Range | 40 blocks |
| Pull duration | 3 s (`PULL_DURATION_TICKS` = 60) |
| Pull speed | 0.5 (+0.2 Y) |
| Initial damage | 6 (when struck) |
| Final damage | 10 (when target reaches 2 blocks of you) — with EXPLOSION particles + ENTITY_GENERIC_EXPLODE sound |
| Cooldown | 30 s |
| Particles | PORTAL (initial), EXPLOSION (final) |

- **Give:** `/msc give aetherpullshot` (alias `pullshot`)
- **Theme:** Multiverse (Ender Knight chain via Ender Fragment)

---

## 🔮 Magic Tools

### Skyfire Talisman

> "The storm answers, even when the sky is silent."

A copper amulet humming with the lingering rage of a Storm Caller.

| Stat | Value |
|---|---|
| Material | COPPER_INGOT |
| Active | **Skyfire Strike** — Right-Click Block |
| Strike range | 50 blocks away |
| Strike radius | 3 blocks |
| Strike damage | 8 + brief stun + knockback (0.6 Y) |
| Cooldown | 10 s |
| Passive | Wielder is **immune to lightning damage** while held |
| Particles / Sound | FLASH, ENTITY_LIGHTNING_BOLT_THUNDER |

- **Give:** `/msc give skyfiretalisman` (alias `talisman`)
- **Theme:** Multiverse (Storm Caller chain via Storm Crystal)

---

### Chaos Forge

> "In the orb, all possibilities; in the hand, only one."

A portable anvil laced with entropy. It cannot create — only twist what is already written upon an item.

| Stat | Value |
|---|---|
| Material | ANVIL |
| Active | **Reforge** — Right-Click with Forge in off-hand, enchanted item in main hand |
| Effect | Each existing enchantment on the target rises by **+1 level** (cap 30) |
| Restrictions | Only items that already have enchantments · each item can only be reforged once (marks `msc_chaos_reforged` PDC) · consumes **1 Chaos Orb** from inventory |
| Reforge tag | Appends `§4§o⟡ Reforged by Chaos ⟡` to the target's lore |
| Particles / Sounds | ENCHANT, BLOCK_ENCHANTMENT_TABLE_USE |

- **Give:** `/msc give chaosforge`
- **Theme:** Multiverse (Chaos Mage chain via Chaos Orb)

---

### Sentinel Grimoire

> "Every universe answers to the one who reads."

A forbidden tome bound with the leather of a fallen Sentinel. It holds 8 spell pages, each with its own original seal. `Shift + Right-Click` changes the page (action bar shows the selection), `Right-Click` casts the current spell.

| Page | Spell | Effect | Cooldown |
|---|---|---|---|
| 1 | 🔥 Blazing Pentagram | Vertical flaming pentagram aimed at your target · 10 dmg + ignite in 3.5 blocks | 8 s |
| 2 | 🗡️ Lance Rain | Runic triangle seal + rain of luminous lances · 12 dmg over 3 pulses | 7 s |
| 3 | ⚡ Divine Judgment | Divine seal + 3 lightning strikes · 18 dmg total | 10 s |
| 4 | ❌ Executioner's Mark | Red executioner's X over the target · explodes after 2.5 s (14 dmg + knock-up + blindness) | 10 s |
| 5 | 🌀 Singular Vortex | Vortex seal that pulls enemies in · 8 dmg | 15 s |
| 6 | 🌋 Earthquake | Quake seal · 10 dmg + launch into the air | 9 s |
| 7 | 🛡️ Celestial Bulwark | Celestial seal · Absorption (4 hearts) + Resistance | 20 s |
| 8 | ✨ Sentinel Aura | Invulnerability aura · immune to damage for 3.5 s | 45 s |

- **Give:** `/msc give sentinelgrimoire` (alias `grimoire`)
- **Recipe:** BOOK ×4 + Multiversal Core ×2 + Sentinel Core (see Recipes.md)
- **Config:** damages/cooldowns under `grimoire:` in config.yml

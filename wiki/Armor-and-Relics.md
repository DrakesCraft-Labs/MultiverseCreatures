# 🪖 Armor & Relics

This page covers armor pieces and off-hand relics. All items are unbreakable and use `ItemBuilder` + `msc_<item>` persistent data tags. Each armor/handler self-registers via `MultiverseCreatures.onEnable()`.

---

## 🪖 Armor

### Eight-Handled Wheel (Jujutsu Kaisen — Mahoraga)

> "That which adapts cannot break, that which breaks cannot return."

A crown carved from a fragment of the Eight-Handled Wheel that once turned against all harm.

| Stat | Value |
|---|---|
| Material | NETHERITE_HELMET |
| Charges | 8 (`MAX_CHARGES = 8`) |
| Charge regen | 1 charge per `CHARGE_REGEN_TICKS` (200 ticks = 10 s); regen starts when first charge consumed |
| Active (passive) | **Adaptation** — on receiving damage, consume 1 charge to become **immune to that DamageCause for 8 seconds** |
| Block cooldown | 15 s per damage cause (`BLOCK_COOLDOWN_MS`); multiple damage types in the same tick spawn separate immunity effects |
| Particles / Sound | END_ROD, BLOCK_BEACON_ACTIVATE |
| Unbreakable | Yes |

The wheel **learns the type of damage you take**, then adapts. Different damage causes have independent cooldowns.

- **Give:** `/msc give eighthandledwheel` (alias `wheel`)
- **Craft:** Refined Wheel Core (Wheel Essence → Wheel Core → *Blast Furnace* → Molten Wheel Core + Molten Netherite → Refined Wheel Core) + 4 Netherite Blocks (see [Recipes](./Recipes.md))
- **Theme:** Jujutsu Kaisen (Mahoraga drop → Wheel Essence)

---

### Obsidian Bastion (4-piece Netherite set)

> "Blacker than night, harder than resolve."

| Piece | Material |
|---|---|
| Helm | NETHERITE_HELMET |
| Chestplate | NETHERITE_CHESTPLATE |
| Greaves | NETHERITE_LEGGINGS |
| Sabatons | NETHERITE_BOOTS |

All 4 pieces ship pre-enchanted with **Protection IV · Blast Protection IV · Thorns II · Unbreaking III**.

### Full set bonus

| Effect | Value |
|---|---|
| Max Health | **+40%** (`MAX_HEALTH_BONUS = 0.4` AttributeModifier) |
| Knockback Resistance | **1.0** (full immunity) |
| Fire / Lava immunity | Yes (`FIRE`, `LAVA`, `FIRE_TICK` damage causes cancelled) |
| Movement Speed penalty | **-20%** (`SPEED_PENALTY = 0.2` AttributeModifier) |

The set bonus is re-checked on join/quit/item-break and broadcasts "set bonus activated/lost" chat messages. Reference implementation for armor-set modifiers: idempotent `getModifier(key)` check before adding, `removeModifier(key)` on cleanup — no per-player maps needed.

- **Give:** `/msc give obsidianbastionhelmet` (and `chestplate` / `leggings` / `boots`) — aliases `bastionhelmet` etc.
- **Theme:** Multiverse (Obsidian Guard chain via Obsidian Shard)

---

## 🛡️ Off-hand Relics

All three off-hand relics only work in the off-hand slot and use unique `msc_<item>` tags.

### Marrow Aegis

> "Death's architecture, preserved in marrow."

A shield carved from reinforced bone.

| Stat | Value |
|---|---|
| Material | SHIELD |
| Passive | **Successful block reflects 50%** of incoming damage as **true damage** (`REFLECT_FRACTION = 0.5`) to the attacker |
| Block bonus | Grants **Resistance II** + **Strength I** for 5 seconds on a successful block |
| Recharge cooldown | 15 s (`RECHARGE_COOLDOWN_MS`) |
| Particles | ITEM_SHIELD_BLOCK, CRIT |
| Unbreakable | Yes |

- **Give:** `/msc give marrowaegis` (alias `aegis`)
- **Theme:** Multiverse (Bone Shield chain via Reinforced Bone)

---

### Veilwalker Mantle

> "Time stops where I tread, and the world forgets my name."

A chronomantic pocket-watch torn from the shadow of a Rogue. Its ticking bends light and time.

| Stat | Value |
|---|---|
| Material | CLOCK |
| Active | **Step Through** — Right-Click Air |
| Stealth duration | 10 s (Invisibility + Speed I) |
| Stealth cooldown | 30 s |
| Passive | First strike from stealth deals **+50%** damage (`BACKSTAB_DAMAGE_MULTIPLIER = 1.5`) — backstab removes stealth + shows "Backstab! +N% damage" message |
| Sound | ENTITY_ENDERMAN_TELEPORT |
| PDC tags | `msc_veilwalker_mantle`, stealth tag `MSC_VeilMantle_Stealth` |
| Unbreakable | Yes |

- **Give:** `/msc give veilwalkermantle` (alias `mantle`)
- **Theme:** Multiverse (Shadow Rogue chain via Shadow Cloak Fragment)

---

### Frost Heart (off-hand)

> "It beats once a century, and winter follows."

A frozen core pulsed from a Frost Golem's chest. Only the off-hand can steady its endless chill.

| Stat | Value |
|---|---|
| Material | LIGHT_BLUE_DYE |
| Passive (active only in off-hand) | Melee attackers are chilled: **Slowness II** + **Weakness I** for 3 s |
| Aura | Enemies within **4 blocks** are slowed |
| Wielder bonus | Grants **Frost Walker I** while held |
| Particles / Sound | SNOWFLAKE, ENTITY_PLAYER_HURT_FREEZE |
| Unbreakable | Yes |

- **Give:** `/msc give frostheartoffhand` (alias `frostoffhand`)
- **Theme:** Multiverse (Frost Golem chain via Frost Heart component)

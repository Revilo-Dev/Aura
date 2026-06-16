# Aura Mod

Aura is a progression and combat expansion mod with two connected systems:

- **Skills** (passive character growth and utility/combat scaling)
- **Abilities** (active elemental ability trees with cooldowns, specialization switching, and HUD support)

The mod integrates with **LevelUP** for progression rewards.

---

## Core Features

### 1) Skills System
- Persistent per-player skill tree with primary and secondary nodes.
- Skill points gained from leveling (LevelUP integration).
- Upgrade/downgrade UI and server-side validation.
- Passive and reactive effects applied through events and periodic ticks.

#### Skill categories
- **Strength**: Strength, Power, Crit Power, Haste
- **Resistance**: Resistance, Fire Resistance, Projectile Resistance, Knockback Resistance
- **Agility**: Agility, Leaping
- **Vitality**: Vitality, Regeneration, Leaching, Immunity (Cleanse reduction)
- **Luck**: Luck, Looting, Fortune

#### Skill gameplay hooks
- Extra bow damage, crit scaling, mining speed.
- Incoming damage reduction and knockback reduction.
- Movement/jump buffs.
- Regeneration/vitality bonuses.
- Leaching chance (excludes armor stands / invalid targets).
- Looting and ore-drop multipliers.
- Negative effect duration/strength reduction.

---

### 2) Abilities System
- Elemental ability trees with:
  - **Core mastery node** per element (upgrade/downgrade)
  - **Specialization chain** (selectable active sub-abilities)
- Ability points gained from level intervals.
- Cooldown, rank scaling, duration/radius/damage tuning.
- Client + server sync, anti-desync behavior, cooldown checks.

#### Elements and ability chains
- **Fire**: Nova -> Burst -> Implode -> Storm
- **Ice**: Nova -> Burst -> Implode -> Pierce -> Glacier -> Storm
- **Lightning**: Nova -> Zap -> Implode -> Strike -> Storm
- **Poison**: Nova -> Burst -> Implode
- **Force**: Aegis -> Implode(Burst-type) -> Rampage
- **Magic**: Heal -> Cleanse
- **Wind**: Dash -> Leap -> Lunge

#### Current behavior highlights
- Force Blast/Implode-style burst does not move caster; explosion-focused.
- Lightning Strike uses actual lightning bolt behavior without persistent fire left behind.
- Lightning Storm cooldown fixed to **60s** and uses dense particle strikes.
- Storm/Nova/tooltip behavior updated to match current balancing changes.

---

### 3) Ability Switching and Restrictions
- Specializations are switched via UI selection.
- Switching can be blocked when:
  - target ability is on cooldown
  - currently selected specialization for that element is on cooldown
- Added cancellable event:
  - `AbilitySwitchEvent.Pre` (cancel to block switching, e.g. dungeon lockout)
  - `AbilitySwitchEvent.Post`

---

### 4) HUD and Input
- Shared per-element keybind model (e.g. Fire Ability, Ice Ability, etc.).
- Ability HUD with cooldown overlay + optional timer text.
- Most-recently-used ordering with immediate updates when specialization changes.
- ALT grid selection mode for active specializations.

---

### 5) Effects and Potions
- **Ability Power Boost** custom effect + potion chain.
- **Rampaging** custom effect (used by Rampage ability), with custom icon and name.
- Rampage now applies the custom effect rather than directly applying vanilla Speed/Strength effects.

---

### 6) Equipment Bonuses for Skills and Abilities (Aura 4.1+)
- Armor and other equipment can affect Aura skills and abilities through normal Minecraft/NeoForge attribute modifiers.
- `aura:ability_power` scales ability damage, radius, duration, healing, and mobility.
- `aura:skill_<skill>_bonus` adds temporary effective skill levels without changing saved player skill levels.
- `aura:ability_<ability>_bonus` adds temporary effective ability ranks without changing saved player ability ranks.
- Server gameplay uses effective values. Saved progression, points, and NBT formats are unchanged.

#### Implementing armor bonuses in another mod
For Aura `4.1+`, create armor normally and add Aura attributes through the item attribute modifier API for the relevant equipment slot. Use:

- `AttributeModifier.Operation.ADD_VALUE` for skill and ability rank bonuses such as `+1 speed`, `+1 lightning`, or `+1 rampage`.
- `AttributeModifier.Operation.ADD_VALUE` or another deliberate operation for `aura:ability_power`, depending on whether your set should add a flat amount or scale an existing value.
- Stable modifier ids per armor piece and attribute, so Minecraft can remove the modifier cleanly when the armor is unequipped.

Example concept for a chestplate:

```java
new AttributeModifier(
        ResourceLocation.fromNamespaceAndPath("yourmod", "assassin_chest_rampage"),
        1.0D,
        AttributeModifier.Operation.ADD_VALUE
)
```

Attach that modifier to `aura:ability_force_rampage_bonus` for the chest equipment slot. Repeat the same pattern for `aura:ability_power`, vanilla movement speed, vanilla armor, max health, thorns enchantments, or any other loadout stats your armor set needs.

#### Skill bonus attributes
- `aura:skill_strength_bonus`
- `aura:skill_power_bonus`
- `aura:skill_crit_power_bonus`
- `aura:skill_haste_bonus`
- `aura:skill_resistance_bonus`
- `aura:skill_fire_resistance_bonus`
- `aura:skill_projectile_resistance_bonus`
- `aura:skill_knockback_resistance_bonus`
- `aura:skill_agility_bonus` (use this for loadout speed skill bonuses)
- `aura:skill_leaping_bonus`
- `aura:skill_vitality_bonus`
- `aura:skill_regeneration_bonus`
- `aura:skill_health_boost_bonus` (Leaching)
- `aura:skill_cleanse_bonus`
- `aura:skill_luck_bonus`
- `aura:skill_looting_bonus`
- `aura:skill_fortune_bonus`

#### Ability bonus attributes
- Core element bonuses: `aura:ability_fire_bonus`, `aura:ability_ice_bonus`, `aura:ability_lightning_bonus`, `aura:ability_poison_bonus`, `aura:ability_force_bonus`, `aura:ability_magic_bonus`, `aura:ability_wind_bonus`
- Specialization bonuses use the enum-style id, for example `aura:ability_force_rampage_bonus`, `aura:ability_lightning_strike_bonus`, `aura:ability_wind_lunge_bonus`, `aura:ability_poison_nova_bonus`.
- A core element bonus such as `aura:ability_lightning_bonus` grants effective access/scaling for that element. A specialization bonus can grant access to that specialization at effective core rank 1.

#### Loadout armor example
Another mod can implement class armor by adding these attributes to its armor pieces:

| Armor set | Aura attribute examples |
| --- | --- |
| Assassin Set | high vanilla movement speed, low armor/toughness, low `aura:ability_power`, `aura:ability_force_rampage_bonus` +1 |
| Knight Set | high vanilla armor, medium health/speed, low `aura:ability_power`, `aura:skill_strength_bonus` +2 |
| Berserker Set | medium armor/speed, high health, low `aura:ability_power`, `aura:ability_force_rampage_bonus` +1, `aura:skill_strength_bonus` +1 |
| Vanguard Set | very high armor, very low speed, massive health, medium thorns, very low `aura:ability_power`, `aura:ability_lightning_bonus` +1 |
| Samurai Set | high speed, medium armor/health, small `aura:ability_power`, `aura:ability_poison_bonus` +1 |
| Reaper Set | medium armor/speed, high thorns, medium-high `aura:ability_power`, `aura:ability_wind_bonus` +1 |
| Ranger Set | high speed, medium armor, low health, low `aura:ability_power`, `aura:skill_power_bonus` +1, `aura:skill_agility_bonus` +1 |
| Marksman Set | medium armor/speed, small health, medium `aura:ability_power`, `aura:skill_power_bonus` +1, `aura:skill_resistance_bonus` +1 |
| Gladiator Set | medium-high armor, medium speed/thorns/health, medium `aura:ability_power`, `aura:skill_resistance_bonus` +1, `aura:skill_strength_bonus` +1 |
| Spellblade Set | medium armor/speed, high `aura:ability_power`, small health, `aura:ability_magic_bonus` +1, `aura:ability_ice_bonus` +1 |
| Warlord Set | very high armor, high thorns/health, very low speed, medium `aura:ability_power`, `aura:ability_fire_bonus` +1, `aura:skill_strength_bonus` +1 |
| Nomad Set | very high speed, low armor, small health, medium `aura:ability_power`, `aura:ability_fire_bonus` +1, `aura:ability_poison_bonus` +1 |

Use stable UUIDs/resource ids per item modifier and the relevant armor slot so bonuses are removed automatically when the armor is unequipped. Ability power is an attribute with base `1.0`; values below `1.0` weaken abilities and values above `1.0` strengthen them.

---

### 7) UI
- Inventory-integrated Skills/Abilities panel (`SkillsPanelClient`).
- Standalone Aura Book screen (`StandaloneSkillsBookScreen`).
- Ability and skill detail panels with stat lines and contextual tooltips.
- Ability tooltip stat model supports per-ability metric overrides (not only default DPS/duration).
- LevelUP values screen (`LevelUpConfigScreen`) for client/common LevelUP tuning, HUD offsets, and HUD color.

---

## Aura Client Locks (`aura-client.json`)

- `blockAbilitySwitching` (default `false`): prevents selecting/changing active abilities in the UI.
- `blockUpgradeDowngrade` (default `false`): prevents skill/ability rank upgrades and downgrades in the UI.
- `blockOpenSkillsAbilitiesPanel` (default `false`): prevents opening the inventory/standalone Skills & Abilities panel.

---

## Commands

### `/skills` (permission level 2)
- `/skills level up <skill> <amount>`
- `/skills points add <amount>`
- `/skills points set <amount>`
- `/skills points reset`
- `/skills reset`

### `/abilities` (permission level 2)
- `/abilities points add <amount>`
- `/abilities points set <amount>`
- `/abilities points reset`
- `/abilities unlock <ability>`
- `/abilities reset`

---

## Configuration

### Skill config (`SkillConfig`)
- Points per level
- Spawn with skills book
- Max levels per skill
- Per-level scaling values (damage, resistances, speed, regen, leach, loot, etc.)

### Ability config (`AbilityConfig`)
- Ability enable/disable global switch
- Point interval levels
- Cooldown/scaling multipliers
- Ability-power enchant scaling
- HUD options (enabled, position, timer text)
- Max ranks per ability
- Per-ability cooldown/damage/radius/duration values

---

## Progression Integration (LevelUP)
- Skill points awarded from level-up events.
- Ability points awarded on configured level intervals.
- Server-side sync marks dirty and pushes client updates.
- XP sources routed through LevelUP APIs in skill logic paths.

---

## Technical Notes
- Mod bootstrap: `CodexMod`, client bootstrap: `CodexClientMod`.
- Data storage uses player attachments (`PlayerSkills`, `PlayerAbilities`).
- Networking includes sync payloads and action payloads for both systems.
- Most gameplay checks are server authoritative; UI mirrors server constraints.

---

## Asset Notes
- Ability icons: `assets/aura/textures/gui/abilities/`
- Skill icons: `assets/aura/textures/gui/skills/`
- Effect icons: `assets/aura/textures/mob_effect/`

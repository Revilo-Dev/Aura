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

### 6) UI
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

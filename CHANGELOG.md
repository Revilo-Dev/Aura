# Changelog

## Update 4.1

### Equipment Integration
- Added Aura attribute support for armor and other equipment-driven loadouts.
- Added `aura:ability_power` equipment scaling support for ability damage, radius, duration, healing, and mobility.
- Added additive skill bonus attributes:
  - `aura:skill_<skill>_bonus`
  - examples: `aura:skill_agility_bonus`, `aura:skill_strength_bonus`, `aura:skill_power_bonus`
- Added additive ability bonus attributes:
  - `aura:ability_<ability>_bonus`
  - examples: `aura:ability_lightning_bonus`, `aura:ability_force_rampage_bonus`, `aura:ability_magic_bonus`
- Effective skill and ability values are calculated at runtime from saved progression plus equipment attributes.
- Saved player skill/ability data and point totals are unchanged.

### Mod Integration Notes
- Other mods can implement class armor sets by adding normal NeoForge item attribute modifiers for Aura attributes.
- Use `ADD_VALUE` for loadout rank bonuses such as `+1 lightning`, `+1 rampage`, `+1 power`, or `+2 strength`.
- Use stable modifier ids per armor item and attribute so bonuses are removed automatically when the armor is unequipped.
- See the README section `Equipment Bonuses for Skills and Abilities (Aura 4.1+)` for the full attribute list and loadout examples.

## Update 3

### Abilities
- Leaching now only procs on valid mobs with health (ignores armor stands/non-mob targets).
- Force Blast behavior updated:
  - no caster pull/movement
  - explosion-focused hit
  - slightly increased damage
- Storm cooldown updates:
  - Fire Storm: 30s
  - Ice Storm: 35s
  - Lightning Storm: 60s (fixed)
- Ability descriptions improved for clearer behavior/scaling text.
- Lightning Nova visual update:
  - denser lightning particles
  - added lightning-bolt style particle arcs
  - removed glowing application from lightning hits
- Nova duration increased by +2s.
- Lightning Strike now summons real lightning and suppresses fire left by the strike.
- Lightning Storm:
  - no longer uses real bolt summon during storm ticks (prevents caster self-hit behavior)
  - denser strike particles
  - damage tuned to 5 DPS.
- Wind abilities buffed (dash/leap/lunge mobility and lunge damage increased).

### Ability Switching / Rules
- Added cooldown guard for specialization switching:
  - cannot switch into a specialization on cooldown
  - cannot switch away if current specialization for that element is on cooldown
- Added new cancellable switch event:
  - `AbilitySwitchEvent.Pre`
  - `AbilitySwitchEvent.Post`

### UI / HUD / Tooltips
- Mastery (core) nodes now use upgrade/downgrade clicks.
- Sub-ability (specialization) nodes reverted to select-only behavior.
- HUD now updates immediately on specialization switch.
- HUD recent list fixed to keep most-recent ability first and replace unselected specialization from the same element.
- Keybind names normalized to shared element names:
  - Fire Ability, Ice Ability, Lightning Ability, Poison Ability, Force Ability, Magic Ability, Wind Ability.
- Ability tooltip stats reworked to use per-ability meaningful metrics (instead of generic defaults), including:
  - Distance / Height / Damage / Health / Dmg Avoids / Radius / Projectiles where appropriate.

### Effects
- Rampage now uses a custom `Rampaging` effect instead of directly applying vanilla Strength/Speed effects.
- Added Rampaging effect icon and name localization.

### Screen / Layout
- Ability list cutout/viewport alignment updated in both inventory panel and standalone Codex screen to match expected right-edge alignment.

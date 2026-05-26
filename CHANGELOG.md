# Changelog

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

package net.revilodev.codex.abilities;

import net.minecraft.resources.ResourceLocation;
import net.revilodev.codex.skills.SkillId;

public enum AbilityId {
    FIRE("Fire", "Mastery of fire abilities.", AbilityElement.FIRE, null, AbilityNodeType.CORE, null, SkillId.STRENGTH, 5, 0, "fire"),
    FIRE_NOVA("Fire Nova", "Radius of fire around the player.", AbilityElement.FIRE, AbilitySpecialization.NOVA, AbilityNodeType.SPECIALIZATION, FIRE, SkillId.STRENGTH, 1, 200, "fire_nova"),
    FIRE_BURST("Fire Burst", "Burst of fire projectiles.", AbilityElement.FIRE, AbilitySpecialization.BURST, AbilityNodeType.SPECIALIZATION, FIRE_NOVA, SkillId.STRENGTH, 1, 120, "fire_burst"),
    FIRE_IMPLODE("Fire Implode", "Explosion that sets enemies on fire.", AbilityElement.FIRE, AbilitySpecialization.IMPLODE, AbilityNodeType.SPECIALIZATION, FIRE_BURST, SkillId.STRENGTH, 1, 240, "fire_implode"),
    FIRE_STORM("Fire Storm", "Rains fire from the sky.", AbilityElement.FIRE, AbilitySpecialization.STORM, AbilityNodeType.SPECIALIZATION, FIRE_IMPLODE, SkillId.STRENGTH, 1, 420, "fire_storm"),

    ICE("Ice", "Mastery of ice abilities.", AbilityElement.ICE, null, AbilityNodeType.CORE, null, SkillId.RESISTANCE, 5, 0, "ice"),
    ICE_NOVA("Ice Nova", "Radius of slowness around the player.", AbilityElement.ICE, AbilitySpecialization.NOVA, AbilityNodeType.SPECIALIZATION, ICE, SkillId.RESISTANCE, 1, 200, "ice_nova"),
    ICE_BURST("Ice Burst", "Burst of ice projectiles.", AbilityElement.ICE, AbilitySpecialization.BURST, AbilityNodeType.SPECIALIZATION, ICE_NOVA, SkillId.RESISTANCE, 1, 120, "ice_burst"),
    ICE_IMPLODE("Ice Implode", "Explosion that slows enemies.", AbilityElement.ICE, AbilitySpecialization.IMPLODE, AbilityNodeType.SPECIALIZATION, ICE_BURST, SkillId.RESISTANCE, 1, 240, "ice_implode"),
    ICE_PIERCE("Ice Pierce", "Piercing bullet of ice.", AbilityElement.ICE, AbilitySpecialization.PIERCE, AbilityNodeType.SPECIALIZATION, ICE_IMPLODE, SkillId.RESISTANCE, 1, 180, "ice_pierce"),
    ICE_GLACIER("Ice Glacier", "Ice spikes erupt from the ground.", AbilityElement.ICE, AbilitySpecialization.GLACIER, AbilityNodeType.SPECIALIZATION, ICE_PIERCE, SkillId.RESISTANCE, 1, 360, "ice_glacier"),
    ICE_STORM("Ice Storm", "Raining ice spikes.", AbilityElement.ICE, AbilitySpecialization.STORM, AbilityNodeType.SPECIALIZATION, ICE_PIERCE, SkillId.RESISTANCE, 1, 460, "ice_storm"),

    LIGHTNING("Lightning", "Mastery of lightning abilities.", AbilityElement.LIGHTNING, null, AbilityNodeType.CORE, null, SkillId.AGILITY, 5, 0, "lightning"),
    LIGHTNING_NOVA("Lightning Nova", "Radius that strikes nearby mobs.", AbilityElement.LIGHTNING, AbilitySpecialization.NOVA, AbilityNodeType.SPECIALIZATION, LIGHTNING, SkillId.AGILITY, 1, 220, "lightning_nova"),
    LIGHTNING_ZAP("Lightning Zap", "Strike multiple enemies with weak lightning.", AbilityElement.LIGHTNING, AbilitySpecialization.ZAP, AbilityNodeType.SPECIALIZATION, LIGHTNING_NOVA, SkillId.AGILITY, 1, 180, "lightning_zap"),
    LIGHTNING_IMPLODE("Lightning Implode", "Explosion that strikes enemies.", AbilityElement.LIGHTNING, AbilitySpecialization.IMPLODE, AbilityNodeType.SPECIALIZATION, LIGHTNING_ZAP, SkillId.AGILITY, 1, 260, "lightning_implode"),
    LIGHTNING_STRIKE("Lightning Strike", "Strike one enemy with lightning.", AbilityElement.LIGHTNING, AbilitySpecialization.STRIKE, AbilityNodeType.SPECIALIZATION, LIGHTNING_IMPLODE, SkillId.AGILITY, 1, 120, "lightning_strike"),
    LIGHTNING_STORM("Lightning Storm", "Storm of many weak lightning bolts.", AbilityElement.LIGHTNING, AbilitySpecialization.STORM, AbilityNodeType.SPECIALIZATION, LIGHTNING_STRIKE, SkillId.AGILITY, 1, 420, "lightning_storm"),

    POISON("Poison", "Mastery of poison abilities.", AbilityElement.POISON, null, AbilityNodeType.CORE, null, SkillId.LUCK, 5, 0, "poison"),
    POISON_NOVA("Poison Nova", "Radius circle of poison.", AbilityElement.POISON, AbilitySpecialization.NOVA, AbilityNodeType.SPECIALIZATION, POISON, SkillId.LUCK, 1, 200, "poison_nova"),
    POISON_BURST("Poison Burst", "Burst of poison projectiles.", AbilityElement.POISON, AbilitySpecialization.BURST, AbilityNodeType.SPECIALIZATION, POISON_NOVA, SkillId.LUCK, 1, 120, "poison_burst"),
    POISON_IMPLODE("Poison Implode", "Explosion that poisons enemies.", AbilityElement.POISON, AbilitySpecialization.IMPLODE, AbilityNodeType.SPECIALIZATION, POISON_BURST, SkillId.LUCK, 1, 260, "poison_implode"),

    FORCE("Force", "Mastery of force abilities.", AbilityElement.FORCE, null, AbilityNodeType.CORE, null, SkillId.RESISTANCE, 5, 0, "force"),
    FORCE_AEGIS("Aegis", "Protects player for hits, then knocks enemies back.", AbilityElement.FORCE, AbilitySpecialization.AEGIS, AbilityNodeType.SPECIALIZATION, FORCE, SkillId.RESISTANCE, 1, 260, "aegis"),
    FORCE_BURST("Implode", "Knocks back enemies in radius.", AbilityElement.FORCE, AbilitySpecialization.BURST, AbilityNodeType.SPECIALIZATION, FORCE_AEGIS, SkillId.RESISTANCE, 1, 180, "blast"),
    FORCE_RAMPAGE("Rampage", "Existing rampage behavior.", AbilityElement.FORCE, AbilitySpecialization.RAMPAGE, AbilityNodeType.SPECIALIZATION, FORCE_BURST, SkillId.RESISTANCE, 1, 300, "rampage"),

    MAGIC("Magic", "Mastery of support magic.", AbilityElement.MAGIC, null, AbilityNodeType.CORE, null, SkillId.VITALITY, 5, 0, "magic"),
    MAGIC_HEAL("Heal", "Restore health.", AbilityElement.MAGIC, AbilitySpecialization.STRIKE, AbilityNodeType.SPECIALIZATION, MAGIC, SkillId.VITALITY, 1, 180, "heal"),
    MAGIC_CLEANSE("Cleanse", "Remove debuffs and recover.", AbilityElement.MAGIC, AbilitySpecialization.AEGIS, AbilityNodeType.SPECIALIZATION, MAGIC_HEAL, SkillId.VITALITY, 1, 240, "cleanse"),

    WIND("Wind", "Mastery of wind movement abilities.", AbilityElement.WIND, null, AbilityNodeType.CORE, null, SkillId.AGILITY, 5, 0, "wind"),
    WIND_DASH("Dash", "Quick directional dash.", AbilityElement.WIND, AbilitySpecialization.STRIKE, AbilityNodeType.SPECIALIZATION, WIND, SkillId.AGILITY, 1, 120, "dash"),
    WIND_LEAP("Leap", "Leap with forward momentum.", AbilityElement.WIND, AbilitySpecialization.BURST, AbilityNodeType.SPECIALIZATION, WIND_DASH, SkillId.AGILITY, 1, 150, "leap"),
    WIND_LUNGE("Lunge", "Lunge into a target.", AbilityElement.WIND, AbilitySpecialization.PIERCE, AbilityNodeType.SPECIALIZATION, WIND_LEAP, SkillId.AGILITY, 1, 160, "lunge");

    private final String title;
    private final String description;
    private final AbilityElement element;
    private final AbilitySpecialization specialization;
    private final AbilityNodeType type;
    private final AbilityId required;
    private final SkillId scalingSkill;
    private final int defaultMaxRank;
    private final int baseCooldownTicks;
    private final String iconName;

    AbilityId(String title, String description, AbilityElement element, AbilitySpecialization specialization, AbilityNodeType type, AbilityId required, SkillId scalingSkill, int defaultMaxRank, int baseCooldownTicks, String iconName) {
        this.title = title;
        this.description = description;
        this.element = element;
        this.specialization = specialization;
        this.type = type;
        this.required = required;
        this.scalingSkill = scalingSkill;
        this.defaultMaxRank = defaultMaxRank;
        this.baseCooldownTicks = baseCooldownTicks;
        this.iconName = iconName;
    }

    public String title() { return title; }
    public String description() { return description; }
    public AbilityElement element() { return element; }
    public AbilitySpecialization specialization() { return specialization; }
    public AbilityNodeType type() { return type; }
    public AbilityId required() { return required; }
    public SkillId scalingSkill() { return scalingSkill; }
    public int defaultMaxRank() { return defaultMaxRank; }
    public int baseCooldownTicks() { return baseCooldownTicks; }
    public boolean isCore() { return type == AbilityNodeType.CORE; }
    public boolean isSpecialization() { return type == AbilityNodeType.SPECIALIZATION; }

    public AbilityId core() {
        if (isCore()) return this;
        for (AbilityId id = this; id != null; id = id.required) {
            if (id.isCore()) return id;
        }
        return this;
    }

    public int maxRank() {
        return Math.max(1, Math.min(defaultMaxRank, AbilityConfig.maxRank(this)));
    }

    public ResourceLocation iconTexture() {
        return ResourceLocation.fromNamespaceAndPath("codex", "textures/gui/abilities/" + iconName + ".png");
    }

    public static AbilityId byOrdinal(int ordinal) {
        AbilityId[] values = values();
        if (ordinal < 0 || ordinal >= values.length) return null;
        return values[ordinal];
    }
}

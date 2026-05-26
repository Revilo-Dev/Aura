package net.revilodev.aura.abilities;

import net.minecraft.resources.ResourceLocation;
import net.revilodev.aura.skills.SkillId;

public enum AbilityId {
    FIRE("Fire", "Unlocks and strengthens fire specializations.", AbilityElement.FIRE, null, AbilityNodeType.CORE, null, SkillId.STRENGTH, 6, 0, "fire"),
    FIRE_NOVA("Fire Nova", "Ignites nearby enemies in an area around you.", AbilityElement.FIRE, AbilitySpecialization.NOVA, AbilityNodeType.SPECIALIZATION, FIRE, SkillId.STRENGTH, 1, 200, "fire_nova"),
    FIRE_BURST("Fire Burst", "Fires a forward spread of flame projectiles.", AbilityElement.FIRE, AbilitySpecialization.BURST, AbilityNodeType.SPECIALIZATION, FIRE_NOVA, SkillId.STRENGTH, 1, 120, "fire_burst"),
    FIRE_IMPLODE("Fire Implode", "Pulls nearby enemies inward, then burns them.", AbilityElement.FIRE, AbilitySpecialization.IMPLODE, AbilityNodeType.SPECIALIZATION, FIRE_BURST, SkillId.STRENGTH, 1, 240, "fire_implode"),
    FIRE_STORM("Fire Storm", "Creates a storm that repeatedly scorches nearby enemies while active.", AbilityElement.FIRE, AbilitySpecialization.STORM, AbilityNodeType.SPECIALIZATION, FIRE_IMPLODE, SkillId.STRENGTH, 1, 600, "fire_storm"),

    ICE("Ice", "Unlocks and strengthens ice specializations.", AbilityElement.ICE, null, AbilityNodeType.CORE, null, SkillId.RESISTANCE, 6, 0, "ice"),
    ICE_NOVA("Ice Nova", "Damages and heavily slows nearby enemies.", AbilityElement.ICE, AbilitySpecialization.NOVA, AbilityNodeType.SPECIALIZATION, ICE, SkillId.RESISTANCE, 1, 200, "ice_nova"),
    ICE_BURST("Ice Burst", "Fires a forward spread of ice projectiles.", AbilityElement.ICE, AbilitySpecialization.BURST, AbilityNodeType.SPECIALIZATION, ICE_NOVA, SkillId.RESISTANCE, 1, 120, "ice_burst"),
    ICE_IMPLODE("Ice Implode", "Pulls nearby enemies inward and applies a slow.", AbilityElement.ICE, AbilitySpecialization.IMPLODE, AbilityNodeType.SPECIALIZATION, ICE_BURST, SkillId.RESISTANCE, 1, 240, "ice_implode"),
    ICE_PIERCE("Ice Pierce", "Launches piercing ice shots that can hit multiple enemies in a line.", AbilityElement.ICE, AbilitySpecialization.PIERCE, AbilityNodeType.SPECIALIZATION, ICE_IMPLODE, SkillId.RESISTANCE, 1, 180, "ice_pierce"),
    ICE_GLACIER("Ice Glacier", "Strikes nearby enemies with heavy ice damage and strong slow.", AbilityElement.ICE, AbilitySpecialization.GLACIER, AbilityNodeType.SPECIALIZATION, ICE_PIERCE, SkillId.RESISTANCE, 1, 360, "ice_glacier"),
    ICE_STORM("Ice Storm", "Creates a storm that repeatedly pelts nearby enemies with ice while active.", AbilityElement.ICE, AbilitySpecialization.STORM, AbilityNodeType.SPECIALIZATION, ICE_PIERCE, SkillId.RESISTANCE, 1, 700, "ice_storm"),

    LIGHTNING("Lightning", "Unlocks and strengthens lightning specializations.", AbilityElement.LIGHTNING, null, AbilityNodeType.CORE, null, SkillId.AGILITY, 6, 0, "lightning"),
    LIGHTNING_NOVA("Lightning Nova", "Shocks nearby enemies in an area around you.", AbilityElement.LIGHTNING, AbilitySpecialization.NOVA, AbilityNodeType.SPECIALIZATION, LIGHTNING, SkillId.AGILITY, 1, 220, "lightning_nova"),
    LIGHTNING_ZAP("Lightning Zap", "Chains weak lightning hits into multiple nearby enemies.", AbilityElement.LIGHTNING, AbilitySpecialization.ZAP, AbilityNodeType.SPECIALIZATION, LIGHTNING_NOVA, SkillId.AGILITY, 1, 180, "lightning_zap"),
    LIGHTNING_IMPLODE("Lightning Implode", "Pulls nearby enemies inward and strikes them with lightning.", AbilityElement.LIGHTNING, AbilitySpecialization.IMPLODE, AbilityNodeType.SPECIALIZATION, LIGHTNING_ZAP, SkillId.AGILITY, 1, 260, "lightning_implode"),
    LIGHTNING_STRIKE("Lightning Strike", "Calls a focused lightning strike on one enemy in front of you.", AbilityElement.LIGHTNING, AbilitySpecialization.STRIKE, AbilityNodeType.SPECIALIZATION, LIGHTNING_IMPLODE, SkillId.AGILITY, 1, 120, "lightning_strike"),
    LIGHTNING_STORM("Lightning Storm", "Creates a storm that repeatedly zaps nearby enemies while active.", AbilityElement.LIGHTNING, AbilitySpecialization.STORM, AbilityNodeType.SPECIALIZATION, LIGHTNING_STRIKE, SkillId.AGILITY, 1, 1200, "lightning_storm"),

    POISON("Poison", "Unlocks and strengthens poison specializations.", AbilityElement.POISON, null, AbilityNodeType.CORE, null, SkillId.LUCK, 6, 0, "poison"),
    POISON_NOVA("Poison Nova", "Poisons nearby enemies in an area around you.", AbilityElement.POISON, AbilitySpecialization.NOVA, AbilityNodeType.SPECIALIZATION, POISON, SkillId.LUCK, 1, 200, "poison_nova"),
    POISON_BURST("Poison Burst", "Fires a forward spread of poison projectiles.", AbilityElement.POISON, AbilitySpecialization.BURST, AbilityNodeType.SPECIALIZATION, POISON_NOVA, SkillId.LUCK, 1, 120, "poison_burst"),
    POISON_IMPLODE("Poison Implode", "Pulls nearby enemies inward and applies poison.", AbilityElement.POISON, AbilitySpecialization.IMPLODE, AbilityNodeType.SPECIALIZATION, POISON_BURST, SkillId.LUCK, 1, 260, "poison_implode"),

    FORCE("Force", "Unlocks and strengthens force specializations.", AbilityElement.FORCE, null, AbilityNodeType.CORE, null, SkillId.RESISTANCE, 5, 0, "force"),
    FORCE_AEGIS("Aegis", "Grants temporary aegis charges that block incoming hits and trigger knockback.", AbilityElement.FORCE, AbilitySpecialization.AEGIS, AbilityNodeType.SPECIALIZATION, FORCE, SkillId.RESISTANCE, 1, 260, "aegis"),
    FORCE_BURST("Force Burst", "Unleashes a close-range force blast that damages and knocks back nearby enemies.", AbilityElement.FORCE, AbilitySpecialization.BURST, AbilityNodeType.SPECIALIZATION, FORCE_AEGIS, SkillId.RESISTANCE, 1, 180, "blast"),
    FORCE_RAMPAGE("Rampage", "Applies the Rampaging effect for a duration, boosting melee pressure while active.", AbilityElement.FORCE, AbilitySpecialization.RAMPAGE, AbilityNodeType.SPECIALIZATION, FORCE_BURST, SkillId.RESISTANCE, 1, 300, "rampage"),

    MAGIC("Magic", "Unlocks and strengthens support magic specializations.", AbilityElement.MAGIC, null, AbilityNodeType.CORE, null, SkillId.VITALITY, 4, 0, "magic"),
    MAGIC_HEAL("Heal", "Restore a chunk of your health instantly.", AbilityElement.MAGIC, AbilitySpecialization.STRIKE, AbilityNodeType.SPECIALIZATION, MAGIC, SkillId.VITALITY, 1, 180, "heal"),
    MAGIC_CLEANSE("Cleanse", "Removes harmful effects, extinguishes fire, and restores health.", AbilityElement.MAGIC, AbilitySpecialization.AEGIS, AbilityNodeType.SPECIALIZATION, MAGIC_HEAL, SkillId.VITALITY, 1, 240, "cleanse"),

    WIND("Wind", "Unlocks and strengthens wind mobility specializations.", AbilityElement.WIND, null, AbilityNodeType.CORE, null, SkillId.AGILITY, 4, 0, "wind"),
    WIND_DASH("Dash", "Dashes you quickly in your look direction.", AbilityElement.WIND, AbilitySpecialization.STRIKE, AbilityNodeType.SPECIALIZATION, WIND, SkillId.AGILITY, 1, 120, "dash"),
    WIND_LEAP("Leap", "Launches you forward and upward.", AbilityElement.WIND, AbilitySpecialization.BURST, AbilityNodeType.SPECIALIZATION, WIND_DASH, SkillId.AGILITY, 1, 150, "leap"),
    WIND_LUNGE("Lunge", "Rushes to an enemy in front of you and deals impact damage.", AbilityElement.WIND, AbilitySpecialization.PIERCE, AbilityNodeType.SPECIALIZATION, WIND_LEAP, SkillId.AGILITY, 1, 160, "lunge");

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
        return ResourceLocation.fromNamespaceAndPath("aura", "textures/gui/abilities/" + iconName + ".png");
    }

    public static AbilityId byOrdinal(int ordinal) {
        AbilityId[] values = values();
        if (ordinal < 0 || ordinal >= values.length) return null;
        return values[ordinal];
    }
}

package net.revilodev.aura.abilities;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.chat.Component;
import net.revilodev.aura.skills.SkillId;

public enum AbilityId {
    FIRE(AbilityElement.FIRE, null, AbilityNodeType.CORE, null, SkillId.STRENGTH, 6, 0, "fire"),
    FIRE_NOVA(AbilityElement.FIRE, AbilitySpecialization.NOVA, AbilityNodeType.SPECIALIZATION, FIRE, SkillId.STRENGTH, 1, 200, "fire_nova"),
    FIRE_BURST(AbilityElement.FIRE, AbilitySpecialization.BURST, AbilityNodeType.SPECIALIZATION, FIRE_NOVA, SkillId.STRENGTH, 1, 120, "fire_burst"),
    FIRE_IMPLODE(AbilityElement.FIRE, AbilitySpecialization.IMPLODE, AbilityNodeType.SPECIALIZATION, FIRE_BURST, SkillId.STRENGTH, 1, 240, "fire_implode"),
    FIRE_STORM(AbilityElement.FIRE, AbilitySpecialization.STORM, AbilityNodeType.SPECIALIZATION, FIRE_IMPLODE, SkillId.STRENGTH, 1, 600, "fire_storm"),

    ICE(AbilityElement.ICE, null, AbilityNodeType.CORE, null, SkillId.RESISTANCE, 6, 0, "ice"),
    ICE_NOVA(AbilityElement.ICE, AbilitySpecialization.NOVA, AbilityNodeType.SPECIALIZATION, ICE, SkillId.RESISTANCE, 1, 200, "ice_nova"),
    ICE_BURST(AbilityElement.ICE, AbilitySpecialization.BURST, AbilityNodeType.SPECIALIZATION, ICE_NOVA, SkillId.RESISTANCE, 1, 120, "ice_burst"),
    ICE_IMPLODE(AbilityElement.ICE, AbilitySpecialization.IMPLODE, AbilityNodeType.SPECIALIZATION, ICE_BURST, SkillId.RESISTANCE, 1, 240, "ice_implode"),
    ICE_PIERCE(AbilityElement.ICE, AbilitySpecialization.PIERCE, AbilityNodeType.SPECIALIZATION, ICE_IMPLODE, SkillId.RESISTANCE, 1, 180, "ice_pierce"),
    ICE_GLACIER(AbilityElement.ICE, AbilitySpecialization.GLACIER, AbilityNodeType.SPECIALIZATION, ICE_PIERCE, SkillId.RESISTANCE, 1, 360, "ice_glacier"),
    ICE_STORM(AbilityElement.ICE, AbilitySpecialization.STORM, AbilityNodeType.SPECIALIZATION, ICE_PIERCE, SkillId.RESISTANCE, 1, 700, "ice_storm"),

    LIGHTNING(AbilityElement.LIGHTNING, null, AbilityNodeType.CORE, null, SkillId.AGILITY, 6, 0, "lightning"),
    LIGHTNING_NOVA(AbilityElement.LIGHTNING, AbilitySpecialization.NOVA, AbilityNodeType.SPECIALIZATION, LIGHTNING, SkillId.AGILITY, 1, 220, "lightning_nova"),
    LIGHTNING_ZAP(AbilityElement.LIGHTNING, AbilitySpecialization.ZAP, AbilityNodeType.SPECIALIZATION, LIGHTNING_NOVA, SkillId.AGILITY, 1, 180, "lightning_zap"),
    LIGHTNING_IMPLODE(AbilityElement.LIGHTNING, AbilitySpecialization.IMPLODE, AbilityNodeType.SPECIALIZATION, LIGHTNING_ZAP, SkillId.AGILITY, 1, 260, "lightning_implode"),
    LIGHTNING_STRIKE(AbilityElement.LIGHTNING, AbilitySpecialization.STRIKE, AbilityNodeType.SPECIALIZATION, LIGHTNING_IMPLODE, SkillId.AGILITY, 1, 120, "lightning_strike"),
    LIGHTNING_STORM(AbilityElement.LIGHTNING, AbilitySpecialization.STORM, AbilityNodeType.SPECIALIZATION, LIGHTNING_STRIKE, SkillId.AGILITY, 1, 1200, "lightning_storm"),

    POISON(AbilityElement.POISON, null, AbilityNodeType.CORE, null, SkillId.LUCK, 6, 0, "poison"),
    POISON_NOVA(AbilityElement.POISON, AbilitySpecialization.NOVA, AbilityNodeType.SPECIALIZATION, POISON, SkillId.LUCK, 1, 200, "poison_nova"),
    POISON_BURST(AbilityElement.POISON, AbilitySpecialization.BURST, AbilityNodeType.SPECIALIZATION, POISON_NOVA, SkillId.LUCK, 1, 120, "poison_burst"),
    POISON_IMPLODE(AbilityElement.POISON, AbilitySpecialization.IMPLODE, AbilityNodeType.SPECIALIZATION, POISON_BURST, SkillId.LUCK, 1, 260, "poison_implode"),

    FORCE(AbilityElement.FORCE, null, AbilityNodeType.CORE, null, SkillId.RESISTANCE, 5, 0, "force"),
    FORCE_AEGIS(AbilityElement.FORCE, AbilitySpecialization.AEGIS, AbilityNodeType.SPECIALIZATION, FORCE, SkillId.RESISTANCE, 1, 260, "aegis"),
    FORCE_BURST(AbilityElement.FORCE, AbilitySpecialization.BURST, AbilityNodeType.SPECIALIZATION, FORCE_AEGIS, SkillId.RESISTANCE, 1, 180, "blast"),
    FORCE_RAMPAGE(AbilityElement.FORCE, AbilitySpecialization.RAMPAGE, AbilityNodeType.SPECIALIZATION, FORCE_BURST, SkillId.RESISTANCE, 1, 300, "rampage"),

    MAGIC(AbilityElement.MAGIC, null, AbilityNodeType.CORE, null, SkillId.VITALITY, 4, 0, "magic"),
    MAGIC_HEAL(AbilityElement.MAGIC, AbilitySpecialization.STRIKE, AbilityNodeType.SPECIALIZATION, MAGIC, SkillId.VITALITY, 1, 180, "heal"),
    MAGIC_CLEANSE(AbilityElement.MAGIC, AbilitySpecialization.AEGIS, AbilityNodeType.SPECIALIZATION, MAGIC_HEAL, SkillId.VITALITY, 1, 240, "cleanse"),

    WIND(AbilityElement.WIND, null, AbilityNodeType.CORE, null, SkillId.AGILITY, 4, 0, "wind"),
    WIND_DASH(AbilityElement.WIND, AbilitySpecialization.STRIKE, AbilityNodeType.SPECIALIZATION, WIND, SkillId.AGILITY, 1, 120, "dash"),
    WIND_LEAP(AbilityElement.WIND, AbilitySpecialization.BURST, AbilityNodeType.SPECIALIZATION, WIND_DASH, SkillId.AGILITY, 1, 150, "leap"),
    WIND_LUNGE(AbilityElement.WIND, AbilitySpecialization.PIERCE, AbilityNodeType.SPECIALIZATION, WIND_LEAP, SkillId.AGILITY, 1, 160, "lunge");

    private final AbilityElement element;
    private final AbilitySpecialization specialization;
    private final AbilityNodeType type;
    private final AbilityId required;
    private final SkillId scalingSkill;
    private final int defaultMaxRank;
    private final int baseCooldownTicks;
    private final String iconName;

    AbilityId(AbilityElement element, AbilitySpecialization specialization, AbilityNodeType type, AbilityId required, SkillId scalingSkill, int defaultMaxRank, int baseCooldownTicks, String iconName) {
        this.element = element;
        this.specialization = specialization;
        this.type = type;
        this.required = required;
        this.scalingSkill = scalingSkill;
        this.defaultMaxRank = defaultMaxRank;
        this.baseCooldownTicks = baseCooldownTicks;
        this.iconName = iconName;
    }

    public String title() { return Component.translatable("ability.aura." + iconName + ".name").getString(); }
    public String description() { return Component.translatable("ability.aura." + iconName + ".description").getString(); }
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

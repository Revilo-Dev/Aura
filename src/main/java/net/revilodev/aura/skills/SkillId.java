package net.revilodev.aura.skills;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.chat.Component;
import net.revilodev.aura.CodexMod;

public enum SkillId {
    STRENGTH(SkillCategory.STRENGTH, true, null, "strength", 10),
    POWER(SkillCategory.STRENGTH, false, STRENGTH, "strength-power", 5),
    CRIT_POWER(SkillCategory.STRENGTH, false, STRENGTH, "strength-crit", 5),
    HASTE(SkillCategory.STRENGTH, false, STRENGTH, "strength-haste", 2),

    RESISTANCE(SkillCategory.RESISTANCE, true, null, "resistance", 10),
    FIRE_RESISTANCE(SkillCategory.RESISTANCE, false, RESISTANCE, "resistance-fire", 5),
    PROJECTILE_RESISTANCE(SkillCategory.RESISTANCE, false, RESISTANCE, "resistance-projectile", 5),
    KNOCKBACK_RESISTANCE(SkillCategory.RESISTANCE, false, RESISTANCE, "resistance-knockback", 5),

    AGILITY(SkillCategory.AGILITY, true, null, "agility", 10),
    LEAPING(SkillCategory.AGILITY, false, AGILITY, "agility-jump", 5),

    VITALITY(SkillCategory.VITALITY, true, null, "vitaility", 10),
    REGENERATION(SkillCategory.VITALITY, false, VITALITY, "vitaility-regen", 5),
    HEALTH_BOOST(SkillCategory.VITALITY, false, VITALITY, "vitaility-health_boost", 5),
    CLEANSE(SkillCategory.VITALITY, false, VITALITY, "vitaility-cleanse", 5),

    LUCK(SkillCategory.LUCK, true, null, "luck", 10),
    LOOTING(SkillCategory.LUCK, false, LUCK, "luck-looting", 2),
    FORTUNE(SkillCategory.LUCK, false, LUCK, "luck-fortune", 2);

    private final SkillCategory category;
    private final boolean primary;
    private final SkillId parent;
    private final String translationPath;
    private final ResourceLocation icon;
    private final int defaultMaxLevel;

    SkillId(SkillCategory category, boolean primary, SkillId parent, String iconPath, int maxLevel) {
        this.category = category;
        this.primary = primary;
        this.parent = parent;
        this.translationPath = iconPath;
        this.icon = ResourceLocation.fromNamespaceAndPath(CodexMod.MOD_ID, "textures/gui/skills/" + iconPath + ".png");
        this.defaultMaxLevel = maxLevel;
    }

    public SkillCategory category() { return category; }
    public boolean primary() { return primary; }
    public boolean secondary() { return !primary; }
    public SkillId parent() { return parent; }
    public String title() { return Component.translatable("skill.aura." + translationPath + ".name").getString(); }
    public ResourceLocation icon() { return icon; }
    public String description() { return Component.translatable("skill.aura." + translationPath + ".description").getString(); }
    public int maxLevel() { return SkillConfig.maxLevel(this); }
    public int defaultMaxLevel() { return defaultMaxLevel; }

    public static SkillId byOrdinal(int ord) {
        SkillId[] v = values();
        if (ord < 0 || ord >= v.length) return null;
        return v[ord];
    }
}

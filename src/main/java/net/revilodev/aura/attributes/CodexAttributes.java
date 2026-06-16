package net.revilodev.aura.attributes;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.EntityAttributeModificationEvent;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.revilodev.aura.CodexMod;
import net.revilodev.aura.abilities.AbilityId;
import net.revilodev.aura.abilities.event.AbilityPowerCalculationEvent;
import net.revilodev.aura.skills.SkillId;

import java.util.EnumMap;
import java.util.Map;

public final class CodexAttributes {
    public static final DeferredRegister<Attribute> REGISTER = DeferredRegister.create(Registries.ATTRIBUTE, CodexMod.MOD_ID);
    public static final Holder<Attribute> ABILITY_POWER = REGISTER.register("ability_power",
            () -> new RangedAttribute("attribute.name.aura.ability_power", 1.0D, 0.0D, 1024.0D).setSyncable(true));
    public static final Holder<Attribute> ABILITY_SKILL_EDIT_LOCK = REGISTER.register("ability_skill_edit_lock",
            () -> new RangedAttribute("attribute.name.aura.ability_skill_edit_lock", 0.0D, 0.0D, 1.0D).setSyncable(true));
    private static final EnumMap<SkillId, Holder<Attribute>> SKILL_BONUSES = new EnumMap<>(SkillId.class);
    private static final EnumMap<AbilityId, Holder<Attribute>> ABILITY_BONUSES = new EnumMap<>(AbilityId.class);

    static {
        for (SkillId id : SkillId.values()) {
            SKILL_BONUSES.put(id, REGISTER.register("skill_" + id.attributePath() + "_bonus",
                    () -> bonusAttribute("attribute.name.aura.skill_" + id.attributePath() + "_bonus")));
        }
        for (AbilityId id : AbilityId.values()) {
            ABILITY_BONUSES.put(id, REGISTER.register("ability_" + id.attributePath() + "_bonus",
                    () -> bonusAttribute("attribute.name.aura.ability_" + id.attributePath() + "_bonus")));
        }
    }

    private CodexAttributes() {}

    public static void register(IEventBus modBus) {
        REGISTER.register(modBus);
        modBus.addListener(CodexAttributes::onEntityAttributeModification);
    }

    public static double abilityPower(LivingEntity entity) {
        return abilityPower(entity, null);
    }

    public static double abilityPower(LivingEntity entity, AbilityId abilityId) {
        double value = baseAbilityPower(entity);
        if (entity instanceof net.minecraft.server.level.ServerPlayer player) {
            AbilityPowerCalculationEvent event = new AbilityPowerCalculationEvent(player, abilityId, value);
            NeoForge.EVENT_BUS.post(event);
            value = event.getAbilityPower();
        }
        return Math.max(0.0D, value);
    }

    public static double baseAbilityPower(LivingEntity entity) {
        if (entity == null) return 1.0D;
        AttributeInstance instance = entity.getAttribute(ABILITY_POWER);
        return instance == null ? 1.0D : instance.getValue();
    }

    public static int skillBonus(LivingEntity entity, SkillId id) {
        return levelBonus(entity, SKILL_BONUSES.get(id));
    }

    public static int abilityBonus(LivingEntity entity, AbilityId id) {
        return levelBonus(entity, ABILITY_BONUSES.get(id));
    }

    public static Holder<Attribute> skillBonusAttribute(SkillId id) {
        return SKILL_BONUSES.get(id);
    }

    public static Holder<Attribute> abilityBonusAttribute(AbilityId id) {
        return ABILITY_BONUSES.get(id);
    }

    public static Map<SkillId, Holder<Attribute>> skillBonusAttributes() {
        return Map.copyOf(SKILL_BONUSES);
    }

    public static Map<AbilityId, Holder<Attribute>> abilityBonusAttributes() {
        return Map.copyOf(ABILITY_BONUSES);
    }

    public static boolean isAbilitySkillEditLocked(LivingEntity entity) {
        if (entity == null) return false;
        AttributeInstance instance = entity.getAttribute(ABILITY_SKILL_EDIT_LOCK);
        return instance != null && instance.getValue() > 0.0D;
    }

    private static void onEntityAttributeModification(EntityAttributeModificationEvent event) {
        event.add(EntityType.PLAYER, ABILITY_POWER, 1.0D);
        event.add(EntityType.PLAYER, ABILITY_SKILL_EDIT_LOCK, 0.0D);
        for (Holder<Attribute> attribute : SKILL_BONUSES.values()) event.add(EntityType.PLAYER, attribute, 0.0D);
        for (Holder<Attribute> attribute : ABILITY_BONUSES.values()) event.add(EntityType.PLAYER, attribute, 0.0D);
    }

    private static Attribute bonusAttribute(String descriptionId) {
        return new RangedAttribute(descriptionId, 0.0D, -1024.0D, 1024.0D).setSyncable(true);
    }

    private static int levelBonus(LivingEntity entity, Holder<Attribute> attribute) {
        if (entity == null || attribute == null) return 0;
        AttributeInstance instance = entity.getAttribute(attribute);
        if (instance == null) return 0;
        return (int) Math.floor(instance.getValue());
    }
}

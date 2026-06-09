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

public final class CodexAttributes {
    public static final DeferredRegister<Attribute> REGISTER = DeferredRegister.create(Registries.ATTRIBUTE, CodexMod.MOD_ID);
    public static final Holder<Attribute> ABILITY_POWER = REGISTER.register("ability_power",
            () -> new RangedAttribute("attribute.name.aura.ability_power", 1.0D, 0.0D, 1024.0D).setSyncable(true));
    public static final Holder<Attribute> ABILITY_SKILL_EDIT_LOCK = REGISTER.register("ability_skill_edit_lock",
            () -> new RangedAttribute("attribute.name.aura.ability_skill_edit_lock", 0.0D, 0.0D, 1.0D).setSyncable(true));

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

    public static boolean isAbilitySkillEditLocked(LivingEntity entity) {
        if (entity == null) return false;
        AttributeInstance instance = entity.getAttribute(ABILITY_SKILL_EDIT_LOCK);
        return instance != null && instance.getValue() > 0.0D;
    }

    private static void onEntityAttributeModification(EntityAttributeModificationEvent event) {
        event.add(EntityType.PLAYER, ABILITY_POWER, 1.0D);
        event.add(EntityType.PLAYER, ABILITY_SKILL_EDIT_LOCK, 0.0D);
    }
}

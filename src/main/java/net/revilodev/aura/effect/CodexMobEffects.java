package net.revilodev.aura.effect;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.revilodev.aura.CodexMod;
import net.revilodev.aura.attributes.CodexAttributes;

public final class CodexMobEffects {
    //  ability player status effects
    public static final DeferredRegister<MobEffect> REGISTER = DeferredRegister.create(Registries.MOB_EFFECT, CodexMod.MOD_ID);
    public static final Holder<MobEffect> ABILITY_POWER_BOOST = REGISTER.register("ability_power_boost", () ->
            new AbilityPowerBoostEffect()
                    .addAttributeModifier(CodexAttributes.ABILITY_POWER,
                            ResourceLocation.fromNamespaceAndPath(CodexMod.MOD_ID, "ability_power_boost"),
                            AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL,
                            amplifier -> switch (amplifier) {
                                case 0 -> 0.25D;
                                case 1 -> 0.50D;
                                default -> 1.0D;
                            }));
    // raging effect functionality: vanilla attack damage and speed effects
    public static final Holder<MobEffect> RAMPAGING = REGISTER.register("rampaging", () ->
            new RampagingEffect()
                    .addAttributeModifier(Attributes.ATTACK_DAMAGE,
                            ResourceLocation.fromNamespaceAndPath(CodexMod.MOD_ID, "rampaging_attack_damage"),
                            AttributeModifier.Operation.ADD_VALUE,
                            amplifier -> {
                                int coreRank = amplifier + 1;
                                int strengthAmp = Math.min(4, coreRank / 2);
                                return 3.0D * (strengthAmp + 1);
                            })
                    .addAttributeModifier(Attributes.MOVEMENT_SPEED,
                            ResourceLocation.fromNamespaceAndPath(CodexMod.MOD_ID, "rampaging_move_speed"),
                            AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL,
                            amplifier -> {
                                int coreRank = amplifier + 1;
                                int speedAmp = Math.min(2, coreRank / 3);
                                return 0.2D * (speedAmp + 1);
                            }));

    private CodexMobEffects() {}

    // registers mob effects
    public static void register(IEventBus modBus) {
        REGISTER.register(modBus);
    }
}

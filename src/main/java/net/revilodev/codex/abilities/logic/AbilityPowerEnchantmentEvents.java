package net.revilodev.codex.abilities.logic;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.neoforged.neoforge.common.NeoForge;
import net.revilodev.codex.CodexMod;
import net.revilodev.codex.abilities.AbilityConfig;
import net.revilodev.codex.abilities.event.AbilityPowerCalculationEvent;

public final class AbilityPowerEnchantmentEvents {
    private static final ResourceKey<Enchantment> ABILITY_POWER_ENCHANT =
            ResourceKey.create(Registries.ENCHANTMENT, ResourceLocation.fromNamespaceAndPath(CodexMod.MOD_ID, "ability_power"));
    private static boolean REGISTERED = false;

    private AbilityPowerEnchantmentEvents() {}

    public static void register() {
        if (REGISTERED) return;
        REGISTERED = true;
        NeoForge.EVENT_BUS.addListener(AbilityPowerEnchantmentEvents::onAbilityPower);
    }

    private static void onAbilityPower(AbilityPowerCalculationEvent event) {
        Holder<Enchantment> holder = event.getPlayer().level().registryAccess().registryOrThrow(Registries.ENCHANTMENT).getHolder(ABILITY_POWER_ENCHANT).orElse(null);
        if (holder == null) return;

        int level = 0;
        for (ItemStack stack : event.getPlayer().getArmorSlots()) {
            level = Math.max(level, EnchantmentHelper.getItemEnchantmentLevel(holder, stack));
        }
        level = Math.max(level, EnchantmentHelper.getItemEnchantmentLevel(holder, event.getPlayer().getMainHandItem()));
        level = Math.max(level, EnchantmentHelper.getItemEnchantmentLevel(holder, event.getPlayer().getOffhandItem()));
        if (level <= 0) return;

        double scale = AbilityConfig.abilityPowerEnchantScale(level);
        event.setAbilityPower(event.getAbilityPower() * (1.0D + scale));
    }
}

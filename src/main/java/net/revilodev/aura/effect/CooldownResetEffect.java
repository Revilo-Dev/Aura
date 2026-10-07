package net.revilodev.aura.effect;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.InstantenousMobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.revilodev.aura.abilities.AbilitiesAttachments;
import net.revilodev.aura.abilities.logic.AbilitySyncEvents;

import javax.annotation.Nullable;

public final class CooldownResetEffect extends InstantenousMobEffect {
    public CooldownResetEffect() {
        super(MobEffectCategory.BENEFICIAL, 0xC4A5FF);
    }

    @Override
    public boolean applyEffectTick(LivingEntity entity, int amplifier) {
        resetCooldowns(entity);
        return true;
    }

    @Override
    public void applyInstantenousEffect(@Nullable Entity source, @Nullable Entity indirectSource, LivingEntity entity, int amplifier, double health) {
        resetCooldowns(entity);
    }

    private static void resetCooldowns(LivingEntity entity) {
        if (!(entity instanceof ServerPlayer player)) return;
        if (player.getData(AbilitiesAttachments.PLAYER_ABILITIES.get()).resetCooldowns()) {
            AbilitySyncEvents.markDirty(player);
        }
    }
}

package net.revilodev.aura.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

public final class PermafrostEffect extends MobEffect {
    public PermafrostEffect() {
        super(MobEffectCategory.HARMFUL, 0x8FE8FF);
    }

    @Override
    public boolean applyEffectTick(LivingEntity entity, int amplifier) {
        if (entity.isOnFire()) {
            entity.removeEffect(CodexMobEffects.PERMAFROST);
            return true;
        }
        MobEffectInstance effect = entity.getEffect(CodexMobEffects.PERMAFROST);
        if (effect != null && effect.getDuration() <= 60) {
            Vec3 movement = entity.getDeltaMovement();
            entity.setDeltaMovement(0.0D, Math.min(0.0D, movement.y), 0.0D);
            entity.hurtMarked = true;
        }
        return true;
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        return true;
    }
}

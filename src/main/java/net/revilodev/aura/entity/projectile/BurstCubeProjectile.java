package net.revilodev.aura.entity.projectile;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.revilodev.aura.abilities.AbilityElement;
import net.revilodev.aura.abilities.logic.AbilityLogic;
import net.revilodev.aura.entity.ModEntities;

// burst cube projectile
public final class BurstCubeProjectile extends ThrowableProjectile {
    private static final EntityDataAccessor<Integer> ELEMENT = SynchedEntityData.defineId(BurstCubeProjectile.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Float> DAMAGE = SynchedEntityData.defineId(BurstCubeProjectile.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Integer> DURATION = SynchedEntityData.defineId(BurstCubeProjectile.class, EntityDataSerializers.INT);

    public BurstCubeProjectile(EntityType<? extends BurstCubeProjectile> type, Level level) {
        super(type, level);
    }

    public BurstCubeProjectile(Level level, LivingEntity owner, AbilityElement element, float damage, int duration) {
        this(ModEntities.BURST_CUBE.get(), level);
        setOwner(owner);
        setPos(owner.getX(), owner.getEyeY() - 0.1D, owner.getZ());
        setElement(element);
        setDamage(damage);
        setDuration(duration);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(ELEMENT, AbilityElement.FIRE.ordinal());
        builder.define(DAMAGE, 1.0F);
        builder.define(DURATION, 40);
    }

    @Override
    public void tick() {
        super.tick();
        if (tickCount > 30) {
            discard();
            return;
        }
        if (level() instanceof ServerLevel level) {
            var particle = switch (element()) {
                case ICE -> net.minecraft.core.particles.ParticleTypes.SNOWFLAKE;
                case POISON -> net.minecraft.core.particles.ParticleTypes.WITCH;
                default -> net.minecraft.core.particles.ParticleTypes.FLAME;
            };
            level.sendParticles(particle, getX(), getY(), getZ(), 2, 0.03D, 0.03D, 0.03D, 0.001D);
        }
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);
        if (!(getOwner() instanceof ServerPlayer player)) return;
        if (result.getEntity() instanceof LivingEntity living) {
            AbilityLogic.applyElementHit(player, element(), living, damage(), duration());
        }
        discard();
    }

    @Override
    protected void onHit(HitResult result) {
        super.onHit(result);
        if (result.getType() != HitResult.Type.ENTITY) {
            discard();
        }
    }

    @Override
    protected double getDefaultGravity() {
        return 0.0D;
    }

    public AbilityElement element() {
        int ord = entityData.get(ELEMENT);
        AbilityElement[] values = AbilityElement.values();
        if (ord < 0 || ord >= values.length) return AbilityElement.FIRE;
        return values[ord];
    }

    public void setElement(AbilityElement element) {
        entityData.set(ELEMENT, element == null ? AbilityElement.FIRE.ordinal() : element.ordinal());
    }

    public float damage() {
        return entityData.get(DAMAGE);
    }

    public void setDamage(float damage) {
        entityData.set(DAMAGE, Math.max(0.0F, damage));
    }

    public int duration() {
        return entityData.get(DURATION);
    }

    public void setDuration(int duration) {
        entityData.set(DURATION, Math.max(1, duration));
    }
}

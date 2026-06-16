package net.revilodev.aura.abilities.logic;

import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.NeoForge;
import net.revilodev.aura.abilities.AbilitiesAttachments;
import net.revilodev.aura.abilities.AbilityConfig;
import net.revilodev.aura.abilities.AbilityElement;
import net.revilodev.aura.abilities.AbilityId;
import net.revilodev.aura.abilities.AbilitySpecialization;
import net.revilodev.aura.abilities.PlayerAbilities;
import net.revilodev.aura.abilities.event.AbilityUseEvent;
import net.revilodev.aura.attributes.CodexAttributes;
import net.revilodev.aura.effect.CodexMobEffects;
import net.revilodev.aura.skills.PlayerSkills;
import net.revilodev.aura.skills.SkillsAttachments;
import net.revilodev.aura.stats.CodexStats;
import net.revilodev.aura.entity.projectile.BurstCubeProjectile;

import java.util.Comparator;
import java.util.List;

public final class AbilityLogic {
    private AbilityLogic() {}

    public static boolean tryActivate(ServerPlayer player, AbilityId id) {
        if (player == null || id == null || !id.isSpecialization()) return false;
        PlayerAbilities abilities = player.getData(AbilitiesAttachments.PLAYER_ABILITIES.get());
        int effectiveRank = effectiveRank(player, abilities, id);
        int effectiveCoreRank = effectiveCoreRank(player, abilities, id);
        if (!AbilityConfig.enabled(id) || effectiveCoreRank <= 0 || abilities.cooldownTicks(id) > 0) return false;

        PlayerSkills skills = player.getData(SkillsAttachments.PLAYER_SKILLS.get());
        double abilityPower = CodexAttributes.abilityPower(player, id);
        AbilityUseEvent.Pre preEvent = new AbilityUseEvent.Pre(player, id, effectiveRank, skills, abilityPower);
        if (NeoForge.EVENT_BUS.post(preEvent).isCanceled()) return false;

        int coreRank = Math.max(1, effectiveCoreRank);
        if (!execute(player, id, coreRank, preEvent.getAbilityPower())) return false;
        abilities.setCooldown(id, AbilityScaling.cooldownTicks(id, coreRank, skills));
        abilities.markUsed(id);
        player.awardStat(CodexStats.ABILITIES_USED);
        player.awardStat(CodexStats.abilityUse(id));
        NeoForge.EVENT_BUS.post(new AbilityUseEvent.Post(player, id, effectiveRank, skills, preEvent.getAbilityPower()));
        return true;
    }

    public static int effectiveRank(ServerPlayer player, PlayerAbilities abilities, AbilityId id) {
        if (abilities == null || id == null) return 0;
        return Math.max(0, abilities.rank(id) + CodexAttributes.abilityBonus(player, id));
    }

    public static int effectiveCoreRank(ServerPlayer player, PlayerAbilities abilities, AbilityId id) {
        if (abilities == null || id == null) return 0;
        AbilityId core = id.core();
        int coreRank = effectiveRank(player, abilities, core);
        if (id.isSpecialization() && effectiveRank(player, abilities, id) > 0) {
            coreRank = Math.max(1, coreRank);
        }
        return coreRank;
    }

    private static boolean execute(ServerPlayer player, AbilityId id, int coreRank, double abilityPower) {
        if (id == AbilityId.MAGIC_HEAL) return magicHeal(player, id, coreRank, abilityPower);
        if (id == AbilityId.MAGIC_CLEANSE) return magicCleanse(player, id, coreRank, abilityPower);
        if (id == AbilityId.WIND_DASH) return windDash(player, id, coreRank, abilityPower);
        if (id == AbilityId.WIND_LEAP) return windLeap(player, id, coreRank, abilityPower);
        if (id == AbilityId.WIND_LUNGE) return windLunge(player, id, coreRank, abilityPower);
        return switch (id.specialization()) {
            case BURST -> burst(player, id, coreRank, abilityPower);
            case NOVA -> nova(player, id, coreRank, abilityPower);
            case IMPLODE -> implode(player, id, coreRank, abilityPower);
            case STORM -> storm(player, id, coreRank, abilityPower);
            case PIERCE -> pierce(player, id, coreRank, abilityPower);
            case GLACIER -> glacier(player, id, coreRank, abilityPower);
            case STRIKE -> strike(player, id, coreRank, abilityPower);
            case ZAP -> zap(player, id, coreRank, abilityPower);
            case AEGIS -> aegis(player, id, coreRank, abilityPower);
            case RAMPAGE -> rampage(player, id, coreRank, abilityPower);
            case BASH -> bash(player, id, coreRank, abilityPower);
            default -> false;
        };
    }

    private static boolean magicHeal(ServerPlayer player, AbilityId id, int coreRank, double abilityPower) {
        player.heal(Math.max(1.0F, AbilityScaling.damage(id, coreRank, abilityPower) * 0.6F));
        fx(player, ParticleTypes.HEART, SoundEvents.AMETHYST_BLOCK_CHIME);
        return true;
    }

    private static boolean magicCleanse(ServerPlayer player, AbilityId id, int coreRank, double abilityPower) {
        for (MobEffectInstance effect : List.copyOf(player.getActiveEffects())) {
            if (!effect.getEffect().value().isBeneficial()) player.removeEffect(effect.getEffect());
        }
        if (player.isOnFire()) player.clearFire();
        player.heal(Math.max(1.0F, AbilityScaling.damage(id, coreRank, abilityPower) * 0.35F));
        fx(player, ParticleTypes.WAX_OFF, SoundEvents.GENERIC_DRINK);
        return true;
    }

    private static boolean windDash(ServerPlayer player, AbilityId id, int coreRank, double abilityPower) {
        Vec3 look = player.getLookAngle();
        double ap = powerScale(abilityPower);
        Vec3 horiz = new Vec3(look.x, 0.0D, look.z).normalize().scale((1.0D + coreRank * 0.2D) * ap);
        player.push(horiz.x, 0.09D, horiz.z);
        player.hurtMarked = true;
        fx(player, ParticleTypes.CLOUD, SoundEvents.BREEZE_WIND_CHARGE_BURST.value());
        return true;
    }

    private static boolean windLeap(ServerPlayer player, AbilityId id, int coreRank, double abilityPower) {
        Vec3 look = player.getLookAngle();
        double ap = powerScale(abilityPower);
        Vec3 horiz = new Vec3(look.x, 0.0D, look.z).normalize().scale((0.75D + coreRank * 0.15D) * ap);
        player.setDeltaMovement(horiz.x, (0.55D + coreRank * 0.05D) * ap, horiz.z);
        player.hurtMarked = true;
        fx(player, ParticleTypes.POOF, SoundEvents.GOAT_LONG_JUMP);
        return true;
    }

    private static boolean windLunge(ServerPlayer player, AbilityId id, int coreRank, double abilityPower) {
        LivingEntity target = nearby(player, AbilityScaling.radius(id, coreRank, abilityPower) + 4.0D).stream().filter(e -> inFront(player, e)).findFirst().orElse(null);
        if (target == null) return false;
        double ap = powerScale(abilityPower);
        Vec3 toward = target.position().subtract(player.position()).normalize();
        player.setDeltaMovement(toward.x * 1.12D * ap, 0.14D * ap, toward.z * 1.12D * ap);
        player.hurtMarked = true;
        target.hurt(player.damageSources().playerAttack(player), AbilityScaling.damage(id, coreRank, abilityPower) * 1.25F);
        fx(player, ParticleTypes.SWEEP_ATTACK, SoundEvents.PLAYER_ATTACK_KNOCKBACK);
        return true;
    }

    private static boolean burst(ServerPlayer player, AbilityId id, int coreRank, double abilityPower) {
        if (id == AbilityId.FORCE_BURST) {
            double radius = AbilityScaling.radius(id, coreRank, abilityPower) + 1.5D;
            List<LivingEntity> targets = nearby(player, radius);
            if (targets.isEmpty()) return false;
            float damage = AbilityScaling.damage(id, coreRank, abilityPower) * 1.25F;
            double ap = powerScale(abilityPower);
            for (LivingEntity target : targets) {
                target.hurt(player.damageSources().magic(), damage);
                pushAwayFrom(player, target, (1.05D + coreRank * 0.06D) * ap);
            }
            fx(player, ParticleTypes.EXPLOSION, SoundEvents.GENERIC_EXPLODE.value());
            return true;
        }

        int projectiles = 1 + Math.max(0, coreRank * 2);
        double stepDeg = 15.0D;
        double half = (projectiles - 1) * 0.5D;
        Vec3 look = player.getLookAngle().normalize();
        float damage = AbilityScaling.damage(id, coreRank, abilityPower);
        int duration = AbilityScaling.durationTicks(id, coreRank, abilityPower);
        for (int i = 0; i < projectiles; i++) {
            double angle = (i - half) * stepDeg;
            Vec3 dir = rotateYaw(look, angle).normalize();
            BurstCubeProjectile projectile = new BurstCubeProjectile(player.level(), player, id.element(), damage, duration);
            projectile.shoot(dir.x, dir.y, dir.z, 1.32F, 0.0F);
            player.level().addFreshEntity(projectile);
        }
        fx(player, ParticleTypes.SMOKE, SoundEvents.BLAZE_SHOOT);
        return true;
    }

    private static boolean nova(ServerPlayer player, AbilityId id, int coreRank, double abilityPower) {
        double radius = AbilityScaling.radius(id, coreRank, abilityPower) + 1.5D;
        List<LivingEntity> targets = nearby(player, radius);
        if (targets.isEmpty()) return false;
        float damage = AbilityScaling.damage(id, coreRank, abilityPower) * 1.15F;
        int duration = AbilityScaling.durationTicks(id, coreRank, abilityPower) + 40;
        for (LivingEntity target : targets) {
            applyElementHit(player, id.element(), target, damage, duration);
            if (id == AbilityId.LIGHTNING_NOVA && player.level() instanceof ServerLevel level) {
                Vec3 from = player.position().add(0.0D, 1.1D, 0.0D);
                Vec3 to = target.position().add(0.0D, 0.6D, 0.0D);
                spawnLightningBoltParticles(level, from, to);
                level.sendParticles(ParticleTypes.ELECTRIC_SPARK, to.x, to.y + 0.4D, to.z, 16, 0.25D, 0.35D, 0.25D, 0.015D);
            }
        }
        spawnNovaRing(player, radius, particleForElement(id.element()));
        if (id == AbilityId.LIGHTNING_NOVA && player.level() instanceof ServerLevel level) {
            level.sendParticles(ParticleTypes.ELECTRIC_SPARK, player.getX(), player.getY() + 1.1D, player.getZ(), 34, 0.9D, 0.35D, 0.9D, 0.02D);
        }
        fx(player, particleForElement(id.element()), SoundEvents.BEACON_ACTIVATE);
        return true;
    }

    private static boolean implode(ServerPlayer player, AbilityId id, int coreRank, double abilityPower) {
        List<LivingEntity> targets = nearby(player, AbilityScaling.radius(id, coreRank, abilityPower) + 1.0D);
        if (targets.isEmpty()) return false;
        float damageScale = id.element() == AbilityElement.FORCE ? 0.75F : 1.0F;
        double pullStrength = id.element() == AbilityElement.FORCE ? (0.3D + coreRank * 0.03D) : (0.5D + coreRank * 0.05D);
        pullStrength *= powerScale(abilityPower);
        for (LivingEntity target : targets) {
            pullToward(player, target, pullStrength);
            applyElementHit(player, id.element(), target, AbilityScaling.damage(id, coreRank, abilityPower) * damageScale, AbilityScaling.durationTicks(id, coreRank, abilityPower));
            if (id == AbilityId.LIGHTNING_IMPLODE && player.level() instanceof ServerLevel level) strikeLightning(level, target.getX(), target.getY(), target.getZ());
        }
        fx(player, ParticleTypes.EXPLOSION, SoundEvents.GENERIC_EXPLODE.value());
        return true;
    }

    private static boolean storm(ServerPlayer player, AbilityId id, int coreRank, double abilityPower) {
        int duration = Math.max(60, AbilityScaling.durationTicks(id, coreRank, abilityPower));
        player.getData(AbilitiesAttachments.PLAYER_ABILITIES.get()).setActiveTicks(id, duration);
        fx(player, particleForElement(id.element()), SoundEvents.LIGHTNING_BOLT_THUNDER);
        return true;
    }

    private static boolean pierce(ServerPlayer player, AbilityId id, int coreRank, double abilityPower) {
        int projectiles = 1 + Math.max(0, coreRank);
        int pierceCount = Math.max(1, coreRank);
        double hitRadius = 0.35D + (coreRank * 0.08D);
        Vec3 eye = player.getEyePosition();
        Vec3 look = player.getLookAngle().normalize();
        double range = AbilityScaling.radius(id, coreRank, abilityPower) + 10.0D;
        float damage = AbilityScaling.damage(id, coreRank, abilityPower);
        int duration = AbilityScaling.durationTicks(id, coreRank, abilityPower);
        boolean hitAny = false;
        for (int i = 0; i < projectiles; i++) {
            List<LivingEntity> hits = hitsOnRay(player, eye, look, range, hitRadius, pierceCount);
            for (LivingEntity hit : hits) {
                applyElementHit(player, id.element(), hit, damage, duration);
                hitAny = true;
            }
            spawnRayParticles(player, eye, eye.add(look.scale(range)), ParticleTypes.SNOWFLAKE);
        }
        fx(player, ParticleTypes.SWEEP_ATTACK, SoundEvents.PLAYER_ATTACK_SWEEP);
        return hitAny;
    }

    private static boolean glacier(ServerPlayer player, AbilityId id, int coreRank, double abilityPower) {
        List<LivingEntity> targets = nearby(player, AbilityScaling.radius(id, coreRank, abilityPower) + 2.0D);
        if (targets.isEmpty()) return false;
        int duration = AbilityScaling.durationTicks(id, coreRank, abilityPower);
        for (LivingEntity target : targets) {
            target.hurt(player.damageSources().magic(), AbilityScaling.damage(id, coreRank, abilityPower));
            target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, duration, 3, false, true, true));
            if (player.level() instanceof ServerLevel level) {
                level.sendParticles(ParticleTypes.ITEM_SNOWBALL, target.getX(), target.getY(), target.getZ(), 6, 0.15D, 0.7D, 0.15D, 0.01D);
            }
        }
        fx(player, ParticleTypes.SNOWFLAKE, SoundEvents.GLASS_BREAK);
        return true;
    }

    private static boolean strike(ServerPlayer player, AbilityId id, int coreRank, double abilityPower) {
        LivingEntity target = nearby(player, AbilityScaling.radius(id, coreRank, abilityPower) + 2.0D).stream().filter(e -> inFront(player, e)).findFirst().orElse(null);
        if (target == null) return false;
        applyElementHit(player, id.element(), target, AbilityScaling.damage(id, coreRank, abilityPower) * 1.2F, AbilityScaling.durationTicks(id, coreRank, abilityPower));
        if (id.element() == AbilityElement.LIGHTNING && player.level() instanceof ServerLevel level) {
            strikeLightningNoFire(level, target.getX(), target.getY(), target.getZ());
            level.sendParticles(ParticleTypes.ELECTRIC_SPARK, target.getX(), target.getY() + 1.0D, target.getZ(), 24, 0.22D, 0.55D, 0.22D, 0.02D);
        }
        fx(player, ParticleTypes.CRIT, SoundEvents.PLAYER_ATTACK_CRIT);
        return true;
    }

    private static boolean zap(ServerPlayer player, AbilityId id, int coreRank, double abilityPower) {
        int count = 2 + (Math.max(0, coreRank) * 2);
        List<LivingEntity> targets = nearby(player, AbilityScaling.radius(id, coreRank, abilityPower) + 3.0D).stream().sorted(Comparator.comparingDouble(e -> e.distanceToSqr(player))).limit(count).toList();
        if (targets.isEmpty()) return false;
        float damage = AbilityScaling.damage(id, coreRank, abilityPower) * 0.5F;
        int duration = AbilityScaling.durationTicks(id, coreRank, abilityPower);
        if (player.level() instanceof ServerLevel level) {
            level.playSound(null, player.blockPosition(), SoundEvents.LIGHTNING_BOLT_THUNDER, SoundSource.PLAYERS, 0.45F, 1.35F);
        }
        for (int i = 0; i < targets.size(); i++) {
            LivingEntity target = targets.get(i);
            applyElementHit(player, AbilityElement.LIGHTNING, target, damage, duration);
            if (player.level() instanceof ServerLevel level) {
                spawnSmallLightningParticles(level, target.position().add(0.0D, 1.8D, 0.0D), target.position().add(0.0D, 0.3D, 0.0D));
            }
        }
        fx(player, ParticleTypes.ELECTRIC_SPARK, SoundEvents.LIGHTNING_BOLT_IMPACT);
        return true;
    }

    private static boolean aegis(ServerPlayer player, AbilityId id, int coreRank, double abilityPower) {
        int charges = Math.max(1, (int) Math.round(coreRank * powerScale(abilityPower)));
        player.getData(AbilitiesAttachments.PLAYER_ABILITIES.get()).setActiveTicks(id, charges);
        fx(player, ParticleTypes.TOTEM_OF_UNDYING, SoundEvents.SHIELD_BLOCK);
        return true;
    }

    private static boolean rampage(ServerPlayer player, AbilityId id, int coreRank, double abilityPower) {
        int duration = AbilityScaling.durationTicks(id, coreRank, abilityPower);
        player.addEffect(new MobEffectInstance(CodexMobEffects.RAMPAGING, duration, Math.max(0, coreRank - 1), false, true, true));
        fx(player, ParticleTypes.ANGRY_VILLAGER, SoundEvents.RAID_HORN.value());
        return true;
    }

    private static boolean bash(ServerPlayer player, AbilityId id, int coreRank, double abilityPower) {
        LivingEntity target = nearby(player, AbilityScaling.radius(id, coreRank, abilityPower) + 1.5D).stream().filter(e -> inFront(player, e)).findFirst().orElse(null);
        if (target == null) return false;
        target.hurt(player.damageSources().playerAttack(player), AbilityScaling.damage(id, coreRank, abilityPower) * 1.4F);
        target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 30 + coreRank * 8, 4, false, true, true));
        pullToward(target, player, 0.8D + coreRank * 0.06D);
        fx(player, ParticleTypes.SWEEP_ATTACK, SoundEvents.MACE_SMASH_GROUND_HEAVY);
        return true;
    }

    private static List<LivingEntity> nearby(ServerPlayer player, double radius) {
        return player.level().getEntitiesOfClass(LivingEntity.class, player.getBoundingBox().inflate(radius), e -> e != player && e.isAlive());
    }

    private static boolean inFront(ServerPlayer player, LivingEntity target) {
        Vec3 facing = player.getLookAngle().normalize();
        Vec3 to = target.position().subtract(player.position()).normalize();
        return facing.dot(to) > 0.1D;
    }

    public static void applyElementHit(ServerPlayer player, AbilityElement element, LivingEntity target, float damage, int duration) {
        target.hurt(player.damageSources().magic(), damage);
        switch (element) {
            case FIRE -> target.igniteForSeconds(Math.max(1, duration / 20));
            case ICE -> target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, duration, 2, false, true, true));
            case LIGHTNING -> {}
            case POISON -> target.addEffect(new MobEffectInstance(MobEffects.POISON, duration, 1, false, true, true));
            case FORCE -> pullToward(target, player, 0.6D);
            case MAGIC -> target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, Math.max(20, duration / 2), 0, false, true, true));
            case WIND -> pullToward(target, player, 0.65D);
        }
    }

    private static void pullToward(LivingEntity anchor, LivingEntity target, double strength) {
        Vec3 dir = anchor.position().subtract(target.position());
        Vec3 horiz = new Vec3(dir.x, 0.0D, dir.z);
        if (horiz.lengthSqr() < 1.0E-5D) return;
        Vec3 impulse = horiz.normalize().scale(strength);
        target.push(impulse.x, 0.08D, impulse.z);
        target.hurtMarked = true;
    }

    private static void pushAwayFrom(LivingEntity anchor, LivingEntity target, double strength) {
        Vec3 dir = target.position().subtract(anchor.position());
        Vec3 horiz = new Vec3(dir.x, 0.0D, dir.z);
        if (horiz.lengthSqr() < 1.0E-5D) return;
        Vec3 impulse = horiz.normalize().scale(strength);
        target.push(impulse.x, 0.12D, impulse.z);
        target.hurtMarked = true;
    }

    private static void fx(ServerPlayer player, ParticleOptions particle, SoundEvent sound) {
        if (player.level() instanceof ServerLevel level) {
            level.sendParticles(particle, player.getX(), player.getY() + 1.0D, player.getZ(), 16, 0.6D, 0.3D, 0.6D, 0.01D);
            level.playSound(null, player.blockPosition(), sound, SoundSource.PLAYERS, 0.8F, 1.0F);
        }
    }

    public static void tickActive(ServerPlayer player, PlayerAbilities abilities, PlayerSkills skills) {
        for (AbilityId id : AbilityId.values()) {
            int active = abilities.activeTicks(id);
            if (active <= 0) continue;
            if (id.specialization() != AbilitySpecialization.STORM) continue;
            abilities.setActiveTicks(id, active - 1);
            tickStorm(player, id, abilities, skills);
        }
    }

    private static void tickStorm(ServerPlayer player, AbilityId id, PlayerAbilities abilities, PlayerSkills skills) {
        if (!(player.level() instanceof ServerLevel level)) return;
        int coreRank = Math.max(1, effectiveCoreRank(player, abilities, id));
        double abilityPower = CodexAttributes.abilityPower(player, id);
        int active = abilities.activeTicks(id);
        Vec3 center = player.position().add(0.0D, 4.2D, 0.0D);
        level.sendParticles(ParticleTypes.CLOUD, center.x, center.y, center.z, 14, 0.9D, 0.18D, 0.9D, 0.01D);

        if (active % 10 != 0) return;
        double radius = AbilityScaling.radius(id, coreRank, abilityPower) + 2.5D;
        List<LivingEntity> targets = nearby(player, radius).stream().sorted(Comparator.comparingDouble(e -> e.distanceToSqr(player))).limit(6).toList();
        for (LivingEntity target : targets) {
            float tickDamage = AbilityScaling.damage(id, coreRank, abilityPower) * 0.6F;
            if (id == AbilityId.LIGHTNING_STORM) {
                // Tick interval is 10 ticks (0.5s), so 2.5 damage per tick ~= 5 DPS.
                tickDamage = 2.5F;
            }
            applyElementHit(player, id.element(), target, tickDamage, AbilityScaling.durationTicks(id, coreRank, abilityPower));
            if (id.element() == AbilityElement.LIGHTNING) {
                spawnSmallLightningParticles(level, center, target.position().add(0.0D, 0.2D, 0.0D));
                spawnLightningBoltParticles(level, center, target.position().add(0.0D, 0.2D, 0.0D));
                level.sendParticles(ParticleTypes.ELECTRIC_SPARK, target.getX(), target.getY() + 1.0D, target.getZ(), 18, 0.22D, 0.45D, 0.22D, 0.015D);
            } else if (id.element() == AbilityElement.FIRE) {
                spawnRainParticles(level, center, target.position().add(0.0D, 0.3D, 0.0D), ParticleTypes.FLAME);
            } else if (id.element() == AbilityElement.ICE) {
                spawnRainParticles(level, center, target.position().add(0.0D, 0.3D, 0.0D), ParticleTypes.SNOWFLAKE);
            }
        }
    }

    private static ParticleOptions particleForElement(AbilityElement element) {
        return switch (element) {
            case ICE -> ParticleTypes.SNOWFLAKE;
            case POISON -> ParticleTypes.WITCH;
            case LIGHTNING -> ParticleTypes.ELECTRIC_SPARK;
            case FORCE -> ParticleTypes.CRIT;
            case WIND -> ParticleTypes.CLOUD;
            default -> ParticleTypes.FLAME;
        };
    }

    private static void spawnNovaRing(ServerPlayer player, double radius, ParticleOptions particle) {
        if (!(player.level() instanceof ServerLevel level)) return;
        Vec3 center = player.position().add(0.0D, 0.2D, 0.0D);
        for (int i = 0; i < 48; i++) {
            double angle = (Math.PI * 2.0D * i) / 48.0D;
            double x = center.x + Math.cos(angle) * radius;
            double z = center.z + Math.sin(angle) * radius;
            level.sendParticles(particle, x, center.y, z, 1, 0.0D, 0.03D, 0.0D, 0.0D);
        }
    }

    private static Vec3 rotateYaw(Vec3 direction, double yawDegrees) {
        double rad = Math.toRadians(yawDegrees);
        double cos = Math.cos(rad);
        double sin = Math.sin(rad);
        return new Vec3(direction.x * cos - direction.z * sin, direction.y, direction.x * sin + direction.z * cos);
    }

    private static LivingEntity firstHitOnRay(ServerPlayer player, Vec3 origin, Vec3 direction, double range, double hitRadius) {
        List<LivingEntity> all = nearby(player, range + 2.0D);
        LivingEntity best = null;
        double bestDist = Double.MAX_VALUE;
        for (LivingEntity e : all) {
            double t = projectionT(origin, direction, e.getBoundingBox().getCenter());
            if (t < 0.0D || t > range) continue;
            Vec3 point = origin.add(direction.scale(t));
            double distance = e.getBoundingBox().distanceToSqr(point);
            if (distance > hitRadius * hitRadius) continue;
            if (t < bestDist) {
                bestDist = t;
                best = e;
            }
        }
        return best;
    }

    private static List<LivingEntity> hitsOnRay(ServerPlayer player, Vec3 origin, Vec3 direction, double range, double hitRadius, int limit) {
        return nearby(player, range + 2.0D).stream()
                .filter(e -> {
                    double t = projectionT(origin, direction, e.getBoundingBox().getCenter());
                    if (t < 0.0D || t > range) return false;
                    Vec3 point = origin.add(direction.scale(t));
                    return e.getBoundingBox().distanceToSqr(point) <= hitRadius * hitRadius;
                })
                .sorted(Comparator.comparingDouble(e -> projectionT(origin, direction, e.getBoundingBox().getCenter())))
                .limit(limit)
                .toList();
    }

    private static double projectionT(Vec3 origin, Vec3 direction, Vec3 point) {
        return point.subtract(origin).dot(direction);
    }

    private static void spawnRayParticles(ServerPlayer player, Vec3 start, Vec3 end, ParticleOptions particle) {
        if (!(player.level() instanceof ServerLevel level)) return;
        for (int i = 0; i <= 10; i++) {
            Vec3 p = start.lerp(end, i / 10.0D);
            level.sendParticles(particle, p.x, p.y, p.z, 1, 0.0D, 0.0D, 0.0D, 0.0D);
        }
    }

    private static void spawnRainParticles(ServerLevel level, Vec3 from, Vec3 to, ParticleOptions particle) {
        for (int i = 0; i <= 8; i++) {
            Vec3 p = from.lerp(to, i / 8.0D);
            level.sendParticles(particle, p.x, p.y, p.z, 1, 0.02D, 0.02D, 0.02D, 0.0D);
        }
    }

    private static void spawnSmallLightningParticles(ServerLevel level, Vec3 from, Vec3 to) {
        for (int i = 0; i <= 10; i++) {
            Vec3 p = from.lerp(to, i / 10.0D);
            level.sendParticles(ParticleTypes.ELECTRIC_SPARK, p.x, p.y, p.z, 1, 0.04D, 0.04D, 0.04D, 0.0D);
        }
    }

    private static void spawnLightningBoltParticles(ServerLevel level, Vec3 from, Vec3 to) {
        Vec3 dir = to.subtract(from);
        if (dir.lengthSqr() < 1.0E-6D) return;

        int segments = 14;
        Vec3 prev = from;
        for (int i = 1; i <= segments; i++) {
            double t = i / (double) segments;
            Vec3 base = from.lerp(to, t);
            double jitter = 0.22D * (1.0D - Math.abs(0.5D - t) * 1.5D);
            Vec3 next = base.add(
                    (level.random.nextDouble() - 0.5D) * jitter,
                    (level.random.nextDouble() - 0.5D) * jitter,
                    (level.random.nextDouble() - 0.5D) * jitter
            );
            for (int s = 0; s <= 2; s++) {
                Vec3 p = prev.lerp(next, s / 2.0D);
                level.sendParticles(ParticleTypes.ELECTRIC_SPARK, p.x, p.y, p.z, 1, 0.02D, 0.02D, 0.02D, 0.0D);
            }
            prev = next;
        }
    }

    private static void strikeLightning(ServerLevel level, double x, double y, double z) {
        LightningBolt bolt = EntityType.LIGHTNING_BOLT.create(level);
        if (bolt == null) return;
        bolt.moveTo(x, y, z);
        level.addFreshEntity(bolt);
    }

    private static void strikeLightningNoFire(ServerLevel level, double x, double y, double z) {
        LightningBolt bolt = EntityType.LIGHTNING_BOLT.create(level);
        if (bolt == null) return;
        bolt.moveTo(x, y, z);
        level.addFreshEntity(bolt);
        clearFireAtStrike(level, BlockPos.containing(x, y, z));
    }

    private static void clearFireAtStrike(ServerLevel level, BlockPos center) {
        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 2; dy++) {
                for (int dz = -1; dz <= 1; dz++) {
                    BlockPos pos = center.offset(dx, dy, dz);
                    if (level.getBlockState(pos).is(Blocks.FIRE)) {
                        level.removeBlock(pos, false);
                    }
                }
            }
        }
    }

    private static double powerScale(double abilityPower) {
        return 0.85D + (Math.max(0.0D, abilityPower) * 0.15D);
    }
}

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
import net.minecraft.world.damagesource.DamageSource;
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
import net.revilodev.aura.particle.ModParticles;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class AbilityLogic {
    private static final Map<UUID, UUID> BLOOD_DRAIN_TARGETS = new HashMap<>();
    private static final ThreadLocal<AbilityElement> DAMAGE_ELEMENT = new ThreadLocal<>();
    private static final ThreadLocal<ServerPlayer> DAMAGE_ATTACKER = new ThreadLocal<>();

    private AbilityLogic() {}

    public static boolean tryActivate(ServerPlayer player, AbilityId id) {
        if (player == null || id == null || !id.isSpecialization()) return false;
        PlayerAbilities abilities = player.getData(AbilitiesAttachments.PLAYER_ABILITIES.get());
        PlayerSkills skills = player.getData(SkillsAttachments.PLAYER_SKILLS.get());
        int effectiveRank = effectiveRank(player, abilities, id);
        int effectiveCoreRank = effectiveCoreRank(player, abilities, id);
        if (!AbilityConfig.enabled(id) || AbilityConfig.affinityLocked(abilities, id) || effectiveCoreRank <= 0
                || (!player.isCreative() && abilities.cooldownTicks(id) > 0)) return false;

        double abilityPower = CodexAttributes.abilityPower(player, id);
        AbilityUseEvent.Pre preEvent = new AbilityUseEvent.Pre(player, id, effectiveRank, skills, abilityPower);
        if (NeoForge.EVENT_BUS.post(preEvent).isCanceled()) return false;

        int coreRank = Math.max(1, effectiveCoreRank);
        boolean finalForm = isFinalForm(abilities, id);
        if (!execute(player, id, coreRank, preEvent.getAbilityPower(), finalForm)) return false;
        if (player.isCreative()) abilities.setCooldown(id, 0);
        else abilities.setCooldown(id, AbilityScaling.cooldownTicks(id, coreRank, skills));
        if (player.gameMode.isSurvival() && AbilityConfig.switchCooldownsEnabled()) {
            abilities.setSwitchCooldownTicks(id.core(), AbilityConfig.survivalSwitchCooldownTicks());
        }
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

    public static boolean isFinalForm(PlayerAbilities abilities, AbilityId id) {
        if (abilities == null || id == null) return false;
        AbilityId core = id.core();
        return AbilityConfig.ultimateAbilitiesEnabled() && abilities.rank(core) >= core.maxRank();
    }

    public static AbilityElement currentDamageElement() {
        return DAMAGE_ELEMENT.get();
    }

    public static ServerPlayer currentDamageAttacker() {
        return DAMAGE_ATTACKER.get();
    }

    private static boolean execute(ServerPlayer player, AbilityId id, int coreRank, double abilityPower, boolean finalForm) {
        if (id == AbilityId.BLOOD_HEAL) return bloodHeal(player, id, coreRank, abilityPower, finalForm);
        if (id == AbilityId.BLOOD_CLEANSE) return bloodCleanse(player, id, coreRank, abilityPower, finalForm);
        if (id == AbilityId.BLOOD_BURST) return bloodBurst(player, id, coreRank, abilityPower, finalForm);
        if (id == AbilityId.BLOOD_DRAIN) return bloodDrain(player, id, coreRank, abilityPower, finalForm);
        if (id == AbilityId.WIND_DASH) return windDash(player, id, coreRank, abilityPower, finalForm);
        if (id == AbilityId.WIND_LEAP) return windLeap(player, id, coreRank, abilityPower, finalForm);
        if (id == AbilityId.WIND_LUNGE) return windLunge(player, id, coreRank, abilityPower, finalForm);
        return switch (id.specialization()) {
            case BURST -> burst(player, id, coreRank, abilityPower, finalForm);
            case NOVA -> aura(player, id, coreRank, abilityPower, finalForm);
            case IMPLODE -> implode(player, id, coreRank, abilityPower, finalForm);
            case STORM -> storm(player, id, coreRank, abilityPower, finalForm);
            case PIERCE -> pierce(player, id, coreRank, abilityPower, finalForm);
            case GLACIER -> glacier(player, id, coreRank, abilityPower, finalForm);
            case STRIKE -> strike(player, id, coreRank, abilityPower, finalForm);
            case DRAIN -> bloodDrain(player, id, coreRank, abilityPower, finalForm);
            case ZAP -> zap(player, id, coreRank, abilityPower, finalForm);
            case AEGIS -> aegis(player, id, coreRank, abilityPower, finalForm);
            case RAMPAGE -> rampage(player, id, coreRank, abilityPower, finalForm);
            case BASH -> bash(player, id, coreRank, abilityPower);
            default -> false;
        };
    }

    private static boolean bloodHeal(ServerPlayer player, AbilityId id, int coreRank, double abilityPower, boolean finalForm) {
        player.heal(Math.max(1.0F, AbilityScaling.damage(id, coreRank, abilityPower) * 0.6F));
        if (finalForm) {
            player.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 200, 0, false, true, true));
            for (LivingEntity target : nearby(player, 5.0D)) target.igniteForSeconds(10);
        }
        fx(player, ParticleTypes.HEART, SoundEvents.AMETHYST_BLOCK_CHIME);
        return true;
    }

    private static boolean bloodCleanse(ServerPlayer player, AbilityId id, int coreRank, double abilityPower, boolean finalForm) {
        List<MobEffectInstance> harmful = List.copyOf(player.getActiveEffects()).stream()
                .filter(effect -> !effect.getEffect().value().isBeneficial())
                .toList();
        for (MobEffectInstance effect : harmful) {
            if (!effect.getEffect().value().isBeneficial()) player.removeEffect(effect.getEffect());
        }
        if (finalForm) {
            for (LivingEntity target : nearby(player, 4.0D)) {
                target.igniteForSeconds(10);
                for (MobEffectInstance effect : harmful) target.addEffect(new MobEffectInstance(effect));
            }
        }
        if (player.isOnFire()) player.clearFire();
        player.heal(Math.max(1.0F, AbilityScaling.damage(id, coreRank, abilityPower) * 0.35F));
        fx(player, ParticleTypes.WAX_OFF, SoundEvents.GENERIC_DRINK);
        return true;
    }

    private static boolean bloodBurst(ServerPlayer player, AbilityId id, int coreRank, double abilityPower, boolean finalForm) {
        float cost = Math.min(Math.max(0.0F, player.getHealth() - 1.0F), Math.max(1.0F, AbilityScaling.damage(id, coreRank, abilityPower)));
        if (cost <= 0.0F) return false;

        player.setHealth(player.getHealth() - cost);
        launchBurstProjectiles(player, id, coreRank, cost, abilityPower, finalForm);
        return true;
    }

    private static boolean bloodDrain(ServerPlayer player, AbilityId id, int coreRank, double abilityPower, boolean finalForm) {
        double range = AbilityScaling.radius(id, coreRank, abilityPower) + 12.0D;
        LivingEntity target = firstHitOnRay(player, player.getEyePosition(), player.getLookAngle().normalize(), range, 0.75D);
        if (target == null) return false;
        if (finalForm) target.igniteForSeconds(10);

        BLOOD_DRAIN_TARGETS.put(player.getUUID(), target.getUUID());
        player.getData(AbilitiesAttachments.PLAYER_ABILITIES.get()).setActiveTicks(id, Math.max(40, AbilityScaling.durationTicks(id, coreRank, abilityPower)));
        spawnRayParticles(player, player.getEyePosition(), target.position().add(0.0D, target.getBbHeight() * 0.5D, 0.0D), ParticleTypes.DAMAGE_INDICATOR);
        fx(player, ParticleTypes.DAMAGE_INDICATOR, SoundEvents.BEACON_ACTIVATE);
        return true;
    }

    private static boolean windDash(ServerPlayer player, AbilityId id, int coreRank, double abilityPower, boolean finalForm) {
        Vec3 look = player.getLookAngle();
        double ap = powerScale(abilityPower);
        double boost = (finalForm ? 1.5D : 1.0D) * AbilityScaling.masteryPowerMultiplier(AbilityElement.WIND, coreRank);
        Vec3 horiz = new Vec3(look.x, 0.0D, look.z).normalize().scale((1.0D + coreRank * 0.2D) * ap * boost);
        player.push(horiz.x, 0.09D, horiz.z);
        player.hurtMarked = true;
        fx(player, ParticleTypes.CLOUD, SoundEvents.BREEZE_WIND_CHARGE_BURST.value());
        if (finalForm) tempestBlast(player, coreRank, abilityPower);
        return true;
    }

    private static boolean windLeap(ServerPlayer player, AbilityId id, int coreRank, double abilityPower, boolean finalForm) {
        Vec3 look = player.getLookAngle();
        double ap = powerScale(abilityPower);
        double boost = (finalForm ? 1.45D : 1.0D) * AbilityScaling.masteryPowerMultiplier(AbilityElement.WIND, coreRank);
        Vec3 horiz = new Vec3(look.x, 0.0D, look.z).normalize().scale((0.75D + coreRank * 0.15D) * ap * boost);
        player.setDeltaMovement(horiz.x, (0.55D + coreRank * 0.05D) * ap * boost, horiz.z);
        player.hurtMarked = true;
        fx(player, ParticleTypes.POOF, SoundEvents.GOAT_LONG_JUMP);
        if (finalForm) tempestBlast(player, coreRank, abilityPower);
        return true;
    }

    private static boolean windLunge(ServerPlayer player, AbilityId id, int coreRank, double abilityPower, boolean finalForm) {
        LivingEntity target = nearby(player, AbilityScaling.radius(id, coreRank, abilityPower) + 4.0D).stream().filter(e -> inFront(player, e)).findFirst().orElse(null);
        if (target == null) return false;
        double ap = powerScale(abilityPower);
        Vec3 toward = target.position().subtract(player.position()).normalize();
        double boost = (finalForm ? 1.5D : 1.0D) * AbilityScaling.masteryPowerMultiplier(AbilityElement.WIND, coreRank);
        player.setDeltaMovement(toward.x * 1.12D * ap * boost, 0.14D * ap * boost, toward.z * 1.12D * ap * boost);
        player.hurtMarked = true;
        hurtWithAbility(player, target, AbilityElement.WIND, player.damageSources().playerAttack(player), AbilityScaling.damage(id, coreRank, abilityPower) * (finalForm ? 1.75F : 1.25F));
        fx(player, ParticleTypes.SWEEP_ATTACK, SoundEvents.PLAYER_ATTACK_KNOCKBACK);
        if (finalForm) tempestBlast(player, coreRank, abilityPower);
        return true;
    }

    private static boolean burst(ServerPlayer player, AbilityId id, int coreRank, double abilityPower, boolean finalForm) {
        if (id == AbilityId.FORCE_BURST) {
            double radius = AbilityScaling.radius(id, coreRank, abilityPower) + 1.5D;
            List<LivingEntity> targets = nearby(player, radius);
            if (targets.isEmpty()) return false;
            float damage = AbilityScaling.damage(id, coreRank, abilityPower) * (finalForm ? 1.6F : 1.25F);
            double ap = powerScale(abilityPower);
            for (LivingEntity target : targets) {
                hurtWithAbility(player, target, AbilityElement.FORCE, player.damageSources().magic(), damage);
                pushAwayFrom(player, target, (1.05D + coreRank * 0.06D) * ap * (finalForm ? 1.75D : 1.0D));
            }
            fx(player, ParticleTypes.EXPLOSION, SoundEvents.GENERIC_EXPLODE.value());
            return true;
        }

        float damage = AbilityScaling.damage(id, coreRank, abilityPower) * (finalForm && id.element() == AbilityElement.FIRE ? 1.5F : 1.0F);
        launchBurstProjectiles(player, id, coreRank, damage, abilityPower, finalForm);
        return true;
    }

    private static void launchBurstProjectiles(ServerPlayer player, AbilityId id, int coreRank, float damage, double abilityPower, boolean finalForm) {
        int projectiles = AbilityScaling.burstProjectiles(coreRank, finalForm);
        double stepDeg = 15.0D;
        double half = (projectiles - 1) * 0.5D;
        Vec3 look = player.getLookAngle().normalize();
        int duration = AbilityScaling.durationTicks(id, coreRank, abilityPower);
        for (int i = 0; i < projectiles; i++) {
            double angle = (i - half) * stepDeg;
            Vec3 dir = rotateYaw(look, angle).normalize();
            BurstCubeProjectile projectile = new BurstCubeProjectile(player.level(), player, id.element(), damage, duration, finalForm);
            projectile.shoot(dir.x, dir.y, dir.z, 1.32F, 0.0F);
            player.level().addFreshEntity(projectile);
        }
        fx(player, particleForElement(id.element(), finalForm), SoundEvents.BLAZE_SHOOT);
    }

    private static boolean aura(ServerPlayer player, AbilityId id, int coreRank, double abilityPower, boolean finalForm) {
        int duration = AbilityScaling.auraDurationTicks(coreRank, finalForm);
        player.getData(AbilitiesAttachments.PLAYER_ABILITIES.get()).setActiveTicks(id, duration);
        fx(player, particleForElement(id.element(), finalForm), SoundEvents.BEACON_ACTIVATE);
        return true;
    }

    private static boolean implode(ServerPlayer player, AbilityId id, int coreRank, double abilityPower, boolean finalForm) {
        List<LivingEntity> targets = nearby(player, AbilityScaling.radius(id, coreRank, abilityPower) + 1.0D);
        if (targets.isEmpty()) return false;
        float damageScale = id.element() == AbilityElement.FORCE ? 0.75F : 1.0F;
        double pullStrength = id.element() == AbilityElement.FORCE ? (0.3D + coreRank * 0.03D) : (0.5D + coreRank * 0.05D);
        pullStrength *= powerScale(abilityPower);
        for (LivingEntity target : targets) {
            if (finalForm && id.element() == AbilityElement.FIRE) pushAwayFrom(player, target, pullStrength * 1.35D);
            else pullToward(player, target, pullStrength);
            applyElementHit(player, id.element(), target, AbilityScaling.damage(id, coreRank, abilityPower) * damageScale, AbilityScaling.durationTicks(id, coreRank, abilityPower), finalForm);
            if (id == AbilityId.LIGHTNING_IMPLODE && player.level() instanceof ServerLevel level) strikeLightning(level, target.getX(), target.getY(), target.getZ());
        }
        fx(player, id.element() == AbilityElement.FIRE ? particleForElement(AbilityElement.FIRE, finalForm) : ParticleTypes.EXPLOSION,
                SoundEvents.GENERIC_EXPLODE.value());
        return true;
    }

    private static boolean storm(ServerPlayer player, AbilityId id, int coreRank, double abilityPower, boolean finalForm) {
        int duration = Math.max(60, AbilityScaling.durationTicks(id, coreRank, abilityPower));
        player.getData(AbilitiesAttachments.PLAYER_ABILITIES.get()).setActiveTicks(id, duration);
        fx(player, particleForElement(id.element(), finalForm), SoundEvents.LIGHTNING_BOLT_THUNDER);
        return true;
    }

    private static boolean pierce(ServerPlayer player, AbilityId id, int coreRank, double abilityPower, boolean finalForm) {
        int projectiles = AbilityScaling.pierceProjectiles(coreRank);
        int pierceCount = Math.max(1, coreRank);
        double hitRadius = 0.35D + (coreRank * 0.08D);
        Vec3 eye = player.getEyePosition();
        Vec3 look = player.getLookAngle().normalize();
        double range = AbilityScaling.radius(id, coreRank, abilityPower) + 10.0D;
        float damage = AbilityScaling.damage(id, coreRank, abilityPower);
        int duration = AbilityScaling.durationTicks(id, coreRank, abilityPower);
        boolean hitAny = false;
        for (int i = 0; i < projectiles; i++) {
            double angle = (i - (projectiles - 1) * 0.5D) * 5.0D;
            Vec3 direction = rotateYaw(look, angle).normalize();
            List<LivingEntity> hits = hitsOnRay(player, eye, direction, range, hitRadius, pierceCount);
            for (LivingEntity hit : hits) {
                applyElementHit(player, id.element(), hit, damage, duration, finalForm);
                hitAny = true;
            }
            spawnRayParticles(player, eye, eye.add(direction.scale(range)), ParticleTypes.SNOWFLAKE);
        }
        fx(player, ParticleTypes.SWEEP_ATTACK, SoundEvents.PLAYER_ATTACK_SWEEP);
        return hitAny;
    }

    private static boolean glacier(ServerPlayer player, AbilityId id, int coreRank, double abilityPower, boolean finalForm) {
        List<LivingEntity> targets = nearby(player, AbilityScaling.radius(id, coreRank, abilityPower) + 2.0D);
        if (targets.isEmpty()) return false;
        int duration = AbilityScaling.durationTicks(id, coreRank, abilityPower);
        for (LivingEntity target : targets) {
            hurtWithAbility(player, target, AbilityElement.ICE, player.damageSources().magic(), AbilityScaling.damage(id, coreRank, abilityPower));
            target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, duration, 3, false, true, true));
            if (finalForm) target.addEffect(new MobEffectInstance(CodexMobEffects.PERMAFROST, 120, 0, false, true, true));
            if (player.level() instanceof ServerLevel level) {
                level.sendParticles(ParticleTypes.ITEM_SNOWBALL, target.getX(), target.getY(), target.getZ(), 6, 0.15D, 0.7D, 0.15D, 0.01D);
            }
        }
        fx(player, ParticleTypes.SNOWFLAKE, SoundEvents.GLASS_BREAK);
        return true;
    }

    private static boolean strike(ServerPlayer player, AbilityId id, int coreRank, double abilityPower, boolean finalForm) {
        LivingEntity target = nearby(player, AbilityScaling.radius(id, coreRank, abilityPower) + 2.0D).stream().filter(e -> inFront(player, e)).findFirst().orElse(null);
        if (target == null) return false;
        applyElementHit(player, id.element(), target, AbilityScaling.damage(id, coreRank, abilityPower) * 1.2F, AbilityScaling.durationTicks(id, coreRank, abilityPower), finalForm);
        if (id.element() == AbilityElement.LIGHTNING && player.level() instanceof ServerLevel level) {
            strikeLightningNoFire(level, target.getX(), target.getY(), target.getZ());
            level.sendParticles(lightningStrikeParticle(finalForm), target.getX(), target.getY() + 1.0D, target.getZ(), 24, 0.22D, 0.55D, 0.22D, 0.02D);
        }
        fx(player, ParticleTypes.CRIT, SoundEvents.PLAYER_ATTACK_CRIT);
        return true;
    }

    private static boolean zap(ServerPlayer player, AbilityId id, int coreRank, double abilityPower, boolean finalForm) {
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
            applyElementHit(player, AbilityElement.LIGHTNING, target, damage, duration, finalForm);
            if (player.level() instanceof ServerLevel level) {
                spawnSmallLightningParticles(level, target.position().add(0.0D, 1.8D, 0.0D), target.position().add(0.0D, 0.3D, 0.0D), lightningStrikeParticle(finalForm));
            }
        }
        fx(player, particleForElement(AbilityElement.LIGHTNING, finalForm), SoundEvents.LIGHTNING_BOLT_IMPACT);
        return true;
    }

    private static boolean aegis(ServerPlayer player, AbilityId id, int coreRank, double abilityPower, boolean finalForm) {
        int charges = Math.max(1, (int) Math.round(coreRank * powerScale(abilityPower))) + (finalForm ? 3 : 0);
        player.getData(AbilitiesAttachments.PLAYER_ABILITIES.get()).setActiveTicks(id, charges);
        fx(player, ParticleTypes.TOTEM_OF_UNDYING, SoundEvents.SHIELD_BLOCK);
        return true;
    }

    private static boolean rampage(ServerPlayer player, AbilityId id, int coreRank, double abilityPower, boolean finalForm) {
        int duration = AbilityScaling.durationTicks(id, coreRank, abilityPower);
        player.addEffect(new MobEffectInstance(finalForm ? CodexMobEffects.SINGULARITY_RAMPAGE : CodexMobEffects.RAMPAGING,
                duration, Math.max(0, coreRank - (finalForm ? 0 : 1)), false, true, true));
        fx(player, ParticleTypes.ANGRY_VILLAGER, SoundEvents.RAID_HORN.value());
        return true;
    }

    private static boolean bash(ServerPlayer player, AbilityId id, int coreRank, double abilityPower) {
        LivingEntity target = nearby(player, AbilityScaling.radius(id, coreRank, abilityPower) + 1.5D).stream().filter(e -> inFront(player, e)).findFirst().orElse(null);
        if (target == null) return false;
        hurtWithAbility(player, target, id.element(), player.damageSources().playerAttack(player), AbilityScaling.damage(id, coreRank, abilityPower) * 1.4F);
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

    public static void applyElementHit(ServerPlayer player, AbilityElement element, LivingEntity target, float damage, int duration, boolean finalForm) {
        float appliedDamage = finalForm && element == AbilityElement.LIGHTNING ? damage * 1.5F : damage;
        hurtWithAbility(player, target, element, player.damageSources().magic(), appliedDamage);
        switch (element) {
            case FIRE -> {
                target.igniteForSeconds(Math.max(1, duration / 20) * (finalForm ? 2 : 1));
                if (player.level() instanceof ServerLevel level) {
                    level.sendParticles(finalForm ? ModParticles.SOULFIRE.get() : ModParticles.FIRE.get(), target.getX(), target.getY() + 0.8D, target.getZ(), finalForm ? 10 : 6, 0.3D, 0.5D, 0.3D, 0.01D);
                }
            }
            case ICE -> {
                target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, duration, 2, false, true, true));
                if (finalForm) target.addEffect(new MobEffectInstance(CodexMobEffects.PERMAFROST, 120, 0, false, true, true));
            }
            case LIGHTNING -> {
                if (player.level() instanceof ServerLevel level) {
                    level.sendParticles(lightningStrikeParticle(finalForm), target.getX(), target.getY() + target.getBbHeight() * 0.5D, target.getZ(), 16, 0.3D, 0.45D, 0.3D, 0.025D);
                }
                if (finalForm) plasmaExplosion(player, target, damage * 0.5F);
            }
            case POISON -> {
                target.addEffect(new MobEffectInstance(finalForm ? CodexMobEffects.TOXIN : MobEffects.POISON, duration, finalForm ? 0 : 1, false, true, true));
                if (player.level() instanceof ServerLevel level) {
                    ParticleOptions poisonParticle = finalForm ? ModParticles.TOXIN.get() : ModParticles.POISON.get();
                    level.sendParticles(poisonParticle, target.getX(), target.getY() + target.getBbHeight() * 0.5D, target.getZ(), 18, 0.38D, 0.55D, 0.38D, 0.015D);
                    if (finalForm) {
                        level.sendParticles(ModParticles.POISON.get(), target.getX(), target.getY() + target.getBbHeight() * 0.5D, target.getZ(), 8, 0.3D, 0.45D, 0.3D, 0.01D);
                    }
                }
            }
            case FORCE -> pullToward(target, player, 0.6D);
            case BLOOD -> {
                if (finalForm) target.igniteForSeconds(10);
            }
            case WIND -> pullToward(target, player, 0.65D);
        }
    }

    public static void applyElementHit(ServerPlayer player, AbilityElement element, LivingEntity target, float damage, int duration) {
        applyElementHit(player, element, target, damage, duration, false);
    }

    private static void plasmaExplosion(ServerPlayer player, LivingEntity primary, float damage) {
        if (!(player.level() instanceof ServerLevel level)) return;
        for (LivingEntity nearby : level.getEntitiesOfClass(LivingEntity.class, primary.getBoundingBox().inflate(2.5D),
                entity -> entity != player && entity != primary && entity.isAlive())) {
            hurtWithAbility(player, nearby, AbilityElement.LIGHTNING, player.damageSources().magic(), damage);
        }
        level.sendParticles(ModParticles.PLASMA_BLAST.get(), primary.getX(), primary.getY() + 0.8D, primary.getZ(), 24, 0.55D, 0.55D, 0.55D, 0.08D);
        level.sendParticles(ModParticles.PLASMA_STRIKE.get(), primary.getX(), primary.getY() + 0.8D, primary.getZ(), 12, 0.4D, 0.4D, 0.4D, 0.06D);
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

    private static void tempestBlast(ServerPlayer player, int coreRank, double abilityPower) {
        if (!(player.level() instanceof ServerLevel level)) return;
        double radius = 4.0D;
        float damage = Math.max(4.0F, coreRank * 0.8F) * (float) powerScale(abilityPower);
        for (LivingEntity target : nearby(player, radius)) {
            hurtWithAbility(player, target, AbilityElement.WIND, player.damageSources().magic(), damage);
            pushAwayFrom(player, target, 1.35D * powerScale(abilityPower));
        }
        for (int i = 0; i < 4; i++) {
            double angle = Math.PI * 0.5D * i;
            double x = player.getX() + Math.cos(angle) * 1.2D;
            double z = player.getZ() + Math.sin(angle) * 1.2D;
            level.sendParticles(ParticleTypes.GUST_EMITTER_LARGE, x, player.getY() + 0.5D, z, 1, 0.0D, 0.0D, 0.0D, 0.0D);
        }
        level.playSound(null, player.blockPosition(), SoundEvents.WIND_CHARGE_BURST.value(), SoundSource.PLAYERS, 1.0F, 0.85F);
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
            if (id == AbilityId.FORCE_AEGIS) continue;
            abilities.setActiveTicks(id, active - 1);
            if (id.specialization() == AbilitySpecialization.STORM) {
                tickStorm(player, id, abilities, skills);
            } else if (id.specialization() == AbilitySpecialization.NOVA) {
                tickAura(player, id, abilities);
            } else if (id == AbilityId.BLOOD_DRAIN) {
                tickBloodDrain(player, id, abilities, skills);
            }
        }
        if (abilities.activeTicks(AbilityId.BLOOD_DRAIN) <= 0) {
            BLOOD_DRAIN_TARGETS.remove(player.getUUID());
        }
        AbilityCombatEvents.tickPlayer(player);
    }

    private static void tickAura(ServerPlayer player, AbilityId id, PlayerAbilities abilities) {
        if (!(player.level() instanceof ServerLevel level)) return;
        int coreRank = Math.max(1, effectiveCoreRank(player, abilities, id));
        boolean finalForm = isFinalForm(abilities, id);
        int totalDuration = AbilityScaling.auraDurationTicks(coreRank, finalForm);
        int elapsed = Math.max(1, totalDuration - abilities.activeTicks(id));
        double maxRadius = AbilityScaling.auraRadius(coreRank, finalForm);
        double pulseRadius = maxRadius * ((((elapsed - 1) % 20) + 1) / 20.0D);
        spawnPulseRing(level, player.position().add(0.0D, 0.15D, 0.0D), pulseRadius, particleForElement(id.element(), finalForm));
        if (id.element() == AbilityElement.POISON && finalForm && elapsed % 2 == 0) {
            spawnPulseRing(level, player.position().add(0.0D, 0.25D, 0.0D), pulseRadius * 0.92D, ModParticles.POISON.get());
        }
        if (elapsed % 20 != 0) return;

        double abilityPower = CodexAttributes.abilityPower(player, id);
        float damage = AbilityScaling.damage(id, coreRank, abilityPower) * 0.45F;
        if (finalForm && id.element() == AbilityElement.FIRE) damage *= 2.0F;
        int effectDuration = AbilityScaling.durationTicks(id, coreRank, abilityPower);
        for (LivingEntity target : nearby(player, maxRadius)) {
            applyElementHit(player, id.element(), target, damage, effectDuration, finalForm);
        }
    }

    private static void tickBloodDrain(ServerPlayer player, AbilityId id, PlayerAbilities abilities, PlayerSkills skills) {
        if (!(player.level() instanceof ServerLevel level)) return;

        LivingEntity target = bloodDrainTarget(player, level);
        if (target == null) {
            abilities.setActiveTicks(id, 0);
            BLOOD_DRAIN_TARGETS.remove(player.getUUID());
            return;
        }

        int coreRank = Math.max(1, effectiveCoreRank(player, abilities, id));
        double abilityPower = CodexAttributes.abilityPower(player, id);
        double range = AbilityScaling.radius(id, coreRank, abilityPower) + 12.0D;
        if (target.distanceToSqr(player) > range * range) {
            abilities.setActiveTicks(id, 0);
            BLOOD_DRAIN_TARGETS.remove(player.getUUID());
            return;
        }

        Vec3 from = player.getEyePosition();
        Vec3 to = target.position().add(0.0D, target.getBbHeight() * 0.5D, 0.0D);
        if (abilities.activeTicks(id) % 2 == 0) {
            spawnRayParticles(player, from, to, ParticleTypes.DAMAGE_INDICATOR);
        }
        if (abilities.activeTicks(id) % 10 != 0) return;

        float damage = Math.max(1.0F, AbilityScaling.damage(id, coreRank, abilityPower) * 0.35F);
        float before = target.getHealth();
        if (hurtWithAbility(player, target, AbilityElement.BLOOD, player.damageSources().magic(), damage)) {
            float drained = Math.max(0.0F, before - target.getHealth());
            if (drained > 0.0F) player.heal(drained * 0.5F);
        }
        if (isFinalForm(abilities, id)) target.igniteForSeconds(10);
        level.sendParticles(ParticleTypes.DAMAGE_INDICATOR, target.getX(), target.getY() + target.getBbHeight() * 0.5D, target.getZ(), 4, 0.18D, 0.25D, 0.18D, 0.01D);
    }

    private static LivingEntity bloodDrainTarget(ServerPlayer player, ServerLevel level) {
        UUID targetId = BLOOD_DRAIN_TARGETS.get(player.getUUID());
        if (targetId == null) return null;
        var entity = level.getEntity(targetId);
        return entity instanceof LivingEntity living && living.isAlive() ? living : null;
    }

    private static void tickStorm(ServerPlayer player, AbilityId id, PlayerAbilities abilities, PlayerSkills skills) {
        if (!(player.level() instanceof ServerLevel level)) return;
        int coreRank = Math.max(1, effectiveCoreRank(player, abilities, id));
        double abilityPower = CodexAttributes.abilityPower(player, id);
        int active = abilities.activeTicks(id);
        boolean finalForm = isFinalForm(abilities, id);
        Vec3 center = player.position().add(0.0D, 4.2D, 0.0D);
        level.sendParticles(ParticleTypes.CLOUD, center.x, center.y, center.z, 32, 2.1D, 0.35D, 2.1D, 0.015D);
        if (active % 2 == 0) {
            spawnPulseRing(level, center, 2.7D + Math.sin(active * 0.18D) * 0.45D, particleForElement(id.element(), finalForm));
        }
        if (active % 3 == 0) {
            spawnStormPrecipitation(level, center, id.element(), finalForm);
        }

        if (active % 10 != 0) return;
        double radius = AbilityScaling.radius(id, coreRank, abilityPower) + 2.5D;
        List<LivingEntity> targets = nearby(player, radius).stream().sorted(Comparator.comparingDouble(e -> e.distanceToSqr(player))).limit(6).toList();
        for (LivingEntity target : targets) {
            float tickDamage = AbilityScaling.damage(id, coreRank, abilityPower) * 0.6F;
            if (id == AbilityId.LIGHTNING_STORM) {
                // Tick interval is 10 ticks (0.5s), so 2.5 damage per tick ~= 5 DPS.
                tickDamage = 2.5F;
            }
            applyElementHit(player, id.element(), target, tickDamage, AbilityScaling.durationTicks(id, coreRank, abilityPower), finalForm);
            if (id.element() == AbilityElement.LIGHTNING) {
                ParticleOptions strikeParticle = lightningStrikeParticle(finalForm);
                spawnSmallLightningParticles(level, center, target.position().add(0.0D, 0.2D, 0.0D), strikeParticle);
                spawnLightningBoltParticles(level, center, target.position().add(0.0D, 0.2D, 0.0D), strikeParticle);
                level.sendParticles(strikeParticle, target.getX(), target.getY() + 1.0D, target.getZ(), 18, 0.22D, 0.45D, 0.22D, 0.015D);
            } else if (id.element() == AbilityElement.FIRE) {
                spawnRainParticles(level, center, target.position().add(0.0D, 0.3D, 0.0D), particleForElement(id.element(), finalForm));
            } else if (id.element() == AbilityElement.ICE) {
                spawnRainParticles(level, center, target.position().add(0.0D, 0.3D, 0.0D), ParticleTypes.SNOWFLAKE);
            }
        }
    }

    private static void spawnStormPrecipitation(ServerLevel level, Vec3 center, AbilityElement element, boolean finalForm) {
        double y = center.y - 0.4D;
        if (element == AbilityElement.LIGHTNING) {
            level.sendParticles(ParticleTypes.RAIN, center.x, y, center.z, 10, 2.0D, 0.1D, 2.0D, 0.02D);
        } else if (element == AbilityElement.ICE) {
            level.sendParticles(ParticleTypes.SNOWFLAKE, center.x, y, center.z, 8, 2.0D, 0.1D, 2.0D, 0.02D);
            level.sendParticles(ModParticles.ICICLE.get(), center.x, y, center.z, 5, 2.0D, 0.1D, 2.0D, 0.02D);
        } else if (element == AbilityElement.FIRE) {
            level.sendParticles(ParticleTypes.FALLING_LAVA, center.x, y, center.z, 6, 2.0D, 0.1D, 2.0D, 0.02D);
            level.sendParticles(finalForm ? ModParticles.SOUL_FIRE_EMBER.get() : ModParticles.FIRE_EMBER.get(),
                    center.x, y, center.z, 8, 2.0D, 0.1D, 2.0D, 0.02D);
        }
    }

    private static ParticleOptions particleForElement(AbilityElement element, boolean finalForm) {
        return switch (element) {
            case ICE -> ParticleTypes.SNOWFLAKE;
            case POISON -> finalForm ? ModParticles.TOXIN.get() : ModParticles.POISON.get();
            case LIGHTNING -> finalForm ? ModParticles.PLASMA_BLAST.get() : ModParticles.LIGHTNING_STRIKE.get();
            case FORCE -> finalForm ? ParticleTypes.REVERSE_PORTAL : ParticleTypes.CRIT;
            case BLOOD -> ParticleTypes.DAMAGE_INDICATOR;
            case WIND -> finalForm ? ParticleTypes.GUST : ParticleTypes.CLOUD;
            case FIRE -> finalForm ? ModParticles.SOUL_FIRE_EMBER.get() : ModParticles.FIRE_EMBER.get();
        };
    }

    private static ParticleOptions lightningStrikeParticle(boolean finalForm) {
        return finalForm ? ModParticles.PLASMA_STRIKE.get() : ModParticles.LIGHTNING_STRIKE.get();
    }

    private static void spawnPulseRing(ServerLevel level, Vec3 center, double radius, ParticleOptions particle) {
        int points = Math.max(16, (int) Math.ceil(radius * 6.0D));
        for (int i = 0; i < points; i++) {
            double angle = (Math.PI * 2.0D * i) / points;
            double x = center.x + Math.cos(angle) * radius;
            double z = center.z + Math.sin(angle) * radius;
            level.sendParticles(particle, x, center.y, z, 1, 0.0D, 0.025D, 0.0D, 0.0D);
        }
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

    private static void spawnSmallLightningParticles(ServerLevel level, Vec3 from, Vec3 to, ParticleOptions particle) {
        for (int i = 0; i <= 10; i++) {
            Vec3 p = from.lerp(to, i / 10.0D);
            level.sendParticles(particle, p.x, p.y, p.z, 1, 0.04D, 0.04D, 0.04D, 0.0D);
        }
    }

    private static void spawnLightningBoltParticles(ServerLevel level, Vec3 from, Vec3 to, ParticleOptions particle) {
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
                level.sendParticles(particle, p.x, p.y, p.z, 1, 0.02D, 0.02D, 0.02D, 0.0D);
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

    private static boolean hurtWithAbility(ServerPlayer player, LivingEntity target, AbilityElement element, DamageSource source, float damage) {
        PlayerAbilities abilities = player.getData(AbilitiesAttachments.PLAYER_ABILITIES.get());
        int masteryRank = abilities.rank(AbilityId.valueOf(element.name()));
        float scaledDamage = damage * (float) (1.0D + AbilityScaling.masteryDamageBonus(masteryRank));
        AbilityElement previous = DAMAGE_ELEMENT.get();
        ServerPlayer previousAttacker = DAMAGE_ATTACKER.get();
        DAMAGE_ELEMENT.set(element);
        DAMAGE_ATTACKER.set(player);
        try {
            return target.hurt(source, scaledDamage);
        } finally {
            if (previous == null) DAMAGE_ELEMENT.remove();
            else DAMAGE_ELEMENT.set(previous);
            if (previousAttacker == null) DAMAGE_ATTACKER.remove();
            else DAMAGE_ATTACKER.set(previousAttacker);
        }
    }
}

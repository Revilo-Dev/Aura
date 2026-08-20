package net.revilodev.aura.skills.logic;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.revilodev.aura.CodexMod;
import net.revilodev.aura.attributes.CodexAttributes;
import net.revilodev.aura.skills.PlayerSkills;
import net.revilodev.aura.skills.SkillBalance;
import net.revilodev.aura.skills.SkillId;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

// skill logic
public final class SkillLogic {
    private SkillLogic() {}

    private static final ResourceLocation MOD_MAX_HEALTH = ResourceLocation.fromNamespaceAndPath(CodexMod.MOD_ID, "skill_health_boost");
    private static final ResourceLocation MOD_ATTACK_DAMAGE = ResourceLocation.fromNamespaceAndPath(CodexMod.MOD_ID, "skill_strength");
    private static final ResourceLocation MOD_MOVEMENT_SPEED = ResourceLocation.fromNamespaceAndPath(CodexMod.MOD_ID, "skill_agility");
    private static final ResourceLocation MOD_KB_RES = ResourceLocation.fromNamespaceAndPath(CodexMod.MOD_ID, "skill_knockback_res");
    private static final ResourceLocation MOD_LUCK = ResourceLocation.fromNamespaceAndPath(CodexMod.MOD_ID, "skill_luck");
    private static final Map<UUID, Streak> COMBAT_STREAK = new HashMap<>();
    private static final int COMBAT_WINDOW_TICKS = 200;

    public static boolean tryUpgrade(ServerPlayer player, PlayerSkills skills, SkillId id) {
        if (player == null || id == null) return false;
        int cur = skills.level(id);
        if (cur >= id.maxLevel()) return false;
        if (!skills.canUnlock(id)) return false;
        return skills.tryUpgrade(id);
    }

    public static boolean tryDowngrade(PlayerSkills skills, SkillId id) {
        return skills.tryDowngrade(id);
    }

    public static int effectiveLevel(ServerPlayer player, PlayerSkills skills, SkillId id) {
        if (skills == null || id == null) return 0;
        return Math.max(0, skills.level(id) + CodexAttributes.skillBonus(player, id));
    }

    public static void clearStreaks(UUID id) {
        if (id != null) COMBAT_STREAK.remove(id);
    }

    public static boolean awardCombatKill(ServerPlayer killer, LivingEntity victim) {
        return false;
    }

    public static boolean awardSurvivalPrevented(ServerPlayer player, float preventedBySkills) {
        return false;
    }

    public static int requiredLevelForNextRank(SkillId id, int currentSkillLevel) {
        return 1;
    }

    public static float applyIncomingReductions(ServerPlayer target, PlayerSkills skills, DamageSource src, float amount) {
        float out = amount;
        int resistance = effectiveLevel(target, skills, SkillId.RESISTANCE);
        if (resistance > 0) {
            out *= (float) (1.0D - SkillBalance.resistance(resistance));
        }

        int fire = effectiveLevel(target, skills, SkillId.FIRE_RESISTANCE);
        if (fire > 0 && src.is(DamageTypeTags.IS_FIRE)) {
            out *= (float) (1.0D - SkillBalance.fireResistance(fire));
        }

        int proj = effectiveLevel(target, skills, SkillId.PROJECTILE_RESISTANCE);
        if (proj > 0 && src.is(DamageTypeTags.IS_PROJECTILE)) {
            out *= (float) (1.0D - SkillBalance.projectileResistance(proj));
        }
        return out;
    }

    public static void applyAllEffects(ServerPlayer player, PlayerSkills skills) {
        skills.consumeModifiersDirty();
        applyAttributeModifiers(player, skills);
        applyTickEffects(player, skills);
    }

    private static void applyAttributeModifiers(ServerPlayer player, PlayerSkills skills) {
        int strength = effectiveLevel(player, skills, SkillId.STRENGTH);
        int vitality = effectiveLevel(player, skills, SkillId.VITALITY);
        int agility = effectiveLevel(player, skills, SkillId.AGILITY);
        int kb = effectiveLevel(player, skills, SkillId.KNOCKBACK_RESISTANCE);
        int luck = effectiveLevel(player, skills, SkillId.LUCK);

        applyModifier(player, Attributes.ATTACK_DAMAGE, MOD_ATTACK_DAMAGE, SkillBalance.strengthDamage(strength), AttributeModifier.Operation.ADD_VALUE);
        applyModifier(player, Attributes.MAX_HEALTH, MOD_MAX_HEALTH, SkillBalance.vitalityHearts(vitality) * 2.0D, AttributeModifier.Operation.ADD_VALUE);
        applyModifier(player, Attributes.MOVEMENT_SPEED, MOD_MOVEMENT_SPEED, SkillBalance.agilitySpeed(agility), AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
        applyModifier(player, Attributes.KNOCKBACK_RESISTANCE, MOD_KB_RES, SkillBalance.knockbackResistance(kb), AttributeModifier.Operation.ADD_VALUE);
        applyModifier(player, Attributes.LUCK, MOD_LUCK, SkillBalance.luck(luck), AttributeModifier.Operation.ADD_VALUE);
        clampToMaxHealth(player);
    }

    private static void applyTickEffects(ServerPlayer player, PlayerSkills skills) {
        int regen = effectiveLevel(player, skills, SkillId.REGENERATION);
        if (regen > 0 && player.getHealth() < player.getMaxHealth()) {
            float heal = SkillBalance.regenHeartsPerSecond(regen);
            if (heal > 0.0F) player.heal(heal);
        }

        int jump = effectiveLevel(player, skills, SkillId.LEAPING);
        if (jump > 0) {
            int amp = Math.max(0, Math.min(4, (int) Math.floor(SkillBalance.leapingBonus(jump))));
            addIfStronger(player, new MobEffectInstance(MobEffects.JUMP, 220, amp, true, false, false));
        }

        clampToMaxHealth(player);
    }

    private static void clampToMaxHealth(ServerPlayer player) {
        if (player.getHealth() > player.getMaxHealth()) player.setHealth(player.getMaxHealth());
    }

    private static void addIfStronger(ServerPlayer player, MobEffectInstance inst) {
        MobEffectInstance cur = player.getEffect(inst.getEffect());
        if (cur == null || cur.getAmplifier() < inst.getAmplifier() || cur.getDuration() < 40) player.addEffect(inst);
    }

    private static void applyModifier(ServerPlayer p, net.minecraft.core.Holder<net.minecraft.world.entity.ai.attributes.Attribute> attr, ResourceLocation id, double amount, AttributeModifier.Operation op) {
        AttributeInstance inst = p.getAttribute(attr);
        if (inst == null) return;
        AttributeModifier existing = inst.getModifier(id);
        if (amount == 0.0D) {
            if (existing != null) inst.removeModifier(id);
            return;
        }
        if (existing != null && existing.operation() == op && Double.compare(existing.amount(), amount) == 0) {
            return;
        }
        inst.removeModifier(id);
        inst.addPermanentModifier(new AttributeModifier(id, amount, op));
    }

    private static float streakMultiplier(Map<UUID, Streak> map, UUID id, int tick, int window, float per, float cap) {
        Streak s = map.get(id);
        if (s == null) {
            s = new Streak();
            s.lastTick = tick;
            s.count = 1;
            map.put(id, s);
            return 1.0F;
        }
        if (tick - s.lastTick <= window) s.count++;
        else s.count = 1;
        s.lastTick = tick;
        float mult = 1.0F + (Math.max(0, s.count - 1) * per);
        if (mult > cap) mult = cap;
        return mult;
    }

    private static int clampInt(int v, int min, int max) {
        return Math.max(min, Math.min(max, v));
    }

    private static final class Streak {
        int lastTick;
        int count;
    }
}

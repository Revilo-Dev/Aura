package net.revilodev.aura.abilities.logic;

import net.revilodev.aura.abilities.AbilityConfig;
import net.revilodev.aura.abilities.AbilityElement;
import net.revilodev.aura.abilities.AbilityId;
import net.revilodev.aura.skills.PlayerSkills;

import java.util.Locale;

// ability scaling
public final class AbilityScaling {
    private AbilityScaling() {}

    public static int cooldownTicks(AbilityId id, int rank, PlayerSkills skills) {
        if (id.specialization() == net.revilodev.aura.abilities.AbilitySpecialization.NOVA) {
            boolean finalForm = AbilityConfig.ultimateAbilitiesEnabled() && rank >= id.core().maxRank();
            double auraCooldown = 880.0D - Math.max(0, rank - 1) * 40.0D - (finalForm ? 80.0D : 0.0D);
            return Math.max(10, (int) Math.round(auraCooldown * AbilityConfig.cooldownMultiplier()));
        }
        if (id == AbilityId.LIGHTNING_STORM) {
            return 1200;
        }
        double out = AbilityConfig.configuredCooldown(id);
        out -= (Math.max(1, rank) - 1) * 8.0D;
        out *= AbilityConfig.cooldownMultiplier();
        int minimum = id == AbilityId.FORCE_RAMPAGE ? 1800 : 10;
        return Math.max(minimum, (int) Math.round(out));
    }

    public static float damage(AbilityId id, int coreRank, double abilityPower) {
        double base = AbilityConfig.damage(id) * (1.0D + (Math.max(1, coreRank) - 1) * 0.15D * AbilityConfig.primaryScalingMultiplier());
        return (float) (base * Math.max(0.0D, abilityPower));
    }

    public static double radius(AbilityId id, int coreRank, double abilityPower) {
        double base = AbilityConfig.radius(id) * (1.0D + (Math.max(1, coreRank) - 1) * 0.1D * AbilityConfig.primaryScalingMultiplier());
        return base * (0.85D + (Math.max(0.0D, abilityPower) * 0.15D));
    }

    public static int durationTicks(AbilityId id, int coreRank, double abilityPower) {
        double base = AbilityConfig.durationTicks(id) * (1.0D + (Math.max(1, coreRank) - 1) * 0.1D * AbilityConfig.primaryScalingMultiplier());
        return Math.max(1, (int) Math.round(base * (0.85D + (Math.max(0.0D, abilityPower) * 0.15D))));
    }

    public static int auraDurationTicks(int coreRank, boolean finalForm) {
        return 80 + Math.max(0, coreRank - 1) * 10 + (finalForm ? 80 : 0);
    }

    public static double auraRadius(int coreRank, boolean finalForm) {
        return 5.0D + Math.max(0, coreRank - 1) + (finalForm ? 2.0D : 0.0D);
    }

    public static int burstProjectiles(int coreRank, boolean finalForm) {
        return 1 + Math.max(0, coreRank) + (finalForm ? 2 : 0);
    }

    public static int pierceProjectiles(int coreRank) {
        return 1 + Math.max(0, coreRank / 2);
    }

    public static double masteryDamageBonus(int coreRank) {
        return Math.max(0, coreRank) * 0.05D;
    }

    public static double masteryResistance(int coreRank) {
        return Math.min(0.75D, Math.max(0, coreRank) * 0.05D);
    }

    public static double masteryPowerMultiplier(AbilityElement element, int coreRank) {
        return element == AbilityElement.WIND ? 1.0D + masteryDamageBonus(coreRank) : 1.0D;
    }

    public static String summary(AbilityId id, int rank, PlayerSkills skills) {
        return "Damage " + fmt(damage(id, rank, 1.0D)) + " | Radius " + fmt(radius(id, rank, 1.0D)) + " | Duration " + fmt(durationTicks(id, rank, 1.0D) / 20.0D) + "s";
    }

    private static String fmt(double value) {
        if (Math.abs(value - Math.rint(value)) < 1e-9) return Integer.toString((int) Math.rint(value));
        String out = String.format(Locale.ROOT, "%.2f", value);
        while (out.contains(".") && (out.endsWith("0") || out.endsWith("."))) out = out.substring(0, out.length() - 1);
        return out;
    }
}

package net.revilodev.aura.abilities.logic;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.revilodev.aura.abilities.AbilitiesAttachments;
import net.revilodev.aura.abilities.AbilityId;
import net.revilodev.aura.abilities.PlayerAbilities;

// ability combat events
public final class AbilityCombatEvents {
    private AbilityCombatEvents() {}

    public static void register() {
        NeoForge.EVENT_BUS.addListener(AbilityCombatEvents::onIncomingDamage);
    }

    private static void onIncomingDamage(LivingIncomingDamageEvent event) {
        if (event.getEntity().level().isClientSide) return;
        if (!(event.getEntity() instanceof ServerPlayer target)) return;
        PlayerAbilities abilities = target.getData(AbilitiesAttachments.PLAYER_ABILITIES.get());
        int charges = abilities.activeTicks(AbilityId.FORCE_AEGIS);
        if (charges <= 0) return;

        abilities.setActiveTicks(AbilityId.FORCE_AEGIS, charges - 1);
        event.setAmount(0.0F);

        if (event.getSource().getEntity() instanceof LivingEntity attacker) {
            Vec3 away = attacker.position().subtract(target.position());
            Vec3 horiz = new Vec3(away.x, 0.0D, away.z);
            if (horiz.lengthSqr() > 1.0E-6D) {
                Vec3 kb = horiz.normalize().scale(0.7D);
                attacker.push(kb.x, 0.12D, kb.z);
                attacker.hurtMarked = true;
            }
        }

        if (target.level() instanceof ServerLevel level) {
            level.sendParticles(net.minecraft.core.particles.ParticleTypes.TOTEM_OF_UNDYING, target.getX(), target.getY() + 1.0D, target.getZ(), 12, 0.35D, 0.3D, 0.35D, 0.01D);
        }
    }
}

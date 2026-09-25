package net.revilodev.aura.abilities.logic;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.revilodev.aura.abilities.AbilitiesAttachments;
import net.revilodev.aura.abilities.AbilityElement;
import net.revilodev.aura.abilities.AbilityId;
import net.revilodev.aura.abilities.PlayerAbilities;
import net.revilodev.aura.effect.CodexMobEffects;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

// ability combat events
public final class AbilityCombatEvents {
    private static final Map<UUID, Float> DEFERRED_RAMPAGE_DAMAGE = new HashMap<>();

    private AbilityCombatEvents() {}

    public static void register() {
        NeoForge.EVENT_BUS.addListener(AbilityCombatEvents::onIncomingDamage);
    }

    private static void onIncomingDamage(LivingIncomingDamageEvent event) {
        if (event.getEntity().level().isClientSide) return;
        if (!(event.getEntity() instanceof ServerPlayer target)) return;
        PlayerAbilities abilities = target.getData(AbilitiesAttachments.PLAYER_ABILITIES.get());
        AbilityElement damageElement = AbilityLogic.currentDamageElement();
        ServerPlayer abilityAttacker = AbilityLogic.currentDamageAttacker();
        if (damageElement != null && abilityAttacker != null && abilityAttacker != target) {
            AbilityId mastery = AbilityId.valueOf(damageElement.name());
            double resistance = AbilityScaling.masteryResistance(abilities.rank(mastery));
            event.setAmount((float) (event.getAmount() * (1.0D - resistance)));
        }
        int charges = abilities.activeTicks(AbilityId.FORCE_AEGIS);
        if (charges > 0) {
            abilities.setActiveTicks(AbilityId.FORCE_AEGIS, charges - 1);
            event.setAmount(0.0F);

            if (event.getSource().getEntity() instanceof LivingEntity attacker) {
                Vec3 away = attacker.position().subtract(target.position());
                Vec3 horiz = new Vec3(away.x, 0.0D, away.z);
                if (horiz.lengthSqr() > 1.0E-6D) {
                    double strength = AbilityLogic.isFinalForm(abilities, AbilityId.FORCE_AEGIS) ? 1.3D : 0.7D;
                    Vec3 kb = horiz.normalize().scale(strength);
                    attacker.push(kb.x, 0.12D, kb.z);
                    attacker.hurtMarked = true;
                }
            }

            if (target.level() instanceof ServerLevel level) {
                level.sendParticles(net.minecraft.core.particles.ParticleTypes.TOTEM_OF_UNDYING, target.getX(), target.getY() + 1.0D, target.getZ(), 12, 0.35D, 0.3D, 0.35D, 0.01D);
            }
            return;
        }

        if (AbilityLogic.isFinalForm(abilities, AbilityId.FORCE_RAMPAGE) && target.hasEffect(CodexMobEffects.RAMPAGING)) {
            DEFERRED_RAMPAGE_DAMAGE.merge(target.getUUID(), event.getAmount(), Float::sum);
            event.setAmount(0.0F);
        }
    }

    public static void tickPlayer(ServerPlayer player) {
        Float damage = DEFERRED_RAMPAGE_DAMAGE.get(player.getUUID());
        if (damage == null) return;
        if (!player.isAlive()) {
            DEFERRED_RAMPAGE_DAMAGE.remove(player.getUUID());
            return;
        }
        if (player.hasEffect(CodexMobEffects.RAMPAGING)) return;

        DEFERRED_RAMPAGE_DAMAGE.remove(player.getUUID());
        player.setHealth(Math.max(1.0F, player.getHealth() - damage));
        if (player.level() instanceof ServerLevel level) {
            level.sendParticles(net.minecraft.core.particles.ParticleTypes.DAMAGE_INDICATOR, player.getX(), player.getY() + 1.0D, player.getZ(), 18, 0.4D, 0.45D, 0.4D, 0.02D);
        }
    }
}

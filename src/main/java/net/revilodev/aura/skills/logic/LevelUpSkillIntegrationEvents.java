package net.revilodev.aura.skills.logic;

import com.revilo.levelup.event.LevelUpLevelChangedEvent;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.revilodev.aura.skills.PlayerSkills;
import net.revilodev.aura.abilities.AbilitiesAttachments;
import net.revilodev.aura.abilities.AbilityConfig;
import net.revilodev.aura.abilities.PlayerAbilities;
import net.revilodev.aura.skills.SkillConfig;
import net.revilodev.aura.skills.SkillsAttachments;
import net.revilodev.aura.skills.SkillsNetwork;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

// level up skill integration events
public final class LevelUpSkillIntegrationEvents {
    private LevelUpSkillIntegrationEvents() {}

    private static final int TOAST_MERGE_TICKS = 5;
    private static final Map<UUID, PendingLevelToast> PENDING_TOASTS = new HashMap<>();

    public static void register() {
        NeoForge.EVENT_BUS.addListener(LevelUpSkillIntegrationEvents::onLevelStep);
        NeoForge.EVENT_BUS.addListener(LevelUpSkillIntegrationEvents::onServerTickPost);
    }

    private static void onLevelStep(LevelUpLevelChangedEvent.LevelUp event) {
        ServerPlayer player = event.getPlayer();
        int levelsGained = Math.max(0, event.getNewLevel() - event.getOldLevel());
        if (levelsGained <= 0) return;

        int skillPointsGained = levelsGained * Math.max(0, SkillConfig.pointsPerLevel());
        PlayerSkills skills = player.getData(SkillsAttachments.PLAYER_SKILLS.get());
        skills.adminAddPoints(skillPointsGained);
        SkillSyncEvents.markDirty(player);

        int interval = Math.max(1, AbilityConfig.pointIntervalLevels());
        int oldThresholds = Math.max(0, event.getOldLevel()) / interval;
        int newThresholds = Math.max(0, event.getNewLevel()) / interval;
        int abilityPointsGained = newThresholds - oldThresholds;

        queueLevelToast(player, event.getOldLevel(), event.getNewLevel(), skillPointsGained, abilityPointsGained);
    }

    private static void queueLevelToast(ServerPlayer player, int oldLevel, int newLevel, int skillPointsGained, int abilityPointsGained) {
        UUID id = player.getUUID();
        PendingLevelToast pending = PENDING_TOASTS.get(id);
        if (pending == null) {
            PENDING_TOASTS.put(id, new PendingLevelToast(oldLevel, newLevel, skillPointsGained, Math.max(0, abilityPointsGained), TOAST_MERGE_TICKS));
            return;
        }

        pending.oldLevel = Math.min(pending.oldLevel, oldLevel);
        pending.newLevel = Math.max(pending.newLevel, newLevel);
        pending.skillPointsGained += Math.max(0, skillPointsGained);
        pending.abilityPointsGained += Math.max(0, abilityPointsGained);
        pending.ticksRemaining = TOAST_MERGE_TICKS;
    }

    private static void onServerTickPost(ServerTickEvent.Post event) {
        if (PENDING_TOASTS.isEmpty()) return;

        MinecraftServer server = event.getServer();
        Iterator<Map.Entry<UUID, PendingLevelToast>> it = PENDING_TOASTS.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<UUID, PendingLevelToast> entry = it.next();
            PendingLevelToast pending = entry.getValue();
            pending.ticksRemaining--;
            if (pending.ticksRemaining > 0) continue;

            ServerPlayer player = server.getPlayerList().getPlayer(entry.getKey());
            if (player != null && pending.newLevel > pending.oldLevel) {
                SkillsNetwork.sendLevelUpToast(player, pending.oldLevel, pending.newLevel, pending.skillPointsGained, pending.abilityPointsGained);
            }
            it.remove();
        }
    }

    private static final class PendingLevelToast {
        int oldLevel;
        int newLevel;
        int skillPointsGained;
        int abilityPointsGained;
        int ticksRemaining;

        PendingLevelToast(int oldLevel, int newLevel, int skillPointsGained, int abilityPointsGained, int ticksRemaining) {
            this.oldLevel = oldLevel;
            this.newLevel = newLevel;
            this.skillPointsGained = Math.max(0, skillPointsGained);
            this.abilityPointsGained = Math.max(0, abilityPointsGained);
            this.ticksRemaining = ticksRemaining;
        }
    }
}

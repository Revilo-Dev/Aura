package net.revilodev.aura.skills.logic;

import com.revilo.levelup.event.LevelUpLevelChangedEvent;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.common.NeoForge;
import net.revilodev.aura.skills.PlayerSkills;
import net.revilodev.aura.abilities.AbilitiesAttachments;
import net.revilodev.aura.abilities.AbilityConfig;
import net.revilodev.aura.abilities.PlayerAbilities;
import net.revilodev.aura.skills.SkillConfig;
import net.revilodev.aura.skills.SkillsAttachments;
import net.revilodev.aura.skills.SkillsNetwork;

public final class LevelUpSkillIntegrationEvents {
    private LevelUpSkillIntegrationEvents() {}

    public static void register() {
        NeoForge.EVENT_BUS.addListener(LevelUpSkillIntegrationEvents::onLevelStep);
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

        SkillsNetwork.sendLevelUpToast(player, event.getOldLevel(), event.getNewLevel(), skillPointsGained, abilityPointsGained);
    }
}

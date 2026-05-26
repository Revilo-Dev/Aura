package net.revilodev.aura.abilities.logic;

import com.revilo.levelup.event.LevelUpLevelChangedEvent;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.common.NeoForge;
import net.revilodev.aura.abilities.AbilitiesAttachments;
import net.revilodev.aura.abilities.AbilityConfig;
import net.revilodev.aura.abilities.PlayerAbilities;

public final class AbilityLevelIntegrationEvents {
    private AbilityLevelIntegrationEvents() {}

    public static void register() {
        NeoForge.EVENT_BUS.addListener(AbilityLevelIntegrationEvents::onLevelStep);
    }

    private static void onLevelStep(LevelUpLevelChangedEvent.LevelUp event) {
        ServerPlayer player = event.getPlayer();
        int interval = Math.max(1, AbilityConfig.pointIntervalLevels());
        int oldThresholds = Math.max(0, event.getOldLevel()) / interval;
        int newThresholds = Math.max(0, event.getNewLevel()) / interval;
        int gained = sumTo(newThresholds) - sumTo(oldThresholds);
        if (gained <= 0) return;

        PlayerAbilities abilities = player.getData(AbilitiesAttachments.PLAYER_ABILITIES.get());
        abilities.addPoints(gained);
        AbilitySyncEvents.markDirty(player);
    }

    private static int sumTo(int n) {
        if (n <= 0) return 0;
        return (n * (n + 1)) / 2;
    }
}

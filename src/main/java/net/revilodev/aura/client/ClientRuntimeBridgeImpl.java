package net.revilodev.aura.client;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.revilodev.aura.client.screen.StandaloneSkillsBookScreen;
import net.revilodev.aura.client.toast.LevelUpToast;

@OnlyIn(Dist.CLIENT)
// client runtime bridge impl
public final class ClientRuntimeBridgeImpl {
    private ClientRuntimeBridgeImpl() {}

    public static Player getClientPlayer() {
        Minecraft mc = Minecraft.getInstance();
        return mc == null ? null : mc.player;
    }

    public static void openSkillsBook() {
        if (AuraClientConfig.blockOpenSkillsAbilitiesPanel()) {
            return;
        }

        Minecraft mc = Minecraft.getInstance();
        if (mc == null) {
            return;
        }

        mc.setScreen(new StandaloneSkillsBookScreen());
    }

    public static void afterSkillsSync() {
    }

    public static void showLevelUpToast(int oldLevel, int newLevel, int skillPointsGained, int abilityPointsGained) {
        if (newLevel <= oldLevel) {
            return;
        }
        LevelUpToast.show(oldLevel, newLevel, skillPointsGained, abilityPointsGained);
    }
}

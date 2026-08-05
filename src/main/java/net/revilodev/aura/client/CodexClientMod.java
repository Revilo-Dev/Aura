package net.revilodev.aura.client;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.revilodev.aura.client.abilities.AbilityHudOverlay;
import net.revilodev.aura.client.abilities.AbilityKeybinds;
import net.revilodev.aura.client.abilities.BurstCubeProjectileRenderer;
import net.revilodev.aura.CodexMod;
import net.revilodev.aura.client.skills.SkillsPanelClient;
import net.revilodev.aura.client.toast.LevelUpToast;
import net.revilodev.aura.entity.ModEntities;

@Mod(value = CodexMod.MOD_ID, dist = Dist.CLIENT)
public final class CodexClientMod {
    public CodexClientMod(IEventBus modBus) {
        // client config and screens
        AuraClientConfig.load();
        SkillsPanelClient.register();

        // keybinds and render hooks
        AbilityKeybinds.register(modBus);
        SkillsBookKeybinds.register(modBus);
        modBus.addListener(CodexClientMod::onRegisterRenderers);
        NeoForge.EVENT_BUS.addListener(AbilityHudOverlay::render);
        NeoForge.EVENT_BUS.addListener(EventPriority.LOWEST, false, RenderGuiEvent.Post.class, LevelUpToast::render);
    }

    private static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event) {
        // custom projectile renderer
        event.registerEntityRenderer(ModEntities.BURST_CUBE.get(), BurstCubeProjectileRenderer::new);
    }
}

package net.revilodev.aura.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.revilodev.aura.client.screen.StandaloneSkillsBookScreen;

@OnlyIn(Dist.CLIENT)
// skills book keybinds
public final class SkillsBookKeybinds {
    private static final String CATEGORY = "key.categories.aura";
    private static final KeyMapping OPEN_BOOK = new KeyMapping(
            "key.aura.open_book",
            InputConstants.KEY_RBRACKET,
            CATEGORY
    );
    private static boolean openBookWasDown = false;

    private SkillsBookKeybinds() {}

    public static void register(IEventBus modBus) {
        modBus.addListener(SkillsBookKeybinds::onRegisterKeyMappings);
        NeoForge.EVENT_BUS.addListener(SkillsBookKeybinds::onClientTick);
    }

    private static void onRegisterKeyMappings(RegisterKeyMappingsEvent event) {
        event.register(OPEN_BOOK);
    }

    private static void onClientTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;
        if (AuraClientConfig.blockOpenSkillsAbilitiesPanel()) return;
        boolean pressed = false;
        while (OPEN_BOOK.consumeClick()) {
            pressed = true;
        }
        boolean down = OPEN_BOOK.isDown();
        if (pressed || (down && !openBookWasDown)) {
            mc.setScreen(new StandaloneSkillsBookScreen());
        }
        openBookWasDown = down;
    }
}

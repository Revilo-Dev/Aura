package net.revilodev.aura.client;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.revilodev.aura.CodexMod;

// panel tab
public enum PanelTab {
    SKILLS("gui.aura.tab.skills", ResourceLocation.fromNamespaceAndPath(CodexMod.MOD_ID, "textures/gui/skills.png")),
    ABILITIES("gui.aura.tab.abilities", ResourceLocation.fromNamespaceAndPath(CodexMod.MOD_ID, "textures/gui/abilities.png"));

    private final String titleKey;
    private final ResourceLocation iconTexture;

    PanelTab(String titleKey, ResourceLocation iconTexture) {
        this.titleKey = titleKey;
        this.iconTexture = iconTexture;
    }

    public String title() {
        return Component.translatable(titleKey).getString();
    }

    public ResourceLocation iconTexture() {
        return iconTexture;
    }
}

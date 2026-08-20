package net.revilodev.aura.client.screen;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

// disabled skills abilities screen
public final class DisabledSkillsAbilitiesScreen extends Screen {
    private final Screen parent;

    public DisabledSkillsAbilitiesScreen(Screen parent) {
        super(Component.translatable("gui.aura.disabled_skills_abilities.title"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        int w = 140;
        int h = 20;
        addRenderableWidget(Button.builder(Component.translatable("gui.aura.back"), b -> onClose())
                .bounds((width - w) / 2, height - 35, w, h)
                .build());
    }

    @Override
    public void render(GuiGraphics gg, int mouseX, int mouseY, float partialTick) {
        renderBackground(gg, mouseX, mouseY, partialTick);
        gg.drawCenteredString(font, title, width / 2, 24, 0xFFFFFF);
        gg.drawCenteredString(font, Component.translatable("gui.aura.disabled_skills_abilities.coming_soon"), width / 2, 54, 0xCFCFCF);
        super.render(gg, mouseX, mouseY, partialTick);
    }

    @Override
    public void onClose() {
        minecraft.setScreen(parent);
    }
}

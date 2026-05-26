package net.revilodev.aura.client.screen;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public final class DisabledSkillsAbilitiesScreen extends Screen {
    private final Screen parent;

    public DisabledSkillsAbilitiesScreen(Screen parent) {
        super(Component.literal("Disabled Skills and Abilities"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        int w = 140;
        int h = 20;
        addRenderableWidget(Button.builder(Component.literal("Back"), b -> onClose())
                .bounds((width - w) / 2, height - 35, w, h)
                .build());
    }

    @Override
    public void render(GuiGraphics gg, int mouseX, int mouseY, float partialTick) {
        renderBackground(gg, mouseX, mouseY, partialTick);
        gg.drawCenteredString(font, title, width / 2, 24, 0xFFFFFF);
        gg.drawCenteredString(font, "Specific skill/ability disable controls coming next.", width / 2, 54, 0xCFCFCF);
        super.render(gg, mouseX, mouseY, partialTick);
    }

    @Override
    public void onClose() {
        minecraft.setScreen(parent);
    }
}

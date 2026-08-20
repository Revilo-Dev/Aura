package net.revilodev.aura.client;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.revilodev.aura.CodexMod;

/** Compact back tab used by the player and settings pages. */
public final class SettingsBackTabButton extends AbstractButton {
    private static final ResourceLocation NORMAL = texture("textures/gui/sprites/editor/back_tab.png");
    private static final ResourceLocation HOVERED = texture("textures/gui/sprites/editor/back_tab-hovered.png");

    private final Runnable action;

    public SettingsBackTabButton(int x, int y, Runnable action) {
        super(x, y, 27, 17, Component.translatable("gui.aura.back"));
        this.action = action;
    }

    @Override
    public void onPress() {
        action.run();
    }

    @Override
    protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        ResourceLocation texture = isMouseOver(mouseX, mouseY) ? HOVERED : NORMAL;
        graphics.blit(texture, getX(), getY(), 0, 0, width, height, width, height);
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narration) {}

    private static ResourceLocation texture(String path) {
        return ResourceLocation.fromNamespaceAndPath(CodexMod.MOD_ID, path);
    }
}

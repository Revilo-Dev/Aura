package net.revilodev.aura.client;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public final class BottomPullTabButton extends AbstractButton {
    private final ResourceLocation baseTexture;
    private final ResourceLocation pulledTexture;
    private final ResourceLocation selectedTexture;
    private final Runnable onPress;
    private boolean selected;

    public BottomPullTabButton(int x, int y, Component label, ResourceLocation baseTexture, ResourceLocation pulledTexture, ResourceLocation selectedTexture, Runnable onPress) {
        super(x, y, 32, 32, label);
        this.baseTexture = baseTexture;
        this.pulledTexture = pulledTexture;
        this.selectedTexture = selectedTexture;
        this.onPress = onPress;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    @Override
    public void onPress() {
        if (onPress != null) onPress.run();
    }

    @Override
    protected void renderWidget(GuiGraphics gg, int mouseX, int mouseY, float partialTick) {
        ResourceLocation texture;
        if (selected) {
            texture = selectedTexture != null ? selectedTexture : pulledTexture;
        } else if (isHovered()) {
            texture = pulledTexture != null ? pulledTexture : baseTexture;
        } else {
            texture = baseTexture;
        }
        gg.blit(texture, getX(), getY(), 0, 0, width, height, width, height);
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narration) {}
}

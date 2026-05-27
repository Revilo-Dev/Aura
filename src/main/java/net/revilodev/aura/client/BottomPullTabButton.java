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
    private static final int TAB_WIDTH = 32;
    private static final int NORMAL_HEIGHT = 32;
    private static final int PULLED_HEIGHT = 35;
    private final ResourceLocation baseTexture;
    private final ResourceLocation pulledTexture;
    private final ResourceLocation selectedTexture;
    private final Runnable onPress;
    private boolean selected;

    public BottomPullTabButton(int x, int y, Component label, ResourceLocation baseTexture, ResourceLocation pulledTexture, ResourceLocation selectedTexture, Runnable onPress) {
        super(x, y, TAB_WIDTH, NORMAL_HEIGHT, label);
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
        int drawY = getY();
        int drawH = NORMAL_HEIGHT;
        int texH = NORMAL_HEIGHT;
        if (selected) {
            texture = selectedTexture != null ? selectedTexture : pulledTexture;
            drawY -= 2;
            drawH = PULLED_HEIGHT;
            texH = PULLED_HEIGHT;
        } else if (isHovered()) {
            texture = baseTexture;
            drawY -= 2;
            drawH = NORMAL_HEIGHT;
            texH = NORMAL_HEIGHT;
        } else {
            texture = baseTexture;
        }
        gg.blit(texture, getX(), drawY, 0, 0, width, drawH, width, texH);
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narration) {}
}

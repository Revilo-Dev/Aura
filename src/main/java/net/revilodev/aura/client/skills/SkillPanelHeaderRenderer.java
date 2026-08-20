package net.revilodev.aura.client.skills;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.revilodev.aura.CodexMod;

// three slice header
public final class SkillPanelHeaderRenderer {
    private static final ResourceLocation HEADER_TEX =
            ResourceLocation.fromNamespaceAndPath(CodexMod.MOD_ID, "textures/gui/3-slice-header.png");

    private static final int TEXTURE_WIDTH = 72;
    private static final int TEXTURE_HEIGHT = 10;
    private static final int SLICE_WIDTH = 3;
    private static final float TEXT_SCALE = 0.9F;
    private static final int HORIZONTAL_PADDING = 3;
    private static final int TEXT_COLOR = 0x3E3E3E;
    private static final int TEXT_OFFSET_Y = 4;

    private SkillPanelHeaderRenderer() {}

    public static int height() {
        return TEXTURE_HEIGHT;
    }

    public static int width(Font font, String text) {
        // text plus side caps
        int textWidth = Math.max(0, (int) Math.ceil(font.width(text) * TEXT_SCALE));
        return Math.max((SLICE_WIDTH * 2) + 1, textWidth + HORIZONTAL_PADDING * 2);
    }

    public static void draw(GuiGraphics gg, Font font, int x, int y, String text) {
        // center stretch area
        int width = width(font, text);
        int middleWidth = Math.max(0, width - (SLICE_WIDTH * 2));

        gg.blit(HEADER_TEX, x, y, 0, 0, SLICE_WIDTH, TEXTURE_HEIGHT, TEXTURE_WIDTH, TEXTURE_HEIGHT);
        if (middleWidth > 0) {
            gg.pose().pushPose();
            gg.pose().translate(x + SLICE_WIDTH, y, 0.0F);
            gg.pose().scale(middleWidth / 66.0F, 1.0F, 1.0F);
            gg.blit(HEADER_TEX, 0, 0, SLICE_WIDTH, 0, 66, TEXTURE_HEIGHT, TEXTURE_WIDTH, TEXTURE_HEIGHT);
            gg.pose().popPose();
        }
        gg.blit(HEADER_TEX, x + width - SLICE_WIDTH, y, TEXTURE_WIDTH - SLICE_WIDTH, 0, SLICE_WIDTH, TEXTURE_HEIGHT, TEXTURE_WIDTH, TEXTURE_HEIGHT);

        // scaled title
        int textX = x + HORIZONTAL_PADDING;
        int textY = y + TEXT_OFFSET_Y;

        gg.pose().pushPose();
        gg.pose().translate(textX, textY, 0.0F);
        gg.pose().scale(TEXT_SCALE, TEXT_SCALE, 1.0F);
        gg.drawString(font, text, 0, 0, TEXT_COLOR, false);
        gg.pose().popPose();
    }
}

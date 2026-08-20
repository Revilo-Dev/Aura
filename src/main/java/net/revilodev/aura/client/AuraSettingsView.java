package net.revilodev.aura.client;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.revilodev.aura.CodexMod;

import java.util.List;

/** Boundless-style settings surface shared by the inventory and book screens. */
public final class AuraSettingsView {
    private static final ResourceLocation ROW_TEX = texture("textures/gui/sprites/settings_widget.png");
    private static final ResourceLocation TAB_TEX = texture("textures/gui/sprites/tab.png");
    private static final ResourceLocation TAB_SELECTED_TEX = texture("textures/gui/sprites/tab_selected.png");
    private static final ResourceLocation UI_ICON = texture("textures/gui/icon/ui-icon.png");
    private static final ResourceLocation FEATURES_ICON = texture("textures/gui/icon/features-icon.png");
    private static final ResourceLocation STYLE_ICON = texture("textures/gui/icon/style-icon.png");
    private static final ResourceLocation LEVEL_UP_ICON = texture("textures/gui/skills.png");
    private static final ResourceLocation ABILITIES_ICON = texture("textures/gui/abilities.png");

    private static final int PANEL_W = 147;
    private static final int PANEL_H = 166;
    private static final int CONTENT_X = 8;
    private static final int CONTENT_Y = 8;
    private static final int CONTENT_W = 130;
    private static final int CONTENT_H = 149;
    private static final int ROW_H = 20;
    private static final int ROW_PITCH = 21;
    private static final int TAB_W = 35;
    private static final int TAB_H = 27;
    private static final int TAB_GAP = 1;
    private static final int TAB_START_Y = 1;

    private final AuraBookSettingsModel model;
    private AuraBookSettingsModel.SettingsCategory category = AuraBookSettingsModel.SettingsCategory.UI;
    private List<AuraBookSettingsModel.Row> rows = List.of();
    private int scroll;
    private boolean scrollbarDragging;
    private Component pendingTooltip;

    public AuraSettingsView(AuraBookSettingsModel model) {
        this.model = model;
        refreshRows();
    }

    public Component title() {
        return Component.translatable("gui.aura.book.title");
    }

    public void render(GuiGraphics graphics, int panelX, int panelY, int mouseX, int mouseY) {
        int contentX = panelX + CONTENT_X;
        int contentY = panelY + CONTENT_Y;
        int bottom = contentY + CONTENT_H;
        pendingTooltip = null;

        graphics.enableScissor(contentX, contentY, contentX + CONTENT_W, bottom);
        for (int index = 0; index < rows.size(); index++) {
            int rowY = contentY + 2 + index * ROW_PITCH - scroll;
            if (rowY + ROW_H <= contentY || rowY >= bottom) continue;
            AuraBookSettingsModel.Row row = rows.get(index);
            boolean hovered = mouseX >= contentX && mouseX < contentX + CONTENT_W && mouseY >= rowY && mouseY < rowY + ROW_H;
            renderRow(graphics, row, contentX, rowY, hovered);
            if (hovered) pendingTooltip = Component.literal(row.label());
        }
        graphics.disableScissor();

        for (int index = 0; index < AuraBookSettingsModel.SettingsCategory.values().length; index++) {
            AuraBookSettingsModel.SettingsCategory tab = AuraBookSettingsModel.SettingsCategory.values()[index];
            int tabX = panelX - 31;
            int tabY = panelY + TAB_START_Y + index * (TAB_H + TAB_GAP);
            boolean selected = category == tab;
            int renderX = tabX + 1 - (selected ? 1 : 0);
            graphics.blit(selected ? TAB_SELECTED_TEX : TAB_TEX, renderX, tabY, 0, 0, TAB_W, TAB_H, TAB_W, TAB_H);
            ResourceLocation icon = iconFor(tab);
            int iconSize = 16;
            graphics.blit(icon, renderX + (TAB_W - iconSize) / 2, tabY + (TAB_H - iconSize) / 2, 0, 0, iconSize, iconSize, iconSize, iconSize);
            if (mouseX >= tabX && mouseX < tabX + TAB_W && mouseY >= tabY && mouseY < tabY + TAB_H) pendingTooltip = tab.title();
        }

        renderScrollbar(graphics, contentX, contentY);
    }

    /** Render last, outside the viewport scissor and above the containing screen's widgets. */
    public void renderTooltip(GuiGraphics graphics, int mouseX, int mouseY) {
        if (pendingTooltip == null) return;
        graphics.pose().pushPose();
        graphics.pose().translate(0.0F, 0.0F, 1000.0F);
        graphics.renderTooltip(Minecraft.getInstance().font, pendingTooltip, mouseX, mouseY);
        graphics.pose().popPose();
    }

    public boolean mouseClicked(int panelX, int panelY, double mouseX, double mouseY, int button) {
        if (button != 0 && button != 1) return false;
        int contentY = panelY + CONTENT_Y;
        for (int index = 0; index < AuraBookSettingsModel.SettingsCategory.values().length; index++) {
            int tabX = panelX - 31;
            int tabY = panelY + TAB_START_Y + index * (TAB_H + TAB_GAP);
            if (mouseX >= tabX && mouseX < tabX + TAB_W && mouseY >= tabY && mouseY < tabY + TAB_H) {
                if (button == 0 && category != AuraBookSettingsModel.SettingsCategory.values()[index]) {
                    category = AuraBookSettingsModel.SettingsCategory.values()[index];
                    scroll = 0;
                    refreshRows();
                }
                return true;
            }
        }

        if (button == 0 && isOverScrollbar(panelX, panelY, mouseX, mouseY) && maxScroll() > 0) {
            scrollbarDragging = true;
            setScrollFromMouse(panelY, mouseY);
            return true;
        }

        int contentX = panelX + CONTENT_X;
        int bottom = contentY + CONTENT_H;
        if (mouseX < contentX || mouseX >= contentX + CONTENT_W || mouseY < contentY || mouseY >= bottom) return false;
        int index = (int) ((mouseY - contentY - 2 + scroll) / ROW_PITCH);
        if (index >= 0 && index < rows.size()) return model.clickRow(index, button);
        return false;
    }

    public boolean mouseScrolled(int panelX, int panelY, double mouseX, double mouseY, double scrollY) {
        int contentX = panelX + CONTENT_X;
        int contentY = panelY + CONTENT_Y;
        if (mouseX < contentX || mouseX >= contentX + CONTENT_W || mouseY < contentY || mouseY >= contentY + CONTENT_H) return false;
        scroll = Math.max(0, Math.min(maxScroll(), scroll - (int) Math.signum(scrollY) * 12));
        return true;
    }

    public boolean mouseDragged(int panelY, double mouseY, int button) {
        if (!scrollbarDragging || button != 0) return false;
        setScrollFromMouse(panelY, mouseY);
        return true;
    }

    public boolean mouseReleased(int button) {
        if (button != 0 || !scrollbarDragging) return false;
        scrollbarDragging = false;
        return true;
    }

    public boolean keyPressed(int keyCode) {
        return model.keyPressed(keyCode);
    }

    public boolean charTyped(char codePoint) {
        return model.charTyped(codePoint);
    }

    private void refreshRows() {
        model.rebuild(category);
        rows = model.rows();
    }

    private int contentHeight() {
        return rows.size() * ROW_PITCH;
    }

    private int maxScroll() {
        return Math.max(0, contentHeight() - CONTENT_H + 2);
    }

    private boolean isOverScrollbar(int panelX, int panelY, double mouseX, double mouseY) {
        int contentX = panelX + CONTENT_X;
        int contentY = panelY + CONTENT_Y;
        return mouseX >= contentX + CONTENT_W + 2 && mouseX <= contentX + CONTENT_W + 8 && mouseY >= contentY && mouseY <= contentY + CONTENT_H;
    }

    private void setScrollFromMouse(int panelY, double mouseY) {
        int max = maxScroll();
        int thumbH = thumbHeight();
        double t = (mouseY - (panelY + CONTENT_Y) - thumbH / 2.0D) / Math.max(1.0D, CONTENT_H - thumbH);
        scroll = Math.max(0, Math.min(max, (int) Math.round(t * max)));
    }

    private int thumbHeight() {
        return Math.max(12, CONTENT_H * CONTENT_H / Math.max(CONTENT_H, contentHeight()));
    }

    private void renderScrollbar(GuiGraphics graphics, int contentX, int contentY) {
        int max = maxScroll();
        if (max <= 0) return;
        int thumbH = thumbHeight();
        int thumbY = contentY + Math.round((CONTENT_H - thumbH) * (scroll / (float) max));
        graphics.fill(contentX + CONTENT_W + 4, thumbY, contentX + CONTENT_W + 6, thumbY + thumbH, 0xFF909090);
    }

    private void renderRow(GuiGraphics graphics, AuraBookSettingsModel.Row row, int x, int y, boolean hovered) {
        boolean plainSection = row.style() == AuraBookSettingsModel.RowStyle.PLAIN_SECTION;
        if (!plainSection) {
            RenderSystem.enableBlend();
            if (hovered) RenderSystem.setShaderColor(1.1F, 1.1F, 1.1F, 1.0F);
            graphics.blit(ROW_TEX, x, y, 0, 0, CONTENT_W, ROW_H, CONTENT_W, ROW_H);
            if (hovered) RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        }

        var font = Minecraft.getInstance().font;
        String value = model.stateText(row);
        if (value == null) value = "";
        int valueWidth = Math.round(font.width(value) * 0.72F);
        int valueX = x + CONTENT_W - valueWidth - 5;
        String label = row.label();
        int labelX = x + 7;
        if (row.icon() != null) {
            int iconSize = 16;
            graphics.blit(row.icon(), labelX, y + (ROW_H - iconSize) / 2, 0, 0, iconSize, iconSize, 16, 16);
            labelX += iconSize + 3;
        }
        int maxLabelWidth = valueX - labelX + 9;
        if (font.width(label) > maxLabelWidth) label = font.plainSubstrByWidth(label, Math.max(0, maxLabelWidth - font.width("..."))) + "...";
        boolean mastery = row.style() == AuraBookSettingsModel.RowStyle.MASTERY && row.icon() != null;
        int labelColor = plainSection || row.style() == AuraBookSettingsModel.RowStyle.SECTION ? 0xFFE08A : (mastery ? 0xFFFFFF55 : 0xFFFFFF);
        drawScaled(graphics, label, 0.82F, labelX, y + 5, labelColor, mastery);
        drawScaled(graphics, value, 0.72F, valueX, y + 7, 0xA0C8FF);
    }

    private static void drawScaled(GuiGraphics graphics, String text, float scale, int x, int y, int color) {
        drawScaled(graphics, text, scale, x, y, color, false);
    }

    private static void drawScaled(GuiGraphics graphics, String text, float scale, int x, int y, int color, boolean bold) {
        if (text == null || text.isEmpty()) return;
        graphics.pose().pushPose();
        graphics.pose().scale(scale, scale, 1.0F);
        float inverse = 1.0F / scale;
        graphics.drawString(Minecraft.getInstance().font, text, (int) (x * inverse), (int) (y * inverse), color, false);
        if (bold) graphics.drawString(Minecraft.getInstance().font, text, (int) (x * inverse) + 1, (int) (y * inverse), color, false);
        graphics.pose().popPose();
    }

    private static ResourceLocation iconFor(AuraBookSettingsModel.SettingsCategory category) {
        return switch (category) {
            case UI -> UI_ICON;
            case FEATURES -> FEATURES_ICON;
            case STYLE -> STYLE_ICON;
            case LEVEL_UP -> LEVEL_UP_ICON;
            case ABILITIES -> ABILITIES_ICON;
        };
    }

    private static ResourceLocation texture(String path) {
        return ResourceLocation.fromNamespaceAndPath(CodexMod.MOD_ID, path);
    }
}

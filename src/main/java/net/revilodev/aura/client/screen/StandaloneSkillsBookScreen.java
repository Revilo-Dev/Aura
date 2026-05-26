package net.revilodev.aura.client.screen;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.revilodev.aura.CodexMod;
import net.revilodev.aura.client.AuraClientConfig;
import net.revilodev.aura.client.BottomPullTabButton;
import net.revilodev.aura.client.PanelTab;
import net.revilodev.aura.client.PanelTabButton;
import net.revilodev.aura.client.abilities.AbilityDetailsPanel;
import net.revilodev.aura.client.abilities.AbilityListWidget;
import net.revilodev.aura.client.skills.SkillDetailsPanel;
import net.revilodev.aura.client.skills.SkillListWidget;
import net.revilodev.aura.client.skills.SkillPanelHeaderRenderer;

import java.util.ArrayList;
import java.util.List;

@OnlyIn(Dist.CLIENT)
public final class StandaloneSkillsBookScreen extends Screen {
    private static final ResourceLocation PANEL_TEX =
            ResourceLocation.fromNamespaceAndPath(CodexMod.MOD_ID, "textures/gui/skills_panel.png");
    private static final ResourceLocation PLAYER_TAB_TEX =
            ResourceLocation.fromNamespaceAndPath(CodexMod.MOD_ID, "textures/gui/sprites/pull-tab-player.png");
    private static final ResourceLocation PLAYER_TAB_TEX_PULLED =
            ResourceLocation.fromNamespaceAndPath(CodexMod.MOD_ID, "textures/gui/sprites/pull-tab-player-pulled.png");
    private static final ResourceLocation PLAYER_TAB_TEX_SELECTED =
            ResourceLocation.fromNamespaceAndPath(CodexMod.MOD_ID, "textures/gui/sprites/pull-tab-player-selected.png");
    private static final ResourceLocation SETTINGS_TAB_TEX =
            ResourceLocation.fromNamespaceAndPath(CodexMod.MOD_ID, "textures/gui/sprites/pull-tab-settings.png");
    private static final ResourceLocation SETTINGS_TAB_TEX_PULLED =
            ResourceLocation.fromNamespaceAndPath(CodexMod.MOD_ID, "textures/gui/sprites/pull-tab-settings-pulled.png");
    private static final ResourceLocation SETTINGS_TAB_TEX_PULLED_ALT =
            ResourceLocation.fromNamespaceAndPath(CodexMod.MOD_ID, "textures/gui/sprites/pull-tab-settings_pulled.png");

    private final int panelWidth = 147;
    private final int panelHeight = 166;
    private int panelX;
    private int panelY;

    private SkillListWidget skillsList;
    private SkillDetailsPanel skillsDetails;
    private AbilityListWidget abilityList;
    private AbilityDetailsPanel abilityDetails;
    private PanelTabButton skillsTab;
    private PanelTabButton abilitiesTab;
    private PanelTab activeTab = PanelTab.SKILLS;

    private BottomPullTabButton playerBottomTab;
    private BottomPullTabButton settingsBottomTab;
    private ViewMode viewMode = ViewMode.MAIN;

    private final List<SettingRow> settingsRows = new ArrayList<>();
    private int settingsScroll = 0;
    private static final int SETTINGS_ROW_H = 22;
    private static final int SETTINGS_VIEW_PAD_X = 6;
    private static final int SETTINGS_VIEW_TOP = 18;
    private static final int SETTINGS_VIEW_BOTTOM_PAD = 12;

    private static final int INNER_PAD_X = 6;
    private static final int INNER_PAD_TOP = 5;
    private static final int INNER_PAD_BOTTOM = 6;
    private static final int HEADER_OFFSET_X = 5;
    private static final int HEADER_OFFSET_Y = 3;

    private enum ViewMode {
        MAIN,
        PLAYER,
        SETTINGS
    }

    public StandaloneSkillsBookScreen() {
        super(Component.literal("aura"));
    }

    @Override
    protected void init() {
        panelX = this.width / 2 - panelWidth / 2;
        panelY = this.height / 2 - panelHeight / 2;

        int innerLeft = panelX + INNER_PAD_X;
        int innerRight = panelX + panelWidth - INNER_PAD_X;
        int innerTop = panelY + INNER_PAD_TOP;
        int innerBottom = panelY + panelHeight - INNER_PAD_BOTTOM;
        int detailsH = panelHeight / 3 + 10;
        int detailsW = Math.max(20, (innerRight - innerLeft) - 5);
        int detailsX = innerLeft + 2;
        int detailsY = innerBottom - detailsH - 5;

        skillsList = new SkillListWidget(panelX + (panelWidth - SkillListWidget.gridWidth()) / 2, innerTop, SkillListWidget.gridWidth(), SkillListWidget.preferredHeight(), def -> {
            skillsDetails.setSkill(def);
            skillsList.setSelected(def == null ? null : def.id());
        });
        skillsList.reloadSkills();
        skillsDetails = new SkillDetailsPanel(detailsX, detailsY, detailsW, detailsH);

        abilityList = new AbilityListWidget(panelX + (panelWidth - AbilityListWidget.gridWidth()) / 2, innerTop, AbilityListWidget.gridWidth(), AbilityListWidget.preferredHeight(), def -> {
            abilityDetails.setAbility(def);
            abilityList.setSelected(def == null ? null : def.id());
        });
        abilityList.setViewportTweaks(0, 0);
        abilityList.setShowLocked(true);
        abilityList.setHeaderTextOffsetX(0);
        abilityDetails = new AbilityDetailsPanel(detailsX, detailsY + 3, detailsW, detailsH);
        abilityDetails.setContentTopOffset(3);

        skillsTab = new PanelTabButton(panelX - 31, panelY + 6, PanelTab.SKILLS, () -> setTab(PanelTab.SKILLS));
        abilitiesTab = new PanelTabButton(panelX - 31, panelY + 34, PanelTab.ABILITIES, () -> setTab(PanelTab.ABILITIES));

        int bottomY = panelY + panelHeight - 3;
        int bottomX = panelX + 7;
        playerBottomTab = new BottomPullTabButton(bottomX, bottomY, Component.literal("Player"), PLAYER_TAB_TEX, PLAYER_TAB_TEX_PULLED, PLAYER_TAB_TEX_SELECTED, () -> setViewMode(ViewMode.PLAYER));
        settingsBottomTab = new BottomPullTabButton(bottomX + 32 + 2, bottomY, Component.literal("Settings"), SETTINGS_TAB_TEX, SETTINGS_TAB_TEX_PULLED_ALT, SETTINGS_TAB_TEX_PULLED_ALT, () -> setViewMode(ViewMode.SETTINGS));
        playerBottomTab.setPosition(bottomX - 3, bottomY);
        settingsBottomTab.setPosition((bottomX - 3) + 32 + 2, bottomY);

        addRenderableWidget(skillsList);
        addRenderableWidget(skillsDetails);
        addRenderableWidget(skillsDetails.upgradeButton());
        addRenderableWidget(skillsDetails.downgradeButton());
        addRenderableWidget(abilityList);
        addRenderableWidget(abilityDetails);
        addRenderableWidget(abilityDetails.upgradeButton());
        addRenderableWidget(abilityDetails.downgradeButton());
        addRenderableWidget(abilityDetails.selectButton());
        addRenderableWidget(skillsTab);
        addRenderableWidget(abilitiesTab);
        addRenderableWidget(playerBottomTab);
        addRenderableWidget(settingsBottomTab);

        initSettingsRows();
        setViewMode(ViewMode.MAIN);
        setTab(activeTab);
    }

    private void initSettingsRows() {
        settingsRows.clear();
        settingsRows.add(new SettingRow("Hud display", () -> AuraClientConfig.hudDisplayEnabled() ? "Enabled" : "Disabled", AuraClientConfig::toggleHudDisplayEnabled));
        settingsRows.add(new SettingRow("Disable aura book", () -> AuraClientConfig.disableCodexBook() ? "True" : "False", AuraClientConfig::toggleDisableCodexBook));
        settingsRows.add(new SettingRow("Disable inventory aura book", () -> AuraClientConfig.disableInventoryCodexBook() ? "True" : "False", AuraClientConfig::toggleDisableInventoryCodexBook));
        settingsRows.add(new SettingRow("Reposition hud display", () -> AuraClientConfig.hudPosition().name().toLowerCase(java.util.Locale.ROOT).replace('_', ' '), AuraClientConfig::cycleHudPosition));
        settingsRows.add(new SettingRow("Disable skills and abilities", () -> AuraClientConfig.disableSkillsAndAbilities() ? "True" : "False", AuraClientConfig::toggleDisableSkillsAndAbilities));
        settingsRows.add(new SettingRow("Disabled Skills & Abilities...", () -> "Open", () -> minecraft.setScreen(new DisabledSkillsAbilitiesScreen(this))));
    }

    @Override
    public void renderBackground(GuiGraphics gg, int mouseX, int mouseY, float partialTick) {}

    @Override
    public void render(GuiGraphics gg, int mouseX, int mouseY, float partialTick) {
        gg.fill(0, 0, this.width, this.height, 0xA0000000);
        gg.blit(PANEL_TEX, panelX, panelY, 0, 0, panelWidth, panelHeight, panelWidth, panelHeight);
        SkillPanelHeaderRenderer.draw(
                gg,
                Minecraft.getInstance().font,
                panelX + HEADER_OFFSET_X,
                panelY - SkillPanelHeaderRenderer.height() + HEADER_OFFSET_Y,
                viewMode == ViewMode.SETTINGS ? "Settings" : (viewMode == ViewMode.PLAYER ? "Player" : activeTab.title())
        );
        if (viewMode == ViewMode.PLAYER) {
            gg.drawCenteredString(font, "Player tab coming soon.", panelX + panelWidth / 2, panelY + panelHeight / 2 - 4, 0xE0E0E0);
        }
        if (viewMode == ViewMode.SETTINGS) {
            renderSettingsRows(gg, mouseX, mouseY);
        }
        super.render(gg, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (viewMode == ViewMode.SETTINGS && isInSettingsView(mouseX, mouseY)) {
            int maxScroll = Math.max(0, settingsRows.size() * SETTINGS_ROW_H - settingsViewHeight());
            settingsScroll = Math.max(0, Math.min(maxScroll, settingsScroll - (int) Math.signum(scrollY) * 14));
            return true;
        }
        if (viewMode == ViewMode.MAIN) {
            if (activeTab == PanelTab.SKILLS && skillsDetails != null && skillsDetails.mouseScrolled(mouseX, mouseY, scrollX, scrollY)) {
                return true;
            }
            if (activeTab == PanelTab.ABILITIES && abilityList != null && abilityList.mouseScrolled(mouseX, mouseY, scrollY)) {
                return true;
            }
            if (activeTab == PanelTab.ABILITIES && abilityDetails != null && abilityDetails.mouseScrolled(mouseX, mouseY, scrollX, scrollY)) {
                return true;
            }
        }
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0 && (playerBottomTab.mouseClicked(mouseX, mouseY, button) || settingsBottomTab.mouseClicked(mouseX, mouseY, button))) {
            return true;
        }

        if (viewMode == ViewMode.SETTINGS || viewMode == ViewMode.PLAYER) {
            if (viewMode == ViewMode.SETTINGS && button == 0 && isInSettingsView(mouseX, mouseY)) {
                int index = (int) ((mouseY - settingsViewY() + settingsScroll) / SETTINGS_ROW_H);
                if (index >= 0 && index < settingsRows.size()) {
                    settingsRows.get(index).onClick.run();
                    return true;
                }
            }
            if (isInsidePanelOrTabs(mouseX, mouseY)) return super.mouseClicked(mouseX, mouseY, button);
            return false;
        }

        if (button == 1 && activeTab == PanelTab.SKILLS) {
            return skillsList.mouseClicked(mouseX, mouseY, button) || super.mouseClicked(mouseX, mouseY, button);
        }
        if (button == 0) {
            if (skillsTab.mouseClicked(mouseX, mouseY, button) || abilitiesTab.mouseClicked(mouseX, mouseY, button)) {
                return true;
            }
            if (isInsidePanelOrTabs(mouseX, mouseY)) {
                if (activeTab == PanelTab.SKILLS && skillsDetails.hasSkill()) {
                    boolean inListNode = skillsList.isOnSkillNode(mouseX, mouseY);
                    boolean onButton = skillsDetails.isOnButtons(mouseX, mouseY);
                    boolean inDetails = skillsDetails.containsPoint(mouseX, mouseY);
                    boolean used = skillsDetails.mouseClicked(mouseX, mouseY, button) || skillsList.mouseClicked(mouseX, mouseY, button);
                    if (!used && !inListNode && !onButton && !inDetails) {
                        skillsDetails.setSkill(null);
                        skillsList.setSelected(null);
                    }
                    return true;
                }
                if (activeTab == PanelTab.ABILITIES && abilityDetails.hasAbility()) {
                    boolean inListNode = abilityList.isOnAbilityNode(mouseX, mouseY);
                    boolean onButton = abilityDetails.isOnButtons(mouseX, mouseY);
                    boolean inDetails = abilityDetails.containsPoint(mouseX, mouseY);
                    boolean used = abilityDetails.mouseClicked(mouseX, mouseY, button) || abilityList.mouseClicked(mouseX, mouseY, button);
                    if (!used && !inListNode && !onButton && !inDetails) {
                        abilityDetails.setAbility(null);
                        abilityList.setSelected(null);
                    }
                    return true;
                }
                if (activeTab == PanelTab.SKILLS) {
                    skillsDetails.mouseClicked(mouseX, mouseY, button);
                    skillsList.mouseClicked(mouseX, mouseY, button);
                    return true;
                }
                abilityDetails.mouseClicked(mouseX, mouseY, button);
                abilityList.mouseClicked(mouseX, mouseY, button);
                return true;
            }

            boolean used = false;
            if (activeTab == PanelTab.SKILLS && skillsDetails.hasSkill()) {
                used = skillsDetails.mouseClicked(mouseX, mouseY, button) || skillsList.mouseClicked(mouseX, mouseY, button);
                boolean inListNode = skillsList.isOnSkillNode(mouseX, mouseY);
                boolean onButton = skillsDetails.isOnButtons(mouseX, mouseY);
                boolean inDetails = skillsDetails.containsPoint(mouseX, mouseY);
                if (!used && !inListNode && !onButton && !inDetails) {
                    skillsDetails.setSkill(null);
                    skillsList.setSelected(null);
                }
                return used;
            } else if (activeTab == PanelTab.ABILITIES && abilityDetails.hasAbility()) {
                used = abilityDetails.mouseClicked(mouseX, mouseY, button) || abilityList.mouseClicked(mouseX, mouseY, button);
                boolean inListNode = abilityList.isOnAbilityNode(mouseX, mouseY);
                boolean onButton = abilityDetails.isOnButtons(mouseX, mouseY);
                boolean inDetails = abilityDetails.containsPoint(mouseX, mouseY);
                if (!used && !inListNode && !onButton && !inDetails) {
                    abilityDetails.setAbility(null);
                    abilityList.setSelected(null);
                }
                return used;
            } else if (activeTab == PanelTab.SKILLS) {
                return skillsList.mouseClicked(mouseX, mouseY, button) || skillsDetails.mouseClicked(mouseX, mouseY, button);
            } else {
                return abilityDetails.mouseClicked(mouseX, mouseY, button) || abilityList.mouseClicked(mouseX, mouseY, button);
            }
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (viewMode == ViewMode.MAIN && activeTab == PanelTab.ABILITIES && abilityList != null && abilityList.mouseDragged(mouseX, mouseY, button, dragX, dragY)) {
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (abilityList != null) abilityList.endDrag();
        return super.mouseReleased(mouseX, mouseY, button);
    }

    private void setTab(PanelTab tab) {
        activeTab = tab;
        viewMode = ViewMode.MAIN;
        boolean skillsActive = activeTab == PanelTab.SKILLS && viewMode == ViewMode.MAIN;
        skillsTab.setSelected(skillsActive);
        abilitiesTab.setSelected(!skillsActive && viewMode == ViewMode.MAIN);

        skillsList.visible = skillsActive;
        skillsList.active = skillsActive;
        skillsDetails.visible = false;
        skillsDetails.active = false;
        skillsDetails.upgradeButton().visible = false;
        skillsDetails.upgradeButton().active = false;
        skillsDetails.downgradeButton().visible = false;
        skillsDetails.downgradeButton().active = false;

        abilityList.visible = !skillsActive && viewMode == ViewMode.MAIN;
        abilityList.active = !skillsActive && viewMode == ViewMode.MAIN;
        abilityDetails.visible = false;
        abilityDetails.active = false;
        abilityDetails.upgradeButton().visible = false;
        abilityDetails.upgradeButton().active = false;
        abilityDetails.downgradeButton().visible = false;
        abilityDetails.downgradeButton().active = false;
        abilityDetails.selectButton().visible = false;
        abilityDetails.selectButton().active = false;
    }

    private void setViewMode(ViewMode mode) {
        viewMode = mode;
        playerBottomTab.setSelected(mode == ViewMode.PLAYER);
        settingsBottomTab.setSelected(mode == ViewMode.SETTINGS);

        boolean main = mode == ViewMode.MAIN;
        boolean settings = mode == ViewMode.SETTINGS;
        boolean skillsActive = main && activeTab == PanelTab.SKILLS;
        boolean abilitiesActive = main && activeTab == PanelTab.ABILITIES;

        skillsTab.visible = true;
        skillsTab.active = true;
        skillsTab.setSelected(skillsActive);
        abilitiesTab.visible = true;
        abilitiesTab.active = true;
        abilitiesTab.setSelected(abilitiesActive);

        skillsList.visible = skillsActive;
        skillsList.active = skillsActive;
        abilityList.visible = abilitiesActive;
        abilityList.active = abilitiesActive;
        skillsDetails.visible = false;
        skillsDetails.active = false;
        skillsDetails.upgradeButton().visible = false;
        skillsDetails.upgradeButton().active = false;
        skillsDetails.downgradeButton().visible = false;
        skillsDetails.downgradeButton().active = false;
        abilityDetails.visible = false;
        abilityDetails.active = false;
        abilityDetails.upgradeButton().visible = false;
        abilityDetails.upgradeButton().active = false;
        abilityDetails.downgradeButton().visible = false;
        abilityDetails.downgradeButton().active = false;
        abilityDetails.selectButton().visible = false;
        abilityDetails.selectButton().active = false;

        if (settings) settingsScroll = Math.max(0, settingsScroll);
    }

    private boolean isInsidePanelOrTabs(double mouseX, double mouseY) {
        int left = panelX;
        int top = panelY;
        int right = panelX + panelWidth;
        int bottom = panelY + panelHeight + 35;
        int tabLeft = panelX - 31;
        return mouseX >= tabLeft && mouseX <= right && mouseY >= top && mouseY <= bottom;
    }

    private int settingsViewY() {
        return panelY + SETTINGS_VIEW_TOP;
    }

    private int settingsViewHeight() {
        return panelHeight - SETTINGS_VIEW_TOP - SETTINGS_VIEW_BOTTOM_PAD;
    }

    private boolean isInSettingsView(double mouseX, double mouseY) {
        int x0 = panelX + SETTINGS_VIEW_PAD_X;
        int x1 = panelX + panelWidth - SETTINGS_VIEW_PAD_X;
        int y0 = settingsViewY();
        int y1 = y0 + settingsViewHeight();
        return mouseX >= x0 && mouseX <= x1 && mouseY >= y0 && mouseY <= y1;
    }

    private void renderSettingsRows(GuiGraphics gg, int mouseX, int mouseY) {
        int xLeft = panelX + SETTINGS_VIEW_PAD_X + 2;
        int xRight = panelX + panelWidth - SETTINGS_VIEW_PAD_X - 2;
        int y0 = settingsViewY();
        int y1 = y0 + settingsViewHeight();
        float scale = 0.5F;

        gg.enableScissor(panelX + SETTINGS_VIEW_PAD_X, y0, panelX + panelWidth - SETTINGS_VIEW_PAD_X, y1);
        for (int i = 0; i < settingsRows.size(); i++) {
            int y = y0 + i * SETTINGS_ROW_H - settingsScroll;
            if (y + SETTINGS_ROW_H < y0 || y > y1) continue;
            boolean hovered = mouseX >= panelX + SETTINGS_VIEW_PAD_X && mouseX <= panelX + panelWidth - SETTINGS_VIEW_PAD_X && mouseY >= y && mouseY <= y + SETTINGS_ROW_H;
            int labelColor = hovered ? 0xFFFF55 : 0xFFFFFF;
            int stateColor = 0x6AB2FF;

            String label = settingsRows.get(i).label;
            String state = settingsRows.get(i).state.get();
            int labelY = y + 6;
            drawScaledText(gg, label, xLeft, labelY, labelColor, scale);
            int stateW = (int) (font.width(state) * scale);
            drawScaledText(gg, state, xRight - stateW, labelY, stateColor, scale);
        }
        gg.disableScissor();
    }

    private void drawScaledText(GuiGraphics gg, String text, int x, int y, int color, float scale) {
        gg.pose().pushPose();
        gg.pose().translate(x, y, 0.0F);
        gg.pose().scale(scale, scale, 1.0F);
        gg.drawString(font, text, 0, 0, color, false);
        gg.pose().popPose();
    }

    private static final class SettingRow {
        final String label;
        final java.util.function.Supplier<String> state;
        final Runnable onClick;

        private SettingRow(String label, java.util.function.Supplier<String> state, Runnable onClick) {
            this.label = label;
            this.state = state;
            this.onClick = onClick;
        }
    }
}

package net.revilodev.aura.client.screen;

import com.revilo.levelup.api.LevelUpApi;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.fml.loading.FMLPaths;
import net.revilodev.aura.CodexMod;
import net.revilodev.aura.abilities.AbilitiesAttachments;
import net.revilodev.aura.attributes.CodexAttributes;
import net.revilodev.aura.client.AuraClientConfig;
import net.revilodev.aura.client.BottomPullTabButton;
import net.revilodev.aura.client.PanelTab;
import net.revilodev.aura.client.PanelTabButton;
import net.revilodev.aura.client.abilities.AbilityKeybinds;
import net.revilodev.aura.client.abilities.AbilityDetailsPanel;
import net.revilodev.aura.client.abilities.AbilityListWidget;
import net.revilodev.aura.client.skills.SkillDetailsPanel;
import net.revilodev.aura.client.skills.SkillListWidget;
import net.revilodev.aura.client.skills.SkillPanelHeaderRenderer;
import net.revilodev.aura.abilities.AbilityElement;
import net.revilodev.aura.abilities.AbilityDefinition;
import net.revilodev.aura.abilities.AbilityId;
import net.revilodev.aura.abilities.AbilityRegistry;
import net.revilodev.aura.skills.SkillBalance;
import net.revilodev.aura.skills.SkillDefinition;
import net.revilodev.aura.skills.SkillId;
import net.revilodev.aura.skills.SkillRegistry;
import net.revilodev.aura.skills.SkillsAttachments;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.IntConsumer;

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
            ResourceLocation.fromNamespaceAndPath(CodexMod.MOD_ID, "textures/gui/sprites/pull-tab-settings-selected.png");

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
    private final LevelUpConfigStore levelUpStore = new LevelUpConfigStore();
    private int settingsScroll = 0;
    private boolean levelUpExpanded = false;
    private static final int SETTINGS_ROW_H = 7;
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

    private static final class LevelUpConfigStore {
        boolean showTopCenterLevelOverlay = true;
        boolean showTemporaryLevelOverlay = true;
        boolean showInventoryLevelBar = true;
        String levelHudPosition = "top";
        boolean levelHudStayOnScreen = false;
        int hudColorR = 0x53;
        int hudColorG = 0xA4;
        int hudColorB = 0xBC;
        int hudLevelBarOffsetX = 0;
        int hudLevelBarOffsetY = 0;
        int inventoryLevelBarOffsetX = 0;
        int inventoryLevelBarOffsetY = 0;
        boolean openHudLevelBarRepositionGui = false;
        boolean openInventoryLevelBarRepositionGui = false;
        int baseXpPerLevel = 100;
        int linearXpPerLevel = 20;
        double exponent = 1.35D;
        double levelMultiplier = 0.75D;
        int maxLevel = 500;
        boolean enableMobKillXp = true;
        int mobKillXp = 8;
        boolean dropLevelsOnlyFromMobsWithTag = false;
    }

    public StandaloneSkillsBookScreen() {
        super(Component.translatable("gui.aura.book.title"));
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
        abilityList.setViewportTweaks(0, 0, 8);
        abilityList.setShowLocked(true);
        abilityList.setHeaderTextOffsetX(15);
        abilityDetails = new AbilityDetailsPanel(detailsX, detailsY + 3, detailsW, detailsH);
        abilityDetails.setContentTopOffset(3);

        skillsTab = new PanelTabButton(panelX - 31, panelY + 6, PanelTab.SKILLS, () -> setTab(PanelTab.SKILLS));
        abilitiesTab = new PanelTabButton(panelX - 31, panelY + 34, PanelTab.ABILITIES, () -> setTab(PanelTab.ABILITIES));

        int bottomY = panelY + panelHeight - 3;
        int bottomX = panelX + 7;
        playerBottomTab = new BottomPullTabButton(bottomX, bottomY, Component.translatable("gui.aura.player"), PLAYER_TAB_TEX, PLAYER_TAB_TEX_PULLED, PLAYER_TAB_TEX_SELECTED, () -> setViewMode(ViewMode.PLAYER));
        settingsBottomTab = new BottomPullTabButton(bottomX + 32 + 2, bottomY, Component.translatable("gui.aura.settings"), SETTINGS_TAB_TEX, SETTINGS_TAB_TEX_PULLED, SETTINGS_TAB_TEX_PULLED_ALT, () -> setViewMode(ViewMode.SETTINGS));
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
        loadLevelUpConfig();
        setViewMode(ViewMode.MAIN);
        setTab(activeTab);
    }

    private void initSettingsRows() {
        settingsRows.clear();
        settingsRows.add(new SettingRow(Component.translatable("gui.aura.settings.hud_display").getString(), () -> boolState(AuraClientConfig.hudDisplayEnabled()), AuraClientConfig::toggleHudDisplayEnabled));
        settingsRows.add(new SettingRow(Component.translatable("gui.aura.settings.disable_inventory_book").getString(), () -> boolState(AuraClientConfig.disableInventoryCodexBook()), AuraClientConfig::toggleDisableInventoryCodexBook));
        settingsRows.add(new SettingRow(Component.translatable("gui.aura.settings.reposition_hud").getString(), () -> Component.translatable("gui.aura.hud_position." + AuraClientConfig.hudPosition().name().toLowerCase(java.util.Locale.ROOT)).getString(), AuraClientConfig::cycleHudPosition));
        settingsRows.add(new SettingRow(Component.translatable("gui.aura.settings.disable_skills_abilities").getString(), () -> boolState(AuraClientConfig.disableSkillsAndAbilities()), AuraClientConfig::toggleDisableSkillsAndAbilities));
        settingsRows.add(new SettingRow(Component.translatable("gui.aura.settings.block_ability_switching").getString(), () -> boolState(AuraClientConfig.blockAbilitySwitching()), AuraClientConfig::toggleBlockAbilitySwitching));
        settingsRows.add(new SettingRow(Component.translatable("gui.aura.settings.block_upgrade_downgrade").getString(), () -> boolState(AuraClientConfig.blockUpgradeDowngrade()), AuraClientConfig::toggleBlockUpgradeDowngrade));
        settingsRows.add(new SettingRow(Component.translatable("gui.aura.settings.block_open_panel").getString(), () -> boolState(AuraClientConfig.blockOpenSkillsAbilitiesPanel()), AuraClientConfig::toggleBlockOpenSkillsAbilitiesPanel));
        settingsRows.add(new SettingRow(Component.translatable("gui.aura.settings.levelup_values").getString(), () -> Component.translatable("gui.aura.open").getString(), () -> minecraft.setScreen(new LevelUpConfigScreen(this))));
        settingsRows.add(new SettingRow(Component.translatable("gui.aura.settings.disabled_skills_abilities").getString(), () -> Component.translatable("gui.aura.open").getString(), () -> minecraft.setScreen(new DisabledSkillsAbilitiesScreen(this))));
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
                viewMode == ViewMode.SETTINGS ? Component.translatable("gui.aura.settings").getString() : (viewMode == ViewMode.PLAYER ? Component.translatable("gui.aura.player").getString() : activeTab.title())
        );
        if (viewMode == ViewMode.PLAYER) {
            renderPlayerView(gg, mouseX, mouseY);
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
            if (viewMode == ViewMode.SETTINGS && (button == 0 || button == 1) && isInSettingsView(mouseX, mouseY)) {
                int index = (int) ((mouseY - settingsViewY() + settingsScroll) / SETTINGS_ROW_H);
                if (index >= 0 && index < settingsRows.size()) {
                    settingsRows.get(index).onClick.accept(button);
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
        setViewMode(ViewMode.MAIN);
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

    private void toggleLevelUpExpanded() {
        if (!levelUpExpanded) {
            loadLevelUpConfig();
        }
        levelUpExpanded = !levelUpExpanded;
        int previousScroll = settingsScroll;
        initSettingsRows();
        clampSettingsScroll();
        settingsScroll = Math.min(settingsScroll, previousScroll);
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
        int xLeft = panelX + SETTINGS_VIEW_PAD_X + 7;
        int xRight = panelX + panelWidth - SETTINGS_VIEW_PAD_X - 7;
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
            int labelY = y + 1;
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

    private void drawHoverStat(GuiGraphics gg, int mouseX, int mouseY, int baseX, int baseY, String text, int color, float scale, int yOffset) {
        int y = baseY + yOffset;
        int w = (int) (font.width(text) * scale);
        boolean hovered = mouseX >= baseX && mouseX <= baseX + w + 2 && mouseY >= y && mouseY <= y + 7;
        drawScaledText(gg, text, baseX, y, color, hovered ? scale * 1.05F : scale);
    }

    private Component tooltipIfHovered(Component currentTooltip, int mouseX, int mouseY, int x, int y, String text, float scale) {
        if (currentTooltip != null) return currentTooltip;
        int w = (int) (font.width(text) * scale);
        int h = Math.max(6, (int) (font.lineHeight * scale));
        if (mouseX >= x && mouseX <= x + w + 2 && mouseY >= y && mouseY <= y + h) return Component.literal(text);
        return null;
    }

    private void renderPlayerView(GuiGraphics gg, int mouseX, int mouseY) {
        if (minecraft == null || minecraft.player == null) return;
        Component hoveredTooltip = null;

        int dollX = panelX + 35;
        int dollY = panelY + panelHeight - 22;
        int dollSize = 36;
        gg.pose().pushPose();
        gg.pose().translate(0.0F, 0.0F, 1000.0F);
        InventoryScreen.renderEntityInInventoryFollowsMouse(gg, dollX, dollY, dollSize, dollX - mouseX, dollY - 26 - mouseY, 30.0F, 0.0F, 0.0F, minecraft.player);
        gg.pose().popPose();

        var player = minecraft.player;
        var playerAbilities = player.getData(AbilitiesAttachments.PLAYER_ABILITIES.get());
        var skills = player.getData(SkillsAttachments.PLAYER_SKILLS.get());
        double baseDamage = player.getAttributeValue(Attributes.ATTACK_DAMAGE);
        double critPower = 1.5D + SkillBalance.critPowerDamage(skills.level(SkillId.CRIT_POWER));
        double abilityPower = CodexAttributes.baseAbilityPower(player);
        int defence = player.getArmorValue();
        double speed = player.getAttributeValue(Attributes.MOVEMENT_SPEED);
        double maxHealth = player.getMaxHealth();
        int luckLevel = skills.level(SkillId.LUCK);
        double leechChance = skills.level(SkillId.HEALTH_BOOST) > 0 ? SkillBalance.lifeLeach(luckLevel) * 100.0D : 0.0D;

        int textX = panelX + 11;
        int textY = panelY + 18;
        int lineH = 9;
        float scale = 0.65F;
        String levelText = Component.translatable("gui.aura.player.level", LevelUpApi.getLevel(player)).getString();
        drawScaledText(gg, levelText, panelX + 14, panelY + 10, 0xFFE08A, 0.75F);
        hoveredTooltip = tooltipIfHovered(hoveredTooltip, mouseX, mouseY, panelX + 14, panelY + 10, levelText, 0.75F);
        String stat0 = Component.translatable("gui.aura.player.base_damage", fmt(baseDamage)).getString();
        String stat1 = Component.translatable("gui.aura.player.crit_power", fmt(critPower) + "x").getString();
        String stat2 = Component.translatable("gui.aura.player.ability_power", fmt(abilityPower)).getString();
        String stat3 = Component.translatable("gui.aura.player.defence", defence).getString();
        String stat4 = Component.translatable("gui.aura.player.speed", fmt(speed)).getString();
        String stat5 = Component.translatable("gui.aura.player.leeching", fmt(leechChance) + "%").getString();
        String stat6 = Component.translatable("gui.aura.player.health", fmt(maxHealth)).getString();
        drawHoverStat(gg, mouseX, mouseY, textX, textY, stat0, 0xFF8080, scale, lineH * 0);
        drawHoverStat(gg, mouseX, mouseY, textX, textY, stat1, 0xFFD580, scale, lineH * 1);
        drawHoverStat(gg, mouseX, mouseY, textX, textY, stat2, 0xC78CFF, scale, lineH * 2);
        drawHoverStat(gg, mouseX, mouseY, textX, textY, stat3, 0x8CD3FF, scale, lineH * 3);
        drawHoverStat(gg, mouseX, mouseY, textX, textY, stat4, 0x99FFB6, scale, lineH * 4);
        drawHoverStat(gg, mouseX, mouseY, textX, textY, stat5, 0xFF9CC8, scale, lineH * 5);
        drawHoverStat(gg, mouseX, mouseY, textX, textY, stat6, 0xFFB3B3, scale, lineH * 6);
        hoveredTooltip = tooltipIfHovered(hoveredTooltip, mouseX, mouseY, textX, textY + lineH * 0, stat0, scale);
        hoveredTooltip = tooltipIfHovered(hoveredTooltip, mouseX, mouseY, textX, textY + lineH * 1, stat1, scale);
        hoveredTooltip = tooltipIfHovered(hoveredTooltip, mouseX, mouseY, textX, textY + lineH * 2, stat2, scale);
        hoveredTooltip = tooltipIfHovered(hoveredTooltip, mouseX, mouseY, textX, textY + lineH * 3, stat3, scale);
        hoveredTooltip = tooltipIfHovered(hoveredTooltip, mouseX, mouseY, textX, textY + lineH * 4, stat4, scale);
        hoveredTooltip = tooltipIfHovered(hoveredTooltip, mouseX, mouseY, textX, textY + lineH * 5, stat5, scale);
        hoveredTooltip = tooltipIfHovered(hoveredTooltip, mouseX, mouseY, textX, textY + lineH * 6, stat6, scale);

        int listY = panelY + 85;
        int leftColX = panelX + 11;
        int rightColX = panelX + 80;
        String abilitiesHdr = Component.translatable("gui.aura.player.selected_abilities").getString();
        String skillsHdr = Component.translatable("gui.aura.player.skills").getString();
        drawScaledText(gg, abilitiesHdr, leftColX, listY, 0xE0E0E0, 0.65F);
        drawScaledText(gg, skillsHdr, rightColX, listY, 0xE0E0E0, 0.65F);
        hoveredTooltip = tooltipIfHovered(hoveredTooltip, mouseX, mouseY, leftColX, listY, abilitiesHdr, 0.65F);
        hoveredTooltip = tooltipIfHovered(hoveredTooltip, mouseX, mouseY, rightColX, listY, skillsHdr, 0.65F);
        int row = 0;
        for (AbilityElement element : AbilityElement.values()) {
            AbilityId selected = playerAbilities.selectedSpecialization(element);
            if (selected == null) continue;
            AbilityDefinition def = AbilityRegistry.def(selected);
            if (def == null) continue;
            int y = listY + 8 + (row * 9);
            String line = Component.translatable("gui.aura.player.ability_bind", selected.title(), AbilityKeybinds.keyName(selected)).getString();
            boolean hovered = mouseX >= leftColX && mouseX <= leftColX + 68 && mouseY >= y && mouseY <= y + 8;
            drawScaledText(gg, line, leftColX, y + 1, elementColor(def.element()), hovered ? 0.525F : 0.5F);
            hoveredTooltip = tooltipIfHovered(hoveredTooltip, mouseX, mouseY, leftColX, y + 1, line, hovered ? 0.525F : 0.5F);
            row++;
            if (row > 6) break;
        }

        int skillRow = 0;
        for (SkillDefinition def : SkillRegistry.primarySkills()) {
            int level = skills.level(def.id());
            if (level <= 0) continue;
            int y = listY + 8 + (skillRow * 9);
            boolean hovered = mouseX >= rightColX && mouseX <= rightColX + 58 && mouseY >= y && mouseY <= y + 8;
            String line = Component.translatable("gui.aura.player.skill_level", def.title(), level).getString();
            drawScaledText(gg, line, rightColX, y + 1, skillColor(def.id()), hovered ? 0.525F : 0.5F);
            hoveredTooltip = tooltipIfHovered(hoveredTooltip, mouseX, mouseY, rightColX, y + 1, line, hovered ? 0.525F : 0.5F);
            skillRow++;
            if (skillRow > 6) break;
        }
        if (hoveredTooltip != null) gg.renderTooltip(font, hoveredTooltip, mouseX, mouseY - 6);
    }

    private static int elementColor(AbilityElement element) {
        return switch (element) {
            case FIRE -> 0xFF8A66;
            case ICE -> 0x8CD3FF;
            case LIGHTNING -> 0xFFE36B;
            case POISON -> 0x96E07A;
            case FORCE -> 0xC4A5FF;
            case MAGIC -> 0xFF9CDD;
            case WIND -> 0xA9FFD8;
        };
    }

    private static int skillColor(SkillId id) {
        return switch (id.category()) {
            case STRENGTH -> 0xFF8E8E;
            case RESISTANCE -> 0x8CC7FF;
            case AGILITY -> 0x99FFB6;
            case VITALITY -> 0xFF9CC8;
            case LUCK -> 0xFFE08A;
        };
    }

    private static String fmt(double value) {
        String out = String.format(java.util.Locale.ROOT, "%.2f", value);
        while (out.contains(".") && (out.endsWith("0") || out.endsWith("."))) out = out.substring(0, out.length() - 1);
        return out;
    }

    private static String boolState(boolean value) {
        return Component.translatable(value ? "gui.aura.state.enabled" : "gui.aura.state.disabled").getString();
    }

    private void clampSettingsScroll() {
        int maxScroll = Math.max(0, settingsRows.size() * SETTINGS_ROW_H - settingsViewHeight());
        settingsScroll = Math.max(0, Math.min(maxScroll, settingsScroll));
    }

    private static String hex2(int c) {
        return String.format(Locale.ROOT, "%02X", c & 0xFF);
    }

    private static int clampInt(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    private void openHudReposition() {
        if (!invokeClientApi("openHudLevelBarRepositionGui")) {
            levelUpStore.openHudLevelBarRepositionGui = true;
            saveLevelUpConfig();
        }
    }

    private void openInventoryReposition() {
        if (!invokeClientApi("openInventoryLevelBarRepositionGui")) {
            levelUpStore.openInventoryLevelBarRepositionGui = true;
            saveLevelUpConfig();
        }
    }

    private boolean invokeClientApi(String method) {
        try {
            Class<?> cls = Class.forName("com.revilo.levelup.api.LevelUpClientApi");
            cls.getMethod(method).invoke(null);
            return true;
        } catch (Throwable ignored) {
            return false;
        }
    }

    private Path clientConfigFile() {
        return FMLPaths.CONFIGDIR.get().resolve("levelup-client.toml");
    }

    private Path commonConfigFile() {
        return FMLPaths.CONFIGDIR.get().resolve("levelup-common.toml");
    }

    private void loadLevelUpConfig() {
        List<String> client = readLines(clientConfigFile());
        List<String> common = readLines(commonConfigFile());

        levelUpStore.showTopCenterLevelOverlay = readBool(client, "showTopCenterLevelOverlay", levelUpStore.showTopCenterLevelOverlay);
        levelUpStore.showTemporaryLevelOverlay = readBool(client, "showTemporaryLevelOverlay", levelUpStore.showTemporaryLevelOverlay);
        levelUpStore.showInventoryLevelBar = readBool(client, "showInventoryLevelBar", levelUpStore.showInventoryLevelBar);
        levelUpStore.levelHudPosition = readString(client, "levelHudPosition", levelUpStore.levelHudPosition);
        levelUpStore.levelHudStayOnScreen = readBool(client, "levelHudStayOnScreen", levelUpStore.levelHudStayOnScreen);
        String color = readString(client, "levelHudColor", "#53a4bc");
        if (color.startsWith("#") && color.length() == 7) {
            levelUpStore.hudColorR = Integer.parseInt(color.substring(1, 3), 16);
            levelUpStore.hudColorG = Integer.parseInt(color.substring(3, 5), 16);
            levelUpStore.hudColorB = Integer.parseInt(color.substring(5, 7), 16);
        }
        levelUpStore.hudLevelBarOffsetX = readInt(client, "hudLevelBarOffsetX", levelUpStore.hudLevelBarOffsetX);
        levelUpStore.hudLevelBarOffsetY = readInt(client, "hudLevelBarOffsetY", levelUpStore.hudLevelBarOffsetY);
        levelUpStore.inventoryLevelBarOffsetX = readInt(client, "inventoryLevelBarOffsetX", levelUpStore.inventoryLevelBarOffsetX);
        levelUpStore.inventoryLevelBarOffsetY = readInt(client, "inventoryLevelBarOffsetY", levelUpStore.inventoryLevelBarOffsetY);
        levelUpStore.openHudLevelBarRepositionGui = readBool(client, "openHudLevelBarRepositionGui", false);
        levelUpStore.openInventoryLevelBarRepositionGui = readBool(client, "openInventoryLevelBarRepositionGui", false);

        levelUpStore.baseXpPerLevel = readInt(common, "baseXpPerLevel", levelUpStore.baseXpPerLevel);
        levelUpStore.linearXpPerLevel = readInt(common, "linearXpPerLevel", levelUpStore.linearXpPerLevel);
        levelUpStore.exponent = readDouble(common, "exponent", levelUpStore.exponent);
        levelUpStore.levelMultiplier = readDouble(common, "levelMultiplier", levelUpStore.levelMultiplier);
        levelUpStore.maxLevel = readInt(common, "maxLevel", levelUpStore.maxLevel);
        levelUpStore.enableMobKillXp = readBool(common, "enable_mob_kill_xp", levelUpStore.enableMobKillXp);
        levelUpStore.mobKillXp = readInt(common, "mobKillXp", levelUpStore.mobKillXp);
        levelUpStore.dropLevelsOnlyFromMobsWithTag = readBool(common, "drop_levels_only_from_mobs_with_tag", levelUpStore.dropLevelsOnlyFromMobsWithTag);
    }

    private void saveLevelUpConfig() {
        String clientOut = """
                [hud]
                showTopCenterLevelOverlay=%s
                showTemporaryLevelOverlay=%s
                showInventoryLevelBar=%s
                levelHudPosition="%s"
                levelHudStayOnScreen=%s
                levelHudColor="%s"
                hudLevelBarOffsetX=%d
                hudLevelBarOffsetY=%d
                inventoryLevelBarOffsetX=%d
                inventoryLevelBarOffsetY=%d
                openHudLevelBarRepositionGui=%s
                openInventoryLevelBarRepositionGui=%s
                """.formatted(
                levelUpStore.showTopCenterLevelOverlay,
                levelUpStore.showTemporaryLevelOverlay,
                levelUpStore.showInventoryLevelBar,
                levelUpStore.levelHudPosition,
                levelUpStore.levelHudStayOnScreen,
                "#" + hex2(levelUpStore.hudColorR) + hex2(levelUpStore.hudColorG) + hex2(levelUpStore.hudColorB),
                levelUpStore.hudLevelBarOffsetX,
                levelUpStore.hudLevelBarOffsetY,
                levelUpStore.inventoryLevelBarOffsetX,
                levelUpStore.inventoryLevelBarOffsetY,
                levelUpStore.openHudLevelBarRepositionGui,
                levelUpStore.openInventoryLevelBarRepositionGui
        );
        String commonOut = """
                [progression]
                baseXpPerLevel=%d
                linearXpPerLevel=%d
                exponent=%.2f
                levelMultiplier=%.2f
                maxLevel=%d

                [sources]
                enable_mob_kill_xp=%s
                mobKillXp=%d
                drop_levels_only_from_mobs_with_tag=%s
                """.formatted(
                levelUpStore.baseXpPerLevel,
                levelUpStore.linearXpPerLevel,
                levelUpStore.exponent,
                levelUpStore.levelMultiplier,
                levelUpStore.maxLevel,
                levelUpStore.enableMobKillXp,
                levelUpStore.mobKillXp,
                levelUpStore.dropLevelsOnlyFromMobsWithTag
        );
        writeFile(clientConfigFile(), clientOut);
        writeFile(commonConfigFile(), commonOut);
    }

    private static List<String> readLines(Path path) {
        try {
            if (Files.exists(path)) return Files.readAllLines(path);
        } catch (IOException ignored) {}
        return List.of();
    }

    private static void writeFile(Path path, String text) {
        try {
            Files.createDirectories(path.getParent());
            Files.writeString(path, text);
        } catch (IOException ignored) {}
    }

    private static String cleanValue(String line) {
        int eq = line.indexOf('=');
        if (eq < 0) return "";
        String value = line.substring(eq + 1).trim();
        int hash = value.indexOf('#');
        if (hash >= 0) value = value.substring(0, hash).trim();
        return value;
    }

    private static String readString(List<String> lines, String key, String fallback) {
        for (String line : lines) {
            String trimmed = line.trim();
            if (!trimmed.startsWith(key + "=")) continue;
            String value = cleanValue(trimmed);
            if (value.startsWith("\"") && value.endsWith("\"") && value.length() >= 2) {
                value = value.substring(1, value.length() - 1);
            }
            return value;
        }
        return fallback;
    }

    private static boolean readBool(List<String> lines, String key, boolean fallback) {
        String value = readString(lines, key, fallback ? "true" : "false");
        return "true".equalsIgnoreCase(value);
    }

    private static int readInt(List<String> lines, String key, int fallback) {
        try {
            return Integer.parseInt(readString(lines, key, Integer.toString(fallback)));
        } catch (Exception ignored) {
            return fallback;
        }
    }

    private static double readDouble(List<String> lines, String key, double fallback) {
        try {
            return Double.parseDouble(readString(lines, key, Double.toString(fallback)));
        } catch (Exception ignored) {
            return fallback;
        }
    }

    private SettingRow levelUpSection(String labelKey) {
        return new SettingRow("  " + Component.translatable(labelKey).getString(), () -> "", button -> {});
    }

    private SettingRow levelUpReadOnly(String labelKey, java.util.function.Supplier<String> state) {
        return new SettingRow("  " + Component.translatable(labelKey).getString(), state, button -> {});
    }

    private SettingRow levelUpAction(String labelKey, Runnable action) {
        return new SettingRow("  " + Component.translatable(labelKey).getString(), () -> Component.translatable("gui.aura.open").getString(), button -> {
            if (button == 0) {
                action.run();
            }
        });
    }

    private SettingRow levelUpBool(String labelKey, BoolGetter getter, BoolSetter setter) {
        return new SettingRow("  " + Component.translatable(labelKey).getString(), () -> boolState(getter.get()), button -> {
            setter.set(!getter.get());
            saveLevelUpConfig();
        });
    }

    private SettingRow levelUpEnumTopBottom(String labelKey, StringGetter getter, StringSetter setter) {
        return new SettingRow("  " + Component.translatable(labelKey).getString(), getter::get, button -> {
            setter.set("top".equalsIgnoreCase(getter.get()) ? "bottom" : "top");
            saveLevelUpConfig();
        });
    }

    private SettingRow levelUpInt(String labelKey, IntGetter getter, IntSetter setter, int step, int min, int max) {
        return new SettingRow("  " + Component.translatable(labelKey).getString(), () -> Integer.toString(getter.get()), button -> {
            int delta = button == 1 ? -step : step;
            setter.set(clampInt(getter.get() + delta, min, max));
            saveLevelUpConfig();
        });
    }

    private SettingRow levelUpDouble(String labelKey, DoubleGetter getter, DoubleSetter setter, double step, double min, double max) {
        return new SettingRow("  " + Component.translatable(labelKey).getString(), () -> String.format(Locale.ROOT, "%.2f", getter.get()), button -> {
            double delta = button == 1 ? -step : step;
            setter.set(Math.max(min, Math.min(max, getter.get() + delta)));
            saveLevelUpConfig();
        });
    }

    private static final class SettingRow {
        final String label;
        final java.util.function.Supplier<String> state;
        final IntConsumer onClick;

        private SettingRow(String label, java.util.function.Supplier<String> state, Runnable onClick) {
            this(label, state, button -> onClick.run());
        }

        private SettingRow(String label, java.util.function.Supplier<String> state, IntConsumer onClick) {
            this.label = label;
            this.state = state;
            this.onClick = onClick;
        }
    }

    @FunctionalInterface
    private interface BoolGetter { boolean get(); }
    @FunctionalInterface
    private interface BoolSetter { void set(boolean value); }
    @FunctionalInterface
    private interface StringGetter { String get(); }
    @FunctionalInterface
    private interface StringSetter { void set(String value); }
    @FunctionalInterface
    private interface IntGetter { int get(); }
    @FunctionalInterface
    private interface IntSetter { void set(int value); }
    @FunctionalInterface
    private interface DoubleGetter { double get(); }
    @FunctionalInterface
    private interface DoubleSetter { void set(double value); }
}

package net.revilodev.aura.client.skills;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.revilodev.aura.CodexMod;
import net.revilodev.aura.abilities.AbilitiesAttachments;
import net.revilodev.aura.abilities.AbilityElement;
import net.revilodev.aura.abilities.AbilityDefinition;
import net.revilodev.aura.abilities.AbilityId;
import net.revilodev.aura.abilities.AbilityRegistry;
import net.revilodev.aura.abilities.PlayerAbilities;
import net.revilodev.aura.attributes.CodexAttributes;
import net.revilodev.aura.client.AuraClientConfig;
import net.revilodev.aura.client.BottomPullTabButton;
import net.revilodev.aura.client.PanelTab;
import net.revilodev.aura.client.PanelTabButton;
import net.revilodev.aura.client.SkillsToggleButton;
import net.revilodev.aura.client.abilities.AbilityKeybinds;
import net.revilodev.aura.client.abilities.AbilityDetailsPanel;
import net.revilodev.aura.client.abilities.AbilityListWidget;
import net.revilodev.aura.client.screen.LevelUpConfigScreen;
import net.revilodev.aura.skills.SkillBalance;
import net.revilodev.aura.skills.SkillDefinition;
import net.revilodev.aura.skills.SkillId;
import net.revilodev.aura.skills.SkillRegistry;
import net.revilodev.aura.skills.SkillsAttachments;
import com.revilo.levelup.api.LevelUpApi;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.function.Supplier;

@OnlyIn(Dist.CLIENT)
public final class SkillsPanelClient {
    private static final ResourceLocation BTN_TEX =
            ResourceLocation.fromNamespaceAndPath(CodexMod.MOD_ID, "textures/gui/sprites/skills_button.png");
    private static final ResourceLocation BTN_TEX_HOVER =
            ResourceLocation.fromNamespaceAndPath(CodexMod.MOD_ID, "textures/gui/sprites/skills_button_hovered.png");
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

    private static final int PANEL_W = 147;
    private static final int PANEL_H = 166;
    private static final int INNER_PAD_X = 6;
    private static final int INNER_PAD_TOP = 5;
    private static final int INNER_PAD_BOTTOM = 6;
    private static final int HEADER_OFFSET_X = 5;
    private static final int HEADER_OFFSET_Y = 3;
    private static final int SETTINGS_ROW_H = 7;
    private static final int SETTINGS_VIEW_PAD_X = 6;
    private static final int SETTINGS_VIEW_TOP = 18;
    private static final int SETTINGS_VIEW_BOTTOM_PAD = 12;

    private static final Map<Screen, State> STATES = new WeakHashMap<>();
    private static Field LEFT_FIELD;
    private static boolean lastOpen = false;
    private static PanelTab lastTab = PanelTab.SKILLS;

    private SkillsPanelClient() {}

    public static void register() {
        NeoForge.EVENT_BUS.addListener(ScreenEvent.Init.Post.class, SkillsPanelClient::onScreenInit);
        NeoForge.EVENT_BUS.addListener(ScreenEvent.Closing.class, SkillsPanelClient::onScreenClosing);
        NeoForge.EVENT_BUS.addListener(EventPriority.LOWEST, false, ScreenEvent.Render.Pre.class, SkillsPanelClient::onScreenRenderPre);
        NeoForge.EVENT_BUS.addListener(ScreenEvent.Render.Post.class, SkillsPanelClient::onScreenRenderPost);
        NeoForge.EVENT_BUS.addListener(ScreenEvent.MouseScrolled.Pre.class, SkillsPanelClient::onMouseScrolled);
        NeoForge.EVENT_BUS.addListener(ScreenEvent.MouseButtonPressed.Pre.class, SkillsPanelClient::onMousePressed);
        NeoForge.EVENT_BUS.addListener(ScreenEvent.MouseDragged.Pre.class, SkillsPanelClient::onMouseDragged);
        NeoForge.EVENT_BUS.addListener(ScreenEvent.MouseButtonReleased.Pre.class, SkillsPanelClient::onMouseReleased);
    }

    public static void onScreenInit(ScreenEvent.Init.Post event) {
        if (AuraClientConfig.disableInventoryCodexBook() || AuraClientConfig.blockOpenSkillsAbilitiesPanel()) return;
        Screen screen = event.getScreen();
        if (!(screen instanceof InventoryScreen inv)) return;

        State st = new State(inv);
        STATES.put(screen, st);

        st.btn = new SkillsToggleButton(inv.getGuiLeft() + 145, inv.getGuiTop() + 61, BTN_TEX, BTN_TEX_HOVER, () -> toggle(st));
        st.bg = new PanelBackground(0, 0, PANEL_W, PANEL_H, () -> st.activeTab.title());
        st.skillsList = new SkillListWidget(0, 0, SkillListWidget.gridWidth(), SkillListWidget.preferredHeight(), def -> {
            st.skillsDetails.setSkill(def);
            st.skillsList.setSelected(def == null ? null : def.id());
        });
        st.skillsList.setHeaderVisible(false);
        st.skillsList.reloadSkills();
        st.skillsDetails = new SkillDetailsPanel(0, 0, SkillListWidget.gridWidth(), PANEL_H / 3);
        st.abilityList = new AbilityListWidget(0, 0, AbilityListWidget.gridWidth(), AbilityListWidget.preferredHeight(), def -> {
            st.abilityDetails.setAbility(def);
            st.abilityList.setSelected(def == null ? null : def.id());
        });
        st.abilityList.setHeaderVisible(false);
        st.abilityList.setShowLocked(true);
        st.abilityList.setViewportTweaks(0, 0, 7);
        st.abilityDetails = new AbilityDetailsPanel(0, 0, AbilityListWidget.gridWidth(), PANEL_H / 3);
        st.skillsTab = new PanelTabButton(0, 0, PanelTab.SKILLS, () -> setTab(st, PanelTab.SKILLS));
        st.abilitiesTab = new PanelTabButton(0, 0, PanelTab.ABILITIES, () -> setTab(st, PanelTab.ABILITIES));
        st.playerBottomTab = new BottomPullTabButton(0, 0, Component.translatable("gui.aura.player"), PLAYER_TAB_TEX, PLAYER_TAB_TEX_PULLED, PLAYER_TAB_TEX_SELECTED, () -> setViewMode(st, ViewMode.PLAYER));
        st.settingsBottomTab = new BottomPullTabButton(0, 0, Component.translatable("gui.aura.settings"), SETTINGS_TAB_TEX, SETTINGS_TAB_TEX_PULLED, SETTINGS_TAB_TEX_PULLED_ALT, () -> setViewMode(st, ViewMode.SETTINGS));
        initSettingsRows(st);

        event.addListener(st.btn);

        reposition(inv, st);
        st.recipeBtn = findRecipeButton(inv);
        if (st.recipeBtn != null) {
            st.recipeBtnOffsetX = st.recipeBtn.getX() - inv.getGuiLeft();
            st.recipeBtnOffsetY = st.recipeBtn.getY() - inv.getGuiTop();
        }

        if (lastOpen) {
            st.open = true;
            st.originalLeft = getLeft(inv);
            setLeft(inv, computeCenteredLeft(inv));
        }
        st.activeTab = lastTab;
        st.viewMode = ViewMode.MAIN;
        updateVisibility(st);
        applySkillsVsRecipePanelRule(inv, st);
    }

    public static void onScreenClosing(ScreenEvent.Closing event) {
        State st = STATES.remove(event.getScreen());
        if (st == null) return;
        if (st.open && st.originalLeft != null) {
            setLeft(st.inv, st.originalLeft);
        }
    }

    public static void onScreenRenderPre(ScreenEvent.Render.Pre event) {
        if (AuraClientConfig.disableInventoryCodexBook() || AuraClientConfig.blockOpenSkillsAbilitiesPanel()) return;
        Screen screen = event.getScreen();
        State st = STATES.get(screen);
        if (st == null || !(screen instanceof InventoryScreen inv)) return;

        if (st.open) setLeft(inv, computeCenteredLeft(inv));
        reposition(inv, st);
        updateVisibility(st);
        applySkillsVsRecipePanelRule(inv, st);

        if (st.recipeBtn == null) st.recipeBtn = findRecipeButton(inv);
        if (st.recipeBtn != null && (st.recipeBtnOffsetX == null || st.recipeBtnOffsetY == null)) {
            st.recipeBtnOffsetX = st.recipeBtn.getX() - inv.getGuiLeft();
            st.recipeBtnOffsetY = st.recipeBtn.getY() - inv.getGuiTop();
        }
        updateRecipeButtonPosition(inv, st);
        if (st.open && isRecipePanelOpen(inv)) {
            st.open = false;
            lastOpen = false;
            if (st.originalLeft != null) setLeft(inv, st.originalLeft);
            reposition(inv, st);
            updateVisibility(st);
            applySkillsVsRecipePanelRule(inv, st);
        }
    }

    public static void onScreenRenderPost(ScreenEvent.Render.Post event) {
        if (AuraClientConfig.disableInventoryCodexBook() || AuraClientConfig.blockOpenSkillsAbilitiesPanel()) return;
        Screen screen = event.getScreen();
        State st = STATES.get(screen);
        if (st == null || !st.open || !(screen instanceof InventoryScreen inv)) return;
        event.getGuiGraphics().pose().pushPose();
        event.getGuiGraphics().pose().translate(0.0F, 0.0F, 400.0F);
        st.bg.render(event.getGuiGraphics(), event.getMouseX(), event.getMouseY(), event.getPartialTick());
        st.skillsTab.render(event.getGuiGraphics(), event.getMouseX(), event.getMouseY(), event.getPartialTick());
        st.abilitiesTab.render(event.getGuiGraphics(), event.getMouseX(), event.getMouseY(), event.getPartialTick());
        st.playerBottomTab.render(event.getGuiGraphics(), event.getMouseX(), event.getMouseY(), event.getPartialTick());
        st.settingsBottomTab.render(event.getGuiGraphics(), event.getMouseX(), event.getMouseY(), event.getPartialTick());
        if (st.activeTab == PanelTab.SKILLS) {
            st.skillsList.render(event.getGuiGraphics(), event.getMouseX(), event.getMouseY(), event.getPartialTick());
        } else {
            st.abilityList.render(event.getGuiGraphics(), event.getMouseX(), event.getMouseY(), event.getPartialTick());
        }
        if (st.viewMode == ViewMode.SETTINGS) {
            renderSettingsRows(event.getGuiGraphics(), st, event.getMouseX(), event.getMouseY());
        } else if (st.viewMode == ViewMode.PLAYER) {
            renderPlayerView(event.getGuiGraphics(), st, event.getMouseX(), event.getMouseY());
        }
        renderPointsBadge(event.getGuiGraphics(), st, inv);
        event.getGuiGraphics().pose().popPose();
    }

    public static void onMouseScrolled(ScreenEvent.MouseScrolled.Pre event) {
        if (AuraClientConfig.disableInventoryCodexBook() || AuraClientConfig.blockOpenSkillsAbilitiesPanel()) return;
        State st = STATES.get(event.getScreen());
        if (st == null || !st.open) return;
        if (st.viewMode == ViewMode.SETTINGS) {
            if (!isInSettingsView(st, event.getMouseX(), event.getMouseY())) return;
            int maxScroll = Math.max(0, st.settingsRows.size() * SETTINGS_ROW_H - settingsViewHeight());
            st.settingsScroll = Math.max(0, Math.min(maxScroll, st.settingsScroll - (int) Math.signum(event.getScrollDeltaY()) * 14));
            event.setCanceled(true);
            return;
        }
        if (st.viewMode != ViewMode.MAIN) return;

        double mx = event.getMouseX();
        double my = event.getMouseY();
        double deltaY = event.getScrollDeltaY();
        boolean used = false;

        if (st.activeTab == PanelTab.ABILITIES) {
            used = st.abilityList.mouseScrolled(mx, my, deltaY);
        }

        if (used) event.setCanceled(true);
    }

    public static void onMouseDragged(ScreenEvent.MouseDragged.Pre event) {
        if (AuraClientConfig.disableInventoryCodexBook() || AuraClientConfig.blockOpenSkillsAbilitiesPanel()) return;
        State st = STATES.get(event.getScreen());
        if (st == null || !st.open || st.viewMode != ViewMode.MAIN || st.activeTab != PanelTab.ABILITIES) return;
        if (st.abilityList.mouseDragged(event.getMouseX(), event.getMouseY(), event.getMouseButton(), event.getDragX(), event.getDragY())) {
            event.setCanceled(true);
        }
    }

    public static void onMouseReleased(ScreenEvent.MouseButtonReleased.Pre event) {
        if (AuraClientConfig.disableInventoryCodexBook() || AuraClientConfig.blockOpenSkillsAbilitiesPanel()) return;
        State st = STATES.get(event.getScreen());
        if (st == null || !st.open) return;
        st.abilityList.endDrag();
    }

    public static void onMousePressed(ScreenEvent.MouseButtonPressed.Pre event) {
        if (AuraClientConfig.disableInventoryCodexBook() || AuraClientConfig.blockOpenSkillsAbilitiesPanel()) return;
        State st = STATES.get(event.getScreen());
        if (st == null || !st.open) return;
        if (event.getButton() != 0 && event.getButton() != 1) return;

        double mouseX = event.getMouseX();
        double mouseY = event.getMouseY();
        if (event.getButton() == 0 && st.recipeBtn != null && st.recipeBtn.visible && st.recipeBtn.isMouseOver(mouseX, mouseY) && st.open) {
            toggle(st);
            return;
        }
        if (event.getButton() == 0 && (st.skillsTab.mouseClicked(mouseX, mouseY, event.getButton()) || st.abilitiesTab.mouseClicked(mouseX, mouseY, event.getButton()))) {
            event.setCanceled(true);
            return;
        }
        if (event.getButton() == 0 && (st.playerBottomTab.mouseClicked(mouseX, mouseY, event.getButton()) || st.settingsBottomTab.mouseClicked(mouseX, mouseY, event.getButton()))) {
            event.setCanceled(true);
            return;
        }
        if (st.viewMode == ViewMode.SETTINGS) {
            if (event.getButton() == 0 && isInSettingsView(st, mouseX, mouseY)) {
                int index = (int) ((mouseY - settingsViewY(st) + st.settingsScroll) / SETTINGS_ROW_H);
                if (index >= 0 && index < st.settingsRows.size()) {
                    st.settingsRows.get(index).onClick().run();
                }
            }
            if (isInsideOverlay(st, mouseX, mouseY)) event.setCanceled(true);
            return;
        }
        if (st.viewMode == ViewMode.PLAYER) {
            if (isInsideOverlay(st, mouseX, mouseY)) event.setCanceled(true);
            return;
        }

        boolean used = false;
        if (st.viewMode == ViewMode.MAIN) {
            if (st.activeTab == PanelTab.SKILLS) {
                used = st.skillsList.mouseClicked(mouseX, mouseY, event.getButton());
            } else {
                used = st.abilityList.mouseClicked(mouseX, mouseY, event.getButton());
            }
        }

        if (used || isInsideOverlay(st, mouseX, mouseY)) {
            event.setCanceled(true);
        }
    }

    private static void toggle(State st) {
        if (!st.open && AuraClientConfig.blockOpenSkillsAbilitiesPanel()) return;
        st.open = !st.open;
        lastOpen = st.open;
        if (st.open) {
            if (st.originalLeft == null) st.originalLeft = getLeft(st.inv);
            if (st.recipeBtn != null && isRecipePanelOpen(st.inv)) st.recipeBtn.onPress();
            setLeft(st.inv, computeCenteredLeft(st.inv));
        } else if (st.originalLeft != null) {
            setLeft(st.inv, st.originalLeft);
        }

        reposition(st.inv, st);
        updateVisibility(st);
        applySkillsVsRecipePanelRule(st.inv, st);

        if (st.recipeBtn == null) st.recipeBtn = findRecipeButton(st.inv);
    }

    private static void setTab(State st, PanelTab tab) {
        st.activeTab = tab;
        lastTab = tab;
        st.viewMode = ViewMode.MAIN;
        updateVisibility(st);
    }

    private static void setViewMode(State st, ViewMode viewMode) {
        st.viewMode = viewMode;
        if (viewMode == ViewMode.SETTINGS) {
            st.settingsScroll = Math.max(0, st.settingsScroll);
        }
        updateVisibility(st);
    }

    private static void applySkillsVsRecipePanelRule(InventoryScreen inv, State st) {
        if (st.open) {
            if (st.btn != null) st.btn.visible = true;
            return;
        }

        if (isRecipePanelOpen(inv)) {
            if (st.btn != null) st.btn.visible = false;
        } else {
            if (st.btn != null) st.btn.visible = true;
        }
    }

    private static ImageButton findRecipeButton(InventoryScreen inv) {
        for (var child : inv.children()) {
            if (child instanceof ImageButton btn && btn.getWidth() == 20 && btn.getHeight() == 18) {
                return btn;
            }
        }
        return null;
    }

    private static void updateRecipeButtonPosition(InventoryScreen inv, State st) {
        if (st.recipeBtn == null || st.recipeBtnOffsetX == null || st.recipeBtnOffsetY == null) return;
        st.recipeBtn.setPosition(inv.getGuiLeft() + st.recipeBtnOffsetX, inv.getGuiTop() + st.recipeBtnOffsetY);
    }

    private static boolean isRecipePanelOpen(InventoryScreen inv) {
        int centeredLeft = (inv.width - inv.getXSize()) / 2;
        return inv.getGuiLeft() > centeredLeft + 10;
    }

    private static int computeCenteredLeft(InventoryScreen inv) {
        int total = PANEL_W + 2 + inv.getXSize();
        return (inv.width - total) / 2 + PANEL_W + 2;
    }

    private static int computePanelX(InventoryScreen inv) {
        return inv.getGuiLeft() - PANEL_W - 2;
    }

    private static void reposition(InventoryScreen inv, State st) {
        if (st.btn != null) st.btn.setPosition(inv.getGuiLeft() + 145, inv.getGuiTop() + 61);

        int bgx = computePanelX(inv);
        int bgy = inv.getGuiTop();
        int innerLeft = bgx + INNER_PAD_X;
        int innerRight = bgx + PANEL_W - INNER_PAD_X;
        int innerTop = bgy + INNER_PAD_TOP;
        int innerBottom = bgy + PANEL_H - INNER_PAD_BOTTOM;

        int listW = SkillListWidget.gridWidth();
        int abilityW = AbilityListWidget.gridWidth();
        int listX = bgx + (PANEL_W - listW) / 2;
        int abilityX = bgx + (PANEL_W - abilityW) / 2;
        int listY = innerTop;
        int detailsH = PANEL_H / 3 + 10;
        int detailsW = Math.max(20, (innerRight - innerLeft) - 5);
        int detailsX = innerLeft + 2;
        int detailsY = innerBottom - detailsH - 5;

        st.bg.setBounds(bgx, bgy, PANEL_W, PANEL_H);
        st.skillsList.setBounds(listX, listY, listW, SkillListWidget.preferredHeight());
        st.skillsDetails.setBounds(detailsX, detailsY, detailsW, detailsH);
        st.abilityList.setBounds(abilityX, listY, abilityW, AbilityListWidget.preferredHeight());
        st.abilityDetails.setBounds(detailsX + 1, detailsY + 3, detailsW, detailsH);
        st.skillsTab.setPosition(bgx - 31, bgy + 6);
        st.abilitiesTab.setPosition(bgx - 31, bgy + 34);
        st.playerBottomTab.setPosition(bgx + 4, bgy + PANEL_H - 3);
        st.settingsBottomTab.setPosition(bgx + 4 + 32 + 2, bgy + PANEL_H - 3);
    }

    private static Integer getLeft(InventoryScreen inv) {
        try {
            if (LEFT_FIELD == null) LEFT_FIELD = findLeftField(inv.getClass());
            return (Integer) LEFT_FIELD.get(inv);
        } catch (Throwable t) {
            return inv.getGuiLeft();
        }
    }

    private static void setLeft(InventoryScreen inv, int v) {
        try {
            if (LEFT_FIELD == null) LEFT_FIELD = findLeftField(inv.getClass());
            LEFT_FIELD.setInt(inv, v);
        } catch (Throwable ignored) {}
    }

    private static Field findLeftField(Class<?> c) throws NoSuchFieldException {
        Class<?> cur = c;
        while (cur != null) {
            try {
                Field f = cur.getDeclaredField("leftPos");
                f.setAccessible(true);
                return f;
            } catch (NoSuchFieldException ignored) {
                cur = cur.getSuperclass();
            }
        }
        throw new NoSuchFieldException("leftPos");
    }

    private static void updateVisibility(State st) {
        boolean main = st.viewMode == ViewMode.MAIN;
        boolean skillsActive = st.open && main && st.activeTab == PanelTab.SKILLS;
        boolean abilitiesActive = st.open && main && st.activeTab == PanelTab.ABILITIES;

        st.bg.visible = st.open;
        st.bg.active = st.open;
        st.skillsTab.visible = st.open;
        st.skillsTab.active = st.open;
        st.skillsTab.setSelected(main && st.activeTab == PanelTab.SKILLS);
        st.abilitiesTab.visible = st.open;
        st.abilitiesTab.active = st.open;
        st.abilitiesTab.setSelected(main && st.activeTab == PanelTab.ABILITIES);
        st.playerBottomTab.visible = st.open;
        st.playerBottomTab.active = st.open;
        st.playerBottomTab.setSelected(st.viewMode == ViewMode.PLAYER);
        st.settingsBottomTab.visible = st.open;
        st.settingsBottomTab.active = st.open;
        st.settingsBottomTab.setSelected(st.viewMode == ViewMode.SETTINGS);

        st.skillsList.visible = skillsActive;
        st.skillsList.active = skillsActive;
        st.skillsDetails.visible = false;
        st.skillsDetails.active = false;
        st.skillsDetails.upgradeButton().visible = false;
        st.skillsDetails.upgradeButton().active = false;
        st.skillsDetails.downgradeButton().visible = false;
        st.skillsDetails.downgradeButton().active = false;

        st.abilityList.visible = abilitiesActive;
        st.abilityList.active = abilitiesActive;
        st.abilityDetails.visible = false;
        st.abilityDetails.active = false;
        st.abilityDetails.upgradeButton().visible = false;
        st.abilityDetails.upgradeButton().active = false;
        st.abilityDetails.downgradeButton().visible = false;
        st.abilityDetails.downgradeButton().active = false;
        st.abilityDetails.selectButton().visible = false;
        st.abilityDetails.selectButton().active = false;
    }

    private static void renderPointsBadge(GuiGraphics gg, State st, InventoryScreen inv) {
        if (st.viewMode != ViewMode.MAIN) return;
        var mc = net.minecraft.client.Minecraft.getInstance();
        if (mc.player == null) return;

        boolean abilitiesTab = st.activeTab == PanelTab.ABILITIES;
        int points = abilitiesTab
                ? mc.player.getData(AbilitiesAttachments.PLAYER_ABILITIES.get()).points()
                : mc.player.getData(SkillsAttachments.PLAYER_SKILLS.get()).points();

        String label = Component.translatable(abilitiesTab ? "gui.aura.points.ability" : "gui.aura.points.skill", points).getString();
        float scale = 0.85F;
        int x = (abilitiesTab ? st.abilityList.getX() + 10 : st.skillsList.getX()) + 13;
        int y = (abilitiesTab ? st.abilityList.getY() : st.skillsList.getY()) + 4;
        int color = abilitiesTab ? 0xC78CFF : 0x6AB2FF;
        gg.pose().pushPose();
        gg.pose().translate(x, y, 0.0F);
        gg.pose().scale(scale, scale, 1.0F);
        gg.drawString(mc.font, label, 0, 0, color, false);
        gg.pose().popPose();
    }

    private static boolean isInsideOverlay(State st, double mouseX, double mouseY) {
        int left = st.bg.getX();
        int top = st.bg.getY();
        int right = left + PANEL_W;
        int bottom = top + PANEL_H;
        int tabLeft = left - 31;
        return mouseX >= tabLeft && mouseX <= right && mouseY >= top && mouseY <= bottom;
    }

    private static void initSettingsRows(State st) {
        st.settingsRows.clear();
        st.settingsRows.add(new SettingRow(Component.translatable("gui.aura.settings.hud_display").getString(), () -> boolState(AuraClientConfig.hudDisplayEnabled()), AuraClientConfig::toggleHudDisplayEnabled));
        st.settingsRows.add(new SettingRow(Component.translatable("gui.aura.settings.disable_inventory_book").getString(), () -> boolState(AuraClientConfig.disableInventoryCodexBook()), AuraClientConfig::toggleDisableInventoryCodexBook));
        st.settingsRows.add(new SettingRow(Component.translatable("gui.aura.settings.reposition_hud").getString(), () -> Component.translatable("gui.aura.hud_position." + AuraClientConfig.hudPosition().name().toLowerCase(java.util.Locale.ROOT)).getString(), AuraClientConfig::cycleHudPosition));
        st.settingsRows.add(new SettingRow(Component.translatable("gui.aura.settings.disable_skills_abilities").getString(), () -> boolState(AuraClientConfig.disableSkillsAndAbilities()), AuraClientConfig::toggleDisableSkillsAndAbilities));
        st.settingsRows.add(new SettingRow(Component.translatable("gui.aura.settings.block_ability_switching").getString(), () -> boolState(AuraClientConfig.blockAbilitySwitching()), AuraClientConfig::toggleBlockAbilitySwitching));
        st.settingsRows.add(new SettingRow(Component.translatable("gui.aura.settings.block_upgrade_downgrade").getString(), () -> boolState(AuraClientConfig.blockUpgradeDowngrade()), AuraClientConfig::toggleBlockUpgradeDowngrade));
        st.settingsRows.add(new SettingRow(Component.translatable("gui.aura.settings.block_open_panel").getString(), () -> boolState(AuraClientConfig.blockOpenSkillsAbilitiesPanel()), AuraClientConfig::toggleBlockOpenSkillsAbilitiesPanel));
        st.settingsRows.add(new SettingRow(Component.translatable("gui.aura.settings.levelup_values").getString(), () -> Component.translatable("gui.aura.open").getString(), () -> net.minecraft.client.Minecraft.getInstance().setScreen(new LevelUpConfigScreen(net.minecraft.client.Minecraft.getInstance().screen))));
    }

    private static int settingsViewY(State st) {
        return st.bg.getY() + SETTINGS_VIEW_TOP;
    }

    private static int settingsViewHeight() {
        return PANEL_H - SETTINGS_VIEW_TOP - SETTINGS_VIEW_BOTTOM_PAD;
    }

    private static boolean isInSettingsView(State st, double mouseX, double mouseY) {
        int x0 = st.bg.getX() + SETTINGS_VIEW_PAD_X;
        int x1 = st.bg.getX() + PANEL_W - SETTINGS_VIEW_PAD_X;
        int y0 = settingsViewY(st);
        int y1 = y0 + settingsViewHeight();
        return mouseX >= x0 && mouseX <= x1 && mouseY >= y0 && mouseY <= y1;
    }

    private static void renderSettingsRows(GuiGraphics gg, State st, int mouseX, int mouseY) {
        var mc = net.minecraft.client.Minecraft.getInstance();
        int xLeft = st.bg.getX() + SETTINGS_VIEW_PAD_X + 7;
        int xRight = st.bg.getX() + PANEL_W - SETTINGS_VIEW_PAD_X - 7;
        int y0 = settingsViewY(st);
        int y1 = y0 + settingsViewHeight();
        float scale = 0.5F;

        gg.enableScissor(st.bg.getX() + SETTINGS_VIEW_PAD_X, y0, st.bg.getX() + PANEL_W - SETTINGS_VIEW_PAD_X, y1);
        for (int i = 0; i < st.settingsRows.size(); i++) {
            int y = y0 + i * SETTINGS_ROW_H - st.settingsScroll;
            if (y + SETTINGS_ROW_H < y0 || y > y1) continue;
            boolean hovered = mouseX >= st.bg.getX() + SETTINGS_VIEW_PAD_X && mouseX <= st.bg.getX() + PANEL_W - SETTINGS_VIEW_PAD_X && mouseY >= y && mouseY <= y + SETTINGS_ROW_H;
            int labelColor = hovered ? 0xFFFF55 : 0xFFFFFF;
            int stateColor = 0x6AB2FF;
            String label = st.settingsRows.get(i).label();
            String state = st.settingsRows.get(i).state().get();
            int labelY = y + 1;
            drawScaledText(gg, mc, label, xLeft, labelY, labelColor, scale);
            int stateW = (int) (mc.font.width(state) * scale);
            drawScaledText(gg, mc, state, xRight - stateW, labelY, stateColor, scale);
        }
        gg.disableScissor();
    }

    private static void drawScaledText(GuiGraphics gg, net.minecraft.client.Minecraft mc, String text, int x, int y, int color, float scale) {
        gg.pose().pushPose();
        gg.pose().translate(x, y, 0.0F);
        gg.pose().scale(scale, scale, 1.0F);
        gg.drawString(mc.font, text, 0, 0, color, false);
        gg.pose().popPose();
    }

    private static Component drawHoverStat(GuiGraphics gg, Component currentTooltip, net.minecraft.client.Minecraft mc, int mouseX, int mouseY, int baseX, int baseY, String text, int color, float scale, int yOffset) {
        int y = baseY + yOffset;
        int w = (int) (mc.font.width(text) * scale);
        boolean hovered = mouseX >= baseX && mouseX <= baseX + w + 2 && mouseY >= y && mouseY <= y + 7;
        drawScaledText(gg, mc, text, baseX, y, color, hovered ? scale * 1.05F : scale);
        if (currentTooltip == null && hovered) return Component.literal(text);
        return currentTooltip;
    }

    private static Component tooltipIfHovered(Component currentTooltip, net.minecraft.client.Minecraft mc, int mouseX, int mouseY, int x, int y, String text, float scale) {
        if (currentTooltip != null) return currentTooltip;
        int w = (int) (mc.font.width(text) * scale);
        int h = Math.max(6, (int) (mc.font.lineHeight * scale));
        if (mouseX >= x && mouseX <= x + w + 2 && mouseY >= y && mouseY <= y + h) return Component.literal(text);
        return null;
    }

    private static void renderPlayerView(GuiGraphics gg, State st, int mouseX, int mouseY) {
        var mc = net.minecraft.client.Minecraft.getInstance();
        if (mc.player == null) return;
        Component hoveredTooltip = null;

        int dollX = st.bg.getX() + 35;
        int dollY = st.bg.getY() + PANEL_H - 22;
        int dollSize = 36;
        gg.pose().pushPose();
        gg.pose().translate(0.0F, 0.0F, 1000.0F);
        InventoryScreen.renderEntityInInventoryFollowsMouse(gg, dollX, dollY, dollSize, dollX - mouseX, dollY - 26 - mouseY, 30.0F, 0.0F, 0.0F, mc.player);
        gg.pose().popPose();

        var player = mc.player;
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

        int textX = st.bg.getX() + 11;
        int textY = st.bg.getY() + 18;
        int lineH = 9;
        float scale = 0.65F;
        String levelText = Component.translatable("gui.aura.player.level", LevelUpApi.getLevel(player)).getString();
        drawScaledText(gg, mc, levelText, st.bg.getX() + 14, st.bg.getY() + 10, 0xFFE08A, 0.75F);
        hoveredTooltip = tooltipIfHovered(hoveredTooltip, mc, mouseX, mouseY, st.bg.getX() + 14, st.bg.getY() + 10, levelText, 0.75F);
        hoveredTooltip = drawHoverStat(gg, hoveredTooltip, mc, mouseX, mouseY, textX, textY, Component.translatable("gui.aura.player.base_damage", fmt(baseDamage)).getString(), 0xFF8080, scale, lineH * 0);
        hoveredTooltip = drawHoverStat(gg, hoveredTooltip, mc, mouseX, mouseY, textX, textY, Component.translatable("gui.aura.player.crit_power", fmt(critPower) + "x").getString(), 0xFFD580, scale, lineH * 1);
        hoveredTooltip = drawHoverStat(gg, hoveredTooltip, mc, mouseX, mouseY, textX, textY, Component.translatable("gui.aura.player.ability_power", fmt(abilityPower)).getString(), 0xC78CFF, scale, lineH * 2);
        hoveredTooltip = drawHoverStat(gg, hoveredTooltip, mc, mouseX, mouseY, textX, textY, Component.translatable("gui.aura.player.defence", defence).getString(), 0x8CD3FF, scale, lineH * 3);
        hoveredTooltip = drawHoverStat(gg, hoveredTooltip, mc, mouseX, mouseY, textX, textY, Component.translatable("gui.aura.player.speed", fmt(speed)).getString(), 0x99FFB6, scale, lineH * 4);
        hoveredTooltip = drawHoverStat(gg, hoveredTooltip, mc, mouseX, mouseY, textX, textY, Component.translatable("gui.aura.player.leeching", fmt(leechChance) + "%").getString(), 0xFF9CC8, scale, lineH * 5);
        hoveredTooltip = drawHoverStat(gg, hoveredTooltip, mc, mouseX, mouseY, textX, textY, Component.translatable("gui.aura.player.health", fmt(maxHealth)).getString(), 0xFFB3B3, scale, lineH * 6);

        int listY = st.bg.getY() + 85;
        int leftColX = st.bg.getX() + 11;
        int rightColX = st.bg.getX() + 80;
        String abilitiesHdr = Component.translatable("gui.aura.player.selected_abilities").getString();
        String skillsHdr = Component.translatable("gui.aura.player.skills").getString();
        drawScaledText(gg, mc, abilitiesHdr, leftColX, listY, 0xE0E0E0, 0.65F);
        drawScaledText(gg, mc, skillsHdr, rightColX, listY, 0xE0E0E0, 0.65F);
        hoveredTooltip = tooltipIfHovered(hoveredTooltip, mc, mouseX, mouseY, leftColX, listY, abilitiesHdr, 0.65F);
        hoveredTooltip = tooltipIfHovered(hoveredTooltip, mc, mouseX, mouseY, rightColX, listY, skillsHdr, 0.65F);
        int row = 0;
        for (AbilityElement element : AbilityElement.values()) {
            AbilityId selected = playerAbilities.selectedSpecialization(element);
            if (selected == null) continue;
            AbilityDefinition def = AbilityRegistry.def(selected);
            if (def == null) continue;
            int y = listY + 8 + (row * 9);
            String line = Component.translatable("gui.aura.player.ability_bind", selected.title(), AbilityKeybinds.keyName(selected)).getString();
            boolean hovered = mouseX >= leftColX && mouseX <= leftColX + 68 && mouseY >= y && mouseY <= y + 8;
            drawScaledText(gg, mc, line, leftColX, y + 1, elementColor(def.element()), hovered ? 0.525F : 0.5F);
            hoveredTooltip = tooltipIfHovered(hoveredTooltip, mc, mouseX, mouseY, leftColX, y + 1, line, hovered ? 0.525F : 0.5F);
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
            drawScaledText(gg, mc, line, rightColX, y + 1, skillColor(def.id()), hovered ? 0.525F : 0.5F);
            hoveredTooltip = tooltipIfHovered(hoveredTooltip, mc, mouseX, mouseY, rightColX, y + 1, line, hovered ? 0.525F : 0.5F);
            skillRow++;
            if (skillRow > 6) break;
        }
        if (hoveredTooltip != null) gg.renderTooltip(mc.font, hoveredTooltip, mouseX, mouseY - 6);
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

    private static final class PanelBackground extends AbstractWidget {
        private final Supplier<String> titleSupplier;

        private PanelBackground(int x, int y, int w, int h, Supplier<String> titleSupplier) {
            super(x, y, w, h, Component.empty());
            this.titleSupplier = titleSupplier;
        }

        public void setBounds(int x, int y, int w, int h) {
            setX(x);
            setY(y);
            width = w;
            height = h;
        }

        @Override
        protected void renderWidget(GuiGraphics gg, int mouseX, int mouseY, float partialTick) {
            RenderSystem.disableBlend();
            gg.blit(PANEL_TEX, getX(), getY(), 0, 0, width, height, width, height);
            SkillPanelHeaderRenderer.draw(
                    gg,
                    net.minecraft.client.Minecraft.getInstance().font,
                    getX() + HEADER_OFFSET_X,
                    getY() - SkillPanelHeaderRenderer.height() + HEADER_OFFSET_Y,
                    titleSupplier.get()
            );
        }

        @Override
        protected void updateWidgetNarration(NarrationElementOutput narration) {}
    }

    private static final class State {
        final InventoryScreen inv;
        SkillsToggleButton btn;
        PanelBackground bg;
        SkillListWidget skillsList;
        SkillDetailsPanel skillsDetails;
        AbilityListWidget abilityList;
        AbilityDetailsPanel abilityDetails;
        PanelTabButton skillsTab;
        PanelTabButton abilitiesTab;
        ImageButton recipeBtn;
        Integer recipeBtnOffsetX;
        Integer recipeBtnOffsetY;
        boolean open;
        Integer originalLeft;
        PanelTab activeTab = PanelTab.SKILLS;
        BottomPullTabButton playerBottomTab;
        BottomPullTabButton settingsBottomTab;
        ViewMode viewMode = ViewMode.MAIN;
        final List<SettingRow> settingsRows = new ArrayList<>();
        int settingsScroll = 0;

        State(InventoryScreen inv) {
            this.inv = inv;
        }
    }

    private enum ViewMode {
        MAIN,
        PLAYER,
        SETTINGS
    }

    private record SettingRow(String label, Supplier<String> state, Runnable onClick) {}
}

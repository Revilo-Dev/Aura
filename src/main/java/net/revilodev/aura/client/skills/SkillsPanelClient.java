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
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.revilodev.aura.CodexMod;
import net.revilodev.aura.abilities.AbilitiesAttachments;
import net.revilodev.aura.abilities.PlayerAbilities;
import net.revilodev.aura.client.AuraClientConfig;
import net.revilodev.aura.client.BottomPullTabButton;
import net.revilodev.aura.client.PanelTab;
import net.revilodev.aura.client.PanelTabButton;
import net.revilodev.aura.client.SkillsToggleButton;
import net.revilodev.aura.client.abilities.AbilityDetailsPanel;
import net.revilodev.aura.client.abilities.AbilityListWidget;
import net.revilodev.aura.skills.SkillsAttachments;

import java.lang.reflect.Field;
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
            ResourceLocation.fromNamespaceAndPath(CodexMod.MOD_ID, "textures/gui/sprites/pull-tab-settings_pulled.png");

    private static final int PANEL_W = 147;
    private static final int PANEL_H = 166;
    private static final int INNER_PAD_X = 6;
    private static final int INNER_PAD_TOP = 5;
    private static final int INNER_PAD_BOTTOM = 6;
    private static final int HEADER_OFFSET_X = 5;
    private static final int HEADER_OFFSET_Y = 3;

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
        if (AuraClientConfig.disableInventoryCodexBook()) return;
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
        st.abilityList.setViewportTweaks(0, 0);
        st.abilityDetails = new AbilityDetailsPanel(0, 0, AbilityListWidget.gridWidth(), PANEL_H / 3);
        st.skillsTab = new PanelTabButton(0, 0, PanelTab.SKILLS, () -> setTab(st, PanelTab.SKILLS));
        st.abilitiesTab = new PanelTabButton(0, 0, PanelTab.ABILITIES, () -> setTab(st, PanelTab.ABILITIES));
        st.playerBottomTab = new BottomPullTabButton(0, 0, Component.literal("Player"), PLAYER_TAB_TEX, PLAYER_TAB_TEX_PULLED, PLAYER_TAB_TEX_SELECTED, () -> setViewMode(st, ViewMode.PLAYER));
        st.settingsBottomTab = new BottomPullTabButton(0, 0, Component.literal("Settings"), SETTINGS_TAB_TEX, SETTINGS_TAB_TEX_PULLED_ALT, SETTINGS_TAB_TEX_PULLED_ALT, () -> setViewMode(st, ViewMode.SETTINGS));

        event.addListener(st.btn);

        reposition(inv, st);
        st.recipeBtn = findRecipeButton(inv);

        if (lastOpen) {
            st.open = true;
            st.originalLeft = getLeft(inv);
            setLeft(inv, computeCenteredLeft(inv));
        }
        st.activeTab = lastTab;
        st.viewMode = ViewMode.MAIN;
        updateVisibility(st);
        applySkillsVsRecipePanelRule(inv, st);
        if (st.open) forceHideRecipeButtonIfSkillsOpen(st);
    }

    public static void onScreenClosing(ScreenEvent.Closing event) {
        State st = STATES.remove(event.getScreen());
        if (st == null) return;
        if (st.open && st.originalLeft != null) {
            setLeft(st.inv, st.originalLeft);
        }
    }

    public static void onScreenRenderPre(ScreenEvent.Render.Pre event) {
        if (AuraClientConfig.disableInventoryCodexBook()) return;
        Screen screen = event.getScreen();
        State st = STATES.get(screen);
        if (st == null || !(screen instanceof InventoryScreen inv)) return;

        if (st.open) setLeft(inv, computeCenteredLeft(inv));
        reposition(inv, st);
        updateVisibility(st);
        applySkillsVsRecipePanelRule(inv, st);

        if (st.recipeBtn == null) st.recipeBtn = findRecipeButton(inv);
        if (st.open) forceHideRecipeButtonIfSkillsOpen(st);
        else restoreRecipeButtonIfWeHidIt(inv, st);
    }

    public static void onScreenRenderPost(ScreenEvent.Render.Post event) {
        if (AuraClientConfig.disableInventoryCodexBook()) return;
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
        renderPointsBadge(event.getGuiGraphics(), st, inv);
        event.getGuiGraphics().pose().popPose();
    }

    public static void onMouseScrolled(ScreenEvent.MouseScrolled.Pre event) {
        if (AuraClientConfig.disableInventoryCodexBook()) return;
        State st = STATES.get(event.getScreen());
        if (st == null || !st.open) return;
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
        if (AuraClientConfig.disableInventoryCodexBook()) return;
        State st = STATES.get(event.getScreen());
        if (st == null || !st.open || st.viewMode != ViewMode.MAIN || st.activeTab != PanelTab.ABILITIES) return;
        if (st.abilityList.mouseDragged(event.getMouseX(), event.getMouseY(), event.getMouseButton(), event.getDragX(), event.getDragY())) {
            event.setCanceled(true);
        }
    }

    public static void onMouseReleased(ScreenEvent.MouseButtonReleased.Pre event) {
        if (AuraClientConfig.disableInventoryCodexBook()) return;
        State st = STATES.get(event.getScreen());
        if (st == null || !st.open) return;
        st.abilityList.endDrag();
    }

    public static void onMousePressed(ScreenEvent.MouseButtonPressed.Pre event) {
        if (AuraClientConfig.disableInventoryCodexBook()) return;
        State st = STATES.get(event.getScreen());
        if (st == null || !st.open) return;
        if (event.getButton() != 0 && event.getButton() != 1) return;

        double mouseX = event.getMouseX();
        double mouseY = event.getMouseY();
        if (event.getButton() == 0 && (st.skillsTab.mouseClicked(mouseX, mouseY, event.getButton()) || st.abilitiesTab.mouseClicked(mouseX, mouseY, event.getButton()))) {
            event.setCanceled(true);
            return;
        }
        if (event.getButton() == 0 && (st.playerBottomTab.mouseClicked(mouseX, mouseY, event.getButton()) || st.settingsBottomTab.mouseClicked(mouseX, mouseY, event.getButton()))) {
            event.setCanceled(true);
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
        st.open = !st.open;
        lastOpen = st.open;
        if (st.open) {
            if (st.originalLeft == null) st.originalLeft = getLeft(st.inv);
            setLeft(st.inv, computeCenteredLeft(st.inv));
        } else if (st.originalLeft != null) {
            setLeft(st.inv, st.originalLeft);
        }

        reposition(st.inv, st);
        updateVisibility(st);
        applySkillsVsRecipePanelRule(st.inv, st);

        if (st.recipeBtn == null) st.recipeBtn = findRecipeButton(st.inv);
        if (st.open) forceHideRecipeButtonIfSkillsOpen(st);
        else restoreRecipeButtonIfWeHidIt(st.inv, st);
    }

    private static void setTab(State st, PanelTab tab) {
        st.activeTab = tab;
        lastTab = tab;
        st.viewMode = ViewMode.MAIN;
        updateVisibility(st);
    }

    private static void setViewMode(State st, ViewMode viewMode) {
        st.viewMode = viewMode;
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

    private static void forceHideRecipeButtonIfSkillsOpen(State st) {
        if (st.recipeBtn == null) return;
        st.recipeBtn.visible = false;
        st.recipeBtn.active = false;
        st.recipeHiddenBySkills = true;
    }

    private static void restoreRecipeButtonIfWeHidIt(InventoryScreen inv, State st) {
        if (!st.recipeHiddenBySkills) return;
        if (isQuestPanelOpen(inv)) {
            st.recipeHiddenBySkills = false;
            return;
        }
        if (st.recipeBtn == null) st.recipeBtn = findRecipeButton(inv);
        if (st.recipeBtn != null) {
            st.recipeBtn.visible = true;
            st.recipeBtn.active = true;
        }
        st.recipeHiddenBySkills = false;
    }

    private static ImageButton findRecipeButton(InventoryScreen inv) {
        for (var child : inv.children()) {
            if (child instanceof ImageButton btn && btn.getWidth() == 20 && btn.getHeight() == 18) {
                return btn;
            }
        }
        return null;
    }

    private static boolean isQuestPanelOpen(InventoryScreen inv) {
        for (var child : inv.children()) {
            if (child instanceof AbstractWidget w) {
                String name = child.getClass().getName();
                if (name.equals("net.revilodev.boundless.client.QuestPanelClient$PanelBackground") && w.visible) {
                    return true;
                }
            }
        }
        return false;
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

        String label = abilitiesTab ? "Ability Points: " + points : "Skill Points: " + points;
        float scale = 0.85F;
        int x = (abilitiesTab ? st.abilityList.getX() + 15 : st.skillsList.getX()) + 1;
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
        boolean recipeHiddenBySkills;
        boolean open;
        Integer originalLeft;
        PanelTab activeTab = PanelTab.SKILLS;
        BottomPullTabButton playerBottomTab;
        BottomPullTabButton settingsBottomTab;
        ViewMode viewMode = ViewMode.MAIN;

        State(InventoryScreen inv) {
            this.inv = inv;
        }
    }

    private enum ViewMode {
        MAIN,
        PLAYER,
        SETTINGS
    }
}

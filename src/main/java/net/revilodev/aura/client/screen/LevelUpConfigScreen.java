package net.revilodev.aura.client.screen;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.neoforged.fml.loading.FMLPaths;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.IntConsumer;

public final class LevelUpConfigScreen extends Screen {
    private final Screen parent;
    private final List<Row> rows = new ArrayList<>();
    private int scroll;

    private static final int ROW_H = 12;
    private static final int PAD_X = 16;
    private static final int TOP = 40;
    private static final int BOTTOM = 38;
    private static final int INPUT_W = 90;

    private EditBox levelHudColorInput;
    private EditBox hudOffsetXInput;
    private EditBox hudOffsetYInput;
    private EditBox inventoryOffsetXInput;
    private EditBox inventoryOffsetYInput;

    private static final class LevelUpConfigStore {
        boolean showTopCenterLevelOverlay = true;
        boolean showTemporaryLevelOverlay = true;
        boolean showInventoryLevelBar = true;
        String levelHudPosition = "top";
        boolean levelHudStayOnScreen = false;
        String levelHudColor = "#53A4BC";
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

    private final LevelUpConfigStore store = new LevelUpConfigStore();

    public LevelUpConfigScreen(Screen parent) {
        super(Component.translatable("gui.aura.levelup.title"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        load();
        rows.clear();
        initRows();
        initInputs();
        addRenderableWidget(Button.builder(Component.translatable("gui.aura.back"), b -> onClose()).bounds(width / 2 - 70, height - 26, 140, 20).build());
    }

    private void initRows() {
        rows.add(Row.section("gui.aura.levelup.section.client_hud"));
        rows.add(Row.bool("gui.aura.levelup.show_top_center_level_overlay", () -> store.showTopCenterLevelOverlay, v -> store.showTopCenterLevelOverlay = v));
        rows.add(Row.bool("gui.aura.levelup.show_temporary_level_overlay", () -> store.showTemporaryLevelOverlay, v -> store.showTemporaryLevelOverlay = v));
        rows.add(Row.bool("gui.aura.levelup.show_inventory_level_bar", () -> store.showInventoryLevelBar, v -> store.showInventoryLevelBar = v));
        rows.add(Row.enumTopBottom("gui.aura.levelup.level_hud_position", () -> store.levelHudPosition, v -> store.levelHudPosition = v));
        rows.add(Row.bool("gui.aura.levelup.level_hud_stay_on_screen", () -> store.levelHudStayOnScreen, v -> store.levelHudStayOnScreen = v));
        rows.add(Row.textInput("gui.aura.levelup.level_hud_color"));
        rows.add(Row.textInput("gui.aura.levelup.hud_level_bar_offset_x"));
        rows.add(Row.textInput("gui.aura.levelup.hud_level_bar_offset_y"));
        rows.add(Row.textInput("gui.aura.levelup.inventory_level_bar_offset_x"));
        rows.add(Row.textInput("gui.aura.levelup.inventory_level_bar_offset_y"));
        rows.add(Row.action("gui.aura.levelup.open_hud_reposition", this::openHudReposition));
        rows.add(Row.action("gui.aura.levelup.open_inventory_reposition", this::openInventoryReposition));

        rows.add(Row.section("gui.aura.levelup.section.progression"));
        rows.add(Row.intRow("gui.aura.levelup.base_xp_per_level", () -> store.baseXpPerLevel, v -> store.baseXpPerLevel = v, 1, 0, 1_000_000));
        rows.add(Row.intRow("gui.aura.levelup.linear_xp_per_level", () -> store.linearXpPerLevel, v -> store.linearXpPerLevel = v, 1, 0, 1_000_000));
        rows.add(Row.doubleRow("gui.aura.levelup.exponent", () -> store.exponent, v -> store.exponent = v, 0.01D, 0.01D, 100.0D));
        rows.add(Row.doubleRow("gui.aura.levelup.level_multiplier", () -> store.levelMultiplier, v -> store.levelMultiplier = v, 0.01D, 0.0D, 100.0D));
        rows.add(Row.intRow("gui.aura.levelup.max_level", () -> store.maxLevel, v -> store.maxLevel = v, 1, 1, 1_000_000));

        rows.add(Row.section("gui.aura.levelup.section.sources"));
        rows.add(Row.bool("gui.aura.levelup.enable_mob_kill_xp", () -> store.enableMobKillXp, v -> store.enableMobKillXp = v));
        rows.add(Row.intRow("gui.aura.levelup.mob_kill_xp", () -> store.mobKillXp, v -> store.mobKillXp = v, 1, 0, 1_000_000));
        rows.add(Row.bool("gui.aura.levelup.drop_levels_only_from_mobs_with_tag", () -> store.dropLevelsOnlyFromMobsWithTag, v -> store.dropLevelsOnlyFromMobsWithTag = v));
    }

    @Override
    public void render(GuiGraphics gg, int mouseX, int mouseY, float partialTick) {
        renderBackground(gg, mouseX, mouseY, partialTick);
        gg.pose().pushPose();
        gg.pose().translate(0.0F, 0.0F, 200.0F);
        gg.drawCenteredString(font, title, width / 2, 12, 0xFFFFFF);
        gg.drawCenteredString(font, Component.translatable("gui.aura.levelup.hint"), width / 2, 24, 0xAFAFAF);

        int xLeft = PAD_X;
        int xRight = width - PAD_X;
        positionInputs(xRight);
        int y0 = TOP;
        int y1 = height - BOTTOM;
        gg.enableScissor(0, y0, width, y1);
        for (int i = 0; i < rows.size(); i++) {
            int y = y0 + i * ROW_H - scroll;
            if (y + ROW_H < y0 || y > y1) continue;
            Row row = rows.get(i);
            boolean hovered = mouseX >= xLeft && mouseX <= xRight && mouseY >= y && mouseY <= y + ROW_H - 1;
            int labelColor = row.section ? 0xFFE08A : (hovered ? 0xFFFF55 : 0xFFFFFF);
            gg.drawString(font, row.label(), xLeft, y + 2, labelColor, false);
            if (row.textInput) continue;
            String state = row.currentState();
                int stateW = font.width(state);
                gg.drawString(font, state, xRight - stateW, y + 2, 0x6AB2FF, false);
        }
        gg.disableScissor();
        super.render(gg, mouseX, mouseY, partialTick);
        gg.pose().popPose();
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0 || button == 1) {
            int index = (int) ((mouseY - TOP + scroll) / ROW_H);
            if (index >= 0 && index < rows.size()) {
                Row row = rows.get(index);
                if (!row.section && !row.textInput) {
                    row.adjust(button == 0 ? 1 : -1);
                    save();
                    return true;
                }
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        int maxScroll = Math.max(0, rows.size() * ROW_H - (height - TOP - BOTTOM));
        scroll = Math.max(0, Math.min(maxScroll, scroll - (int) Math.signum(scrollY) * 12));
        return true;
    }

    @Override
    public void onClose() {
        commitInputValues();
        save();
        minecraft.setScreen(parent);
    }

    private static int clampInt(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    private void openHudReposition() {
        if (!invokeClientApi("openHudLevelBarRepositionGui")) {
            store.openHudLevelBarRepositionGui = true;
        }
    }

    private void openInventoryReposition() {
        if (!invokeClientApi("openInventoryLevelBarRepositionGui")) {
            store.openInventoryLevelBarRepositionGui = true;
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

    private Path clientFile() {
        return FMLPaths.CONFIGDIR.get().resolve("levelup-client.toml");
    }

    private Path commonFile() {
        return FMLPaths.CONFIGDIR.get().resolve("levelup-common.toml");
    }

    private void load() {
        List<String> client = readLines(clientFile());
        List<String> common = readLines(commonFile());

        store.showTopCenterLevelOverlay = readBool(client, "showTopCenterLevelOverlay", store.showTopCenterLevelOverlay);
        store.showTemporaryLevelOverlay = readBool(client, "showTemporaryLevelOverlay", store.showTemporaryLevelOverlay);
        store.showInventoryLevelBar = readBool(client, "showInventoryLevelBar", store.showInventoryLevelBar);
        store.levelHudPosition = readString(client, "levelHudPosition", store.levelHudPosition);
        store.levelHudStayOnScreen = readBool(client, "levelHudStayOnScreen", store.levelHudStayOnScreen);
        store.levelHudColor = normalizeColor(readString(client, "levelHudColor", "#53a4bc"), store.levelHudColor);
        store.hudLevelBarOffsetX = readInt(client, "hudLevelBarOffsetX", store.hudLevelBarOffsetX);
        store.hudLevelBarOffsetY = readInt(client, "hudLevelBarOffsetY", store.hudLevelBarOffsetY);
        store.inventoryLevelBarOffsetX = readInt(client, "inventoryLevelBarOffsetX", store.inventoryLevelBarOffsetX);
        store.inventoryLevelBarOffsetY = readInt(client, "inventoryLevelBarOffsetY", store.inventoryLevelBarOffsetY);
        store.openHudLevelBarRepositionGui = readBool(client, "openHudLevelBarRepositionGui", false);
        store.openInventoryLevelBarRepositionGui = readBool(client, "openInventoryLevelBarRepositionGui", false);

        store.baseXpPerLevel = readInt(common, "baseXpPerLevel", store.baseXpPerLevel);
        store.linearXpPerLevel = readInt(common, "linearXpPerLevel", store.linearXpPerLevel);
        store.exponent = readDouble(common, "exponent", store.exponent);
        store.levelMultiplier = readDouble(common, "levelMultiplier", store.levelMultiplier);
        store.maxLevel = readInt(common, "maxLevel", store.maxLevel);
        store.enableMobKillXp = readBool(common, "enable_mob_kill_xp", store.enableMobKillXp);
        store.mobKillXp = readInt(common, "mobKillXp", store.mobKillXp);
        store.dropLevelsOnlyFromMobsWithTag = readBool(common, "drop_levels_only_from_mobs_with_tag", store.dropLevelsOnlyFromMobsWithTag);
    }

    private void save() {
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
                store.showTopCenterLevelOverlay,
                store.showTemporaryLevelOverlay,
                store.showInventoryLevelBar,
                store.levelHudPosition,
                store.levelHudStayOnScreen,
                normalizeColor(store.levelHudColor, "#53A4BC"),
                store.hudLevelBarOffsetX,
                store.hudLevelBarOffsetY,
                store.inventoryLevelBarOffsetX,
                store.inventoryLevelBarOffsetY,
                store.openHudLevelBarRepositionGui,
                store.openInventoryLevelBarRepositionGui
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
                store.baseXpPerLevel,
                store.linearXpPerLevel,
                store.exponent,
                store.levelMultiplier,
                store.maxLevel,
                store.enableMobKillXp,
                store.mobKillXp,
                store.dropLevelsOnlyFromMobsWithTag
        );
        writeFile(clientFile(), clientOut);
        writeFile(commonFile(), commonOut);
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
        String v = line.substring(eq + 1).trim();
        int hash = v.indexOf('#');
        if (hash >= 0) v = v.substring(0, hash).trim();
        return v;
    }

    private static String readString(List<String> lines, String key, String fallback) {
        for (String line : lines) {
            String t = line.trim();
            if (!t.startsWith(key + "=")) continue;
            String value = cleanValue(t);
            if (value.startsWith("\"") && value.endsWith("\"") && value.length() >= 2) {
                value = value.substring(1, value.length() - 1);
            }
            return value;
        }
        return fallback;
    }

    private static boolean readBool(List<String> lines, String key, boolean fallback) {
        String v = readString(lines, key, fallback ? "true" : "false");
        return "true".equalsIgnoreCase(v);
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

    private void initInputs() {
        levelHudColorInput = addRenderableWidget(new EditBox(font, 0, 0, INPUT_W, 12, Component.translatable("gui.aura.levelup.level_hud_color")));
        levelHudColorInput.setMaxLength(7);
        levelHudColorInput.setValue(store.levelHudColor);

        hudOffsetXInput = addRenderableWidget(new EditBox(font, 0, 0, INPUT_W, 12, Component.translatable("gui.aura.levelup.hud_level_bar_offset_x")));
        hudOffsetXInput.setMaxLength(6);
        hudOffsetXInput.setValue(Integer.toString(store.hudLevelBarOffsetX));

        hudOffsetYInput = addRenderableWidget(new EditBox(font, 0, 0, INPUT_W, 12, Component.translatable("gui.aura.levelup.hud_level_bar_offset_y")));
        hudOffsetYInput.setMaxLength(6);
        hudOffsetYInput.setValue(Integer.toString(store.hudLevelBarOffsetY));

        inventoryOffsetXInput = addRenderableWidget(new EditBox(font, 0, 0, INPUT_W, 12, Component.translatable("gui.aura.levelup.inventory_level_bar_offset_x")));
        inventoryOffsetXInput.setMaxLength(6);
        inventoryOffsetXInput.setValue(Integer.toString(store.inventoryLevelBarOffsetX));

        inventoryOffsetYInput = addRenderableWidget(new EditBox(font, 0, 0, INPUT_W, 12, Component.translatable("gui.aura.levelup.inventory_level_bar_offset_y")));
        inventoryOffsetYInput.setMaxLength(6);
        inventoryOffsetYInput.setValue(Integer.toString(store.inventoryLevelBarOffsetY));
    }

    private void positionInputs(int xRight) {
        positionInput(levelHudColorInput, "gui.aura.levelup.level_hud_color", xRight);
        positionInput(hudOffsetXInput, "gui.aura.levelup.hud_level_bar_offset_x", xRight);
        positionInput(hudOffsetYInput, "gui.aura.levelup.hud_level_bar_offset_y", xRight);
        positionInput(inventoryOffsetXInput, "gui.aura.levelup.inventory_level_bar_offset_x", xRight);
        positionInput(inventoryOffsetYInput, "gui.aura.levelup.inventory_level_bar_offset_y", xRight);
    }

    private void positionInput(EditBox box, String key, int xRight) {
        if (box == null) return;
        int index = rowIndex(key);
        if (index < 0) {
            box.visible = false;
            box.active = false;
            return;
        }
        int y = TOP + index * ROW_H - scroll;
        boolean visible = y + ROW_H >= TOP && y <= height - BOTTOM;
        box.setX(xRight - INPUT_W);
        box.setY(y);
        box.visible = visible;
        box.active = visible;
    }

    private int rowIndex(String key) {
        for (int i = 0; i < rows.size(); i++) {
            if (rows.get(i).labelKey.equals(key)) return i;
        }
        return -1;
    }

    private void commitInputValues() {
        if (levelHudColorInput != null) {
            store.levelHudColor = normalizeColor(levelHudColorInput.getValue(), store.levelHudColor);
            levelHudColorInput.setValue(store.levelHudColor);
        }
        store.hudLevelBarOffsetX = parseClampedInt(hudOffsetXInput, store.hudLevelBarOffsetX);
        store.hudLevelBarOffsetY = parseClampedInt(hudOffsetYInput, store.hudLevelBarOffsetY);
        store.inventoryLevelBarOffsetX = parseClampedInt(inventoryOffsetXInput, store.inventoryLevelBarOffsetX);
        store.inventoryLevelBarOffsetY = parseClampedInt(inventoryOffsetYInput, store.inventoryLevelBarOffsetY);
    }

    private int parseClampedInt(EditBox box, int fallback) {
        if (box == null) return fallback;
        try {
            int value = clampInt(Integer.parseInt(box.getValue().trim()), -500, 500);
            box.setValue(Integer.toString(value));
            return value;
        } catch (Exception ignored) {
            box.setValue(Integer.toString(fallback));
            return fallback;
        }
    }

    private static String normalizeColor(String raw, String fallback) {
        if (raw == null) return fallback;
        String value = raw.trim().toUpperCase(Locale.ROOT);
        if (!value.startsWith("#")) value = "#" + value;
        return value.matches("^#[0-9A-F]{6}$") ? value : fallback;
    }

    private record Row(String labelKey, boolean section, boolean textInput, StateSupplier state, IntConsumer clickHandler) {
        static Row section(String labelKey) {
            return new Row(labelKey, true, false, () -> "", d -> {});
        }

        static Row readOnly(String labelKey, StateSupplier state) {
            return new Row(labelKey, false, false, state, d -> {});
        }

        static Row textInput(String labelKey) {
            return new Row(labelKey, false, true, () -> "", d -> {});
        }

        static Row action(String labelKey, Runnable action) {
            return new Row(labelKey, false, false, () -> Component.translatable("gui.aura.open").getString(), d -> {
                if (d > 0) action.run();
            });
        }

        static Row bool(String labelKey, BoolGetter getter, BoolSetter setter) {
            return new Row(labelKey, false, false, () -> Component.translatable(getter.get() ? "gui.aura.state.enabled" : "gui.aura.state.disabled").getString(), d -> setter.set(!getter.get()));
        }

        static Row enumTopBottom(String labelKey, StringGetter getter, StringSetter setter) {
            return new Row(labelKey, false, false, getter::get, d -> setter.set("top".equalsIgnoreCase(getter.get()) ? "bottom" : "top"));
        }

        static Row intRow(String labelKey, IntGetter getter, IntSetter setter, int step, int min, int max) {
            return new Row(labelKey, false, false, () -> Integer.toString(getter.get()), d -> setter.set(clampInt(getter.get() + (d > 0 ? step : -step), min, max)));
        }

        static Row doubleRow(String labelKey, DoubleGetter getter, DoubleSetter setter, double step, double min, double max) {
            return new Row(labelKey, false, false, () -> String.format(Locale.ROOT, "%.2f", getter.get()), d -> {
                double next = getter.get() + (d > 0 ? step : -step);
                setter.set(Math.max(min, Math.min(max, next)));
            });
        }

        String label() {
            return Component.translatable(labelKey).getString();
        }

        String currentState() {
            return state.get();
        }

        void adjust(int direction) {
            clickHandler.accept(direction);
        }
    }

    @FunctionalInterface
    private interface StateSupplier { String get(); }
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

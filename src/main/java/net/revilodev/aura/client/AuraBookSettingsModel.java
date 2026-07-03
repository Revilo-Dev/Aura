package net.revilodev.aura.client;

import net.minecraft.network.chat.Component;
import net.neoforged.fml.loading.FMLPaths;
import net.revilodev.aura.abilities.AbilityElement;
import net.revilodev.aura.abilities.AbilityId;
import net.revilodev.aura.skills.SkillCategory;
import net.revilodev.aura.skills.SkillConfig;
import net.revilodev.aura.skills.SkillId;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.IntConsumer;
import java.util.function.Supplier;

public final class AuraBookSettingsModel {
    private final LevelUpConfigStore levelUpStore = new LevelUpConfigStore();
    private final List<Row> rows = new ArrayList<>();
    private boolean levelUpExpanded;
    private boolean disabledExpanded;
    private Row editingRow;
    private String editingValue = "";

    public List<Row> rows() {
        return rows;
    }

    public void rebuild() {
        rows.clear();
        rows.add(row(Component.translatable("gui.aura.settings.hud_display").getString(), () -> boolState(AuraClientConfig.hudDisplayEnabled()), button -> AuraClientConfig.toggleHudDisplayEnabled()));
        rows.add(row(Component.translatable("gui.aura.settings.disable_inventory_book").getString(), () -> boolState(AuraClientConfig.disableInventoryCodexBook()), button -> AuraClientConfig.toggleDisableInventoryCodexBook()));
        rows.add(row(Component.translatable("gui.aura.settings.spawn_with_book").getString(), () -> boolState(SkillConfig.spawnWithSkillsBook()), button -> SkillConfig.setSpawnWithSkillsBook(!SkillConfig.spawnWithSkillsBook())));
        rows.add(row(Component.translatable("gui.aura.settings.reposition_hud").getString(), () -> Component.translatable("gui.aura.hud_position." + AuraClientConfig.hudPosition().name().toLowerCase(Locale.ROOT)).getString(), button -> AuraClientConfig.cycleHudPosition()));
        rows.add(row(Component.translatable("gui.aura.settings.disable_skills_abilities").getString(), () -> boolState(AuraClientConfig.disableSkillsAndAbilities()), button -> AuraClientConfig.toggleDisableSkillsAndAbilities()));
        rows.add(row(Component.translatable("gui.aura.settings.block_ability_switching").getString(), () -> boolState(AuraClientConfig.blockAbilitySwitching()), button -> AuraClientConfig.toggleBlockAbilitySwitching()));
        rows.add(row(Component.translatable("gui.aura.settings.block_upgrade_downgrade").getString(), () -> boolState(AuraClientConfig.blockUpgradeDowngrade()), button -> AuraClientConfig.toggleBlockUpgradeDowngrade()));
        rows.add(row(Component.translatable("gui.aura.settings.block_open_panel").getString(), () -> boolState(AuraClientConfig.blockOpenSkillsAbilitiesPanel()), button -> AuraClientConfig.toggleBlockOpenSkillsAbilitiesPanel()));
        rows.add(row(Component.translatable("gui.aura.settings.levelup_values").getString(), () -> expandedState(levelUpExpanded), button -> toggleLevelUpExpanded()));
        if (levelUpExpanded) addLevelUpRows();
        rows.add(row(Component.translatable("gui.aura.settings.disabled_skills_abilities").getString(), () -> expandedState(disabledExpanded), button -> toggleDisabledExpanded()));
        if (disabledExpanded) addDisabledRows();
    }

    public boolean clickRow(int index, int button) {
        if (index < 0 || index >= rows.size()) return false;
        Row row = rows.get(index);
        if (row.inputCommit != null) {
            editingRow = row;
            editingValue = row.state.get();
            return true;
        }
        row.onClick.accept(button);
        return true;
    }

    public boolean charTyped(char codePoint) {
        if (editingRow == null) return false;
        if (codePoint >= 32 && codePoint != 127 && editingValue.length() < 16) {
            editingValue += codePoint;
        }
        return true;
    }

    public boolean keyPressed(int keyCode) {
        if (editingRow == null) return false;
        if (keyCode == 257 || keyCode == 335) {
            commitEditing();
            return true;
        }
        if (keyCode == 256) {
            editingRow = null;
            editingValue = "";
            return true;
        }
        if (keyCode == 259) {
            if (!editingValue.isEmpty()) editingValue = editingValue.substring(0, editingValue.length() - 1);
            return true;
        }
        return false;
    }

    public String stateText(Row row) {
        if (row == editingRow) return editingValue + "_";
        return row.state.get();
    }

    private void commitEditing() {
        Row row = editingRow;
        String value = editingValue;
        editingRow = null;
        editingValue = "";
        if (row != null && row.inputCommit != null) {
            row.inputCommit.accept(value);
            saveLevelUpConfig();
        }
    }

    private void toggleLevelUpExpanded() {
        if (!levelUpExpanded) loadLevelUpConfig();
        levelUpExpanded = !levelUpExpanded;
        editingRow = null;
        rebuild();
    }

    private void toggleDisabledExpanded() {
        disabledExpanded = !disabledExpanded;
        editingRow = null;
        rebuild();
    }

    private void addDisabledRows() {
        rows.add(section("gui.aura.disabled_skills_abilities.skills"));
        for (SkillCategory category : SkillCategory.values()) {
            rows.add(mastery(category.title(), () -> boolState(AuraClientConfig.skillMasteryEnabled(category)), button -> AuraClientConfig.toggleSkillMastery(category)));
            for (SkillId id : SkillId.values()) {
                if (id.category() == category) {
                    rows.add(row("  " + id.title(), () -> boolState(AuraClientConfig.skillEnabled(id)), button -> AuraClientConfig.toggleSkill(id)));
                }
            }
        }

        rows.add(section("gui.aura.disabled_skills_abilities.abilities"));
        for (AbilityElement element : AbilityElement.values()) {
            rows.add(mastery(title(element), () -> boolState(AuraClientConfig.abilityMasteryEnabled(element)), button -> AuraClientConfig.toggleAbilityMastery(element)));
            for (AbilityId id : AbilityId.values()) {
                if (id.element() == element) {
                    rows.add(row("  " + id.title(), () -> boolState(AuraClientConfig.abilityEnabled(id)), button -> AuraClientConfig.toggleAbility(id)));
                }
            }
        }
    }

    private void addLevelUpRows() {
        rows.add(section("gui.aura.levelup.section.client_hud"));
        rows.add(levelUpBool("gui.aura.levelup.show_top_center_level_overlay", () -> levelUpStore.showTopCenterLevelOverlay, v -> levelUpStore.showTopCenterLevelOverlay = v));
        rows.add(levelUpBool("gui.aura.levelup.show_temporary_level_overlay", () -> levelUpStore.showTemporaryLevelOverlay, v -> levelUpStore.showTemporaryLevelOverlay = v));
        rows.add(levelUpBool("gui.aura.levelup.show_inventory_level_bar", () -> levelUpStore.showInventoryLevelBar, v -> levelUpStore.showInventoryLevelBar = v));
        rows.add(row("  " + Component.translatable("gui.aura.levelup.level_hud_position").getString(), () -> levelUpStore.levelHudPosition, button -> {
            levelUpStore.levelHudPosition = "top".equalsIgnoreCase(levelUpStore.levelHudPosition) ? "bottom" : "top";
            saveLevelUpConfig();
        }));
        rows.add(levelUpBool("gui.aura.levelup.level_hud_stay_on_screen", () -> levelUpStore.levelHudStayOnScreen, v -> levelUpStore.levelHudStayOnScreen = v));
        rows.add(input("gui.aura.levelup.level_hud_color", this::hudColor, value -> setHudColor(value)));
        rows.add(input("gui.aura.levelup.hud_level_bar_offset_x", () -> Integer.toString(levelUpStore.hudLevelBarOffsetX), value -> levelUpStore.hudLevelBarOffsetX = parseInt(value, levelUpStore.hudLevelBarOffsetX, -500, 500)));
        rows.add(input("gui.aura.levelup.hud_level_bar_offset_y", () -> Integer.toString(levelUpStore.hudLevelBarOffsetY), value -> levelUpStore.hudLevelBarOffsetY = parseInt(value, levelUpStore.hudLevelBarOffsetY, -500, 500)));
        rows.add(input("gui.aura.levelup.inventory_level_bar_offset_x", () -> Integer.toString(levelUpStore.inventoryLevelBarOffsetX), value -> levelUpStore.inventoryLevelBarOffsetX = parseInt(value, levelUpStore.inventoryLevelBarOffsetX, -500, 500)));
        rows.add(input("gui.aura.levelup.inventory_level_bar_offset_y", () -> Integer.toString(levelUpStore.inventoryLevelBarOffsetY), value -> levelUpStore.inventoryLevelBarOffsetY = parseInt(value, levelUpStore.inventoryLevelBarOffsetY, -500, 500)));
        rows.add(action("gui.aura.levelup.open_hud_reposition", this::openHudReposition));
        rows.add(action("gui.aura.levelup.open_inventory_reposition", this::openInventoryReposition));
        rows.add(section("gui.aura.levelup.section.progression"));
        rows.add(levelUpInt("gui.aura.levelup.base_xp_per_level", () -> levelUpStore.baseXpPerLevel, v -> levelUpStore.baseXpPerLevel = v, 1, 0, 1_000_000));
        rows.add(levelUpInt("gui.aura.levelup.linear_xp_per_level", () -> levelUpStore.linearXpPerLevel, v -> levelUpStore.linearXpPerLevel = v, 1, 0, 1_000_000));
        rows.add(levelUpDouble("gui.aura.levelup.exponent", () -> levelUpStore.exponent, v -> levelUpStore.exponent = v, 0.01D, 0.01D, 100.0D));
        rows.add(levelUpDouble("gui.aura.levelup.level_multiplier", () -> levelUpStore.levelMultiplier, v -> levelUpStore.levelMultiplier = v, 0.01D, 0.0D, 100.0D));
        rows.add(levelUpInt("gui.aura.levelup.max_level", () -> levelUpStore.maxLevel, v -> levelUpStore.maxLevel = v, 1, 1, 1_000_000));
        rows.add(section("gui.aura.levelup.section.sources"));
        rows.add(levelUpBool("gui.aura.levelup.enable_mob_kill_xp", () -> levelUpStore.enableMobKillXp, v -> levelUpStore.enableMobKillXp = v));
        rows.add(levelUpInt("gui.aura.levelup.mob_kill_xp", () -> levelUpStore.mobKillXp, v -> levelUpStore.mobKillXp = v, 1, 0, 1_000_000));
        rows.add(levelUpBool("gui.aura.levelup.drop_levels_only_from_mobs_with_tag", () -> levelUpStore.dropLevelsOnlyFromMobsWithTag, v -> levelUpStore.dropLevelsOnlyFromMobsWithTag = v));
    }

    private Row section(String key) {
        return new Row(Component.translatable(key).getString(), () -> "", button -> {}, RowStyle.SECTION, null);
    }

    private Row mastery(String label, Supplier<String> state, IntConsumer click) {
        return new Row(label, state, click, RowStyle.MASTERY, null);
    }

    private Row row(String label, Supplier<String> state, IntConsumer click) {
        return new Row(label, state, click, RowStyle.NORMAL, null);
    }

    private Row action(String key, Runnable action) {
        return row("  " + Component.translatable(key).getString(), () -> Component.translatable("gui.aura.open").getString(), button -> {
            if (button == 0) action.run();
        });
    }

    private Row input(String key, Supplier<String> state, InputCommit commit) {
        return new Row("  " + Component.translatable(key).getString(), state, button -> {}, RowStyle.INPUT, commit);
    }

    private Row levelUpBool(String key, BoolGetter getter, BoolSetter setter) {
        return row("  " + Component.translatable(key).getString(), () -> boolState(getter.get()), button -> {
            setter.set(!getter.get());
            saveLevelUpConfig();
        });
    }

    private Row levelUpInt(String key, IntGetter getter, IntSetter setter, int step, int min, int max) {
        return row("  " + Component.translatable(key).getString(), () -> Integer.toString(getter.get()), button -> {
            int delta = button == 1 ? -step : step;
            setter.set(clampInt(getter.get() + delta, min, max));
            saveLevelUpConfig();
        });
    }

    private Row levelUpDouble(String key, DoubleGetter getter, DoubleSetter setter, double step, double min, double max) {
        return row("  " + Component.translatable(key).getString(), () -> String.format(Locale.ROOT, "%.2f", getter.get()), button -> {
            double delta = button == 1 ? -step : step;
            setter.set(Math.max(min, Math.min(max, getter.get() + delta)));
            saveLevelUpConfig();
        });
    }

    private String hudColor() {
        return "#" + hex2(levelUpStore.hudColorR) + hex2(levelUpStore.hudColorG) + hex2(levelUpStore.hudColorB);
    }

    private void setHudColor(String raw) {
        String value = raw == null ? "" : raw.trim().toUpperCase(Locale.ROOT);
        if (!value.startsWith("#")) value = "#" + value;
        if (!value.matches("^#[0-9A-F]{6}$")) return;
        levelUpStore.hudColorR = Integer.parseInt(value.substring(1, 3), 16);
        levelUpStore.hudColorG = Integer.parseInt(value.substring(3, 5), 16);
        levelUpStore.hudColorB = Integer.parseInt(value.substring(5, 7), 16);
    }

    private static int parseInt(String raw, int fallback, int min, int max) {
        try {
            return clampInt(Integer.parseInt(raw.trim()), min, max);
        } catch (Exception ignored) {
            return fallback;
        }
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
        setHudColor(readString(client, "levelHudColor", hudColor()));
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
                hudColor(),
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
        return "true".equalsIgnoreCase(readString(lines, key, fallback ? "true" : "false"));
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

    private static String boolState(boolean value) {
        return Component.translatable(value ? "gui.aura.state.enabled" : "gui.aura.state.disabled").getString();
    }

    private static String expandedState(boolean value) {
        return Component.translatable(value ? "gui.aura.state.open" : "gui.aura.state.closed").getString();
    }

    private static String title(Enum<?> value) {
        String text = value.name().toLowerCase(Locale.ROOT).replace('_', ' ');
        return Character.toUpperCase(text.charAt(0)) + text.substring(1);
    }

    private static String hex2(int c) {
        return String.format(Locale.ROOT, "%02X", c & 0xFF);
    }

    private static int clampInt(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
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

    public enum RowStyle {
        NORMAL,
        INPUT,
        SECTION,
        MASTERY
    }

    public record Row(String label, Supplier<String> state, IntConsumer onClick, RowStyle style, InputCommit inputCommit) {}

    @FunctionalInterface
    public interface InputCommit { void accept(String value); }
    @FunctionalInterface
    private interface BoolGetter { boolean get(); }
    @FunctionalInterface
    private interface BoolSetter { void set(boolean value); }
    @FunctionalInterface
    private interface IntGetter { int get(); }
    @FunctionalInterface
    private interface IntSetter { void set(int value); }
    @FunctionalInterface
    private interface DoubleGetter { double get(); }
    @FunctionalInterface
    private interface DoubleSetter { void set(double value); }
}

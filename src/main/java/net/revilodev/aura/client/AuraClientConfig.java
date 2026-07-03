package net.revilodev.aura.client;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.neoforged.fml.loading.FMLPaths;
import net.revilodev.aura.abilities.AbilityElement;
import net.revilodev.aura.abilities.AbilityId;
import net.revilodev.aura.abilities.AbilityConfig;
import net.revilodev.aura.skills.SkillCategory;
import net.revilodev.aura.skills.SkillId;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.EnumSet;
import java.util.Locale;

public final class AuraClientConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path FILE = FMLPaths.CONFIGDIR.get().resolve("aura-client.json");

    private static boolean hudDisplayEnabled = true;
    private static boolean disableCodexBook = false;
    private static boolean disableInventoryCodexBook = false;
    private static AbilityConfig.HudPosition hudPosition = AbilityConfig.HudPosition.BOTTOM_LEFT;
    private static boolean disableSkillsAndAbilities = false;
    private static boolean blockAbilitySwitching = false;
    private static boolean blockUpgradeDowngrade = false;
    private static boolean blockOpenSkillsAbilitiesPanel = false;
    private static final EnumSet<SkillId> disabledSkills = EnumSet.noneOf(SkillId.class);
    private static final EnumSet<SkillCategory> disabledSkillMasteries = EnumSet.noneOf(SkillCategory.class);
    private static final EnumSet<AbilityId> disabledAbilities = EnumSet.noneOf(AbilityId.class);
    private static final EnumSet<AbilityElement> disabledAbilityMasteries = EnumSet.noneOf(AbilityElement.class);

    private AuraClientConfig() {}

    public static void load() {
        if (!Files.exists(FILE)) {
            save();
            return;
        }
        try {
            JsonObject root = GSON.fromJson(Files.readString(FILE), JsonObject.class);
            if (root == null) return;
            hudDisplayEnabled = getBoolean(root, "hudDisplayEnabled", true);
            disableCodexBook = getBoolean(root, "disableCodexBook", false);
            disableInventoryCodexBook = getBoolean(root, "disableInventoryCodexBook", false);
            disableSkillsAndAbilities = getBoolean(root, "disableSkillsAndAbilities", false);
            blockAbilitySwitching = getBoolean(root, "blockAbilitySwitching", false);
            blockUpgradeDowngrade = getBoolean(root, "blockUpgradeDowngrade", false);
            blockOpenSkillsAbilitiesPanel = getBoolean(root, "blockOpenSkillsAbilitiesPanel", false);
            readEnumSet(root, "disabledSkills", SkillId.class, disabledSkills);
            readEnumSet(root, "disabledSkillMasteries", SkillCategory.class, disabledSkillMasteries);
            readEnumSet(root, "disabledAbilities", AbilityId.class, disabledAbilities);
            readEnumSet(root, "disabledAbilityMasteries", AbilityElement.class, disabledAbilityMasteries);
            String pos = root.has("hudPosition") ? root.get("hudPosition").getAsString() : "bottom-left";
            AbilityConfig.HudPosition parsed = AbilityConfig.HudPosition.fromConfig(pos);
            hudPosition = parsed == null ? AbilityConfig.HudPosition.BOTTOM_LEFT : parsed;
        } catch (Exception ignored) {}
    }

    public static void save() {
        try {
            Files.createDirectories(FILE.getParent());
            JsonObject root = new JsonObject();
            root.addProperty("hudDisplayEnabled", hudDisplayEnabled);
            root.addProperty("disableCodexBook", disableCodexBook);
            root.addProperty("disableInventoryCodexBook", disableInventoryCodexBook);
            root.addProperty("disableSkillsAndAbilities", disableSkillsAndAbilities);
            root.addProperty("blockAbilitySwitching", blockAbilitySwitching);
            root.addProperty("blockUpgradeDowngrade", blockUpgradeDowngrade);
            root.addProperty("blockOpenSkillsAbilitiesPanel", blockOpenSkillsAbilitiesPanel);
            root.add("disabledSkills", enumArray(disabledSkills));
            root.add("disabledSkillMasteries", enumArray(disabledSkillMasteries));
            root.add("disabledAbilities", enumArray(disabledAbilities));
            root.add("disabledAbilityMasteries", enumArray(disabledAbilityMasteries));
            root.addProperty("hudPosition", hudPosition.name().toLowerCase(java.util.Locale.ROOT).replace('_', '-'));
            Files.writeString(FILE, GSON.toJson(root));
        } catch (IOException ignored) {}
    }

    public static boolean hudDisplayEnabled() {
        return hudDisplayEnabled;
    }

    public static void toggleHudDisplayEnabled() {
        hudDisplayEnabled = !hudDisplayEnabled;
        save();
    }

    public static boolean disableCodexBook() {
        return disableCodexBook;
    }

    public static void toggleDisableCodexBook() {
        disableCodexBook = !disableCodexBook;
        save();
    }

    public static boolean disableInventoryCodexBook() {
        return disableInventoryCodexBook;
    }

    public static void toggleDisableInventoryCodexBook() {
        disableInventoryCodexBook = !disableInventoryCodexBook;
        save();
    }

    public static AbilityConfig.HudPosition hudPosition() {
        return hudPosition;
    }

    public static void cycleHudPosition() {
        hudPosition = switch (hudPosition) {
            case TOP_LEFT -> AbilityConfig.HudPosition.TOP_RIGHT;
            case TOP_RIGHT -> AbilityConfig.HudPosition.BOTTOM_RIGHT;
            case BOTTOM_RIGHT -> AbilityConfig.HudPosition.BOTTOM_LEFT;
            case BOTTOM_LEFT -> AbilityConfig.HudPosition.TOP_LEFT;
        };
        save();
    }

    public static boolean disableSkillsAndAbilities() {
        return disableSkillsAndAbilities;
    }

    public static void toggleDisableSkillsAndAbilities() {
        disableSkillsAndAbilities = !disableSkillsAndAbilities;
        save();
    }

    public static boolean blockAbilitySwitching() {
        return blockAbilitySwitching;
    }

    public static void toggleBlockAbilitySwitching() {
        blockAbilitySwitching = !blockAbilitySwitching;
        save();
    }

    public static boolean blockUpgradeDowngrade() {
        return blockUpgradeDowngrade;
    }

    public static void toggleBlockUpgradeDowngrade() {
        blockUpgradeDowngrade = !blockUpgradeDowngrade;
        save();
    }

    public static boolean blockOpenSkillsAbilitiesPanel() {
        return blockOpenSkillsAbilitiesPanel;
    }

    public static void toggleBlockOpenSkillsAbilitiesPanel() {
        blockOpenSkillsAbilitiesPanel = !blockOpenSkillsAbilitiesPanel;
        save();
    }

    public static boolean skillEnabled(SkillId id) {
        return id != null && !disableSkillsAndAbilities && !disabledSkills.contains(id) && !disabledSkillMasteries.contains(id.category());
    }

    public static void toggleSkill(SkillId id) {
        toggle(disabledSkills, id);
    }

    public static boolean skillMasteryEnabled(SkillCategory category) {
        return category != null && !disableSkillsAndAbilities && !disabledSkillMasteries.contains(category);
    }

    public static void toggleSkillMastery(SkillCategory category) {
        toggle(disabledSkillMasteries, category);
    }

    public static boolean abilityEnabled(AbilityId id) {
        return id != null && !disableSkillsAndAbilities && !disabledAbilities.contains(id) && !disabledAbilityMasteries.contains(id.element());
    }

    public static void toggleAbility(AbilityId id) {
        toggle(disabledAbilities, id);
    }

    public static boolean abilityMasteryEnabled(AbilityElement element) {
        return element != null && !disableSkillsAndAbilities && !disabledAbilityMasteries.contains(element);
    }

    public static void toggleAbilityMastery(AbilityElement element) {
        toggle(disabledAbilityMasteries, element);
    }

    private static boolean getBoolean(JsonObject root, String key, boolean fallback) {
        return root.has(key) ? root.get(key).getAsBoolean() : fallback;
    }

    private static <E extends Enum<E>> void toggle(EnumSet<E> set, E value) {
        if (value == null) return;
        if (!set.remove(value)) set.add(value);
        save();
    }

    private static JsonArray enumArray(EnumSet<?> values) {
        JsonArray out = new JsonArray();
        for (Enum<?> value : values) {
            out.add(value.name().toLowerCase(Locale.ROOT));
        }
        return out;
    }

    private static <E extends Enum<E>> void readEnumSet(JsonObject root, String key, Class<E> type, EnumSet<E> target) {
        target.clear();
        if (!root.has(key) || !root.get(key).isJsonArray()) return;
        for (var element : root.getAsJsonArray(key)) {
            try {
                target.add(Enum.valueOf(type, element.getAsString().trim().toUpperCase(Locale.ROOT)));
            } catch (Exception ignored) {}
        }
    }
}

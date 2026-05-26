package net.revilodev.aura.client;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import net.neoforged.fml.loading.FMLPaths;
import net.revilodev.aura.abilities.AbilityConfig;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class AuraClientConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path FILE = FMLPaths.CONFIGDIR.get().resolve("aura-client.json");

    private static boolean hudDisplayEnabled = true;
    private static boolean disableCodexBook = false;
    private static boolean disableInventoryCodexBook = false;
    private static AbilityConfig.HudPosition hudPosition = AbilityConfig.HudPosition.BOTTOM_LEFT;
    private static boolean disableSkillsAndAbilities = false;

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

    private static boolean getBoolean(JsonObject root, String key, boolean fallback) {
        return root.has(key) ? root.get(key).getAsBoolean() : fallback;
    }
}

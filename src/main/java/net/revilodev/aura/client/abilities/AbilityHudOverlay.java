package net.revilodev.aura.client.abilities;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import net.revilodev.aura.CodexMod;
import net.revilodev.aura.abilities.AbilitiesAttachments;
import net.revilodev.aura.abilities.AbilityConfig;
import net.revilodev.aura.abilities.AbilityDefinition;
import net.revilodev.aura.abilities.AbilityElement;
import net.revilodev.aura.abilities.AbilityId;
import net.revilodev.aura.abilities.AbilityRegistry;
import net.revilodev.aura.abilities.PlayerAbilities;
import net.revilodev.aura.client.AuraClientConfig;
import net.revilodev.aura.abilities.logic.AbilityScaling;
import net.revilodev.aura.skills.PlayerSkills;
import net.revilodev.aura.skills.SkillsAttachments;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@OnlyIn(Dist.CLIENT)
public final class AbilityHudOverlay {
    private static final int SLOT_SIZE = 20;
    private static final int SLOT_STEP = 20;
    private static final int BAR_HEIGHT = 22;
    private static final int BAR_FULL_WIDTH = 82;
    private static final int SELECTOR_WIDTH = 24;
    private static final int SELECTOR_HEIGHT = 23;
    private static final ResourceLocation ABILITY_BAR_TEX =
            ResourceLocation.fromNamespaceAndPath(CodexMod.MOD_ID, "textures/gui/hud/abilitybar.png");
    private static final ResourceLocation ABILITY_BAR_SELECTOR_TEX =
            ResourceLocation.fromNamespaceAndPath(CodexMod.MOD_ID, "textures/gui/hud/abilitybar-selector.png");
    private static final int GRID_COLUMNS = 4;
    private static final int GRID_ROWS = 2;
    private static final long FAIL_FLASH_MS = 350L;
    private static final Map<AbilityId, FailureState> FAILURES = new EnumMap<>(AbilityId.class);

    private AbilityHudOverlay() {}

    public static void notifyFailedUse(AbilityId id, AbilityKeybinds.AbilityUseFail reason) {
        if (id == null) return;
        FailureState state = FAILURES.computeIfAbsent(id, ignored -> new FailureState());
        state.reason = reason;
        state.untilMs = System.currentTimeMillis() + FAIL_FLASH_MS;
    }

    public static void render(RenderGuiEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.options.hideGui || !AbilityConfig.hudEnabled() || !AuraClientConfig.hudDisplayEnabled()) return;

        PlayerAbilities abilities = mc.player.getData(AbilitiesAttachments.PLAYER_ABILITIES.get());
        PlayerSkills skills = mc.player.getData(SkillsAttachments.PLAYER_SKILLS.get());
        GuiGraphics gg = event.getGuiGraphics();

        // alt shows selection grid
        boolean showGrid = Screen.hasAltDown();
        List<AbilityId> displayed = showGrid
                ? AbilityKeybinds.altDisplayAbilities()
                : activeSelectionAbilities(abilities);
        if (displayed.isEmpty()) return;

        int columns = showGrid ? Math.min(GRID_COLUMNS, displayed.size()) : displayed.size();
        int rows = showGrid ? Math.min(GRID_ROWS, (int) Math.ceil(displayed.size() / (double) GRID_COLUMNS)) : 1;
        int contentWidth = showGrid ? (SLOT_SIZE + (Math.max(0, columns - 1) * SLOT_STEP)) : (2 + columns * SLOT_STEP);
        int contentHeight = showGrid ? (SLOT_SIZE + (Math.max(0, rows - 1) * SLOT_STEP)) : BAR_HEIGHT;
        Origin origin = resolveOrigin(gg, contentWidth, contentHeight, AuraClientConfig.hudPosition());

        if (showGrid) {
            int max = Math.min(displayed.size(), GRID_COLUMNS * GRID_ROWS);
            for (int i = 0; i < max; i++) {
                int col = i % GRID_COLUMNS;
                int row = i / GRID_COLUMNS;
                drawAbility(gg, mc.font, origin.x + col * SLOT_STEP, origin.y + row * SLOT_STEP, displayed.get(i), abilities, skills, displayed.get(i) == AbilityKeybinds.altSelection(), true);
            }
        } else {
            int barWidth = Math.min(BAR_FULL_WIDTH, 2 + displayed.size() * SLOT_STEP);
            gg.blit(ABILITY_BAR_TEX, origin.x, origin.y, 0, 0, barWidth, BAR_HEIGHT, BAR_FULL_WIDTH, BAR_HEIGHT);
            for (int i = 0; i < displayed.size(); i++) {
                int slotX = origin.x + 1 + i * SLOT_STEP;
                int slotY = origin.y + 1;
                drawAbility(gg, mc.font, slotX, slotY, displayed.get(i), abilities, skills, false, false);
                if (displayed.get(i) == AbilityKeybinds.altSelection()) {
                    gg.blit(ABILITY_BAR_SELECTOR_TEX, slotX - 2, slotY - 2, 0, 0, SELECTOR_WIDTH, SELECTOR_HEIGHT, SELECTOR_WIDTH, SELECTOR_HEIGHT);
                }
            }
        }
    }

    private static List<AbilityId> activeSelectionAbilities(PlayerAbilities abilities) {
        if (abilities == null) return List.of();

        // prefer recent uses
        List<AbilityId> recent = new ArrayList<>(abilities.recentAbilities());
        if (!recent.isEmpty()) {
            return List.copyOf(recent.subList(0, Math.min(4, recent.size())));
        }

        // fallback to active picks
        List<AbilityId> selected = new ArrayList<>();
        for (AbilityElement element : AbilityElement.values()) {
            AbilityId id = abilities.selectedSpecialization(element);
            if (id != null && id.isSpecialization() && abilities.rank(id.core()) > 0) {
                selected.add(id);
            }
        }
        if (!selected.isEmpty()) {
            return List.copyOf(selected.subList(0, Math.min(4, selected.size())));
        }
        return List.of();
    }

    private static Origin resolveOrigin(GuiGraphics gg, int contentWidth, int contentHeight, AbilityConfig.HudPosition position) {
        int margin = 8;
        int x = switch (position) {
            case TOP_RIGHT, BOTTOM_RIGHT -> gg.guiWidth() - margin - contentWidth;
            default -> margin;
        };
        int y = switch (position) {
            case TOP_LEFT, TOP_RIGHT -> margin;
            default -> gg.guiHeight() - margin - contentHeight;
        };
        return new Origin(x, y);
    }

    private static void drawAbility(GuiGraphics gg, Font font, int x, int y, AbilityId id, PlayerAbilities abilities, PlayerSkills skills, boolean selected, boolean drawFrame) {
        if (id == null) return;

        // fail flash and shake
        FailureState failure = activeFailure(id);
        int shakeX = 0;
        if (failure != null) {
            double progress = (failure.untilMs - System.currentTimeMillis()) / (double) FAIL_FLASH_MS;
            progress = Math.max(0.0D, Math.min(1.0D, progress));
            shakeX = (int) Math.round(Math.sin(progress * Math.PI * 4.0D) * 1.5D * progress);
        }
        int drawX = x + shakeX;
        if (drawFrame) {
            int borderColor = selected ? 0xFFB67CFF : 0x80383838;
            int fillColor = failure != null ? 0xC0321212 : 0xB0101010;
            gg.fill(drawX, y, drawX + SLOT_SIZE, y + SLOT_SIZE, fillColor);
            gg.fill(drawX - 1, y - 1, drawX + SLOT_SIZE + 1, y, borderColor);
            gg.fill(drawX - 1, y + SLOT_SIZE, drawX + SLOT_SIZE + 1, y + SLOT_SIZE + 1, borderColor);
            gg.fill(drawX - 1, y, drawX, y + SLOT_SIZE, borderColor);
            gg.fill(drawX + SLOT_SIZE, y, drawX + SLOT_SIZE + 1, y + SLOT_SIZE, borderColor);
        }

        AbilityDefinition def = AbilityRegistry.def(id);
        if (def != null) gg.blit(def.iconTexture(), drawX + 2, y + 2, 0, 0, 16, 16, 16, 16);

        // cooldown fill
        int remaining = abilities.cooldownTicks(id);
        if (remaining > 0) {
            int rank = Math.max(1, abilities.rank(id));
            int max = Math.max(1, AbilityScaling.cooldownTicks(id, rank, skills));
            int overlay = (int) Math.ceil(16.0D * remaining / (double) max);
            gg.fill(drawX + 2, y + 18 - overlay, drawX + 18, y + 18, 0xA0000000);
            if (AbilityConfig.hudTimerText()) {
                String text = remaining > 20 ? Integer.toString((int) Math.ceil(remaining / 20.0D)) : String.format(java.util.Locale.ROOT, "%.1f", remaining / 20.0D);
                gg.drawCenteredString(font, text, drawX + 10, y + 6, 0xFFFFFF);
            }
        }

        String keybind = compactKeybind(AbilityKeybinds.keyName(id));
        int labelWidth = Math.max(1, font.width(keybind));
        int labelX = drawX + SLOT_SIZE - labelWidth - 1;
        int labelY = y + SLOT_SIZE - font.lineHeight + 1;
        int textColor = failure != null ? 0xFF7878 : (abilities.unlocked(id) ? 0xFFF2D2 : 0x909090);
        drawOutlinedText(gg, font, keybind, labelX, labelY, textColor, 0xFF000000);
    }

    private static String compactKeybind(String keybind) {
        if (keybind == null || keybind.isBlank()) return "?";
        String cleaned = keybind.replace("key.keyboard.", "")
                .replace("KEY.", "")
                .replace("NumPad-", "N")
                .replace("NUMPAD", "N")
                .replace("Button ", "M");
        if (cleaned.length() <= 3) return cleaned.toUpperCase(java.util.Locale.ROOT);

        String[] parts = cleaned.split("\\s+");
        if (parts.length > 1) {
            StringBuilder out = new StringBuilder();
            for (String part : parts) {
                if (!part.isEmpty()) out.append(Character.toUpperCase(part.charAt(0)));
            }
            if (!out.isEmpty()) return out.toString();
        }
        return cleaned.substring(0, Math.min(3, cleaned.length())).toUpperCase(java.util.Locale.ROOT);
    }

    private static void drawOutlinedText(GuiGraphics gg, Font font, String text, int x, int y, int color, int outlineColor) {
        // simple pixel outline
        gg.drawString(font, text, x - 1, y, outlineColor, false);
        gg.drawString(font, text, x + 1, y, outlineColor, false);
        gg.drawString(font, text, x, y - 1, outlineColor, false);
        gg.drawString(font, text, x, y + 1, outlineColor, false);
        gg.drawString(font, text, x, y, color, false);
    }

    private static FailureState activeFailure(AbilityId id) {
        FailureState state = FAILURES.get(id);
        if (state == null) return null;
        if (System.currentTimeMillis() > state.untilMs) {
            FAILURES.remove(id);
            return null;
        }
        return state;
    }

    private static final class FailureState {
        AbilityKeybinds.AbilityUseFail reason;
        long untilMs;
    }

    private record Origin(int x, int y) {}
}

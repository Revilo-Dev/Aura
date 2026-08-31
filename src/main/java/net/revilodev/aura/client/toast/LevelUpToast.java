package net.revilodev.aura.client.toast;

import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.neoforged.neoforge.client.event.RenderGuiEvent;

// level up toast
public final class LevelUpToast {
    private static final long DISPLAY_TIME_MS = 5000L;
    private static final long SLIDE_TIME_MS = 350L;
    private static final int BOX_WIDTH = 190;
    private static final int BOX_HEIGHT = 44;
    private static final int TOP_MARGIN = 12;
    private static final int TITLE_COLOR = 0xFFFF55;
    private static final int REWARD_COLOR = 0xFFFFFF;

    private static Component title = Component.empty();
    private static Component rewardLine = Component.empty();
    private static long shownAt = -1L;

    private LevelUpToast() {}

    public static void show(int oldLevel, int newLevel, int skillPointsGained, int abilityPointsGained) {
        Minecraft mc = Minecraft.getInstance();
        if (mc == null) return;

        title = Component.translatable("toast.aura.level_up_levels", oldLevel, newLevel);
        rewardLine = abilityPointsGained > 0
                ? Component.translatable("toast.aura.level_up_skill_points", Math.max(0, skillPointsGained))
                .append(Component.translatable("toast.aura.level_up_ability_points", abilityPointsGained))
                : Component.translatable("toast.aura.level_up_skill_points", Math.max(0, skillPointsGained));
        if (mc.player != null) {
            mc.player.playSound(SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, 1.0F, 1.0F);
        }
        shownAt = Util.getMillis();
    }

    public static void render(RenderGuiEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc == null || mc.options.hideGui || shownAt < 0L) return;

        long elapsed = Util.getMillis() - shownAt;
        if (elapsed >= DISPLAY_TIME_MS) {
            shownAt = -1L;
            return;
        }

        GuiGraphics gg = event.getGuiGraphics();
        Font font = mc.font;
        long visibleFor = DISPLAY_TIME_MS;
        float slide = toastSlide(elapsed, visibleFor);
        int x = (gg.guiWidth() - BOX_WIDTH) / 2;
        int hiddenY = -BOX_HEIGHT;
        int y = Math.round(hiddenY + (TOP_MARGIN - hiddenY) * slide);

        gg.pose().pushPose();
        gg.pose().translate(0.0F, 0.0F, 800.0F);
        gg.fill(x, y, x + BOX_WIDTH, y + BOX_HEIGHT, 0xFF1A1A1A);
        gg.fill(x + 1, y + 1, x + BOX_WIDTH - 1, y + BOX_HEIGHT - 1, 0xFF3A2D12);
        gg.fill(x + 3, y + 3, x + BOX_WIDTH - 3, y + BOX_HEIGHT - 3, 0xEE111111);
        gg.fill(x + 3, y + 21, x + BOX_WIDTH - 3, y + 22, 0xFF8E6B25);
        gg.drawCenteredString(font, title, x + BOX_WIDTH / 2, y + 7, TITLE_COLOR);
        gg.drawCenteredString(font, rewardLine, x + BOX_WIDTH / 2, y + 28, REWARD_COLOR);
        gg.pose().popPose();
    }

    private static float toastSlide(long elapsed, long visibleFor) {
        if (elapsed <= SLIDE_TIME_MS) {
            return easeOut(elapsed / (float) SLIDE_TIME_MS);
        }
        if (elapsed >= visibleFor - SLIDE_TIME_MS) {
            return easeOut(Math.max(0.0F, (visibleFor - elapsed) / (float) SLIDE_TIME_MS));
        }
        return 1.0F;
    }

    private static float easeOut(float value) {
        float clamped = Math.max(0.0F, Math.min(1.0F, value));
        float inv = 1.0F - clamped;
        return 1.0F - inv * inv * inv;
    }
}

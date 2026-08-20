package net.revilodev.aura.client.abilities;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.revilodev.aura.CodexMod;
import net.revilodev.aura.abilities.AbilitiesAttachments;
import net.revilodev.aura.abilities.AbilitiesNetwork;
import net.revilodev.aura.abilities.AbilityElement;
import net.revilodev.aura.abilities.AbilityDefinition;
import net.revilodev.aura.abilities.AbilityConfig;
import net.revilodev.aura.abilities.AbilityId;
import net.revilodev.aura.abilities.AbilityRegistry;
import net.revilodev.aura.abilities.AbilitySpecialization;
import net.revilodev.aura.abilities.PlayerAbilities;
import net.revilodev.aura.attributes.CodexAttributes;
import net.revilodev.aura.abilities.logic.AbilityScaling;
import net.revilodev.aura.client.AuraClientConfig;
import net.revilodev.aura.skills.PlayerSkills;
import net.revilodev.aura.skills.SkillsAttachments;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

@OnlyIn(Dist.CLIENT)
public final class AbilityListWidget extends AbstractWidget {
    private static final ResourceLocation WIDGET_TEX =
            ResourceLocation.fromNamespaceAndPath(CodexMod.MOD_ID, "textures/gui/sprites/skill_widget.png");
    private static final ResourceLocation WIDGET_HOVER_TEX =
            ResourceLocation.fromNamespaceAndPath(CodexMod.MOD_ID, "textures/gui/sprites/skill_widget-hovered.png");
    private static final ResourceLocation WIDGET_DISABLED_TEX =
            ResourceLocation.fromNamespaceAndPath(CodexMod.MOD_ID, "textures/gui/sprites/skill_widget-disabled.png");
    private static final ResourceLocation WIDGET_DISABLED_HOVER_TEX =
            ResourceLocation.fromNamespaceAndPath(CodexMod.MOD_ID, "textures/gui/sprites/skill_widget-disabled-hovered.png");
    private static final ResourceLocation WIDGET_PRIMARY_TEX =
            ResourceLocation.fromNamespaceAndPath(CodexMod.MOD_ID, "textures/gui/sprites/ability-primary.png");
    private static final ResourceLocation WIDGET_PRIMARY_DISABLED_TEX =
            ResourceLocation.fromNamespaceAndPath(CodexMod.MOD_ID, "textures/gui/sprites/ability-primary-disabled.png");
    private static final ResourceLocation WIDGET_PRIMARY_HOVER_TEX =
            ResourceLocation.fromNamespaceAndPath(CodexMod.MOD_ID, "textures/gui/sprites/ability-primary-hovered.png");
    private static final ResourceLocation LINK_TEX =
            ResourceLocation.fromNamespaceAndPath(CodexMod.MOD_ID, "textures/gui/sprites/link.png");
    private static final ResourceLocation LINK_DISABLED_TEX =
            ResourceLocation.fromNamespaceAndPath(CodexMod.MOD_ID, "textures/gui/sprites/link-disabled.png");
    private static final ResourceLocation ABILITY_ORB_TEX =
            ResourceLocation.fromNamespaceAndPath(CodexMod.MOD_ID, "textures/gui/icon/ability_orb.png");
    private static final ResourceLocation LOCKED_TEX =
            ResourceLocation.fromNamespaceAndPath(CodexMod.MOD_ID, "textures/gui/icon/locked.png");

    private static final int HEADER_HEIGHT = 11;
    private static final int CELL_SIZE = 23;
    private static final int GAP = 1;
    private static final int VIEWPORT_W = 130;
    private static final int VIEWPORT_H = 150;
    private static final int LINK_WIDTH = 10;
    private static final int LINK_HEIGHT = 20;
    private static final int VIEWPORT_OFFSET_X = 18;
    private static final int VIEWPORT_OFFSET_Y = -8;
    private static final List<AbilityElement> COLUMN_ORDER = List.of(
            AbilityElement.FIRE,
            AbilityElement.ICE,
            AbilityElement.LIGHTNING,
            AbilityElement.POISON,
            AbilityElement.FORCE,
            AbilityElement.BLOOD,
            AbilityElement.WIND
    );

    private final Minecraft mc = Minecraft.getInstance();
    private final Consumer<AbilityDefinition> onClick;
    private final List<Node> nodes = new ArrayList<>();
    private AbilityId selected;
    private boolean headerVisible = true;
    private boolean showLocked = true;
    private int offsetX = 0;
    private int offsetY = 0;
    private boolean dragging = false;
    private int viewportExtraOffsetX = 0;
    private int viewportExtraWidth = 0;
    private int viewportExtraOffsetY = 0;
    private int headerTextOffsetX = 0;

    public AbilityListWidget(int x, int y, int w, int h, Consumer<AbilityDefinition> onClick) {
        super(x, y, w, h, Component.empty());
        this.onClick = onClick;
        reloadAbilities();
    }

    public void reloadAbilities() {
        nodes.clear();
        List<AbilityDefinition> all = AbilityRegistry.all();
        PlayerAbilities abilities = mc.player == null ? null : mc.player.getData(AbilitiesAttachments.PLAYER_ABILITIES.get());

        // one column per element
        int visibleCol = 0;
        for (AbilityElement element : COLUMN_ORDER) {
            AbilityDefinition core = all.stream().filter(def -> def.element() == element && def.type() == net.revilodev.aura.abilities.AbilityNodeType.CORE).findFirst().orElse(null);
            if (core == null) continue;
            if (!AuraClientConfig.abilityEnabled(core.id())) continue;
            if (!showLocked && abilities != null && !abilities.unlocked(core.id())) continue;
            nodes.add(new Node(core, visibleCol, 0));

            AbilityId cursor = core.id();
            int row = 1;
            while (true) {
                AbilityDefinition next = null;
                for (AbilityDefinition def : all) {
                    if (def.required() == cursor) {
                        next = def;
                        break;
                    }
                }
                if (next == null) break;
                if (!AuraClientConfig.abilityEnabled(next.id())) break;
                if (showLocked || abilities == null || abilities.unlocked(next.id())) {
                    nodes.add(new Node(next, visibleCol, row));
                }
                cursor = next.id();
                row++;
            }
            visibleCol++;
        }

        if (!showLocked && selected != null && nodes.stream().noneMatch(n -> n.def.id() == selected)) {
            selected = null;
            if (onClick != null) onClick.accept(null);
        }
        if (showLocked && selected != null && nodes.stream().noneMatch(n -> n.def.id() == selected)) {
            selected = null;
            if (onClick != null) onClick.accept(null);
        }
    }

    public void setBounds(int x, int y, int w, int h) {
        setX(x);
        setY(y);
        width = w;
        height = h;
    }

    public void setSelected(AbilityId selected) {
        this.selected = selected;
    }

    public void setHeaderVisible(boolean headerVisible) {
        this.headerVisible = headerVisible;
    }

    public void setShowLocked(boolean showLocked) {
        this.showLocked = showLocked;
        reloadAbilities();
    }

    public void setViewportTweaks(int extraOffsetX, int extraWidth, int extraOffsetY) {
        this.viewportExtraOffsetX = extraOffsetX;
        this.viewportExtraWidth = extraWidth;
        this.viewportExtraOffsetY = extraOffsetY;
    }

    public void setHeaderTextOffsetX(int offset) {
        this.headerTextOffsetX = offset;
    }

    public boolean isOnAbilityNode(double mx, double my) {
        return nodeAt(mx, my) != null;
    }

    public static int gridWidth() {
        return 7 * CELL_SIZE + 6 * GAP;
    }

    public static int gridHeight() {
        int rows = 1;
        for (AbilityElement element : COLUMN_ORDER) {
            int count = 0;
            for (AbilityDefinition def : AbilityRegistry.all()) {
                if (def.element() == element) count++;
            }
            rows = Math.max(rows, count);
        }
        return rows * CELL_SIZE + Math.max(0, rows - 1) * GAP;
    }

    public static int preferredHeight() {
        return 166;
    }

    @Override
    protected void renderWidget(GuiGraphics gg, int mouseX, int mouseY, float partialTick) {
        if (!visible || mc.player == null) return;
        reloadAbilities();
        PlayerAbilities abilities = mc.player.getData(AbilitiesAttachments.PLAYER_ABILITIES.get());
        PlayerSkills skills = mc.player.getData(SkillsAttachments.PLAYER_SKILLS.get());
        boolean editLocked = CodexAttributes.isAbilitySkillEditLocked(mc.player);

        // header points
        if (headerVisible) {
            int textX = getX() + VIEWPORT_OFFSET_X + 10 + headerTextOffsetX;
            int textY = getY() + 4;
            drawScaledIcon(gg, ABILITY_ORB_TEX, textX - 10, textY - 1, 8);
            drawScaledText(gg, Component.translatable("gui.aura.points.ability", abilities.points()).getString(), textX, textY, 0xC78CFF, 0.85F);
        }

        int viewportX = getX() + VIEWPORT_OFFSET_X + viewportExtraOffsetX;
        int viewportY = getY() + HEADER_HEIGHT + VIEWPORT_OFFSET_Y + viewportExtraOffsetY;
        int viewportW = Math.min(width, VIEWPORT_W + viewportExtraWidth);
        int viewportH = Math.min(height - HEADER_HEIGHT, VIEWPORT_H);
        int maxOffsetX = Math.max(0, gridWidth() - viewportW);
        int maxOffsetY = Math.max(0, gridHeight() - viewportH);
        offsetX = Math.max(0, Math.min(offsetX, maxOffsetX));
        offsetY = Math.max(0, Math.min(offsetY, maxOffsetY));

        int top = viewportY;
        RenderSystem.enableBlend();
        gg.enableScissor(viewportX, viewportY, viewportX + viewportW, viewportY + viewportH);

        // dependency links
        for (Node node : nodes) {
            if (node.row <= 0 || node.def.required() == null) continue;
            int x = viewportX + node.col * (CELL_SIZE + GAP) - offsetX;
            int y = top + node.row * (CELL_SIZE + GAP) - offsetY;
            ResourceLocation tex = abilities.canUpgrade(node.def.id()) || abilities.unlocked(node.def.id()) ? LINK_TEX : LINK_DISABLED_TEX;
            int linkX = x + (CELL_SIZE - LINK_WIDTH) / 2;
            int linkY = y - ((LINK_HEIGHT - GAP) / 2);
            gg.blit(tex, linkX, linkY, 0, 0, LINK_WIDTH, LINK_HEIGHT, LINK_WIDTH, LINK_HEIGHT);
        }

        for (Node node : nodes) {
            int x = viewportX + node.col * (CELL_SIZE + GAP) - offsetX;
            int y = top + node.row * (CELL_SIZE + GAP) - offsetY;
            AbilityDefinition def = node.def;

            // node state
            boolean hovered = isNodeVisible(x, y, viewportX, viewportY, viewportW, viewportH)
                    && mouseX >= x && mouseX <= x + CELL_SIZE && mouseY >= y && mouseY <= y + CELL_SIZE;
            int rank = abilities.rank(def.id());
            boolean unlocked = rank > 0;
            boolean maxed = rank >= def.maxRank();
            boolean primary = def.type() == net.revilodev.aura.abilities.AbilityNodeType.CORE;
            AbilityId selectedSpec = abilities.selectedSpecialization(def.element());
            boolean specialization = def.type() == net.revilodev.aura.abilities.AbilityNodeType.SPECIALIZATION;
            boolean isSelectedSpecialization = specialization && def.id() == selectedSpec;
            boolean configEnabled = AuraClientConfig.abilityEnabled(def.id());
            boolean affinityLocked = AbilityConfig.affinityLocked(abilities, def.id());
            int switchCooldown = abilities.switchCooldownTicks(def.id());

            ResourceLocation tex;
            if (!configEnabled || affinityLocked) {
                tex = hovered ? WIDGET_DISABLED_HOVER_TEX : WIDGET_DISABLED_TEX;
            } else if (primary && !unlocked) {
                tex = WIDGET_PRIMARY_DISABLED_TEX;
            } else if (specialization && selectedSpec != null && !isSelectedSpecialization) {
                tex = hovered ? WIDGET_DISABLED_HOVER_TEX : WIDGET_DISABLED_TEX;
            } else if (!primary && !abilities.canUpgrade(def.id()) && !unlocked && !(specialization && isSelectedSpecialization)) {
                tex = hovered ? WIDGET_DISABLED_HOVER_TEX : WIDGET_DISABLED_TEX;
            } else if (primary) {
                tex = (selected == def.id() || hovered) ? WIDGET_PRIMARY_HOVER_TEX : WIDGET_PRIMARY_TEX;
            } else {
                tex = (selected == def.id() || hovered) ? WIDGET_HOVER_TEX : WIDGET_TEX;
            }
            if (maxed && !primary) {
                tex = (selected == def.id() || hovered) ? WIDGET_PRIMARY_HOVER_TEX : WIDGET_PRIMARY_TEX;
            }
            drawScaledTile(gg, tex, x, y, CELL_SIZE, CELL_SIZE);
            gg.blit(def.iconTexture(), x + 3, y + 3, 0, 0, 16, 16, 16, 16);
            if (affinityLocked) {
                gg.blit(LOCKED_TEX, x + 3, y + 3, 0, 0, 16, 16, 16, 16);
            }
            if (specialization && AbilityConfig.switchCooldownsEnabled() && switchCooldown > 0) {
                gg.fill(x + 2, y + 2, x + CELL_SIZE - 2, y + CELL_SIZE - 2, 0xA0000000);
                String remaining = ((switchCooldown + 19) / 20) + "s";
                int textX = x + (CELL_SIZE - mc.font.width(remaining)) / 2;
                int textY = y + (CELL_SIZE - mc.font.lineHeight) / 2;
                gg.drawString(mc.font, remaining, textX, textY, 0xFFFFFFFF, true);
            }
            if (hovered) {
                int lvl = abilities.rank(def.id().core());
                Component name = Component.literal(def.title()).withStyle(def.type() == net.revilodev.aura.abilities.AbilityNodeType.CORE ? ChatFormatting.GOLD : ChatFormatting.WHITE);
                List<Component> tooltip = new ArrayList<>();
                tooltip.add(Component.empty()
                        .append(name)
                        .append(Component.literal(" "))
                        .append(Component.translatable("gui.aura.level.short", lvl).withStyle(ChatFormatting.LIGHT_PURPLE)));
                tooltip.add(Component.literal(def.description()).withStyle(ChatFormatting.GRAY));
                if (affinityLocked) {
                    tooltip.add(Component.literal("Requires " + def.id().core().title() + " mastery level " + AbilityConfig.requiredAffinityLevel(def.id())).withStyle(ChatFormatting.RED));
                }
                if (def.type() == net.revilodev.aura.abilities.AbilityNodeType.CORE) {
                    int cost = abilities.upgradeCost(def.id());
                    int refund = abilities.rank(def.id());
                    tooltip.add(Component.literal("| " + pointText(cost)).withStyle(ChatFormatting.GREEN));
                    if (refund > 0) tooltip.add(Component.literal("| " + pointText(refund)).withStyle(ChatFormatting.RED));
                } else {
                    tooltip.add(styledStatLine(statParts(def.id(), Math.max(1, lvl), skills)));
                    tooltip.add(Component.translatable("gui.aura.hint.click_select").withStyle(ChatFormatting.GREEN));
                }
                gg.disableScissor();
                gg.renderTooltip(mc.font, tooltip, java.util.Optional.empty(), mouseX, mouseY - 4);
                gg.enableScissor(viewportX, viewportY, viewportX + viewportW, viewportY + viewportH);
            }
        }
        gg.disableScissor();
    }

    @Override
    public boolean mouseClicked(double mx, double my, int button) {
        if (!visible || !active || (button != 0 && button != 1) || !isMouseOver(mx, my) || mc.player == null) return false;
        Node node = nodeAt(mx, my);
        if (node == null) {
            selected = null;
            if (onClick != null) onClick.accept(null);
            return true;
        }
        PlayerAbilities abilities = mc.player.getData(AbilitiesAttachments.PLAYER_ABILITIES.get());
        boolean editLocked = CodexAttributes.isAbilitySkillEditLocked(mc.player);

        // select first then send action
        selected = node.def.id();
        if (onClick != null) onClick.accept(node.def);
        if (!AuraClientConfig.abilityEnabled(node.def.id())) return true;
        if (AbilityConfig.affinityLocked(abilities, node.def.id())) return true;
        boolean isCore = node.def.type() == net.revilodev.aura.abilities.AbilityNodeType.CORE;
        boolean isSpecialization = node.def.type() == net.revilodev.aura.abilities.AbilityNodeType.SPECIALIZATION;
        if (editLocked) return true;
        if (isCore) {
            if (button == 0) {
                PacketDistributor.sendToServer(new AbilitiesNetwork.AbilityActionPayload(0, node.def.id().ordinal()));
            } else if (button == 1) {
                PacketDistributor.sendToServer(new AbilitiesNetwork.AbilityActionPayload(1, node.def.id().ordinal()));
            }
        } else if (isSpecialization && button == 0) {
            PacketDistributor.sendToServer(new AbilitiesNetwork.AbilityActionPayload(2, node.def.id().ordinal()));
        }
        return true;
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narration) {}

    private Node nodeAt(double mx, double my) {
        int viewportX = getX() + VIEWPORT_OFFSET_X + viewportExtraOffsetX;
        int viewportY = getY() + HEADER_HEIGHT + VIEWPORT_OFFSET_Y + viewportExtraOffsetY;
        int viewportW = Math.min(width, VIEWPORT_W + viewportExtraWidth);
        int viewportH = Math.min(height - HEADER_HEIGHT, VIEWPORT_H);
        for (Node node : nodes) {
            int x = viewportX + node.col * (CELL_SIZE + GAP) - offsetX;
            int y = viewportY + node.row * (CELL_SIZE + GAP) - offsetY;
            if (isNodeVisible(x, y, viewportX, viewportY, viewportW, viewportH)
                    && mx >= x && mx <= x + CELL_SIZE && my >= y && my <= y + CELL_SIZE) {
                return node;
            }
        }
        return null;
    }

    private boolean isNodeVisible(int x, int y, int viewportX, int viewportY, int viewportW, int viewportH) {
        return x + CELL_SIZE > viewportX
                && x < viewportX + viewportW
                && y + CELL_SIZE > viewportY
                && y < viewportY + viewportH;
    }

    public boolean mouseScrolled(double mouseX, double mouseY, double deltaY) {
        if (!visible || !active) return false;
        if (!isMouseOver(mouseX, mouseY)) return false;
        offsetY = Math.max(0, offsetY - (int) Math.round(deltaY * 12.0D));
        return true;
    }

    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (!visible || !active || button != 0) return false;
        if (!dragging && !isMouseOver(mouseX, mouseY)) return false;
        dragging = true;

        // drag moves viewport
        offsetX = Math.max(0, offsetX - (int) Math.round(dragX));
        offsetY = Math.max(0, offsetY - (int) Math.round(dragY));
        return true;
    }

    public void endDrag() {
        dragging = false;
    }

    private void drawScaledTile(GuiGraphics gg, ResourceLocation tex, int x, int y, int w, int h) {
        gg.pose().pushPose();
        gg.pose().translate(x, y, 0.0F);
        gg.pose().scale(w / 26.0F, h / 26.0F, 1.0F);
        gg.blit(tex, 0, 0, 0, 0, 26, 26, 26, 26);
        gg.pose().popPose();
    }

    private void drawScaledIcon(GuiGraphics gg, ResourceLocation tex, int x, int y, int size) {
        gg.pose().pushPose();
        gg.pose().translate(x, y, 0.0F);
        gg.pose().scale(size / 16.0F, size / 16.0F, 1.0F);
        gg.blit(tex, 0, 0, 0, 0, 16, 16, 16, 16);
        gg.pose().popPose();
    }

    private void drawScaledText(GuiGraphics gg, String text, int x, int y, int color, float scale) {
        gg.pose().pushPose();
        gg.pose().translate(x, y, 0.0F);
        gg.pose().scale(scale, scale, 1.0F);
        gg.drawString(mc.font, text, 0, 0, color, false);
        gg.pose().popPose();
    }

    private record Node(AbilityDefinition def, int col, int row) {}

    private static String formatSeconds(int ticks) {
        return String.format(java.util.Locale.ROOT, "%.2fs", ticks / 20.0D);
    }

    private static String formatDps(float damage, int durationTicks) {
        double seconds = Math.max(0.05D, durationTicks / 20.0D);
        return String.format(java.util.Locale.ROOT, "%.1fhp", damage / seconds);
    }

    private List<StatPart> statParts(AbilityId id, int level, PlayerSkills skills) {
        List<StatPart> out = new ArrayList<>();
        out.add(new StatPart("Cooldown " + formatSeconds(AbilityScaling.cooldownTicks(id, level, skills)), ChatFormatting.YELLOW));

        // default stat triplet
        String durationText = "Duration " + formatSeconds(AbilityScaling.durationTicks(id, level, 1.0D));
        String thirdText = "DPS " + formatDps(AbilityScaling.damage(id, level, 1.0D), AbilityScaling.durationTicks(id, level, 1.0D));

        if (id == AbilityId.WIND_DASH) {
            durationText = null;
            thirdText = "Distance " + fmt(1.0D + (level * 0.2D));
        } else if (id == AbilityId.WIND_LEAP) {
            durationText = null;
            thirdText = "Height " + fmt(0.55D + (level * 0.05D));
        } else if (id == AbilityId.WIND_LUNGE) {
            durationText = "Distance " + fmt(AbilityScaling.radius(id, level, 1.0D) + 4.0D);
            thirdText = "Damage " + fmt(AbilityScaling.damage(id, level, 1.0D) * 1.25D);
        } else if (id == AbilityId.BLOOD_HEAL) {
            durationText = null;
            thirdText = "Health " + fmt(AbilityScaling.damage(id, level, 1.0D) * 0.6D);
        } else if (id == AbilityId.BLOOD_CLEANSE) {
            durationText = null;
        } else if (id == AbilityId.BLOOD_BURST) {
            double cost = AbilityScaling.damage(id, level, 1.0D);
            durationText = "Health Cost " + fmt(cost);
            thirdText = "Damage " + fmt(cost);
        } else if (id == AbilityId.BLOOD_DRAIN) {
            durationText = "Duration " + formatSeconds(AbilityScaling.durationTicks(id, level, 1.0D));
            thirdText = "Drain " + fmt(Math.max(1.0D, AbilityScaling.damage(id, level, 1.0D) * 0.35D)) + "/0.5s";
        } else if (id == AbilityId.FORCE_RAMPAGE) {
            int coreRank = Math.max(1, level);
            int strengthAmp = Math.min(4, coreRank / 2);
            thirdText = "Damage +" + fmt(3.0D * (strengthAmp + 1));
        } else if (id == AbilityId.FORCE_AEGIS) {
            thirdText = "Dmg Avoids " + Math.max(1, (int) Math.round(level));
        } else if (id.specialization() == AbilitySpecialization.NOVA) {
            thirdText = "Radius " + fmt(AbilityScaling.radius(id, level, 1.0D) + 1.5D);
        }

        if (id.specialization() == AbilitySpecialization.IMPLODE) {
            durationText = "Radius " + fmt(AbilityScaling.radius(id, level, 1.0D) + 1.0D);
        } else if (id.specialization() == AbilitySpecialization.BURST && id != AbilityId.BLOOD_BURST) {
            if (id == AbilityId.FIRE_BURST || id == AbilityId.ICE_BURST || id == AbilityId.POISON_BURST) {
                durationText = "Projectiles " + (1 + Math.max(0, level * 2));
            } else if (id == AbilityId.FORCE_BURST) {
                durationText = "Projectiles 1";
            }
        }

        if (durationText != null && !durationText.isEmpty()) {
            out.add(new StatPart(durationText, ChatFormatting.BLUE));
        }
        if (thirdText != null && !thirdText.isEmpty()) {
            out.add(new StatPart(thirdText, ChatFormatting.RED));
        }
        return out;
    }

    private static Component styledStatLine(List<StatPart> stats) {
        MutableComponent line = Component.empty();
        for (int i = 0; i < stats.size(); i++) {
            StatPart stat = stats.get(i);
            line = line.append(Component.literal(stat.text()).withStyle(stat.style()));
            if (i < stats.size() - 1) {
                line = line.append(Component.literal(" | ").withStyle(ChatFormatting.GRAY));
            }
        }
        return line;
    }

    private static String fmt(double value) {
        String out = String.format(java.util.Locale.ROOT, "%.2f", value);
        while (out.contains(".") && (out.endsWith("0") || out.endsWith("."))) out = out.substring(0, out.length() - 1);
        return out;
    }

    private static String pointText(int points) {
        return points + " Point" + (points == 1 ? "" : "s");
    }

    private record StatPart(String text, ChatFormatting style) {}
}

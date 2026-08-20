package net.revilodev.aura.skills.logic;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.revilodev.aura.item.ModItems;
import net.revilodev.aura.skills.PlayerSkills;
import net.revilodev.aura.skills.SkillConfig;
import net.revilodev.aura.skills.SkillsAttachments;
import net.revilodev.aura.skills.SkillsNetwork;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

// skill sync events
public final class SkillSyncEvents {
    private SkillSyncEvents() {}

    private static final java.util.Set<UUID> PENDING = ConcurrentHashMap.newKeySet();
    public static void register() {
        NeoForge.EVENT_BUS.addListener(SkillSyncEvents::onLogin);
        NeoForge.EVENT_BUS.addListener(SkillSyncEvents::onRespawn);
        NeoForge.EVENT_BUS.addListener(SkillSyncEvents::onChangedDimension);
        NeoForge.EVENT_BUS.addListener(SkillSyncEvents::onServerTickPost);
    }

    public static void markDirty(ServerPlayer sp) {
        if (sp == null) return;
        PENDING.add(sp.getUUID());
    }

    private static void onLogin(PlayerEvent.PlayerLoggedInEvent e) {
        if (!(e.getEntity() instanceof ServerPlayer sp)) return;
        giveStartingBook(sp);
        applyEffectsNow(sp);
        markDirty(sp);
    }

    private static void onRespawn(PlayerEvent.PlayerRespawnEvent e) {
        if (!(e.getEntity() instanceof ServerPlayer sp)) return;
        applyEffectsNow(sp);
        markDirty(sp);
    }

    private static void onChangedDimension(PlayerEvent.PlayerChangedDimensionEvent e) {
        if (!(e.getEntity() instanceof ServerPlayer sp)) return;
        applyEffectsNow(sp);
        markDirty(sp);
    }

    private static void onServerTickPost(ServerTickEvent.Post e) {
        if (PENDING.isEmpty()) return;

        MinecraftServer server = e.getServer();

        UUID[] ids = PENDING.toArray(new UUID[0]);
        PENDING.clear();

        for (UUID id : ids) {
            ServerPlayer sp = server.getPlayerList().getPlayer(id);
            if (sp == null) continue;
            SkillsNetwork.syncTo(sp);
        }
    }

    private static void applyEffectsNow(ServerPlayer sp) {
        PlayerSkills skills = sp.getData(SkillsAttachments.PLAYER_SKILLS.get());
        SkillLogic.applyAllEffects(sp, skills);
    }

    private static void giveStartingBook(ServerPlayer sp) {
        if (!SkillConfig.spawnWithSkillsBook()) return;
        PlayerSkills skills = sp.getData(SkillsAttachments.PLAYER_SKILLS.get());
        if (skills.startingBookGiven()) return;
        skills.markStartingBookGiven();
        markDirty(sp);

        ItemStack stack = new ItemStack(ModItems.SKILLS_BOOK.get());
        boolean added = sp.getInventory().add(stack);
        if (!added && !stack.isEmpty()) {
            sp.drop(stack, false);
        }
    }
}

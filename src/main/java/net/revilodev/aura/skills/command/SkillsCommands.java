package net.revilodev.aura.skills.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.revilodev.aura.skills.PlayerSkills;
import net.revilodev.aura.skills.SkillId;
import net.revilodev.aura.skills.SkillsAttachments;
import net.revilodev.aura.skills.SkillsNetwork;
import net.revilodev.aura.skills.logic.SkillLogic;

import java.util.Arrays;
import java.util.Collection;
import java.util.Locale;

public final class SkillsCommands {
    private SkillsCommands() {}

    private static final SuggestionProvider<CommandSourceStack> SUGGEST_SKILLS =
            (ctx, builder) -> SharedSuggestionProvider.suggest(
                    Arrays.stream(SkillId.values()).map(SkillId::name),
                    builder
            );

    public static void register() {
        NeoForge.EVENT_BUS.addListener(SkillsCommands::onRegisterCommands);
    }

    private static void onRegisterCommands(RegisterCommandsEvent e) {
        registerAll(e.getDispatcher());
    }

    private static void registerAll(CommandDispatcher<CommandSourceStack> d) {
        var root = Commands.literal("skills").requires(s -> s.hasPermission(2));

        root.then(Commands.literal("level")
                .then(Commands.literal("up")
                        .then(Commands.argument("skill", StringArgumentType.word()).suggests(SUGGEST_SKILLS)
                                .then(Commands.argument("amount", IntegerArgumentType.integer(1))
                                        .executes(ctx -> {
                                            ServerPlayer sp = ctx.getSource().getPlayerOrException();
                                            SkillId id = parseSkill(StringArgumentType.getString(ctx, "skill"));
                                            int amt = IntegerArgumentType.getInteger(ctx, "amount");
                                            if (id == null) {
                                                ctx.getSource().sendFailure(Component.translatable("command.aura.skills.error.unknown_skill"));
                                                return 0;
                                            }
                                            PlayerSkills data = sp.getData(SkillsAttachments.PLAYER_SKILLS.get());
                                            int before = data.level(id);
                                            int after = data.adminAddLevel(id, amt);
                                            SkillLogic.applyAllEffects(sp, data);
                                            SkillsNetwork.syncTo(sp);
                                            ctx.getSource().sendSuccess(() -> Component.translatable("command.aura.skills.level_up.success", id.title(), before, after), true);
                                            return 1;
                                        })))));

        root.then(Commands.literal("points")
                .then(Commands.literal("add")
                        .then(Commands.argument("amount", IntegerArgumentType.integer(1))
                                .executes(ctx -> addPoints(ctx, java.util.List.of(ctx.getSource().getPlayerOrException()), IntegerArgumentType.getInteger(ctx, "amount"))))
                        .then(Commands.argument("targets", EntityArgument.players())
                                .then(Commands.argument("amount", IntegerArgumentType.integer(1))
                                        .executes(ctx -> addPoints(ctx, EntityArgument.getPlayers(ctx, "targets"), IntegerArgumentType.getInteger(ctx, "amount"))))))
                .then(Commands.literal("set")
                        .then(Commands.argument("amount", IntegerArgumentType.integer(0))
                                .executes(ctx -> setPoints(ctx, java.util.List.of(ctx.getSource().getPlayerOrException()), IntegerArgumentType.getInteger(ctx, "amount"))))
                        .then(Commands.argument("targets", EntityArgument.players())
                                .then(Commands.argument("amount", IntegerArgumentType.integer(0))
                                        .executes(ctx -> setPoints(ctx, EntityArgument.getPlayers(ctx, "targets"), IntegerArgumentType.getInteger(ctx, "amount"))))))
                .then(Commands.literal("reset")
                        .executes(ctx -> resetPoints(ctx, java.util.List.of(ctx.getSource().getPlayerOrException())))
                        .then(Commands.argument("targets", EntityArgument.players())
                                .executes(ctx -> resetPoints(ctx, EntityArgument.getPlayers(ctx, "targets"))))));

        root.then(Commands.literal("reset")
                .executes(ctx -> {
                    ServerPlayer sp = ctx.getSource().getPlayerOrException();
                    PlayerSkills data = sp.getData(SkillsAttachments.PLAYER_SKILLS.get());
                    data.adminResetAll();
                    SkillLogic.applyAllEffects(sp, data);
                    SkillsNetwork.syncTo(sp);
                    ctx.getSource().sendSuccess(() -> Component.translatable("command.aura.skills.reset_all.success"), true);
                    return 1;
                }));

        d.register(root);
    }

    // updates and syncs skill points
    private static int addPoints(CommandContext<CommandSourceStack> ctx, Collection<ServerPlayer> players, int amount) {
        for (ServerPlayer player : players) {
            player.getData(SkillsAttachments.PLAYER_SKILLS.get()).adminAddPoints(amount);
            SkillsNetwork.syncTo(player);
            ctx.getSource().sendSuccess(() -> Component.translatable("command.aura.skills.points_add.success", amount, player.getDisplayName()), true);
        }
        return players.size();
    }

    // updates and syncs skill points
    private static int setPoints(CommandContext<CommandSourceStack> ctx, Collection<ServerPlayer> players, int amount) {
        for (ServerPlayer player : players) {
            player.getData(SkillsAttachments.PLAYER_SKILLS.get()).adminSetPoints(amount);
            SkillsNetwork.syncTo(player);
            ctx.getSource().sendSuccess(() -> Component.translatable("command.aura.skills.points_set.success", player.getDisplayName(), amount), true);
        }
        return players.size();
    }

    // clears and syncs skill points
    private static int resetPoints(CommandContext<CommandSourceStack> ctx, Collection<ServerPlayer> players) {
        for (ServerPlayer player : players) {
            player.getData(SkillsAttachments.PLAYER_SKILLS.get()).adminResetPoints();
            SkillsNetwork.syncTo(player);
            ctx.getSource().sendSuccess(() -> Component.translatable("command.aura.skills.points_reset.success", player.getDisplayName()), true);
        }
        return players.size();
    }

    private static SkillId parseSkill(String s) {
        try {
            return SkillId.valueOf(s.toUpperCase(Locale.ROOT));
        } catch (Exception ignored) {
            return null;
        }
    }
}

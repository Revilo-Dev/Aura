package net.revilodev.aura.abilities.command;

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
import net.revilodev.aura.abilities.AbilitiesAttachments;
import net.revilodev.aura.abilities.AbilitiesNetwork;
import net.revilodev.aura.abilities.AbilityId;
import net.revilodev.aura.abilities.PlayerAbilities;

import java.util.Arrays;
import java.util.Collection;
import java.util.Locale;

public final class AbilitiesCommands {
    private static final SuggestionProvider<CommandSourceStack> SUGGEST_ABILITIES =
            (ctx, builder) -> SharedSuggestionProvider.suggest(Arrays.stream(AbilityId.values()).map(AbilityId::name), builder);

    private AbilitiesCommands() {}

    public static void register() {
        NeoForge.EVENT_BUS.addListener(AbilitiesCommands::onRegisterCommands);
    }

    private static void onRegisterCommands(RegisterCommandsEvent event) {
        registerAll(event.getDispatcher());
    }

    private static void registerAll(CommandDispatcher<CommandSourceStack> dispatcher) {
        var root = Commands.literal("abilities").requires(source -> source.hasPermission(2));

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

        root.then(Commands.literal("unlock")
                .then(Commands.argument("ability", StringArgumentType.word()).suggests(SUGGEST_ABILITIES)
                        .executes(ctx -> {
                            ServerPlayer player = ctx.getSource().getPlayerOrException();
                            AbilityId id = parseAbility(StringArgumentType.getString(ctx, "ability"));
                            if (id == null) {
                                ctx.getSource().sendFailure(Component.translatable("command.aura.abilities.error.unknown_ability"));
                                return 0;
                            }
                            PlayerAbilities data = player.getData(AbilitiesAttachments.PLAYER_ABILITIES.get());
                            data.setRank(id, Math.max(1, data.rank(id)));
                            AbilitiesNetwork.syncTo(player);
                            ctx.getSource().sendSuccess(() -> Component.translatable("command.aura.abilities.unlock.success", id.title()), true);
                            return 1;
                        })));

        root.then(Commands.literal("reset")
                .executes(ctx -> {
                    ServerPlayer player = ctx.getSource().getPlayerOrException();
                    PlayerAbilities data = player.getData(AbilitiesAttachments.PLAYER_ABILITIES.get());
                    data.adminReset();
                    AbilitiesNetwork.syncTo(player);
                    ctx.getSource().sendSuccess(() -> Component.translatable("command.aura.abilities.reset_all.success"), true);
                    return 1;
                }));

        dispatcher.register(root);
    }

    // updates and syncs ability points
    private static int addPoints(CommandContext<CommandSourceStack> ctx, Collection<ServerPlayer> players, int amount) {
        for (ServerPlayer player : players) {
            player.getData(AbilitiesAttachments.PLAYER_ABILITIES.get()).addPoints(amount);
            AbilitiesNetwork.syncTo(player);
            ctx.getSource().sendSuccess(() -> Component.translatable("command.aura.abilities.points_add.success", amount, player.getDisplayName()), true);
        }
        return players.size();
    }

    // updates and syncs ability points
    private static int setPoints(CommandContext<CommandSourceStack> ctx, Collection<ServerPlayer> players, int amount) {
        for (ServerPlayer player : players) {
            player.getData(AbilitiesAttachments.PLAYER_ABILITIES.get()).setPoints(amount);
            AbilitiesNetwork.syncTo(player);
            ctx.getSource().sendSuccess(() -> Component.translatable("command.aura.abilities.points_set.success", player.getDisplayName(), amount), true);
        }
        return players.size();
    }

    // clears and syncs ability points
    private static int resetPoints(CommandContext<CommandSourceStack> ctx, Collection<ServerPlayer> players) {
        for (ServerPlayer player : players) {
            player.getData(AbilitiesAttachments.PLAYER_ABILITIES.get()).setPoints(0);
            AbilitiesNetwork.syncTo(player);
            ctx.getSource().sendSuccess(() -> Component.translatable("command.aura.abilities.points_reset.success", player.getDisplayName()), true);
        }
        return players.size();
    }

    private static AbilityId parseAbility(String value) {
        try {
            return AbilityId.valueOf(value.toUpperCase(Locale.ROOT));
        } catch (Exception ignored) {
            return null;
        }
    }

}

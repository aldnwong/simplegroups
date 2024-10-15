package ong.aldenw.commands;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import ong.aldenw.GroupManager;
import ong.aldenw.arguments.PlayerSuggestions;
import ong.aldenw.data.PlayerGroupData;
import ong.aldenw.network.GroupUpdatePayload;

public class GroupCommand {
    public final static String commandName = "group";
    public final static int permissionLevel = 0;

    public final static String getCommandName = "get";
    public static int getCommandExecute(CommandContext<ServerCommandSource> context) {
        String playerArg = StringArgumentType.getString(context, "player");
        ServerPlayerEntity player = context.getSource().getWorld().getServer().getPlayerManager().getPlayer(playerArg);

        PlayerGroupData playerState = GroupManager.getPlayerState(player);

        String result = !playerState.GroupName.isEmpty() ? playerArg + " is in group " + playerState.GroupName : playerArg + " is not in a group";

        context.getSource().sendFeedback(() -> Text.literal(result), false);
        return 1;
    }

    public final static String setCommandName = "set";
    public static int setCommandExecute (CommandContext<ServerCommandSource> context) {
        ServerPlayerEntity player = context.getSource().getPlayer();

        PlayerGroupData playerState = GroupManager.getPlayerState(player);
        playerState.GroupName = "hallo";

        GroupUpdatePayload data = new GroupUpdatePayload(playerState.GroupName);

        context.getSource().getServer().execute(() -> {
            ServerPlayNetworking.send(player, data);
        });

        context.getSource().sendFeedback(() -> Text.literal("Set group to hallo."), false);
        return 1;
    }

    public static LiteralArgumentBuilder<ServerCommandSource> register() {
        return CommandManager.literal(commandName)
                .requires(source -> source.hasPermissionLevel(permissionLevel))
                .then(CommandManager.literal(getCommandName)
                        .then(CommandManager.argument("player", StringArgumentType.string())
                                .suggests(new PlayerSuggestions())
                                .executes(GroupCommand::getCommandExecute)
                        )
                )
                .then(CommandManager.literal(setCommandName)
                        .executes(GroupCommand::setCommandExecute)
                );
    }
}

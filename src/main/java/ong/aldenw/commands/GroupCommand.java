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
import ong.aldenw.commands.suggestions.PlayerSuggestions;
import ong.aldenw.data.GroupData;
import ong.aldenw.data.PlayerData;
import ong.aldenw.network.UpdatePayload;
import ong.aldenw.util.RgbFormat;

public class GroupCommand {
    public final static String commandName = "group";
    public final static int permissionLevel = 0;

    public final static String getCommandName = "get";
    public static int getCommandExecute(CommandContext<ServerCommandSource> context) {
        String playerArg = StringArgumentType.getString(context, "player");
        ServerPlayerEntity player = context.getSource().getWorld().getServer().getPlayerManager().getPlayer(playerArg);

        PlayerData playerState = GroupManager.getPlayerState(player);

        String result = !playerState.groupId.isEmpty() ? playerArg + " is in group " + playerState.groupId : playerArg + " is not in a group";

        context.getSource().sendFeedback(() -> Text.literal(result), false);
        return 1;
    }

    public final static String createCommandName = "create";
    public static int createCommandExecute(CommandContext<ServerCommandSource> context) {
        if (!context.getSource().isExecutedByPlayer()) {
            context.getSource().sendFeedback(() -> Text.literal("This command is only available to players.").withColor(RgbFormat.fromThree(255, 0, 0)), false);
            return 1;
        }

        GroupManager state = GroupManager.getServerState(context.getSource().getServer());
        ServerPlayerEntity player = context.getSource().getPlayer();

        if (!state.players.get(player.getUuid()).groupId.isEmpty()) {
            context.getSource().sendFeedback(() -> Text.literal("You are already in a group.").withColor(RgbFormat.fromThree(255, 0, 0)), false);
            return 1;
        }

        String groupName = StringArgumentType.getString(context, "groupName");
        String groupId = GroupManager.generateGroupId(context.getSource().getServer());
        GroupData newGroupData = new GroupData();

        newGroupData.name = groupName;
        newGroupData.leader = player.getUuid();
        newGroupData.players.add(player.getUuid());

        state.groupList.put(groupId, newGroupData);

        PlayerData playerState = GroupManager.getPlayerState(player);
        playerState.groupId = groupId;

        UpdatePayload data = new UpdatePayload(playerState.groupId);

        context.getSource().getServer().execute(() -> {
            ServerPlayNetworking.send(player, data);
        });

        context.getSource().sendFeedback(() -> Text.literal("Created new group "+groupName+"."), false);
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
                .then(CommandManager.literal(createCommandName)
                        .then(CommandManager.argument("groupName", StringArgumentType.string())
                                .executes(GroupCommand::createCommandExecute))
                );
    }
}

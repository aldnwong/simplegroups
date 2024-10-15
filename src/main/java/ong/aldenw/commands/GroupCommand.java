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

public class GroupCommand {
    public final static String commandName = "group";
    public final static int permissionLevel = 0;

    public final static String getCommandName = "get";
    public static int getCommandExecute(CommandContext<ServerCommandSource> context) {
        String playerArg = StringArgumentType.getString(context, "player");
        ServerPlayerEntity player = context.getSource().getWorld().getServer().getPlayerManager().getPlayer(playerArg);

        PlayerData playerState = GroupManager.getPlayerState(player);

        String result = !playerState.GroupId.isEmpty() ? playerArg + " is in group " + playerState.GroupId : playerArg + " is not in a group";

        context.getSource().sendFeedback(() -> Text.literal(result), false);
        return 1;
    }

    public final static String createCommandName = "create";
    public static int createCommandExecute(CommandContext<ServerCommandSource> context) {
        GroupManager state = GroupManager.getServerState(context.getSource().getServer());
        ServerPlayerEntity player = context.getSource().getPlayer();

        GroupData newGroupData = new GroupData();
        newGroupData.name = "hallo";
        newGroupData.prefix = "[HLLO]";
        newGroupData.color = GroupManager.getTextColorFromRGB(0, 255, 155);
        newGroupData.listed = true;
        newGroupData.open = false;
        newGroupData.leader = player.getUuid();
        newGroupData.players.add(player.getUuid());

        String groupId = GroupManager.generateGroupId(context.getSource().getServer());
        state.groupList.put(groupId, newGroupData);

        PlayerData playerState = GroupManager.getPlayerState(player);
        playerState.GroupId = groupId;

        UpdatePayload data = new UpdatePayload(playerState.GroupId);

        context.getSource().getServer().execute(() -> {
            ServerPlayNetworking.send(player, data);
        });

        context.getSource().sendFeedback(() -> Text.literal("Created new dummy group."), false);
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
                        .executes(GroupCommand::createCommandExecute)
                );
    }
}

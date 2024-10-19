package ong.aldenw.commands;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import ong.aldenw.GroupManager;
import ong.aldenw.data.GroupData;
import ong.aldenw.data.PlayerData;
import ong.aldenw.network.UpdatePayload;
import ong.aldenw.util.RgbFormat;

public class GroupCreateCommand {
    public static int execute(CommandContext<ServerCommandSource> context) {
        if (!context.getSource().isExecutedByPlayer()) {
            context.getSource().sendFeedback(() -> Text.literal("This command is only available to players.").withColor(RgbFormat.fromThree(255, 0, 0)), false);
            return 1;
        }

        GroupManager state = GroupManager.getServerState(context.getSource().getServer());
        ServerPlayerEntity player = context.getSource().getPlayer();

        if (!state.players.get(player.getUuid()).groupName.isEmpty()) {
            context.getSource().sendFeedback(() -> Text.literal("You are already in a group.").withColor(RgbFormat.fromThree(255, 0, 0)), false);
            return 1;
        }

        String groupName = StringArgumentType.getString(context, "groupName");
        GroupData newGroupData = new GroupData();

        if (state.groupList.containsKey(groupName)) {
            context.getSource().sendFeedback(() -> Text.literal("A group with this name already exists.").withColor(RgbFormat.fromThree(255, 0, 0)), false);
            return 1;
        }

        newGroupData.displayName = groupName;
        newGroupData.leader = player.getUuid();
        newGroupData.players.add(player.getUuid());

        state.groupList.put(groupName, newGroupData);

        PlayerData playerState = GroupManager.getPlayerState(player);
        playerState.groupName = groupName;

        UpdatePayload data = new UpdatePayload(playerState.groupName);

        context.getSource().getServer().execute(() -> {
            ServerPlayNetworking.send(player, data);
        });

        context.getSource().sendFeedback(() -> Text.literal("Created new group "+groupName+"."), false);
        return 1;
    }
}
